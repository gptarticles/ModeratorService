package me.zedaster.moderationservice.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
public class PublishArticleDto {
    /**
     * Title of the article
     */
    private final String title;

    /**
     * String content of the article
     */
    private final String content;

    /**
     * User ID of creator of the article
     */
    private final Long creatorId;
}
