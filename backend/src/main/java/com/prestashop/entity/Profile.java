package com.prestashop.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ps_profile")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_profile")
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    public static final String SUPER_ADMIN = "SuperAdmin";
    public static final String ADMIN = "Admin";
    public static final String CATALOG_MANAGER = "CatalogManager";
}
