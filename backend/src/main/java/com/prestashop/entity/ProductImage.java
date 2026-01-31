package com.prestashop.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ps_image")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_image")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_product", nullable = false)
    private Product product;

    @Column(nullable = false)
    @Builder.Default
    private Integer position = 0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean cover = false;

    @Column(nullable = false)
    private String filename;

    @Column(name = "original_filename")
    private String originalFilename;

    @Column
    private String legend;

    @Column(name = "mime_type")
    private String mimeType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "s3_key")
    private String s3Key;

    @Column(name = "s3_url", length = 512)
    private String s3Url;

    public String getUrl() {
        // Return S3 URL if available, otherwise fall back to local path
        if (s3Url != null && !s3Url.isEmpty()) {
            return s3Url;
        }
        return "/images/products/" + product.getId() + "/" + filename;
    }
}
