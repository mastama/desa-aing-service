package com.yolifay.domain.port.read;

import com.yolifay.domain.model.User;

import java.util.Optional;

public interface UserQueryRepositoryPort {
    Optional<User> findByUsernameOrEmail(String usernameOrEmail);
    Optional<User> findProfileById(Long id);
}
