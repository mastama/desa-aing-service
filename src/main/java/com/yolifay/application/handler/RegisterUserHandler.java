package com.yolifay.application.handler;

import com.yolifay.application.command.RegisterUserCommand;
import com.yolifay.common.CommonConstants;
import com.yolifay.common.ResponseService;
import com.yolifay.common.ResponseUtil;
import com.yolifay.domain.model.User;
import com.yolifay.domain.port.read.UserQueryRepositoryPort;
import com.yolifay.domain.port.write.UserCommandRepositoryPort;
import com.yolifay.domain.service.Clock;
import com.yolifay.domain.service.PasswordHasherPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegisterUserHandler {

    @Value("${service.id}")
    private String serviceId;

    private final UserCommandRepositoryPort userCmdRepo; //write side
    private final UserQueryRepositoryPort userQueryRepo; //read side
    private final PasswordHasherPort hasher;
    private final Clock clock;

    public ResponseService executeRegister(RegisterUserCommand cmd) {
        final String username = cmd.username().toLowerCase();
        final String email = cmd.email().toLowerCase();
        log.info("[REGISTER] start username={} email={}", username, email);

        // Cek eksistensi via read port (CQRS-friendly)
        if (userQueryRepo.findByUsernameOrEmail(username).isPresent()) {
            log.warn("[REGISTER] username exists: {}", cmd.username());
            throw new IllegalArgumentException(CommonConstants.RESPONSE.DATA_EXISTS.getDescription()+": username");
        }
        if (userQueryRepo.findByUsernameOrEmail(email).isPresent()) {
            log.warn("[REGISTER] email exists: {}", cmd.email());
            throw new IllegalArgumentException(CommonConstants.RESPONSE.DATA_EXISTS.getDescription()+": email");
        }

        // Buat aggregate/domain entity
        var user = User.newUser(
                cmd.fullName(),
                cmd.username().toLowerCase(),
                cmd.email().toLowerCase(),
                cmd.phoneNumber(),
                hasher.hash(cmd.password()),
                clock.now(),
                cmd.roleOrDefault() // default role dari command
                );
        // Persist via write-side
        User savedUser = userCmdRepo.save(user);

        log.info("[REGISTER] success userId={} emaik={}", savedUser.getId(), email);

        // Data response (tanpa passwordHash)
        Map<String, Object> data = Map.of(
                "id", savedUser.getId(),
                "fullName", savedUser.getFullName(),
                "username", savedUser.getUsername(),
                "email", savedUser.getEmail(),
                "phoneNumber", savedUser.getPhoneNumber(),
                "createdAt", savedUser.getCreatedAt(),
                "role", savedUser.getRoles()
        );
        log.info("[REGISTER] End userId={} email={}", savedUser.getId(), email);
        return ResponseUtil.setResponse(
                HttpStatus.OK.value(), serviceId, CommonConstants.RESPONSE.APPROVED, data
        );
    }
}
