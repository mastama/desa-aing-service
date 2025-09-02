package com.yolifay.infrastructure.adapter.out.persistence.repository;

import com.yolifay.infrastructure.adapter.out.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByUsernameIgnoreCase(String username);

    @Query("""
        select u from UserEntity u
        left join fetch u.roles r
        where lower(u.username) = lower(:uoe) or lower(u.email) = lower(:uoe)
        """)
    Optional<UserEntity> findByUsernameOrEmail(String uoe);
}
