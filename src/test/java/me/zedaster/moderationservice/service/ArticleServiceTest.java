package me.zedaster.moderationservice.service;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import me.zedaster.moderationservice.configuration.microservice.ArticleServiceConfiguration;
import me.zedaster.moderationservice.dto.PublishArticleDto;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.net.URI;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest(classes = {ArticleService.class, ArticleServiceTest.TestConfig.class})
@WireMockTest(httpPort = 8080, proxyMode = true)
public class ArticleServiceTest {

    @Autowired
    private ArticleService articleService;

    @TestConfiguration
    public static class TestConfig {
        @Bean
        @Primary
        public ArticleServiceConfiguration articleServiceConfiguration() {
            ArticleServiceConfiguration configuration = Mockito.mock(ArticleServiceConfiguration.class);
            Mockito.when(configuration.getUri()).thenReturn(URI.create("http://article-service:8080"));
            return configuration;
        }
    }


    @Test
    public void saveArticle() {
        String expectedJson = """
            {
                "title": "Test Title",
                "content": "Test Content",
                "creatorId": 1
            }""";

        stubFor(post(urlEqualTo("/internal/articles"))
                .withHost(equalTo("article-service"))
                .willReturn(ok()));

        PublishArticleDto dto = new PublishArticleDto("Test Title", "Test Content", 1L);
        articleService.saveArticle(dto);

        verify(postRequestedFor(urlEqualTo("/internal/articles"))
                .withRequestBody(equalToJson(expectedJson)));
    }

//    @Test
//    public void saveArticleWhenServiceIsUnavailable() {
//        PublishArticleDto dto = new PublishArticleDto("Test Title", "Test Content", 1L);
//        ExternalConnectException ex = assertThrows(ExternalConnectException.class,
//                () -> articleService.saveArticle(dto));
//        assertEquals("Article service is not available", ex.getMessage());
//    }
}