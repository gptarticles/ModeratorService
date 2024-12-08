package me.zedaster.moderationservice.service;

import me.zedaster.moderationservice.configuration.S3Configuration;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Service
public class ContentService {

    /**
     * Client for interacting with S3
     */
    private final S3Client s3Client;

    /**
     * Bucket name for storing content
     */
    private final String bucketName;

    public ContentService(S3Configuration s3config) {
        // 18_000 * 4 bytes = 72_000 bytes = 0.072 MB (max size of file with content)
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(s3config.getAccessKey(), s3config.getSecretKey());
        this.bucketName = s3config.getContentBucketName();
        this.s3Client = S3Client.builder()
                .endpointOverride(URI.create(s3config.getEndpointUrl()))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .forcePathStyle(true)
                .region(Region.AWS_GLOBAL)
                .build();
        createBucketIfNotExists();
    }

    public Optional<String> getContent(long articleId) throws ExternalConnectException {
        String key = "articles/" + articleId + ".txt";

        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            String content = s3Client.getObjectAsBytes(getObjectRequest).asUtf8String();
            return Optional.of(content);
        } catch (NoSuchKeyException e) {
            return Optional.empty();
        } catch (SdkClientException e) {
            throw new ExternalConnectException("Failed to fetch content for article with ID %d from S3 storage"
                    .formatted(articleId), e);
        }
    }

    public void saveContent(long articleId, String content) throws ExternalConnectException  {
        String key = "articles/" + articleId + ".txt";

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromString(content, StandardCharsets.UTF_8));
        } catch (SdkClientException e) {
            throw new ExternalConnectException("Failed to save content for article with ID %d in S3 storage"
                    .formatted(articleId), e);
        }
    }

    public void removeContent(long articleId) throws ExternalConnectException  {
        String key = "articles/" + articleId + ".txt";

        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
        } catch (SdkClientException e) {
            throw new ExternalConnectException("Failed to remove content for article with ID %d from S3 storage"
                    .formatted(articleId), e);
        }
    }

    /**
     * Create a bucket if it doesn't exist
     */
    private void createBucketIfNotExists() {
        try {
            if (!bucketExists()) {
                s3Client.createBucket(b -> b.bucket(bucketName));
            }
        } catch (SdkClientException e) {
            throw new RuntimeException("Failed to create bucket: " + bucketName, e);
        }
    }

    /**
     * Check if the bucket exists
     * @return True if the bucket exists, false otherwise
     */
    private boolean bucketExists() {
        HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
                .bucket(bucketName)
                .build();

        try {
            s3Client.headBucket(headBucketRequest);
            return true;
        } catch (NoSuchBucketException e) {
            return false;
        } catch (SdkClientException e) {
            throw new RuntimeException("Failed to check if bucket exists: " + bucketName, e);
        }
    }
}
