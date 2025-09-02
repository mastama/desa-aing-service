package com.yolifay.infrastructure.adapter.out.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenerationTime;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;
    @Column(unique = true) private String username;
    @Column(unique = true) private String email;
    private String passwordHash;
    @Version private long version;
    private String phoneNumber;

    private String status; // ACTIVE/LOCKED/...
    // created_at diisi default DB saat INSERT
    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private Instant createdAt;

    // updated_at diisi trigger DB saat UPDATE
    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    @org.hibernate.annotations.Generated(GenerationTime.ALWAYS)
    private Instant updatedAt;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<RoleEntity> roles = new HashSet<>();
}
