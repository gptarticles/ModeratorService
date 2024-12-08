package me.zedaster.moderationservice.service;

import jakarta.validation.ConstraintViolationException;
import me.zedaster.moderationservice.TestUtils;
import me.zedaster.moderationservice.dto.*;
import me.zedaster.moderationservice.entity.ArticleSummaryEntity;
import me.zedaster.moderationservice.entity.ModeratorCommentEntity;
import me.zedaster.moderationservice.repository.ArticleSummaryRepository;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link ArticleModerationService} class.
 */
@SpringBootTest(classes = {ArticleModerationService.class, ValidationAutoConfiguration.class})
public class ArticleModerationServiceTest {

    /**
     * Page size for article summaries.
     */
    private static final int ARTICLE_SUMMARIES_PAGE_SIZE = 10;

    /**
     * Article moderation service.
     */
    @Autowired
    private ArticleModerationService articleModerationService;

    @MockitoBean
    private ArticleSummaryRepository articleSummaryRepository;

    @MockitoBean
    private ContentService contentService;

    @MockitoBean
    private CreatorService creatorService;

    @MockitoBean
    private ArticleService articleService;

    /**
     * Test {@link ArticleModerationService#getUserArticleSummaries(long, int)} method.
     */
    @Test
    public void getUserArticleSummaries() {
        Instant createdAt1 = TestUtils.createInstantOf(2021, 1, 1, 12, 30, 0);
        Instant createdAt2 = TestUtils.createInstantOf(2022, 1, 1, 12, 30, 0);

        ArticleSummaryEntity articleSummary1 = new ArticleSummaryEntity();
        articleSummary1.setId(1L);
        articleSummary1.setTitle("a".repeat(15));
        articleSummary1.setStatus(ModerationStatus.MODERATING);
        articleSummary1.setCreatedAt(createdAt1);
        articleSummary1.setCreatorId(1L);

        ArticleSummaryEntity articleSummary2 = new ArticleSummaryEntity();
        articleSummary2.setId(2L);
        articleSummary2.setTitle("b".repeat(15));
        articleSummary2.setStatus(ModerationStatus.EDIT_REQUESTED);
        articleSummary2.setModeratorComment(new ModeratorCommentEntity(2L, "comment"));
        articleSummary2.setCreatedAt(createdAt2);
        articleSummary2.setCreatorId(1L);

        PageRequest pageRequest = PageRequest.of(12 - 1, ARTICLE_SUMMARIES_PAGE_SIZE);
        when(articleSummaryRepository.findAllByCreatorId(1L, pageRequest)).thenReturn(List.of(articleSummary1, articleSummary2));
        List<ArticleSummary> summaries = articleModerationService.getUserArticleSummaries(1, 12);

        assertEquals(2, summaries.size());
        assertEquals(1L, summaries.get(0).getId());
        assertEquals("a".repeat(15), summaries.get(0).getTitle());
        assertEquals(ModerationStatus.MODERATING, summaries.get(0).getStatus());
        assertNull(summaries.get(0).getModeratorComment());
        assertEquals(createdAt1, summaries.get(0).getCreatedAt());
        assertEquals(2L, summaries.get(1).getId());
        assertEquals("b".repeat(15), summaries.get(1).getTitle());
        assertEquals(ModerationStatus.EDIT_REQUESTED, summaries.get(1).getStatus());
        assertEquals("comment", summaries.get(1).getModeratorComment());
        assertEquals(createdAt2, summaries.get(1).getCreatedAt());
    }

    /**
     * Test {@link ArticleModerationService#getUserArticleSummaries(long, int)} method with incorrect user ID.
     */
    @Test
    public void getUserArticleSummariesByIncorrectUserId() {
        assertThrows(ConstraintViolationException.class,
                () -> articleModerationService.getUserArticleSummaries(0, 12));
    }

