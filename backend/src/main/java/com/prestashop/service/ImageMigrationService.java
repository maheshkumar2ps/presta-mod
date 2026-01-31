package com.prestashop.service;

import com.prestashop.entity.ProductImage;
import com.prestashop.repository.ProductImageRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageMigrationService {
    private Logger LOGGER = LoggerFactory.getLogger(getClass());

    private final ProductImageRepository imageRepository;
    private final S3Client s3Client;
    private final S3Service s3Service;

    @Value("${upload.images.path:./uploads/images}")
    private String uploadPath;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.region}")
    private String region;

    /**
     * Migrates all local images to S3 that don't have S3 URLs yet
     */
    @Transactional
    public MigrationResult migrateLocalImagesToS3() {
        LOGGER.info("Starting migration of local images to S3...");

        List<ProductImage> imagesWithoutS3 = imageRepository.findImagesWithoutS3Url();
        LOGGER.info("Found {} images without S3 URLs", imagesWithoutS3.size());

        int success = 0;
        int failed = 0;
        int skipped = 0;

        for (ProductImage image : imagesWithoutS3) {
            try {
                boolean migrated = migrateImageToS3(image);
                if (migrated) {
                    success++;
                } else {
                    skipped++;
                }
            } catch (Exception e) {
                LOGGER.error("Failed to migrate image {} for product {}: {}",
                        image.getId(), image.getProduct().getId(), e.getMessage());
                failed++;
            }
        }

        LOGGER.info("Migration completed: {} success, {} failed, {} skipped",
                success, failed, skipped);

        return new MigrationResult(imagesWithoutS3.size(), success, failed, skipped);
    }

    /**
     * Migrates a single image to S3
     */
    private boolean migrateImageToS3(ProductImage image) throws IOException {
        Long productId = image.getProduct().getId();
        String filename = image.getFilename();

        // Build local file path
        Path localFilePath = Paths.get(uploadPath, "products", productId.toString(), filename);

        LOGGER.debug("Checking local file: {}", localFilePath.toAbsolutePath());

        if (!Files.exists(localFilePath)) {
            LOGGER.warn("Local file not found for image {} (product {}): {}",
                    image.getId(), productId, localFilePath);
            return false;
        }

        // Read file content
        byte[] fileContent = Files.readAllBytes(localFilePath);
        String contentType = image.getMimeType() != null ? image.getMimeType() : "image/jpeg";

        // Generate S3 key
        String s3Key = "products/" + productId + "/" + filename;

        LOGGER.info("Uploading image {} to S3: {} ({} bytes)", image.getId(), s3Key, fileContent.length);

        // Upload to S3
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .contentType(contentType)
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(fileContent));

        // Update database record
        String s3Url = s3Service.getPublicUrl(s3Key);
        image.setS3Key(s3Key);
        image.setS3Url(s3Url);
        imageRepository.save(image);

        LOGGER.info("Successfully migrated image {} to S3: {}", image.getId(), s3Url);
        return true;
    }

    public record MigrationResult(int total, int success, int failed, int skipped) {}
}
