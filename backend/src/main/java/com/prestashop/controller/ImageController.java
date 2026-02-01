package com.prestashop.controller;

import com.prestashop.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Serves product images from S3 at /images/products/{productId}/{filename}.
 * Uses streaming to avoid ResourceHttpRequestHandler's getFile() requirement.
 */
@RestController
@RequestMapping("/images")
@RequiredArgsConstructor
public class ImageController {

    private static final String PRODUCTS_PREFIX = "products/";
    private static final Map<String, MediaType> EXTENSION_TO_MEDIA = Map.of(
            "jpg", MediaType.IMAGE_JPEG,
            "jpeg", MediaType.IMAGE_JPEG,
            "png", MediaType.IMAGE_PNG,
            "gif", MediaType.IMAGE_GIF,
            "webp", MediaType.parseMediaType("image/webp")
    );

    private final S3Service s3Service;

    @GetMapping("/products/{productId}/{filename}")
    public ResponseEntity<StreamingResponseBody> getProductImage(
            @PathVariable Long productId,
            @PathVariable String filename) {
        String s3Key = PRODUCTS_PREFIX + productId + "/" + filename;
        try {
            InputStream source = s3Service.getObjectContent(s3Key);
            MediaType mediaType = contentTypeFromFilename(filename);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(mediaType);
            headers.setCacheControl("public, max-age=86400");

            StreamingResponseBody body = outputStream -> {
                try (source) {
                    source.transferTo(outputStream);
                }
            };
            return new ResponseEntity<>(body, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private static MediaType contentTypeFromFilename(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot >= 0) {
            String ext = filename.substring(dot + 1).toLowerCase();
            MediaType type = EXTENSION_TO_MEDIA.get(ext);
            if (type != null) return type;
        }
        return MediaType.IMAGE_JPEG;
    }
}
