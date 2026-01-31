package com.prestashop.config;

import com.prestashop.service.S3Service;
import org.springframework.core.io.AbstractResource;

import java.io.IOException;
import java.io.InputStream;

/**
 * Spring Resource that reads content from S3.
 * Used by WebConfig to serve /images/products/** from the S3 bucket.
 */
public class S3Resource extends AbstractResource {

    private final S3Service s3Service;
    private final String s3Key;

    public S3Resource(S3Service s3Service, String s3Key) {
        this.s3Service = s3Service;
        this.s3Key = s3Key;
    }

    @Override
    public String getDescription() {
        return "S3 resource [" + s3Key + "]";
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return s3Service.getObjectContent(s3Key);
    }

    @Override
    public boolean exists() {
        return true;
    }

    @Override
    public String getFilename() {
        int lastSlash = s3Key.lastIndexOf('/');
        return lastSlash >= 0 ? s3Key.substring(lastSlash + 1) : s3Key;
    }
}
