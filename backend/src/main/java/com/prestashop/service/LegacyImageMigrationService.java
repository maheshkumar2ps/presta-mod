package com.prestashop.service;

import com.prestashop.entity.Product;
import com.prestashop.entity.ProductImage;
import com.prestashop.repository.ProductImageRepository;
import com.prestashop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Migrates product images from PrestaShop legacy (fixtures or production img folder)
 * to prestashop-mod format.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LegacyImageMigrationService {

    private final ProductRepository productRepository;
    private final ProductImageRepository imageRepository;

    @Value("${legacy.migration.enabled:true}")
    private boolean migrationEnabled;

    @Value("${legacy.migration.fixtures-path:}")
    private String fixturesPath;

    @Value("${legacy.migration.img-path:}")
    private String imgPath;

    @Value("${upload.images.path:./uploads/images}")
    private String uploadPath;

    private static final Pattern IMAGE_ELEMENT = Pattern.compile(
            "<image\\s+id=\"([^\"]+)\"\\s+id_product=\"([^\"]+)\"\\s+cover=\"([^\"]*)\"",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Converts fixture product id (e.g. "Hummingbird_printed_t-shirt") to link_rewrite (e.g. "hummingbird-printed-t-shirt").
     * Handles "Mountain_fox_-_Vector_graphics" -> "mountain-fox-vector-graphics"
     */
    public static String fixtureIdToLinkRewrite(String fixtureId) {
        if (fixtureId == null || fixtureId.isBlank()) return "";
        String s = fixtureId.replace("_-_", "-").replace('_', '-').toLowerCase();
        return s.replaceAll("-+", "-").replaceAll("^-|-$", ""); // collapse multiple hyphens
    }

    /**
     * Resolves the legacy base path (fixtures or img folder).
     */
    public String resolveLegacyPath() {
        if (fixturesPath != null && !fixturesPath.isBlank()) {
            Path p = Paths.get(fixturesPath);
            if (Files.isDirectory(p)) return fixturesPath;
        }
        if (imgPath != null && !imgPath.isBlank()) {
            Path p = Paths.get(imgPath);
            if (Files.isDirectory(p)) return imgPath;
        }
        // Default: look for prestashop-legacy relative to current working directory
        Path defaultFixtures = Paths.get("..", "prestashop-legacy", "install-dev", "fixtures", "fashion");
        if (Files.isDirectory(defaultFixtures)) {
            return defaultFixtures.toAbsolutePath().toString();
        }
        Path defaultImg = Paths.get("..", "prestashop-legacy", "img");
        if (Files.isDirectory(defaultImg)) {
            return defaultImg.toAbsolutePath().toString();
        }
        return null;
    }

    /**
     * Runs the migration if enabled. Call this after products have been created.
     */
    public void runMigrationIfEnabled() {
        if (!migrationEnabled) {
            log.debug("Legacy image migration is disabled");
            return;
        }
        String path = resolveLegacyPath();
        if (path == null || path.isBlank()) {
            log.debug("Legacy migration path not configured, skipping");
            return;
        }
        try {
            int migrated = migrateImagesFromLegacy(path);
            if (migrated > 0) {
                log.info("Legacy image migration completed: {} images migrated", migrated);
            }
        } catch (Exception e) {
            log.warn("Legacy image migration failed: {}", e.getMessage());
        }
    }

    @Transactional
    public int migrateImagesFromLegacy(String legacyBasePath) throws IOException {
        Path base = Paths.get(legacyBasePath);
        if (!Files.isDirectory(base)) {
            throw new IOException("Legacy path is not a directory: " + legacyBasePath);
        }

        // Determine mode: fixtures (has data/image.xml + img/p/) or production (has img/p/ with numeric subdirs)
        Path fixturesImgPath = base.resolve("img").resolve("p");
        Path imageXmlPath = base.resolve("data").resolve("image.xml");
        Path productionImgPath = base.resolve("p");

        List<ImageMapping> mappings = new ArrayList<>();

        if (Files.exists(imageXmlPath) && Files.exists(fixturesImgPath)) {
            // Fixture mode: parse image.xml
            mappings = parseFixtureImageXml(imageXmlPath);
            return migrateFixtureImages(mappings, fixturesImgPath);
        } else if (Files.exists(productionImgPath)) {
            // Production img path passed directly (e.g. ../prestashop-legacy/img when base is img)
            return migrateProductionImages(productionImgPath);
        } else if (Files.exists(fixturesImgPath)) {
            // Try to match by filename pattern (fixture-style names without XML)
            return migrateFromImgFolder(fixturesImgPath);
        } else if (Files.isDirectory(base.resolve("p"))) {
            return migrateProductionImages(base.resolve("p"));
        }

        log.warn("Could not find legacy image structure in {}", legacyBasePath);
        return 0;
    }

    private List<ImageMapping> parseFixtureImageXml(Path imageXmlPath) throws IOException {
        String content = Files.readString(imageXmlPath);
        List<ImageMapping> mappings = new ArrayList<>();
        Matcher m = IMAGE_ELEMENT.matcher(content);
        while (m.find()) {
            String imageId = m.group(1);
            String productId = m.group(2);
            String coverStr = m.group(3);
            boolean cover = "1".equals(coverStr) || "true".equalsIgnoreCase(coverStr);
            mappings.add(new ImageMapping(imageId, productId, cover));
        }
        return mappings;
    }

    private int migrateFixtureImages(List<ImageMapping> mappings, Path imgDir) throws IOException {
        if (mappings.isEmpty()) return 0;

        // Group by product
        Map<String, List<ImageMapping>> byProduct = mappings.stream()
                .collect(Collectors.groupingBy(m -> m.productId));

        int migrated = 0;
        for (Map.Entry<String, List<ImageMapping>> entry : byProduct.entrySet()) {
            String fixtureProductId = entry.getKey();
            String linkRewrite = fixtureIdToLinkRewrite(fixtureProductId);

            Product product = productRepository.findByLinkRewrite(linkRewrite).orElse(null);
            if (product == null) {
                log.debug("No product found for link_rewrite={}, skipping images", linkRewrite);
                continue;
            }

            if (imageRepository.countByProductId(product.getId()) > 0) {
                log.debug("Product {} already has images, skipping", linkRewrite);
                continue;
            }

            List<ImageMapping> productImages = entry.getValue();
            productImages.sort(Comparator.comparing(m -> m.imageId));

            for (int i = 0; i < productImages.size(); i++) {
                ImageMapping im = productImages.get(i);
                Path imageFile = findLegacyImageFile(imgDir, im.imageId);
                if (imageFile != null) {
                    try {
                        migrateSingleImage(product, imageFile, im.cover, i);
                        migrated++;
                    } catch (IOException e) {
                        log.warn("Failed to migrate image {}: {}", im.imageId, e.getMessage());
                    }
                } else {
                    log.debug("Image file not found for {}", im.imageId);
                }
            }
        }
        return migrated;
    }

    private Path findLegacyImageFile(Path imgDir, String imageId) {
        // Try base image first (e.g. Hummingbird_printed_t-shirt.jpg)
        Path base = imgDir.resolve(imageId + ".jpg");
        if (Files.exists(base)) return base;
        // Try without extension variations
        for (String ext : List.of(".jpeg", ".png", ".webp")) {
            Path p = imgDir.resolve(imageId + ext);
            if (Files.exists(p)) return p;
        }
        // Try -large_default or similar (prefer larger)
        Path large = imgDir.resolve(imageId + "-large_default.jpg");
        if (Files.exists(large)) return large;
        Path medium = imgDir.resolve(imageId + "-medium_default.jpg");
        if (Files.exists(medium)) return medium;
        return null;
    }

    private void migrateSingleImage(Product product, Path sourceFile, boolean cover, int position) throws IOException {
        String ext = "";
        String fn = sourceFile.getFileName().toString();
        int dot = fn.lastIndexOf('.');
        if (dot > 0) ext = fn.substring(dot);

        String filename = UUID.randomUUID() + ext;
        Path productDir = Paths.get(uploadPath, "products", product.getId().toString());
        Files.createDirectories(productDir);
        Path targetPath = productDir.resolve(filename);
        Files.copy(sourceFile, targetPath, StandardCopyOption.REPLACE_EXISTING);

        if (cover) {
            imageRepository.clearCoverByProductId(product.getId());
        }

        ProductImage image = ProductImage.builder()
                .product(product)
                .filename(filename)
                .originalFilename(fn)
                .position(position)
                .cover(cover || position == 0)
                .legend(null)
                .mimeType(guessMimeType(ext))
                .fileSize(Files.size(targetPath))
                .build();

        imageRepository.save(image);
    }

    private String guessMimeType(String ext) {
        return switch (ext.toLowerCase()) {
            case ".jpg", ".jpeg" -> "image/jpeg";
            case ".png" -> "image/png";
            case ".webp" -> "image/webp";
            case ".gif" -> "image/gif";
            default -> "image/jpeg";
        };
    }

    private int migrateProductionImages(Path imgDir) throws IOException {
        // Production: img/p/1/2/3/123.jpg - id_image in path
        // We need id_product from the path or DB. In production, folder structure is by id_image.
        // Legacy ps_image has id_product. We'd need DB access. For now, skip production.
        log.info("Production image migration requires database mapping - use fixtures path for fixture-based migration");
        return 0;
    }

    private int migrateFromImgFolder(Path imgDir) throws IOException {
        // Scan img/p/ for *.jpg, derive product from filename (Hummingbird_printed_t-shirt -> product)
        Set<String> processedProducts = new HashSet<>();
        int migrated = 0;

        try (Stream<Path> files = Files.list(imgDir)) {
            // Base images: exclude resized variants (-home_default.jpg, -large_default.jpg, etc.)
            List<Path> jpgFiles = files
                    .filter(p -> Files.isRegularFile(p))
                    .filter(p -> {
                        String n = p.getFileName().toString();
                        return n.endsWith(".jpg") && !n.matches(".*-[a-z_]+_default\\.jpg$");
                    })
                    .sorted()
                    .toList();

            for (Path file : jpgFiles) {
                String baseName = file.getFileName().toString().replaceFirst("\\.jpg$", "");
                String linkRewrite = fixtureIdToLinkRewrite(baseName);

                Product product = productRepository.findByLinkRewrite(linkRewrite).orElse(null);
                if (product == null) continue;
                if (processedProducts.contains(linkRewrite)) continue;
                if (imageRepository.countByProductId(product.getId()) > 0) continue;

                try {
                    migrateSingleImage(product, file, true, 0);
                    migrated++;
                    processedProducts.add(linkRewrite);
                } catch (IOException e) {
                    log.warn("Failed to migrate {}: {}", baseName, e.getMessage());
                }
            }
        }
        return migrated;
    }

    private record ImageMapping(String imageId, String productId, boolean cover) {}
}