    /**
     * Test {@link ArticleModerationService#getUserArticleSummaries(long, int)} method with incorrect page number.
     */
    @Test
    public void getUserArticleSummariesByIncorrectPage() {
        assertThrows(ConstraintViolationException.class,
                () -> articleModerationService.getUserArticleSummaries(1, 0));
    }

    /**
     * Test {@link ArticleModerationService#getArticleSummaries(int)} method.
     */
    @Test
    public void getArticleSummaries() {
        Instant createdAt1 = TestUtils.createInstantOf(2021, 1, 1, 12, 30, 0);
        Instant createdAt2 = TestUtils.createInstantOf(2022, 1, 1, 12, 30, 0);

        ArticleSummaryEntity articleSummary1 = new ArticleSummaryEntity();
        articleSummary1.setId(1L);
        articleSummary1.setTitle("a".repeat(15));
        articleSummary1.setStatus(ModerationStatus.MODERATING);
        articleSummary1.setCreatedAt(createdAt1);
        articleSummary1.setCreatorId(1L);

        ArticleSummaryEntity articleSummary2 = new ArticleSummaryEntity();
        articleSummary2.setId(2L);
        articleSummary2.setTitle("b".repeat(15));
        articleSummary2.setStatus(ModerationStatus.EDIT_REQUESTED);
        articleSummary2.setModeratorComment(new ModeratorCommentEntity(2L, "comment"));
        articleSummary2.setCreatedAt(createdAt2);
        articleSummary2.setCreatorId(2L);

        PageRequest pageRequest = PageRequest.of(12 - 1, ARTICLE_SUMMARIES_PAGE_SIZE);
        when(articleSummaryRepository.findAll(pageRequest)).thenReturn(List.of(articleSummary1, articleSummary2));

        Creator creator1 = new Creator(1L, "alice");
        Creator creator2 = new Creator(2L, "bob");
        List<Creator> creators = List.of(creator1, creator2);
        when(creatorService.getCreatorsByIds(List.of(1L, 2L))).thenReturn(creators);
        List<NamedArticleSummary> summaries = articleModerationService.getArticleSummaries(12);

        assertEquals(2, summaries.size());
        assertEquals(1L, summaries.get(0).getId());
        assertEquals("a".repeat(15), summaries.get(0).getTitle());
        assertEquals(ModerationStatus.MODERATING, summaries.get(0).getStatus());
        assertNull(summaries.get(0).getModeratorComment());
        assertEquals(createdAt1, summaries.get(0).getCreatedAt());
        assertEquals(1L, summaries.get(0).getCreator().getId());
        assertEquals("alice", summaries.get(0).getCreator().getName());
        assertEquals(2L, summaries.get(1).getId());
        assertEquals("b".repeat(15), summaries.get(1).getTitle());
        assertEquals(ModerationStatus.EDIT_REQUESTED, summaries.get(1).getStatus());
        assertEquals("comment", summaries.get(1).getModeratorComment());
        assertEquals(createdAt2, summaries.get(1).getCreatedAt());
        assertEquals(2L, summaries.get(1).getCreator().getId());
        assertEquals("bob", summaries.get(1).getCreator().getName());
    }

    /**
     * Test {@link ArticleModerationService#getArticleSummaries(int)} method with incorrect page number.
     */
    @Test
    public void getArticleSummariesByIncorrectPage() {
        assertThrows(ConstraintViolationException.class,
                () -> articleModerationService.getArticleSummaries(0));
    }

