package com.yolifay.infrastructure.adapter.out.persistence.repository;

import com.yolifay.infrastructure.adapter.out.entity.AuthAuditEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthAuditJpaRepository extends JpaRepository<AuthAuditEntity, Long> {
}
