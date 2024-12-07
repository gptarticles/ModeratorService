package me.zedaster.moderationservice.service;

import java.util.NoSuchElementException;

public class NoSuchArticleException extends NoSuchElementException {
    public NoSuchArticleException(long wrongArticleId) {
        super("Article with ID %d was not found!".formatted(wrongArticleId));
    }
}
