package me.zedaster.moderationservice.dto;

import lombok.Getter;

import java.time.Instant;

/**
 * Summary of some article with creator data
 */
@Getter
public class NamedArticleSummary extends ArticleSummary {
    /**
     * Creator data
     */
    private final Creator creator;

    public NamedArticleSummary(long id, String title, Instant createdAt, Creator creator) {
        super(id, title, createdAt);
        this.creator = creator;
    }
}