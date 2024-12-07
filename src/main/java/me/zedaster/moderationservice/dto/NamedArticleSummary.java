package me.zedaster.moderationservice.dto;

import lombok.Getter;

/**
 * Summary of some article with creator data
 */
@Getter
public class NamedArticleSummary extends ArticleSummary {
    /**
     * Creator data
     */
    private final Creator creator;

    public NamedArticleSummary(ArticleSummary articleSummary, Creator creator) {
        super(articleSummary.getId(),
                articleSummary.getTitle(),
                articleSummary.getCreatedAt(),
                articleSummary.getStatus(),
                articleSummary.getModeratorComment());
        this.creator = creator;
    }
}