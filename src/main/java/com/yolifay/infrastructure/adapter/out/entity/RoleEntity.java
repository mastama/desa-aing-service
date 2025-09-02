package com.yolifay.infrastructure.adapter.out.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "roles", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
public class RoleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64, unique = true)
    private String name; // contoh: SUPER_ADMIN, KEPALA_DESA, ...

    // (opsional) deskripsi/metadata
    private String description;
}
