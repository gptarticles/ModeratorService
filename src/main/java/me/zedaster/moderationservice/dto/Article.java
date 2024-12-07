package me.zedaster.moderationservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.Instant;

@RequiredArgsConstructor
@Getter
@EqualsAndHashCode(of = "id")
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
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
     * Comment of the moderator
     */
    private final String moderatorComment;

    /**
     * Data of creator of the article
     */
    private final Creator creator;
}
