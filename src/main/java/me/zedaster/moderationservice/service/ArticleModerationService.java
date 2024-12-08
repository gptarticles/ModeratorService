package me.zedaster.moderationservice.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import me.zedaster.moderationservice.dto.*;
import me.zedaster.moderationservice.entity.ArticleSummaryEntity;
import me.zedaster.moderationservice.entity.ModeratorCommentEntity;
import me.zedaster.moderationservice.repository.ArticleSummaryRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * Service for moderation of articles
 */
@Validated
@Service
@RequiredArgsConstructor
public class ArticleModerationService {
    /**
     * Size of the page with article summaries
     */
    private static final int ARTICLE_SUMMARIES_PAGE_SIZE = 10;

    /**
     * Repository of article summaries
     */
    private final ArticleSummaryRepository articleSummaryRepository;

    /**
     * Service for article content
     */
    private final ContentService contentService;

    /**
     * Remote service for fetching creators
     */
    private final CreatorService creatorService;

    /**
     * Remote service for saving articles
     */
    private final ArticleService articleService;

    /**
     * Get summaries of articles created by user for moderation
     * @param userId ID of the user
     * @param page Page number
     * @return List of article summaries
     */
    @Transactional(propagation = Propagation.SUPPORTS)
    public List<ArticleSummary> getUserArticleSummaries(@Min(1) long userId, @Min(1) int page) {
        PageRequest pageRequest = PageRequest.of(page - 1, ARTICLE_SUMMARIES_PAGE_SIZE);
        return articleSummaryRepository
                .findAllByCreatorId(userId, pageRequest)
                .stream()
                .map(this::entityToArticleSummary)
                .toList();

    }

    /**
     * Get summaries of articles for moderation
     * @param page Page number
     * @return List of article summaries
     * @throws ExternalConnectException if connection to external services was failed
     */
    @Transactional(propagation = Propagation.SUPPORTS, rollbackFor = ExternalConnectException.class)
    public List<NamedArticleSummary> getArticleSummaries(@Min(1) int page) {
        PageRequest pageRequest = PageRequest.of(page - 1, ARTICLE_SUMMARIES_PAGE_SIZE);

        List<ArticleSummaryEntity> summaryEntities = articleSummaryRepository.findAll(pageRequest);
        List<Long> creatorIds = summaryEntities.stream()
                .map(ArticleSummaryEntity::getCreatorId)
                .toList();
        List<Creator> creators = creatorService.getCreatorsByIds(creatorIds);

        return IntStream
                .range(0, summaryEntities.size())
                .mapToObj(i -> entityToNamedArticleSummary(summaryEntities.get(i), creators.get(i)))
                .toList();
    }

    /**
     * Get moderating article by ID
     * @param articleId ID of the article
     * @return Article
     * @throws ExternalConnectException if connection to external services was failed
     * @throws NoSuchArticleException if the article was not found
     */
    @Transactional(
            propagation = Propagation.SUPPORTS,
            rollbackFor = {NoSuchArticleException.class, ExternalConnectException.class})
    public Article getArticle(@Min(1) long articleId) {
        ArticleSummaryEntity summaryEntity = articleSummaryRepository
                .findById(articleId)
                .orElseThrow(() -> new NoSuchArticleException(articleId));
        String content = contentService.getContent(articleId).orElseThrow(() -> new NoSuchArticleException(articleId));
        Creator creator = creatorService.getCreator(summaryEntity.getCreatorId());
        return articleFromSummaryEntity(summaryEntity, content, creator);
    }

    /**
     * Check if user owns article.
     * @param userId ID of the user
     * @param articleId ID of the article
     * @return true if user owns article, false otherwise. If the article or the user was not found, returns false
     * as well.
     */
    @Transactional(propagation = Propagation.SUPPORTS)
    public boolean userOwnArticle(@Min(1) long userId, @Min(1) long articleId) {
        return articleSummaryRepository.existsByIdAndCreatorId(articleId, userId);
    }

