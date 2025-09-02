package com.yolifay.application.handler;

import com.yolifay.application.command.RegisterUserCommand;
import com.yolifay.domain.model.User;
import com.yolifay.domain.port.out.ClockPortOut;
import com.yolifay.domain.port.out.PasswordHasherPortOut;
import com.yolifay.domain.port.out.UserRepositoryPortOut;
import com.yolifay.infrastructure.adapter.in.web.dto.RegisterResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegisterUserHandler {
    private final UserRepositoryPortOut userRepoOut;
    private final PasswordHasherPortOut hasher;
    private final ClockPortOut clock;

    @Transactional
    public RegisterResponse handleRegister(RegisterUserCommand cmd) {
        final String username = cmd.username().toLowerCase();
        final String email    = cmd.email().toLowerCase();

        if (userRepoOut.existsByUsername(username)) throw new IllegalArgumentException("Username already exists");
        if (userRepoOut.existsByEmail(email))       throw new IllegalArgumentException("Email already exists");

        // ⚠️ pastikan urutan argumen sesuai definisi User.newRegistered(...)
        // Dari gejala output Anda, sepertinya urutan yang benar adalah (fullName, username, email, ...)
        var user = User.newRegistered(
                cmd.fullName(),
                cmd.username(),
                cmd.email(),
                hasher.hash(cmd.password()),
                cmd.phoneNumber(),
                clock.now(),
                cmd.roleOrDefault()
        );
        user = userRepoOut.save(user);

        // mapping domain -> DTO (tanpa passwordHash)
        var roles = user.getRoles().stream().map(Enum::name).collect(java.util.stream.Collectors.toSet());
        return new RegisterResponse(
                user.getId(),
                user.getFullName(),
                user.getUsername(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getVersion(),
                roles
        );
    }
}
