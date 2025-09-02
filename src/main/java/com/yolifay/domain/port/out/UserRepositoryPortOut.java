package com.yolifay.domain.port.out;

import com.yolifay.domain.model.User;

import java.util.Optional;

public interface UserRepositoryPortOut {
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    Optional<User> findByUsernameOrEmail(String usernameOrEmail);
    Optional<User> findById(Long id);
    User save(User user);
}
