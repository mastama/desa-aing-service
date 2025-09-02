package com.yolifay.application.handler;

import com.yolifay.application.command.GetCurrentUserCommand;
import com.yolifay.domain.model.User;
import com.yolifay.domain.port.out.UserRepositoryPortOut;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetCurrentUserHandler {

    private final UserRepositoryPortOut userRepo;

    @Transactional(readOnly = true)
    public GetCurrentUserCommand handleGetCurrentUser() {
        Long userId = resolveCurrentUserId();
        User u = userRepo.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));

        Set<String> roles = u.getRoles().stream().map(Enum::name).collect(Collectors.toSet());

        return new GetCurrentUserCommand(
                u.getId(),
                u.getFullName(),
                u.getUsername(),
                u.getEmail(),
                u.getPhoneNumber(),
                u.getStatus(),
                u.getCreatedAt(),
                u.getUpdatedAt(),
                u.getVersion(),
                roles
        );
    }

    private Long resolveCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Unauthenticated");
        }
        // Asumsi JwtAuthenticationFilter mengisi Authentication.getName() = sub (userId string)
        try {
            return Long.valueOf(auth.getName());
        } catch (NumberFormatException e) {
            // fallback lain bisa dibuat jika principal berupa map/claims
            throw new AccessDeniedException("Invalid subject in security context");
        }
    }
}
