package me.zedaster.moderationservice.service;

import me.zedaster.moderationservice.configuration.microservice.ArticleServiceConfiguration;
import me.zedaster.moderationservice.dto.PublishArticleDto;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

/**
 * Proxy for the remote article microservice.
 */
@Service
public class ArticleService {
    private final RestClient restClient;

    public ArticleService(ArticleServiceConfiguration configuration) {
        this.restClient = RestClient.create(configuration.getUri());
    }

    /**
     * Saves an article to the article service.
     * @param article Article data to save
     * @throws ExternalConnectException If the article service is not available
     */
    public void saveArticle(PublishArticleDto article) {
        try {
            restClient.post()
                    .uri("/internal/articles")
                    .body(article)
                    .retrieve()
                    .toBodilessEntity();
        } catch (ResourceAccessException e) {
            throw new ExternalConnectException("Article service is not available", e);
        }
    }
}
