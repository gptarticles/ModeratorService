package me.zedaster.moderationservice.service;

import me.zedaster.moderationservice.configuration.S3Configuration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

@SpringBootTest(classes = {ContentService.class, ContentServiceTest.TestConfig.class})
@Testcontainers
public class ContentServiceTest {

    private static final String MINIO_IMAGE = "minio/minio:RELEASE.2024-10-13T13-34-11Z";

    @Container
    private static final MinIOContainer minIoContainer = new MinIOContainer(MINIO_IMAGE);

    @TestConfiguration
    public static class TestConfig {
        @Bean
        @Primary
        public S3Configuration s3Configuration() {
            S3Configuration s3Config = Mockito.mock(S3Configuration.class);
            Mockito.when(s3Config.getAccessKey()).thenReturn(minIoContainer.getUserName());
            Mockito.when(s3Config.getSecretKey()).thenReturn(minIoContainer.getPassword());
            Mockito.when(s3Config.getEndpointUrl()).thenReturn(minIoContainer.getS3URL());
            Mockito.when(s3Config.getContentBucketName()).thenReturn("contents");
            return s3Config;
        }

    }

    @Autowired
    private ContentService contentService;

    @BeforeAll
    static void beforeAll() {
        minIoContainer.start();

    }

    @AfterAll
    static void afterAll() {
        minIoContainer.stop();
    }

    @Test
    public void testCrud() {
        String testContent = "a".repeat(100);
        // no content
        Optional<String> noContent = contentService.getContent(1L);
        Assertions.assertTrue(noContent.isEmpty());
        // create content
        contentService.saveContent(1L, testContent);
        // get existing content
        Optional<String> existingContent = contentService.getContent(1L);
        Assertions.assertTrue(existingContent.isPresent());
        Assertions.assertEquals(testContent, existingContent.get());
        // update content
        String updatedContent = "b".repeat(100);
        contentService.saveContent(1L, updatedContent);
        // get updated content
        Optional<String> updated = contentService.getContent(1L);
        Assertions.assertTrue(updated.isPresent());
        Assertions.assertEquals(updatedContent, updated.get());
        // remove content
        contentService.removeContent(1L);
        // no content
        Optional<String> noContentAfterRemove = contentService.getContent(1L);
        Assertions.assertTrue(noContentAfterRemove.isEmpty());
    }

    @Test
    public void getContentShouldThrowConnectException() {
        minIoContainer.stop();
        ExternalConnectException ex = Assertions.assertThrows(ExternalConnectException.class,
                () -> contentService.getContent(1L));
        Assertions.assertEquals("Failed to fetch content for article with ID 1 from S3 storage",
                ex.getMessage());
    }

    @Test
    public void saveContentShouldThrowConnectException() {
        minIoContainer.stop();
        String testContent = "a".repeat(100);
        ExternalConnectException ex = Assertions.assertThrows(ExternalConnectException.class,
                () -> contentService.saveContent(1L, testContent));
        Assertions.assertEquals("Failed to save content for article with ID 1 in S3 storage", ex.getMessage());
    }

    @Test
    public void removeContentShouldThrowConnectException() {
        minIoContainer.stop();
        ExternalConnectException ex = Assertions.assertThrows(ExternalConnectException.class,
                () -> contentService.removeContent(1L));
        Assertions.assertEquals("Failed to remove content for article with ID 1 from S3 storage", ex.getMessage());
    }
}
