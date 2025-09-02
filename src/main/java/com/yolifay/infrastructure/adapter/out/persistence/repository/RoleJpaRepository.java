package com.yolifay.infrastructure.adapter.out.persistence.repository;

import com.yolifay.infrastructure.adapter.out.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleJpaRepository extends JpaRepository<RoleEntity, Long> {
    Optional<RoleEntity> findByName(String name);
    boolean existsByName(String name);
}
