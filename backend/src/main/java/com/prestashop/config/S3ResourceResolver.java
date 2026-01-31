package com.prestashop.config;

import com.prestashop.service.S3Service;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.resource.ResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolverChain;

import java.util.List;

/**
 * Resolves /images/products/** to resources loaded from the S3 bucket.
 * Other /images/** paths are delegated to the chain (e.g. local file fallback).
 */
public class S3ResourceResolver implements ResourceResolver {

    private static final String PRODUCTS_PREFIX = "products/";

    private final S3Service s3Service;

    public S3ResourceResolver(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    @Override
    @Nullable
    public Resource resolveResource(@Nullable HttpServletRequest request, String requestPath,
                                    List<? extends Resource> locations, ResourceResolverChain chain) {
        if (requestPath != null && requestPath.startsWith(PRODUCTS_PREFIX)) {
            return new S3Resource(s3Service, requestPath);
        }
        return chain.resolveResource(request, requestPath, locations);
    }

    @Override
    @Nullable
    public String resolveUrlPath(String resourcePath, List<? extends Resource> locations,
                                 ResourceResolverChain chain) {
        return chain.resolveUrlPath(resourcePath, locations);
    }
}