    /**
     * Save article for moderation
     * @param creatorId ID of the creator
     * @param createDto DTO with article data
     * @throws ExternalConnectException if connection to ${@link ContentService} was failed
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = ExternalConnectException.class)
    public void saveArticle(@Min(1) long creatorId, @Valid CreateArticleDto createDto) {
        ArticleSummaryEntity entity = new ArticleSummaryEntity(createDto.getTitle(), Instant.now(), creatorId);
        ArticleSummaryEntity savedEntity = articleSummaryRepository.save(entity);
        long articleId = savedEntity.getId();
        contentService.saveContent(articleId, createDto.getContent());
    }

    /**
     * Publish a moderating article
     * @param articleId ID of the article
     * @throws ExternalConnectException if connection to ${@link ContentService} was failed
     * @throws NoSuchArticleException if the article was not found
     */
    @Transactional(propagation = Propagation.REQUIRED,
            rollbackFor = {ExternalConnectException.class, NoSuchArticleException.class})
    public void publishArticle(@Min(1) long articleId) {
        Optional<ArticleSummaryEntity> summaryEntityOptional = articleSummaryRepository.findById(articleId);
        if (summaryEntityOptional.isEmpty()) {
            throw new NoSuchArticleException(articleId);
        }

        if (summaryEntityOptional.get().getStatus() != ModerationStatus.MODERATING) {
            throw new IllegalStateException("Article with ID %d is not in MODERATING status".formatted(articleId));
        }

        String title = summaryEntityOptional.get().getTitle();
        String content = contentService.getContent(articleId).orElseThrow(() -> new NoSuchArticleException(articleId));;
        long creatorId = summaryEntityOptional.get().getCreatorId();

        articleService.saveArticle(new PublishArticleDto(title, content, creatorId));

        removeExistingArticle(articleId);
    }

    /**
     * Ask for edit of a moderating article
     * @param articleId ID of the article
     * @throws NoSuchArticleException if the article was not found
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = NoSuchArticleException.class)
    public void askEdit(@Min(1) long articleId, @NotNull @NotBlank String comment) {
        Optional<ArticleSummaryEntity> summaryEntityOptional = articleSummaryRepository.findById(articleId);
        if (summaryEntityOptional.isEmpty()) {
            throw new NoSuchArticleException(articleId);
        }

        ArticleSummaryEntity summaryEntity = summaryEntityOptional.get();
        summaryEntity.setStatus(ModerationStatus.EDIT_REQUESTED);
        summaryEntity.setModeratorComment(new ModeratorCommentEntity(articleId, comment));
        articleSummaryRepository.save(summaryEntity);
    }

    /**
     * Remove a moderating article
     * @param articleId ID of the article
     * @throws NoSuchArticleException if the article was not found
     * @throws ExternalConnectException if connection to ${@link ContentService} was failed
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = NoSuchArticleException.class)
    public void removeArticle(@Min(1) long articleId) {
        if (!articleSummaryRepository.existsById(articleId)) {
            throw new NoSuchArticleException(articleId);
        }

        removeExistingArticle(articleId);
    }

    /**
     * Remove existing article
     * @param articleId ID of the article
     * @throws ExternalConnectException if connection to ${@link ContentService} was failed
     */
    private void removeExistingArticle(long articleId) {
        contentService.removeContent(articleId);
        articleSummaryRepository.deleteById(articleId);
    }

    /**
     * Convert {@link ArticleSummaryEntity} to article summary
     * @param summaryEntity Entity of article summary
     * @return Article summary
     */
    private ArticleSummary entityToArticleSummary(ArticleSummaryEntity summaryEntity) {
        String comment = summaryEntity.getModeratorComment() == null ?
                null : summaryEntity.getModeratorComment().getComment();
        return ArticleSummary.builder()
                .id(summaryEntity.getId())
                .title(summaryEntity.getTitle())
                .createdAt(summaryEntity.getCreatedAt())
                .status(summaryEntity.getStatus())
                .moderatorComment(comment)
                .build();
    }

    /**
     * Convert {@link ArticleSummaryEntity} to named article summary
     * @param summaryEntity Entity of article summary
     * @param creator Creator of the article
     * @return Named article summary
     */
    private NamedArticleSummary entityToNamedArticleSummary(ArticleSummaryEntity summaryEntity, Creator creator) {
        return new NamedArticleSummary(
                entityToArticleSummary(summaryEntity),
                creator
        );
    }

    /**
     * Create article from {@link ArticleSummaryEntity}
     * @param summaryEntity Entity of article summary
     * @param content Content of the article
     * @param creator Creator of the article
     * @return Article
     */
    private Article articleFromSummaryEntity(ArticleSummaryEntity summaryEntity, String content, Creator creator) {
        String moderatorComment = summaryEntity.getModeratorComment() == null ?
                null : summaryEntity.getModeratorComment().getComment();

        return Article.builder()
                .id(summaryEntity.getId())
                .title(summaryEntity.getTitle())
                .content(content)
                .createdAt(summaryEntity.getCreatedAt())
                .status(summaryEntity.getStatus())
                .moderatorComment(moderatorComment)
                .creator(creator)
                .build();
    }
}
