package com.yolifay.infrastructure.adapter.out.persistence;

import com.yolifay.application.shared.paging.PagedResult;
import com.yolifay.domain.model.User;
import com.yolifay.domain.port.out.UserRepositoryPortOut;
import com.yolifay.infrastructure.adapter.out.entity.UserEntity;
import com.yolifay.infrastructure.adapter.out.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Locale;
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

    @Override
    public PagedResult<User> search(String q, int page, int size) {
        String qp = (q == null || q.isBlank()) ? null
                : "%" + q.toLowerCase(Locale.ROOT) + "%";
        Page<UserEntity> p = userJpaRepository.searchLike(qp, PageRequest.of(page, size));
        var content = p.getContent().stream().map(userMapper::toDomain).toList();
        return new PagedResult<>(content, p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages());
    }
}
