package com.yolifay.infrastructure.adapter.in.web;

import com.yolifay.application.command.*;
import com.yolifay.application.handler.*;
import com.yolifay.common.CommonConstants;
import com.yolifay.common.ResponseService;
import com.yolifay.common.ResponseUtil;
import com.yolifay.infrastructure.adapter.in.web.dto.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    private final RegisterUserHandler registerHandler;
    private final LoginUserHandler loginHandler;
    private final RefreshTokenHandler refreshHandler;
    private final LogoutHandler logoutHandler;
    private final GetCurrentUserHandler meHandler;

    @Value("${service.id}")
    private String serviceId;

    @PostMapping("/register")
    public ResponseEntity<ResponseService> register(@RequestBody @Valid RegisterRequest req){
        log.info("Incoming register: {}", req.username());
        var result = registerHandler.handleRegister(new RegisterUserCommand(
                req.fullName(), req.username(), req.email(), req.password(), req.phoneNumber(), req.roles()
        ));

        log.info("Outgoing register: {}", req.email());
        return ResponseEntity.ok(ResponseUtil.setResponse(
                HttpStatus.OK.value(), serviceId, CommonConstants.RESPONSE.APPROVED,
                result
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseService> login(@RequestBody @Valid LoginRequest req,
                                                 @RequestHeader(value = "X-Forwarded-For", required = false) String ip,
                                                 @RequestHeader(value = "User-Agent", required = false) String ua) throws Exception {
        var pair = loginHandler.handleLogin(new LoginUserCommand(
                req.usernameOrEmail(), req.password(),
                ip != null ? ip : "", ua != null ? ua : ""
        ));
        return ResponseEntity.ok(ResponseUtil.setResponse(
                HttpStatus.OK.value(), serviceId, CommonConstants.RESPONSE.APPROVED,
                new TokenResponse(pair.access_token(), pair.token_type(), pair.expires_in(),
                        pair.scope(), pair.jti(), pair.refresh_token())
        ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ResponseService> refresh(@RequestBody @Valid RefreshRequest req, HttpServletRequest http) {
        log.info("Incoming refresh token");
        var ip = http.getRemoteAddr();
        var ua = Optional.ofNullable(http.getHeader("User-Agent")).orElse("");
        var result = refreshHandler.handleRefreshToken(new RefreshTokenCommand(req.refreshToken(), ip, ua));

        log.info("Outgoing refresh token");
        return ResponseEntity.ok(ResponseUtil.setResponse(
                HttpStatus.OK.value(), serviceId, CommonConstants.RESPONSE.APPROVED, result
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<ResponseService> logout(@RequestHeader("Authorization") String authHeader){
        log.info("Incoming logout token");
        logoutHandler.handleLogout(new LogoutCommand(authHeader));

        log.info("Outgoing logout token");
        return ResponseEntity.ok(ResponseUtil.setResponse(
                HttpStatus.OK.value(), serviceId, CommonConstants.RESPONSE.APPROVED, null
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<ResponseService> me() throws Exception {
        log.info("Incoming me");
        GetCurrentUserCommand dto = meHandler.handleGetCurrentUser();

        log.info("Outgoing me");
        return ResponseEntity.ok(ResponseUtil.setResponse(
                HttpStatus.OK.value(), serviceId, CommonConstants.RESPONSE.APPROVED, dto
        ));
    }
}
