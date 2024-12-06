package me.zedaster.moderationservice.dto;

import lombok.Data;

@Data
public class CreateArticleDto {
    /**
     * Title of the article
     */
    private final String title;

    /**
     * String content of the article
     */
    private final String content;
}