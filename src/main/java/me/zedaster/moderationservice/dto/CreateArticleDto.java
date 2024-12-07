package me.zedaster.moderationservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateArticleDto {
    /**
     * Title of the article
     */
    @Size(min = 15, message = "Title must contain at least 15 characters!")
    @Size(max = 100, message = "Title mustn't contain more than 100 characters!")
    @NotNull
    private final String title;

    /**
     * String content of the article
     */
    @Size(min = 100, message = "Content must contain at least 100 characters!")
    @Size(max = 18_000, message = "Content mustn't contain more than 18 000 characters!")
    @NotNull
    private final String content;
}