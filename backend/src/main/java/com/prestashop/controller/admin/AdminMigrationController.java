package com.prestashop.controller.admin;

import com.prestashop.dto.ApiResponse;
import com.prestashop.service.ImageMigrationService;
import com.prestashop.service.LegacyImageMigrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/migration")
@RequiredArgsConstructor
@Tag(name = "Admin - Migration", description = "Legacy data migration endpoints")
@SecurityRequirement(name = "bearerAuth")
public class AdminMigrationController {

    private final LegacyImageMigrationService legacyImageMigrationService;
    private final ImageMigrationService imageMigrationService;

    @PostMapping("/legacy-images")
    @Operation(summary = "Migrate legacy images", description = "Migrate product images from prestashop-legacy fixtures to prestashop-mod")
    public ResponseEntity<ApiResponse<Map<String, Object>>> migrateLegacyImages(
            @RequestParam(required = false) String path) {
        try {
            String basePath = path != null && !path.isBlank()
                    ? path
                    : legacyImageMigrationService.resolveLegacyPath();

            if (basePath == null || basePath.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Legacy path not found. Set LEGACY_FIXTURES_PATH or pass path parameter."));
            }

            int migrated = legacyImageMigrationService.migrateImagesFromLegacy(basePath);
            return ResponseEntity.ok(ApiResponse.success(Map.of(
                    "migrated", migrated,
                    "path", basePath,
                    "message", "Migrated " + migrated + " images from " + basePath
            )));
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Migration failed: " + e.getMessage()));
        }
    }

    @GetMapping("/legacy-path")
    @Operation(summary = "Get resolved legacy path", description = "Returns the resolved path used for legacy image migration")
    public ResponseEntity<ApiResponse<Map<String, String>>> getLegacyPath() {
        String path = legacyImageMigrationService.resolveLegacyPath();
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "resolvedPath", path != null ? path : "",
                "configured", path != null && !path.isBlank() ? "true" : "false"
        )));
    }

    @PostMapping("/images-to-s3")
    @Operation(summary = "Migrate local images to S3", description = "Uploads all local product images to S3 bucket and updates database with S3 URLs")
    public ResponseEntity<ApiResponse<Map<String, Object>>> migrateImagesToS3() {
        try {
            ImageMigrationService.MigrationResult result = imageMigrationService.migrateLocalImagesToS3();
            return ResponseEntity.ok(ApiResponse.success(Map.of(
                    "total", result.total(),
                    "success", result.success(),
                    "failed", result.failed(),
                    "skipped", result.skipped(),
                    "message", String.format("Migration completed: %d success, %d failed, %d skipped out of %d total",
                            result.success(), result.failed(), result.skipped(), result.total())
            )));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("S3 migration failed: " + e.getMessage()));
        }
    }
}
