package com.prestashop.service;

import com.prestashop.dto.ProductImageDto;
import com.prestashop.entity.Product;
import com.prestashop.entity.ProductImage;
import com.prestashop.exception.ResourceNotFoundException;
import com.prestashop.repository.ProductImageRepository;
import com.prestashop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {

    private final ProductImageRepository imageRepository;
    private final ProductRepository productRepository;
    private final S3Service s3Service;

    @Value("${upload.images.path:./uploads/images}")
    private String uploadPath;

    @Transactional(readOnly = true)
    public List<ProductImageDto> getProductImages(Long productId) {
        return imageRepository.findByProductIdOrderByPositionAsc(productId).stream()
                .map(ProductImageDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductImageDto uploadImage(Long productId, MultipartFile file, String legend, boolean cover) throws IOException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";
        String filename = UUID.randomUUID().toString() + extension;

        // Upload to S3
        String s3Key = s3Service.uploadFile(file, productId);
        String s3Url = s3Service.getPublicUrl(s3Key);
        log.info("Uploaded image to S3: {} -> {}", originalFilename, s3Url);

        // If setting as cover, clear existing covers
        if (cover) {
            imageRepository.clearCoverByProductId(productId);
        }

        // Get next position
        Integer maxPosition = imageRepository.findMaxPositionByProductId(productId);
        int position = maxPosition != null ? maxPosition + 1 : 0;

        // Create image entity with S3 details
        ProductImage image = ProductImage.builder()
                .product(product)
                .filename(filename)
                .originalFilename(originalFilename)
                .mimeType(contentType)
                .fileSize(file.getSize())
                .position(position)
                .cover(cover || position == 0) // First image is cover by default
                .legend(legend)
                .s3Key(s3Key)
                .s3Url(s3Url)
                .build();

        image = imageRepository.save(image);
        return ProductImageDto.fromEntity(image);
    }

    @Transactional
    public void deleteImage(Long imageId) {
        ProductImage image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found: " + imageId));

        // Delete from S3 if S3 key exists
        if (image.getS3Key() != null && !image.getS3Key().isEmpty()) {
            try {
                s3Service.deleteFile(image.getS3Key());
                log.info("Deleted image from S3: {}", image.getS3Key());
            } catch (Exception e) {
                log.error("Failed to delete image from S3: {}", image.getS3Key(), e);
            }
        } else {
            // Fallback: Delete from local filesystem (for legacy images)
            try {
                Path filePath = Paths.get(uploadPath, "products", image.getProduct().getId().toString(), image.getFilename());
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                log.error("Failed to delete local image file", e);
            }
        }

        // If this was the cover, make the first remaining image the cover
        boolean wasCover = image.getCover();
        Long productId = image.getProduct().getId();

        imageRepository.delete(image);

        if (wasCover) {
            List<ProductImage> remaining = imageRepository.findByProductIdOrderByPositionAsc(productId);
            if (!remaining.isEmpty()) {
                ProductImage newCover = remaining.get(0);
                newCover.setCover(true);
                imageRepository.save(newCover);
            }
        }
    }

    @Transactional
    public ProductImageDto setCover(Long imageId) {
        ProductImage image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found: " + imageId));

        imageRepository.clearCoverByProductId(image.getProduct().getId());
        image.setCover(true);
        image = imageRepository.save(image);

        return ProductImageDto.fromEntity(image);
    }

    @Transactional
    public void updatePositions(Long productId, List<Long> imageIds) {
        for (int i = 0; i < imageIds.size(); i++) {
            Long imageId = imageIds.get(i);
            int position = i;
            imageRepository.findById(imageId).ifPresent(image -> {
                if (image.getProduct().getId().equals(productId)) {
                    image.setPosition(position);
                    imageRepository.save(image);
                }
            });
        }
    }
}
