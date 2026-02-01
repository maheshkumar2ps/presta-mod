package com.prestashop.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {
    private Logger LOGGER = LoggerFactory.getLogger(getClass());

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.region}")
    private String region;

    @PostConstruct
    public void init() {
        LOGGER.info("=== S3Service Configuration ===");
        LOGGER.info("S3 Bucket Name: {}", bucketName);
        LOGGER.info("AWS Region: {}", region);
        LOGGER.info("S3 URL Pattern: https://{}.s3.{}.amazonaws.com/...", bucketName, region);
        LOGGER.info("================================");
    }

    /**
     * Uploads a file to S3 and returns the S3 key
     */
    public String uploadFile(MultipartFile file, Long productId) throws IOException {
        LOGGER.debug("Starting S3 upload for product {} - file: {}, size: {} bytes, contentType: {}",
                productId, file.getOriginalFilename(), file.getSize(), file.getContentType());

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";

        String filename = UUID.randomUUID().toString() + extension;
        String s3Key = "products/" + productId + "/" + filename;

        LOGGER.debug("Generated S3 key: {}", s3Key);

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(file.getContentType())
                    .build();

            LOGGER.debug("Uploading to S3 bucket: {}", bucketName);
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            String publicUrl = getPublicUrl(s3Key);
            LOGGER.info("Successfully uploaded file to S3: {} -> {}", originalFilename, publicUrl);
            return s3Key;
        } catch (S3Exception e) {
            LOGGER.error("S3 upload failed for product {} - file: {}, error: {}",
                    productId, originalFilename, e.awsErrorDetails().errorMessage(), e);
            throw new IOException("Failed to upload to S3: " + e.awsErrorDetails().errorMessage(), e);
        } catch (Exception e) {
            LOGGER.error("Unexpected error during S3 upload for product {} - file: {}",
                    productId, originalFilename, e);
            throw new IOException("Failed to upload to S3: " + e.getMessage(), e);
        }
    }

    /**
     * Uploads a file from the local filesystem to S3 (e.g. during legacy migration).
     * Used when migrating images directly to S3 instead of copying to upload folder.
     *
     * @param sourcePath path to the source file
     * @param productId product id for the key prefix
     * @param contentType MIME type (e.g. image/jpeg)
     * @return the S3 key (use getPublicUrl(key) for the URL)
     */
    public String uploadFromPath(Path sourcePath, Long productId, String contentType) throws IOException {
        String filename = sourcePath.getFileName().toString();
        String extension = filename.contains(".") ? filename.substring(filename.lastIndexOf(".")) : ".jpg";
        String s3Filename = UUID.randomUUID().toString() + extension;
        String s3Key = "products/" + productId + "/" + s3Filename;

        byte[] bytes = Files.readAllBytes(sourcePath);
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .contentType(contentType != null ? contentType : "image/jpeg")
                .build();
        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(bytes));
        LOGGER.info("Uploaded from path to S3: {} -> {} ({} bytes)", sourcePath.getFileName(), s3Key, bytes.length);
        return s3Key;
    }

    /**
     * Deletes a file from S3
     */
    public void deleteFile(String s3Key) {
        LOGGER.debug("Deleting from S3 - bucket: {}, key: {}", bucketName, s3Key);

        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            LOGGER.info("Successfully deleted file from S3: {}", s3Key);
        } catch (S3Exception e) {
            LOGGER.error("S3 delete failed for key: {}, error: {}", s3Key, e.awsErrorDetails().errorMessage(), e);
            throw e;
        }
    }

    /**
     * Gets object content from S3 as an input stream.
     * Caller is responsible for closing the stream.
     *
     * @param s3Key the S3 object key (e.g. products/1/uuid.jpg)
     * @return input stream of the object content
     * @throws IOException if the object does not exist or cannot be read
     */
    public InputStream getObjectContent(String s3Key) throws IOException {
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();
            ResponseInputStream<GetObjectResponse> response = s3Client.getObject(request);
            LOGGER.debug("Fetched object from S3: {}", s3Key);
            return response;
        } catch (S3Exception e) {
            LOGGER.debug("S3 getObject failed for key: {}, error: {}", s3Key, e.awsErrorDetails().errorMessage());
            throw new IOException("Failed to read from S3: " + e.awsErrorDetails().errorMessage(), e);
        }
    }

    /**
     * Gets the public URL for an S3 object
     */
    public String getPublicUrl(String s3Key) {
        String url = String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, s3Key);
        LOGGER.debug("Generated S3 public URL: {}", url);
        return url;
    }
}