    /**
     * Test {@link ArticleModerationService#getArticle(long)} method.
     */
    @Test
    public void getArticle() {
        Instant createdAt = TestUtils.createInstantOf(2021, 1, 1, 12, 30, 0);

        ArticleSummaryEntity articleSummary = new ArticleSummaryEntity();
        articleSummary.setId(1L);
        articleSummary.setTitle("a".repeat(15));
        articleSummary.setStatus(ModerationStatus.EDIT_REQUESTED);
        articleSummary.setModeratorComment(new ModeratorCommentEntity(1L, "comment"));
        articleSummary.setCreatedAt(createdAt);
        articleSummary.setCreatorId(1L);

        String content = "c".repeat(200);
        when(articleSummaryRepository.findById(1L)).thenReturn(Optional.of(articleSummary));
        when(contentService.getContent(1L)).thenReturn(Optional.of(content));
        when(creatorService.getCreator(1L)).thenReturn(new Creator(1L, "alice"));

        Article article = articleModerationService.getArticle(1);

        assertEquals(1L, article.getId());
        assertEquals("a".repeat(15), article.getTitle());
        assertEquals(ModerationStatus.EDIT_REQUESTED, article.getStatus());
        assertEquals("comment", article.getModeratorComment());
        assertEquals(createdAt, article.getCreatedAt());
        assertEquals(1L, article.getCreator().getId());
        assertEquals("alice", article.getCreator().getName());
        assertEquals(content, article.getContent());
    }

    /**
     * Test {@link ArticleModerationService#getArticle(long)} method with incorrect article ID.
     */
    @Test
    public void getArticleWithContentStorageConnectException() {
        Instant createdAt = TestUtils.createInstantOf(2021, 1, 1, 12, 30, 0);

        ArticleSummaryEntity articleSummary = new ArticleSummaryEntity();
        articleSummary.setId(1L);
        articleSummary.setTitle("a".repeat(15));
        articleSummary.setStatus(ModerationStatus.EDIT_REQUESTED);
        articleSummary.setModeratorComment(new ModeratorCommentEntity(1L, "comment"));
        articleSummary.setCreatedAt(createdAt);
        articleSummary.setCreatorId(1L);

        when(articleSummaryRepository.findById(1L)).thenReturn(Optional.of(articleSummary));
        doThrow(new ExternalConnectException("test", null)).when(contentService).getContent(1L);

        assertThrows(ExternalConnectException.class,
                () -> articleModerationService.getArticle(1));
    }

    /**
     * Test {@link ArticleModerationService#getArticle(long)} method with incorrect article ID.
     */
    @Test
    public void getArticleByIncorrectId() {
        assertThrows(ConstraintViolationException.class, () -> articleModerationService.getArticle(0L));
    }

    /**
     * Test {@link ArticleModerationService#getArticle(long)} method with non-existent article.
     */
    @Test
    public void getNonExistentArticle() {
        when(articleSummaryRepository.findById(1L)).thenReturn(Optional.empty());

        NoSuchArticleException ex = assertThrows(NoSuchArticleException.class, () -> articleModerationService.getArticle(1));
        assertEquals("Article with ID 1 was not found!", ex.getMessage());
    }

    /**
     * Test {@link ArticleModerationService#userOwnArticle(long, long)} method if user owns the article.
     */
    @Test
    public void userOwnArticle() {
        when(articleSummaryRepository.existsByIdAndCreatorId(1L, 777L)).thenReturn(true);
        assertTrue(articleModerationService.userOwnArticle(777, 1));
    }

    /**
     * Test {@link ArticleModerationService#userOwnArticle(long, long)} method if user does not own the article.
     * This test is also applicable for the cases when the article or the user does not exist.
     */
    @Test
    public void userNotOwnArticle() {
        when(articleSummaryRepository.existsByIdAndCreatorId(1L, 777L)).thenReturn(false);
        assertFalse(articleModerationService.userOwnArticle(777, 1));
        verify(articleSummaryRepository, times(1)).existsByIdAndCreatorId(1L, 777L);
    }

    /**
     * Test {@link ArticleModerationService#userOwnArticle(long, long)} method with incorrect user ID.
     */
    @Test
    public void checkUserOwnArticleByIncorrectUserId() {
        assertThrows(ConstraintViolationException.class,
                () -> articleModerationService.userOwnArticle(0, 1));
    }

    /**
     * Test {@link ArticleModerationService#userOwnArticle(long, long)} method with incorrect article ID.
     */
    @Test
    public void checkUserOwnArticleByIncorrectArticleId() {
        assertThrows(ConstraintViolationException.class,
                () -> articleModerationService.userOwnArticle(1, 0));
    }

