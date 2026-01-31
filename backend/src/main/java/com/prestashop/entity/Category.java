package com.prestashop.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "ps_category")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_category")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "link_rewrite", nullable = false, unique = true)
    private String linkRewrite;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_parent")
    private Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    @OrderBy("position ASC")
    @Builder.Default
    private List<Category> children = new ArrayList<>();

    @Column(name = "level_depth")
    @Builder.Default
    private Integer levelDepth = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer position = 0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "is_root_category")
    @Builder.Default
    private Boolean isRootCategory = false;

    @Column(name = "meta_title")
    private String metaTitle;

    @Column(name = "meta_description", columnDefinition = "TEXT")
    private String metaDescription;

    @ManyToMany(mappedBy = "categories")
    @Builder.Default
    private Set<Product> products = new HashSet<>();

    @CreationTimestamp
    @Column(name = "date_add", updatable = false)
    private LocalDateTime dateAdd;

    @UpdateTimestamp
    @Column(name = "date_upd")
    private LocalDateTime dateUpd;

    // Helper methods
    public void addChild(Category child) {
        children.add(child);
        child.setParent(this);
        child.setLevelDepth(this.levelDepth + 1);
    }

    public void removeChild(Category child) {
        children.remove(child);
        child.setParent(null);
    }

    public List<Category> getBreadcrumb() {
        List<Category> breadcrumb = new ArrayList<>();
        Category current = this;
        while (current != null && !current.getIsRootCategory()) {
            breadcrumb.add(0, current);
            current = current.getParent();
        }
        return breadcrumb;
    }
}
