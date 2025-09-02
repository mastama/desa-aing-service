package com.yolifay.infrastructure.adapter.out.persistence;

import com.yolifay.domain.model.Role;
import com.yolifay.domain.model.User;
import com.yolifay.domain.valueobject.PasswordHash;
import com.yolifay.infrastructure.adapter.out.entity.RoleEntity;
import com.yolifay.infrastructure.adapter.out.entity.UserEntity;
import com.yolifay.infrastructure.adapter.out.persistence.repository.RoleJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class UserMapperJpa {

    private final RoleJpaRepository roleRepo;

    public UserMapperJpa(RoleJpaRepository roleRepo) {
        this.roleRepo = roleRepo;
    }

    public UserEntity toEntity(User domain) {
        Objects.requireNonNull(domain, "domain user null");
        var ue = new UserEntity();
        ue.setId(domain.getId()); // jika null, JPA akan generate
        ue.setFullName(domain.getFullName());
        ue.setUsername(domain.getUsername());
        ue.setEmail(domain.getEmail());
        ue.setPasswordHash(domain.getPasswordHash().value()); // asumsi VO PasswordHash
        ue.setPhoneNumber(domain.getPhoneNumber());
        ue.setStatus(domain.getStatus());
        ue.setVersion(domain.getVersion());
        ue.setCreatedAt(domain.getCreatedAt());
        ue.setUpdatedAt(domain.getUpdatedAt());

        var roleEntities = (domain.getRoles() == null ? java.util.Set.<Role>of() : domain.getRoles())
                .stream()
                .map(Role::name)
                .map(name -> roleRepo.findByName(name)
                        .orElseThrow(() -> new IllegalStateException("Role not seeded: " + name)))
                .collect(Collectors.toSet());
        ue.setRoles(roleEntities);

        return ue;
    }

    public User toDomain(UserEntity entity) {
        Objects.requireNonNull(entity, "entity user null");
        var u = new User();
        u.setId(entity.getId());
        u.setFullName(entity.getFullName());
        u.setUsername(entity.getUsername());
        u.setEmail(entity.getEmail());
        u.setPasswordHash(new PasswordHash(entity.getPasswordHash()));
        u.setPhoneNumber(entity.getPhoneNumber());
        u.setStatus(entity.getStatus());
        u.setVersion(entity.getVersion());
        u.setCreatedAt(entity.getCreatedAt());
        u.setUpdatedAt(entity.getUpdatedAt());

        var domainRoles = (entity.getRoles() == null ? java.util.Set.<RoleEntity>of() : entity.getRoles())
                .stream()
                .map(RoleEntity::getName)
                .map(Role::valueOf) // nama harus persis dengan enum
                .collect(Collectors.toSet());
        u.setRoles(domainRoles);

        return u;
    }
}