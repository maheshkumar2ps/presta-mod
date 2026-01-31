package com.prestashop.config;

import com.prestashop.entity.*;
import com.prestashop.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final ProfileRepository profileRepository;
    private final EmployeeRepository employeeRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        initProfiles();
        initAdminUser();
        initCategories();
        initProducts();
    }

    private void initProfiles() {
        if (profileRepository.count() == 0) {
            log.info("Initializing profiles...");
            profileRepository.saveAll(List.of(
                    Profile.builder().name(Profile.SUPER_ADMIN).build(),
                    Profile.builder().name(Profile.ADMIN).build(),
                    Profile.builder().name(Profile.CATALOG_MANAGER).build()
            ));
        }
    }

    private void initAdminUser() {
        if (employeeRepository.count() == 0) {
            log.info("Creating default admin user...");
            Profile superAdmin = profileRepository.findByName(Profile.SUPER_ADMIN)
                    .orElseThrow(() -> new RuntimeException("SuperAdmin profile not found"));

            Employee admin = Employee.builder()
                    .email("admin@prestashop.com")
                    .password(passwordEncoder.encode("admin123"))
                    .firstName("Admin")
                    .lastName("User")
                    .profile(superAdmin)
                    .active(true)
                    .build();

            employeeRepository.save(admin);
            log.info("Default admin created: admin@prestashop.com / admin123");
        }
    }

    private void initCategories() {
        if (categoryRepository.count() == 0) {
            log.info("Initializing categories from PrestaShop legacy data...");

            // Root category
            Category root = Category.builder()
                    .name("Home")
                    .linkRewrite("home")
                    .description("Home category")
                    .isRootCategory(true)
                    .levelDepth(0)
                    .position(0)
                    .active(true)
                    .build();
            root = categoryRepository.save(root);

            // Main categories (from PrestaShop fashion fixtures)
            Category clothes = createCategory("Clothes", "clothes", "Discover our fashionable clothes collection", root, 0);
            Category accessories = createCategory("Accessories", "accessories", "Items and accessories for your lifestyle", root, 1);
            Category art = createCategory("Art", "art", "Framed posters and vector graphics", root, 2);

            // Clothes subcategories
            createCategory("Men", "men", "T-shirts, sweaters, and more for men", clothes, 0);
            createCategory("Women", "women", "T-shirts, sweaters, and more for women", clothes, 1);

            // Accessories subcategories
            createCategory("Stationery", "stationery", "Notebooks and writing accessories", accessories, 0);
            createCategory("Home Accessories", "home-accessories", "Mugs, cushions, and home decor", accessories, 1);
        }
    }

    private Category createCategory(String name, String slug, String description, Category parent, int position) {
        Category category = Category.builder()
                .name(name)
                .linkRewrite(slug)
                .description(description)
                .parent(parent)
                .levelDepth(parent.getLevelDepth() + 1)
                .position(position)
                .active(true)
                .build();
        return categoryRepository.save(category);
    }

    private void initProducts() {
        if (productRepository.count() == 0) {
            log.info("Creating products from PrestaShop legacy data...");

            // Get categories
            Category men = categoryRepository.findByLinkRewrite("men").orElse(null);
            Category women = categoryRepository.findByLinkRewrite("women").orElse(null);
            Category stationery = categoryRepository.findByLinkRewrite("stationery").orElse(null);
            Category homeAccessories = categoryRepository.findByLinkRewrite("home-accessories").orElse(null);
            Category art = categoryRepository.findByLinkRewrite("art").orElse(null);

            // ===== CLOTHES - MEN =====
            if (men != null) {
                createProduct(
                    "Hummingbird printed t-shirt",
                    "hummingbird-printed-t-shirt",
                    "Regular fit, round neckline, short sleeves. Made of extra long staple pima cotton.",
                    "Symbol of lightness and delicacy, the hummingbird evokes curiosity and joy. Studio Design's PolyFaune collection features classic products with colorful patterns, inspired by the traditional Japanese origamis. To wear with a chino or jeans. The sublimation textile printing process provides an exceptional color rendering and target durability.",
                    new BigDecimal("23.90"),
                    new BigDecimal("5.49"),
                    300,
                    "demo_1",
                    men
                );
            }

            // ===== CLOTHES - WOMEN =====
            if (women != null) {
                createProduct(
                    "Hummingbird printed sweater",
                    "hummingbird-printed-sweater",
                    "Regular fit, round neckline, long sleeves. 100% cotton, brushed inner side for extra comfort.",
                    "Studio Design's PolyFaune collection features classic products with colorful patterns, inspired by the traditional Japanese origamis. To wear with a chino or jeans. The sublimation textile printing process provides an exceptional color rendering and target durability.",
                    new BigDecimal("35.90"),
                    new BigDecimal("5.49"),
                    1200,
                    "demo_3",
                    women
                );

                createProduct(
                    "Brown bear printed sweater",
                    "brown-bear-printed-sweater",
                    "Regular fit, round neckline, long sleeves. 100% cotton, brushed inner side for extra comfort.",
                    "Studio Design's PolyFaune collection features classic products with colorful patterns, inspired by the traditional Japanese origamis. To wear with a chino or jeans.",
                    new BigDecimal("35.90"),
                    new BigDecimal("5.49"),
                    800,
                    "demo_2",
                    women
                );
            }

            // ===== STATIONERY =====
            if (stationery != null) {
                createProduct(
                    "Mountain fox notebook",
                    "mountain-fox-notebook",
                    "120 sheets notebook with hard cover made of recycled cardboard. 16x22cm",
                    "The Mountain fox notebook is the best option to write down your most ingenious ideas. At work, at home or when traveling, its endearing design and manufacturing quality will make you feel like writing! 90 gsm paper / double spiral binding.",
                    new BigDecimal("12.90"),
                    new BigDecimal("5.49"),
                    600,
                    "demo_8",
                    stationery
                );

                createProduct(
                    "Brown bear notebook",
                    "brown-bear-notebook",
                    "120 sheets notebook with hard cover made of recycled cardboard. 16x22cm",
                    "The Brown bear notebook is the best option to write down your most ingenious ideas. At work, at home or when traveling, its endearing design and manufacturing quality will make you feel like writing! 90 gsm paper / double spiral binding.",
                    new BigDecimal("12.90"),
                    new BigDecimal("5.49"),
                    600,
                    "demo_9",
                    stationery
                );

                createProduct(
                    "Hummingbird notebook",
                    "hummingbird-notebook",
                    "120 sheets notebook with hard cover made of recycled cardboard. 16x22cm",
                    "The Hummingbird notebook is the best option to write down your most ingenious ideas. At work, at home or when traveling, its endearing design and manufacturing quality will make you feel like writing! 90 gsm paper / double spiral binding.",
                    new BigDecimal("12.90"),
                    new BigDecimal("5.49"),
                    600,
                    "demo_10",
                    stationery
                );
            }

            // ===== HOME ACCESSORIES =====
            if (homeAccessories != null) {
                createProduct(
                    "Mug The best is yet to come",
                    "mug-the-best-is-yet-to-come",
                    "White Ceramic Mug, 325ml.",
                    "The best is yet to come! Start the day off right with a positive thought. 8.2cm diameter / 9.5cm height / 0.43kg. Dishwasher-proof.",
                    new BigDecimal("11.90"),
                    new BigDecimal("5.49"),
                    300,
                    "demo_11",
                    homeAccessories
                );

                createProduct(
                    "Mug The adventure begins",
                    "mug-the-adventure-begins",
                    "White Ceramic Mug, 325ml.",
                    "The adventure begins with a cup of coffee. Set out to conquer the day! 8.2cm diameter / 9.5cm height / 0.43kg. Dishwasher-proof.",
                    new BigDecimal("11.90"),
                    new BigDecimal("5.49"),
                    300,
                    "demo_12",
                    homeAccessories
                );

                createProduct(
                    "Mug Today is a good day",
                    "mug-today-is-a-good-day",
                    "White Ceramic Mug, 325ml.",
                    "Add an optimistic touch to your morning coffee and start the day in a good mood! 8.2cm diameter / 9.5cm height / 0.43kg. Dishwasher-proof.",
                    new BigDecimal("11.90"),
                    new BigDecimal("5.49"),
                    300,
                    "demo_13",
                    homeAccessories
                );

                createProduct(
                    "Customizable mug",
                    "customizable-mug",
                    "White Ceramic Mug, 325ml.",
                    "Customize your mug with the text of your choice. A mood, a message, a quote... It's up to you! Maximum number of characters: 30",
                    new BigDecimal("13.90"),
                    new BigDecimal("5.49"),
                    300,
                    "demo_14",
                    homeAccessories
                );

                createProduct(
                    "Mountain fox cushion",
                    "mountain-fox-cushion",
                    "Cushion with removable cover and invisible zip on the back. 32x32cm",
                    "The mountain fox cushion will add a graphic and colorful touch to your sofa, armchair or bed. Create a modern and zen atmosphere that inspires relaxation. Cover 100% cotton, machine washable at 60°. Filling 100% hypoallergenic polyester.",
                    new BigDecimal("18.90"),
                    new BigDecimal("5.49"),
                    600,
                    "demo_15",
                    homeAccessories
                );

                createProduct(
                    "Brown bear cushion",
                    "brown-bear-cushion",
                    "Cushion with removable cover and invisible zip on the back. 32x32cm",
                    "The brown bear cushion will add a graphic and colorful touch to your sofa, armchair or bed. Create a modern and zen atmosphere that inspires relaxation. Cover 100% cotton, machine washable at 60°. Filling 100% hypoallergenic polyester.",
                    new BigDecimal("18.90"),
                    new BigDecimal("5.49"),
                    600,
                    "demo_16",
                    homeAccessories
                );

                createProduct(
                    "Hummingbird cushion",
                    "hummingbird-cushion",
                    "Cushion with removable cover and invisible zip on the back. 32x32cm",
                    "The hummingbird cushion will add a graphic and colorful touch to your sofa, armchair or bed. Create a modern and zen atmosphere that inspires relaxation. Cover 100% cotton, machine washable at 60°. Filling 100% hypoallergenic polyester.",
                    new BigDecimal("18.90"),
                    new BigDecimal("5.49"),
                    600,
                    "demo_17",
                    homeAccessories
                );
            }

            // ===== ART =====
            if (art != null) {
                createProduct(
                    "The best is yet to come - Framed poster",
                    "the-best-is-yet-to-come-framed-poster",
                    "Printed on rigid matt paper and smooth surface.",
                    "The best is yet to come! Give your walls a voice with a framed poster. This aesthetic, optimistic poster will look great on your desk or in an open-space office. Painted wooden frame with passe-partout for more depth.",
                    new BigDecimal("29.00"),
                    new BigDecimal("5.49"),
                    900,
                    "demo_6",
                    art
                );

                createProduct(
                    "The adventure begins - Framed poster",
                    "the-adventure-begins-framed-poster",
                    "Printed on rigid matt finish and smooth surface.",
                    "Give your walls a voice with a framed poster. This aesthetic, adventurous poster will look great on your desk or in an open-space office. Painted wooden frame with passe-partout for more depth.",
                    new BigDecimal("29.00"),
                    new BigDecimal("5.49"),
                    900,
                    "demo_5",
                    art
                );

                createProduct(
                    "Today is a good day - Framed poster",
                    "today-is-a-good-day-framed-poster",
                    "Printed on rigid paper with matt finish and smooth surface.",
                    "Today is a good day! Give your walls a voice with a framed poster. This aesthetic, optimistic poster will look great on your desk or in an open-space office. Painted wooden frame with passe-partout for more depth.",
                    new BigDecimal("29.00"),
                    new BigDecimal("5.49"),
                    900,
                    "demo_7",
                    art
                );

                createProduct(
                    "Mountain fox - Vector graphics",
                    "mountain-fox-vector-graphics",
                    "Vector graphic, format: svg. Download for personal, private and non-commercial use.",
                    "You have a custom printing creative project? The vector graphic Mountain fox illustration can be used for printing purpose on any support, without size limitation.",
                    new BigDecimal("9.00"),
                    new BigDecimal("5.49"),
                    1200,
                    "demo_18",
                    art
                );

                createProduct(
                    "Brown bear - Vector graphics",
                    "brown-bear-vector-graphics",
                    "Vector graphic, format: svg. Download for personal, private and non-commercial use.",
                    "You have a custom printing creative project? The vector graphic Brown bear illustration can be used for printing purpose on any support, without size limitation.",
                    new BigDecimal("9.00"),
                    new BigDecimal("5.49"),
                    1200,
                    "demo_19",
                    art
                );

                createProduct(
                    "Hummingbird - Vector graphics",
                    "hummingbird-vector-graphics",
                    "Vector graphic, format: svg. Download for personal, private and non-commercial use.",
                    "You have a custom printing creative project? The vector graphic Hummingbird illustration can be used for printing purpose on any support, without size limitation.",
                    new BigDecimal("9.00"),
                    new BigDecimal("5.49"),
                    1200,
                    "demo_20",
                    art
                );
            }

            log.info("Created {} products from PrestaShop legacy data", productRepository.count());
        }
    }

    private Product createProduct(String name, String slug, String shortDesc, String description,
                                  BigDecimal price, BigDecimal wholesalePrice, int quantity,
                                  String reference, Category category) {
        Product product = Product.builder()
                .name(name)
                .linkRewrite(slug)
                .descriptionShort(shortDesc)
                .description(description)
                .price(price)
                .wholesalePrice(wholesalePrice)
                .quantity(quantity)
                .reference(reference)
                .defaultCategory(category)
                .active(true)
                .visibility(Product.Visibility.BOTH)
                .condition(Product.ProductCondition.NEW)
                .productType(Product.ProductType.STANDARD)
                .build();
        product.addCategory(category);
        return productRepository.save(product);
    }
}