    /**
     * Test {@link ArticleModerationService#saveArticle(long, CreateArticleDto)} method.
     */
    @Test
    public void createArticle() {
        Instant createdAt = TestUtils.createInstantOf(2021, 1, 1, 12, 30, 0);

        String testTitle = "a".repeat(15);
        String testContent = "c".repeat(100);
        CreateArticleDto createArticleDto = new CreateArticleDto(testTitle, testContent);

        when(articleSummaryRepository.save(any())).thenAnswer((i) -> {
            ArticleSummaryEntity articleSummary = i.getArgument(0);
            articleSummary.setId(1L);
            return articleSummary;
        });

        doNothing().when(contentService).saveContent(1L, testContent);

        try (MockedStatic<Instant> mockedInstant = mockStatic(Instant.class)) {
            mockedInstant.when(Instant::now).thenReturn(createdAt);
            articleModerationService.saveArticle(777, createArticleDto);
        }

        verify(articleSummaryRepository, times(1)).save(argThat(articleSummary -> {
            assertEquals(testTitle, articleSummary.getTitle());
            assertEquals(createdAt, articleSummary.getCreatedAt());
            assertEquals(ModerationStatus.MODERATING, articleSummary.getStatus());
            assertEquals(777L, articleSummary.getCreatorId());
            assertNull(articleSummary.getModeratorComment());
            return true;
        }));

        verify(contentService, times(1)).saveContent(1L, testContent);
    }

    /**
     * Test {@link ArticleModerationService#saveArticle(long, CreateArticleDto)} method with incorrect creator ID.
     */
    @Test
    public void createArticleByIncorrectCreatorId() {
        String testTitle = "a".repeat(15);
        String testContent = "c".repeat(100);
        assertThrows(ConstraintViolationException.class,
                () -> articleModerationService.saveArticle(0, new CreateArticleDto(testTitle, testContent)));
    }

    /**
     * Test {@link ArticleModerationService#saveArticle(long, CreateArticleDto)} method with too small title.
     */
    @Test
    public void createArticleWithTooSmallTitle() {
        String testTitle = "a".repeat(14);
        String testContent = "c".repeat(100);
        assertThrows(ConstraintViolationException.class,
                () -> articleModerationService.saveArticle(777, new CreateArticleDto(testTitle, testContent)));
    }

    /**
     * Test {@link ArticleModerationService#saveArticle(long, CreateArticleDto)} method with too long title.
     */
    @Test
    public void createArticleWithTooLongTitle() {
        String testTitle = "a".repeat(101);
        String testContent = "c".repeat(100);
        assertThrows(ConstraintViolationException.class,
                () -> articleModerationService.saveArticle(777, new CreateArticleDto(testTitle, testContent)));
    }

    /**
     * Test {@link ArticleModerationService#saveArticle(long, CreateArticleDto)} method with null title.
     */
    @Test
    public void createArticleWithNullTitle() {
        String testContent = "c".repeat(100);
        assertThrows(ConstraintViolationException.class,
                () -> articleModerationService.saveArticle(777, new CreateArticleDto(null, testContent)));
    }

    /**
     * Test {@link ArticleModerationService#saveArticle(long, CreateArticleDto)} method with too small content.
     */
    @Test
    public void createArticleWithTooSmallContent() {
        String testTitle = "a".repeat(15);
        String testContent = "c".repeat(99);
        assertThrows(ConstraintViolationException.class,
                () -> articleModerationService.saveArticle(777, new CreateArticleDto(testTitle, testContent)));
    }

    /**
     * Test {@link ArticleModerationService#saveArticle(long, CreateArticleDto)} method with too long content.
     */
    @Test
    public void createArticleWithTooLongContent() {
        String testTitle = "a".repeat(15);
        String testContent = "c".repeat(18_001);
        assertThrows(ConstraintViolationException.class,
                () -> articleModerationService.saveArticle(777, new CreateArticleDto(testTitle, testContent)));
    }

