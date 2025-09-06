package com.yolifay.infrastructure.adapter.out.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name="audit_event")
public class AuthAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    Long userId;

    @Column(nullable=false)
    String event;

    String resource;
    String method;

    @Column(columnDefinition = "text")
    String userAgent;

    String ip;

    @Column(columnDefinition = "text")
    String reason;

    int status;

    @Column(columnDefinition = "timestamptz", nullable = false)
    Instant createdAt = Instant.now();
}
