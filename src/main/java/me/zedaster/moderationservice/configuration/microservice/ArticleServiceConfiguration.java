package me.zedaster.moderationservice.configuration.microservice;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Configuration for the article microservice.
 */
@ConfigurationProperties(prefix = "microservices.article-service")
@ConfigurationPropertiesScan
public class ArticleServiceConfiguration extends MicroserviceConfiguration {
    public ArticleServiceConfiguration(String url) {
        super(url);
    }
}