    /**
     * Test {@link ArticleModerationService#saveArticle(long, CreateArticleDto)} method with null content.
     */
    @Test
    public void createArticleWithNullContent() {
        String testTitle = "a".repeat(15);
        assertThrows(ConstraintViolationException.class,
                () -> articleModerationService.saveArticle(777, new CreateArticleDto(testTitle, null)));
    }

    /**
     * Test {@link ArticleModerationService#saveArticle(long, CreateArticleDto)} method.
     */
    @Test
    public void publishArticle() {
        String testTitle = "a".repeat(15);
        String testContent = "c".repeat(100);

        ArticleSummaryEntity articleSummary = new ArticleSummaryEntity();
        articleSummary.setTitle(testTitle);
        articleSummary.setStatus(ModerationStatus.MODERATING);
        articleSummary.setCreatorId(777L);

        when(articleSummaryRepository.findById(1L)).thenReturn(Optional.of(articleSummary));
        when(contentService.getContent(1L)).thenReturn(Optional.of(testContent));
        doNothing().when(articleService).saveArticle(any());
        doNothing().when(articleSummaryRepository).deleteById(1L);
        doNothing().when(contentService).removeContent(1L);

        articleModerationService.publishArticle(1L);

        verify(articleService, times(1)).saveArticle(argThat(publishArticleDto -> {
            assertEquals(testTitle, publishArticleDto.getTitle());
            assertEquals(testContent, publishArticleDto.getContent());
            assertEquals(777L, publishArticleDto.getCreatorId());
            return true;
        }));

        verify(articleSummaryRepository, times(1)).deleteById(1L);
        verify(contentService, times(1)).removeContent(1L);
    }

