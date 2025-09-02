package com.yolifay.infrastructure.adapter.out.persistence;

import com.yolifay.domain.model.User;
import com.yolifay.domain.port.out.UserRepositoryPortOut;
import com.yolifay.infrastructure.adapter.out.entity.UserEntity;
import com.yolifay.infrastructure.adapter.out.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JpaUserRepositoryAdapter implements UserRepositoryPortOut {

    private final UserJpaRepository userJpaRepository;
    private final UserMapperJpa userMapper;

    @Override
    public boolean existsByEmail(String email) {
        return userJpaRepository.existsByEmailIgnoreCase(email);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userJpaRepository.existsByUsernameIgnoreCase(username);
    }

    @Override
    public Optional<User> findByUsernameOrEmail(String uoe) {
        return userJpaRepository.findByUsernameOrEmail(uoe).map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findById(Long id) {
        return userJpaRepository.findById(id).map(userMapper::toDomain);
    }

    @Override
    public User save(User user) {
        UserEntity saved = userJpaRepository.save(userMapper.toEntity(user));
        return userMapper.toDomain(saved);
    }
}
