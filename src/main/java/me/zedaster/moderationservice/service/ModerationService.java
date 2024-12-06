package me.zedaster.moderationservice.service;

import me.zedaster.moderationservice.dto.Article;
import me.zedaster.moderationservice.dto.ArticleSummary;
import me.zedaster.moderationservice.dto.CreateArticleDto;
import me.zedaster.moderationservice.dto.NamedArticleSummary;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ModerationService {
    public List<ArticleSummary> getUserArticleSummaries(long userId, int page) {
        // TODO
        return null;
    }

    public List<NamedArticleSummary> getArticleSummaries(int page) {
        // TODO
        return null;
    }

    public Article getArticle(long articleId) throws ArticleNotFoundException {
        // TODO
        return null;
    }

    public boolean userOwnArticle(long userId, long articleId) {
        // TODO
        return false;
    }

    public void createArticle(long creatorId, CreateArticleDto createDto) {
        // TODO
    }

    public void publishArticle(long articleId) throws ArticleNotFoundException {
        // TODO
    }

    public void askEdit(long articleId, String testComment) throws ArticleNotFoundException {
        // TODO
    }

    public void removeArticle(long articleId) throws ArticleNotFoundException {
        // TODO
    }
}
