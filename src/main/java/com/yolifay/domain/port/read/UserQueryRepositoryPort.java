package com.yolifay.domain.port.read;

import com.yolifay.domain.model.view.UserCredentialsView;
import com.yolifay.domain.model.view.UserProfileView;

import java.util.Optional;

public interface UserQueryRepositoryPort {
    Optional<UserCredentialsView> findCredentialsByUsernameOrEmail(String usernameOrEmail);
    Optional<UserProfileView> findProfileById(Long id);
}
