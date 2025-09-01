package com.yolifay.domain.port.write;

import com.yolifay.domain.model.User;

public interface UserCommandRepositoryPort {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    User save(User user);
}
