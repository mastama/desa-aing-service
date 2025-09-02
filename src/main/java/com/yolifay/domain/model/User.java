package com.yolifay.domain.model;

import com.yolifay.domain.valueobject.PasswordHash;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {
    private Long id;
    private String fullName;
    private String username;
    private String email;
    private PasswordHash passwordHash;
    private String phoneNumber;
    private String status; // e.g., ACTIVE, INACTIVE
    private Instant createdAt;
    private Instant updatedAt;
    private long version;
    private Set<Role> roles = new HashSet<>();

    public static User newRegistered(String fullName, String username, String email, String passwordHash,
                                     String phoneNumber, Instant now, Role role) {
        User u = new User();
        u.fullName = fullName;
        u.username = username.toLowerCase();
        u.email = email.toLowerCase();
        u.passwordHash = new PasswordHash(passwordHash);
        u.phoneNumber = phoneNumber;
        u.status = "ACTIVE";
        u.roles.add(Objects.requireNonNullElse(role, Role.USER)); // default USER role
        u.createdAt = now;
        u.updatedAt = now;
        u.version = 0;
        return u;
    }

}
