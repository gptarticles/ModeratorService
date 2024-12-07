package me.zedaster.moderationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.Instant;

/**
 * Summary of some article
 */
@AllArgsConstructor
@Builder
@Getter
@EqualsAndHashCode(of = "id")
public class ArticleSummary {
    /**
     * Article ID
     */
    private final long id;

    /**
     * Title of the article
     */
    private final String title;

    /**
     * Date of creation of the article
     */
    private final Instant createdAt;

    /**
     * Status of the article
     */
    private final ModerationStatus status;

    /**
     * Comment of a moderator if edit is requested
     */
    private final String moderatorComment;
}