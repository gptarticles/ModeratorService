package me.zedaster.moderationservice.service;

public class ArticleNotFoundException extends Exception {
    public ArticleNotFoundException(long wrongArticleId) {
        super("Article with id %d was not found!".formatted(wrongArticleId));
    }
}