    /**
     * Test {@link ArticleModerationService#publishArticle(long)} method with article in non-moderating status.
     */
    @Test
    public void publishNonModeratingArticle() {
        ArticleSummaryEntity articleSummary = new ArticleSummaryEntity();
        articleSummary.setStatus(ModerationStatus.EDIT_REQUESTED);

        when(articleSummaryRepository.findById(1L)).thenReturn(Optional.of(articleSummary));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> articleModerationService.publishArticle(1L));
        assertEquals("Article with ID 1 is not in MODERATING status", ex.getMessage());
    }

    /**
     * Test {@link ArticleModerationService#publishArticle(long)} method with content storage connect exception.
     */
    @Test
    public void publishArticleConnectError() {
        String testTitle = "a".repeat(15);
        String testContent = "c".repeat(100);

        ArticleSummaryEntity articleSummary = new ArticleSummaryEntity();
        articleSummary.setTitle(testTitle);
        articleSummary.setStatus(ModerationStatus.MODERATING);
        articleSummary.setCreatorId(777L);

        when(articleSummaryRepository.findById(1L)).thenReturn(Optional.of(articleSummary));
        when(contentService.getContent(1L)).thenReturn(Optional.of(testContent));

        ExternalConnectException connectException = new ExternalConnectException("test", new Exception());
        doThrow(connectException).when(articleService).saveArticle(any());

        ExternalConnectException thrownEx = assertThrows(ExternalConnectException.class,
                () -> articleModerationService.publishArticle(1L));
        assertSame(connectException, thrownEx);
    }

    /**
     * Test {@link ArticleModerationService#publishArticle(long)} method with incorrect article ID.
     */
    @Test
    public void publishArticleByIncorrectId() {
        assertThrows(ConstraintViolationException.class,
                () -> articleModerationService.publishArticle(0));
    }

    /**
     * Test {@link ArticleModerationService#publishArticle(long)} method with non-existent article.
     */
    @Test
    public void publishNonExistentArticle() {
        when(articleSummaryRepository.findById(1L)).thenReturn(Optional.empty());

        NoSuchArticleException ex = assertThrows(NoSuchArticleException.class,
                () -> articleModerationService.publishArticle(1L));
        assertEquals("Article with ID 1 was not found!", ex.getMessage());
    }

    /**
     * Test {@link ArticleModerationService#askEdit(long, String)} method.
     */
    @Test
    public void askEdit() {
        String testTitle = "a".repeat(15);
        Instant createdAt = TestUtils.createInstantOf(2021, 1, 1, 12, 30, 0);

        ArticleSummaryEntity articleSummary = new ArticleSummaryEntity();
        articleSummary.setId(1L);
        articleSummary.setTitle(testTitle);
        articleSummary.setCreatedAt(createdAt);
        articleSummary.setStatus(ModerationStatus.MODERATING);
        articleSummary.setCreatorId(777L);

        when(articleSummaryRepository.findById(1L)).thenReturn(Optional.of(articleSummary));
        when(articleSummaryRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        articleModerationService.askEdit(1, "comment");

        verify(articleSummaryRepository, times(1)).save(argThat(newArticleSummary -> {
            assertEquals(1L, newArticleSummary.getId());
            assertEquals(testTitle, newArticleSummary.getTitle());
            assertEquals(createdAt, newArticleSummary.getCreatedAt());
            assertEquals(ModerationStatus.EDIT_REQUESTED, newArticleSummary.getStatus());
            assertEquals(777L, newArticleSummary.getCreatorId());
            assertEquals("comment", newArticleSummary.getModeratorComment().getComment());
            return true;
        }));
    }

    /**
     * Test {@link ArticleModerationService#askEdit(long, String)} method for non-existent article.
     */
    @Test
    public void askEditNonExistentArticle() {
        when(articleSummaryRepository.findById(1L)).thenReturn(Optional.empty());

        NoSuchArticleException ex = assertThrows(NoSuchArticleException.class,
                () -> articleModerationService.askEdit(1, "comment"));
        assertEquals("Article with ID 1 was not found!", ex.getMessage());
    }

    /**
     * Test {@link ArticleModerationService#askEdit(long, String)} method with incorrect article ID.
     */
    @Test
    public void askEditByIncorrectArticleId() {
        assertThrows(ConstraintViolationException.class,
                () -> articleModerationService.askEdit(0, "comment"));
    }

    /**
     * Test {@link ArticleModerationService#askEdit(long, String)} method with empty comment.
     */
    @Test
    public void askEditWithEmptyComment() {
        assertThrows(ConstraintViolationException.class,
                () -> articleModerationService.askEdit(1, ""));
    }

    /**
     * Test {@link ArticleModerationService#askEdit(long, String)} method with null comment.
     */
    @Test
    public void askEditWithNullComment() {
        assertThrows(ConstraintViolationException.class,
                () -> articleModerationService.askEdit(1, null));
    }

    /**
     * Test {@link ArticleModerationService#removeArticle(long)} method.
     */
    @Test
    public void removeArticle() {
        when(articleSummaryRepository.existsById(1L)).thenReturn(true);
        doNothing().when(articleSummaryRepository).deleteById(1L);
        doNothing().when(contentService).removeContent(1L);

        articleModerationService.removeArticle(1L);

        verify(articleSummaryRepository, times(1)).deleteById(1L);
        verify(contentService, times(1)).removeContent(1L);
    }

    /**
     * Test {@link ArticleModerationService#removeArticle(long)} method with content storage connect exception.
     */
    @Test
    public void removeArticleByIncorrectId() {
        assertThrows(ConstraintViolationException.class,
                () -> articleModerationService.removeArticle(0));
    }

    /**
     * Test {@link ArticleModerationService#removeArticle(long)} method with non-existent article.
     */
    @Test
    public void removeNonExistentArticle() {
        when(articleSummaryRepository.existsById(1L)).thenReturn(false);

        NoSuchArticleException ex = assertThrows(NoSuchArticleException.class,
                () -> articleModerationService.removeArticle(1));
        assertEquals("Article with ID 1 was not found!", ex.getMessage());
    }
}
