package com.yolifay.infrastructure.adapter.out.persistence.repository;

import com.yolifay.infrastructure.adapter.out.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByUsernameIgnoreCase(String username);

    @Query("""
        select u from UserEntity u
        left join fetch u.roles r
        where lower(u.username) = lower(:uoe) or lower(u.email) = lower(:uoe)
        """)
    Optional<UserEntity> findByUsernameOrEmail(String uoe);

    @Query("""
  select u from UserEntity u
  where (:qp is null
     or u.username like :qp
     or u.email    like :qp
     or lower(u.fullName) like :qp)
  """)
    Page<UserEntity> searchLike(@Param("qp") String qp, Pageable pageable);
}
