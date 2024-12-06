package me.zedaster.moderationservice.dto;

import lombok.*;

import java.time.Instant;

@RequiredArgsConstructor
@Getter
@EqualsAndHashCode(of = "id")
public class Article {
    /**
     * Article ID
     */
    private final Long id;

    /**
     * Title of the article
     */
    private final String title;

    /**
     * String content of the article
     */
    private final String content;

    /**
     * Date of creation of the article
     */
    private final Instant createdAt;

    /**
     * Moderation status of the article
     */
    private final ModerationStatus status;

    /**
     * Data of creator of the article
     */
    private final Creator creator;
}
