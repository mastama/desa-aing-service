package com.yolifay.domain.model;

import java.time.Instant;
import java.util.Set;

public class User {
    private Long id;
    private String fullName;
    private String username;
    private String email;
    private String passwordHash;
    private String phone;
    private Instant createdAt;
    private Set<String> roles;
}
