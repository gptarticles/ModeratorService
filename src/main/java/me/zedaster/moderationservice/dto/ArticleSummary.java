package me.zedaster.moderationservice.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.Instant;

/**
 * Summary of some article
 */
@AllArgsConstructor
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
}