package com.yolifay.infrastructure.adapter.out.audit;

import com.yolifay.domain.port.out.AuditLogPortOut;
import com.yolifay.domain.port.out.JwtProviderPortOut;
import com.yolifay.infrastructure.adapter.in.web.dto.AuditEvent;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.*;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private final AuditLogPortOut audit;
    private final JwtProviderPortOut jwt;

    @Around("@annotation(audited)")
    public Object around(ProceedingJoinPoint pjp, Audited audited) throws Throwable {
        HttpServletRequest req = currentRequest();
        String ip = ipFrom(req).orElseGet(() -> extractIp(pjp.getArgs()).orElse(""));
        String ua = uaFrom(req).orElseGet(() -> extractUa(pjp.getArgs()).orElse(""));
        String resource = (req != null) ? req.getRequestURI() : pjp.getSignature().toShortString();
        String method = (req != null) ? req.getMethod() : "N/A";

        // userId cadangan (dari argumen—refresh/logout)
        Long errUserId = extractUserIdFromArgs(audited.action(), pjp.getArgs()).orElse(null);

        String eventName = resolveEventName(audited);

        try {
            Object result = pjp.proceed();

            Long userId = extractUserIdOnSuccess(audited.action(), result, pjp.getArgs()).orElse(errUserId);
            int status = resolveStatusFromResult(result).orElse(200);

            audit.writeAudit(new AuditEvent(
                    userId, eventName + "_SUCCESS", resource, method, ua, ip, null, status, Instant.now()
            ));
            return result;

        } catch (Throwable ex) {
            int status = mapExceptionToStatus(ex);
            String failEvent = mapFailureEvent(audited.action(), ex);

            audit.writeAudit(new AuditEvent(
                    errUserId, failEvent, resource, method, ua, ip, safe(ex.getMessage()), status, Instant.now()
            ));
            throw ex;
        }
    }

    // ===== Helpers =====
    private HttpServletRequest currentRequest() {
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        if (ra instanceof ServletRequestAttributes sra) return sra.getRequest();
        return null;
    }
    private Optional<String> ipFrom(HttpServletRequest req) {
        if (req == null) return Optional.empty();
        String xff = req.getHeader("X-Forwarded-For");
        String ip = (xff != null && !xff.isBlank()) ? xff.split(",")[0].trim() : req.getRemoteAddr();
        return Optional.ofNullable(ip);
    }
    private Optional<String> uaFrom(HttpServletRequest req) {
        return (req == null) ? Optional.empty() : Optional.ofNullable(req.getHeader("User-Agent"));
    }

    private Optional<String> extractIp(Object[] args) { return findStringMethod(args, "ip"); }
    private Optional<String> extractUa(Object[] args) { return findStringMethod(args, "userAgent"); }
    private Optional<String> findStringMethod(Object[] args, String name) {
        for (Object a : args) {
            if (a == null) continue;
            try {
                Method m = a.getClass().getMethod(name);
                Object v = m.invoke(a);
                if (v instanceof String s) return Optional.of(s);
            } catch (Exception e) {
                log.debug("audit: cannot read {} from {}: {}", name, a.getClass().getSimpleName(), e.getMessage());
            }
        }
        return Optional.empty();
    }

    private Optional<Long> extractUserIdFromArgs(AuditAction action, Object[] args) {
        try {
            switch (action) {
                case REFRESH -> {
                    for (Object a : args) {
                        if (a == null) continue;
                        try {
                            Method m = a.getClass().getMethod("refreshToken");
                            Object tok = m.invoke(a);
                            if (tok instanceof String s && !s.isBlank()) {
                                Map<String,Object> c = jwt.validateAndGetClaims(s);
                                return Optional.of(Long.valueOf(String.valueOf(c.get("sub"))));
                            }
                        } catch (NoSuchMethodException ignored) {
                            // skip
                        }
                    }
                }
                case LOGOUT -> {
                    for (Object a : args) {
                        if (a instanceof String s && s.toLowerCase(Locale.ROOT).startsWith("bearer ")) {
                            String token = s.substring(7).trim();
                            Map<String,Object> c = jwt.validateAndGetClaims(token);
                            return Optional.of(Long.valueOf(String.valueOf(c.get("sub"))));
                        }
                    }
                }
                default -> {}
            }
        } catch (Exception e) {
            log.debug("audit: extract userId from args failed: {}", e.getMessage());
        }
        return Optional.empty();
    }

    private Optional<Long> extractUserIdOnSuccess(AuditAction action, Object result, Object[] args) {
        if (result == null) return Optional.empty();
        try {
            switch (action) {
                case LOGIN, REFRESH -> {
                    String access = readString(result, "accessToken")
                            .or(() -> readString(result, "getAccessToken"))
                            .orElse(null);
                    if (access != null && !access.isBlank()) {
                        Map<String,Object> c = jwt.validateAndGetClaims(access);
                        return Optional.of(Long.valueOf(String.valueOf(c.get("sub"))));
                    }
                }
                case REGISTER -> {
                    Long id = readLong(result, "id").or(() -> readLong(result, "getId")).orElse(null);
                    if (id != null) return Optional.of(id);
                }
                default -> {}
            }
        } catch (Exception e) {
            log.debug("audit: extract userId from result failed: {}", e.getMessage());
        }
        return Optional.empty();
    }

    private Optional<String> readString(Object bean, String method) {
        try {
            Method m = bean.getClass().getMethod(method);
            Object v = m.invoke(bean);
            return (v instanceof String s) ? Optional.of(s) : Optional.empty();
        } catch (Exception ignored) { return Optional.empty(); }
    }
    private Optional<Long> readLong(Object bean, String method) {
        try {
            Method m = bean.getClass().getMethod(method);
            Object v = m.invoke(bean);
            if (v instanceof Long l) return Optional.of(l);
            if (v instanceof Number n) return Optional.of(n.longValue());
            return Optional.empty();
        } catch (Exception e) { return Optional.empty(); }
    }

    private String resolveEventName(Audited audited) {
        if (!audited.value().isBlank()) return audited.value();
        return audited.action() == AuditAction.NONE ? "UNSPECIFIED" : "AUTH_" + audited.action().name();
    }

    private int mapExceptionToStatus(Throwable ex) {
        String n = ex.getClass().getSimpleName();
        return switch (n) {
            case "TooManyRequestsException" -> 429;
            case "BadCredentialsException",
                 "DataNotFoundException"    -> 401;
            case "AccessDeniedException",
                 "UserNotActiveException"   -> 403;
            case "DuplicateResourceException",
                 "DataExistException"       -> 409;
            case "MethodArgumentNotValidException",
                 "ConstraintViolationException",
                 "BadRequestException"      -> 400;
            default -> 500;
        };
    }

    private String mapFailureEvent(AuditAction action, Throwable ex) {
        String name = ex.getClass().getSimpleName();
        return switch (action) {
            case LOGIN    -> switch (name) {
                case "TooManyRequestsException" -> "AUTH_LOGIN_RATE_LIMIT";
                case "UserNotActiveException"   -> "AUTH_LOGIN_BLOCKED";
                default -> "AUTH_LOGIN_FAIL";
            };
            case REFRESH  -> switch (name) {
                case "RefreshReuseException"    -> "AUTH_REFRESH_REUSE";
                case "RefreshInvalidException"  -> "AUTH_REFRESH_INVALID";
                default -> "AUTH_REFRESH_FAIL";
            };
            case LOGOUT   -> "AUTH_LOGOUT_FAIL";
            case REGISTER -> switch (name) {
                case "DataExistException",
                     "DuplicateResourceException" -> "AUTH_REGISTER_DUPLICATE";
                default -> "AUTH_REGISTER_FAIL";
            };
            case NONE -> "AUDIT_FAIL";
        };
    }

    private Optional<Integer> resolveStatusFromResult(Object result) {
        if (result instanceof ResponseEntity<?> re) return Optional.of(re.getStatusCode().value());
        return Optional.empty();
    }

    private String safe(String msg) {
        if (msg == null) return null;
        return msg.length() <= 250 ? msg : msg.substring(0, 250);
    }
}