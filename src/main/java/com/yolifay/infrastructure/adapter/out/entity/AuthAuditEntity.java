package com.yolifay.infrastructure.adapter.out.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name="auth_audit")
public class AuthAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;
    Long userId;
    String event;
    boolean success;
    @Column(columnDefinition = "text")
    String reason;
    String ip;
    @Column(columnDefinition = "text")
    String userAgent;
    @Column(columnDefinition = "timestamptz")
    Instant createdAt = Instant.now();
}
