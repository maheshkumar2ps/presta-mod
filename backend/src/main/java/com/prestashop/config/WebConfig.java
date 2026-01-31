package com.prestashop.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private Logger LOGGER = LoggerFactory.getLogger(getClass());

    @Value("${upload.images.path:./uploads/images}")
    private String uploadPath;

    @PostConstruct
    public void init() {
        LOGGER.info("=== WebConfig Static Resources ===");
        LOGGER.info("Upload path configured: {}", uploadPath);
        File uploadDir = new File(uploadPath);
        LOGGER.info("Upload path absolute: {}", uploadDir.getAbsolutePath());
        LOGGER.info("Upload path exists: {}", uploadDir.exists());
        LOGGER.info("Upload path is directory: {}", uploadDir.isDirectory());
        if (uploadDir.exists() && uploadDir.isDirectory()) {
            String[] files = uploadDir.list();
            LOGGER.info("Upload directory contents: {}", files != null ? files.length + " items" : "empty");
        }
        LOGGER.info("===================================");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String resourceLocation = "file:" + uploadPath + "/";
        LOGGER.info("Registering resource handler: /images/** -> {}", resourceLocation);

        registry.addResourceHandler("/images/**")
                .addResourceLocations(resourceLocation);
    }
}
