package com.yolifay.domain.model;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private Long id;
    private String fullName;
    private String username;
    private String email;
    private String phoneNumber;
    private String passwordHash;
    private Instant createdAt;
    private Role roles;

    public static User newUser(String fullName, String username, String email, String phoneNumber,
                               String passwordHash, Instant now, Role role) {
        return new User(null, fullName, username.toLowerCase(), email.toLowerCase(), phoneNumber,
                passwordHash, now, role == null ? Role.USER : role);
    }
}
