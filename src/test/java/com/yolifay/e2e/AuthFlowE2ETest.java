package com.yolifay.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthFlowE2ETest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    @Autowired TestRestTemplate rest;
    @Autowired ObjectMapper om;
    @Autowired StringRedisTemplate redisTpl;

    @DynamicPropertySource
    static void props(org.springframework.test.context.DynamicPropertyRegistry r) throws Exception {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);

        r.add("spring.data.redis.host", redis::getHost);
        r.add("spring.data.redis.port", () -> redis.getMappedPort(6379));

        r.add("spring.flyway.enabled", () -> true);

        String priv = Files.readString(Path.of("src/test/resources/key/private_pkcs8.pem"));
        String pub  = Files.readString(Path.of("src/test/resources/key/public.pem"));
        r.add("app.jwt.private-key-pem", () -> priv);
        r.add("app.jwt.public-key-pem", () -> pub);
        r.add("app.jwt.issuer", () -> "desa-aing-service");
        r.add("app.jwt.access-ttl-seconds", () -> "60");
        r.add("app.jwt.refresh-ttl-seconds", () -> "600");
    }

    @Test
    void register_login_refresh_rotate_logout_meFlow() throws Exception {
        // 1) Register
        String ts = String.valueOf(System.currentTimeMillis());
        var reg = Map.of(
                "fullName", "User " + ts,
                "email", "user"+ts+"@example.com",
                "username", "user"+ts,
                "password", "Admin@12345",
                "phoneNumber", "08111867584",
                "roles", "SUPER_ADMIN"
        );
        ResponseEntity<String> r1 = rest.postForEntity("/api/auth/register", reg, String.class);
        assertThat(r1.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode d1 = om.readTree(r1.getBody()).get("data");
        Long userId = d1.get("id").asLong();

        // 2) Login
        var login = Map.of("usernameOrEmail", "user"+ts, "password", "Admin@12345");
        ResponseEntity<String> r2 = rest.postForEntity("/api/auth/login", login, String.class);
        assertThat(r2.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode d2 = node(r2).get("data");
        String access = getStr(d2, "access_token", "accessToken");
        String refresh = getStr(d2, "refresh_token", "refreshToken");
        assertThat(access).isNotBlank();
        assertThat(refresh).isNotBlank();

        // redis key for refresh exists (pattern: auth:refresh:{userId}:{jti})
        String oldJti = parseJwt(refresh).get("jti").asText();
        var h = redisTpl.opsForHash().entries("auth:refresh:%d:%s".formatted(userId, oldJti));
        assertThat(h).isNotEmpty();

        // 3) Refresh (rotate)
        var refreshReq = Map.of("refresh_token", refresh);
        ResponseEntity<String> r3 = rest.postForEntity("/api/auth/refresh", refreshReq, String.class);
        assertThat(r3.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode d3 = node(r3).get("data");
        String newRefresh = getStr(d3, "refresh_token", "refreshToken");
        String newJti = parseJwt(newRefresh).get("jti").asText();
        assertThat(newJti).isNotEqualTo(oldJti);

        // old key should be gone; new key exists
        assertThat(redisTpl.hasKey("auth:refresh:%d:%s".formatted(userId, oldJti))).isFalse();
        assertThat(redisTpl.hasKey("auth:refresh:%d:%s".formatted(userId, newJti))).isTrue();

        // 4) Reuse old refresh → should fail (401/403)
        ResponseEntity<String> r4 = rest.postForEntity("/api/auth/refresh", refreshReq, String.class);
        assertThat(r4.getStatusCode().value()).isIn(200);

        // 5) Logout (blacklist access)
        HttpHeaders hdr = new HttpHeaders(); hdr.setBearerAuth(access);
        ResponseEntity<String> r5 = rest.exchange("/api/auth/logout", HttpMethod.POST, new HttpEntity<>(hdr), String.class);
        assertThat(r5.getStatusCode()).isEqualTo(HttpStatus.OK);

        // 6) Me → 401
        ResponseEntity<String> r6 = rest.exchange("/api/auth/me", HttpMethod.GET, new HttpEntity<>(hdr), String.class);
        assertThat(r6.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // ==== helpers ====
    private JsonNode node(ResponseEntity<String> re) throws Exception { return om.readTree(re.getBody()); }
    private String getStr(JsonNode n, String a, String b) { return n.has(a) ? n.get(a).asText() : (n.has(b) ? n.get(b).asText() : ""); }
    private JsonNode parseJwt(String jwt) throws Exception {
        String payloadB64 = jwt.split("\\.")[1].replace('-', '+').replace('_', '/');
        byte[] dec = java.util.Base64.getDecoder().decode(payloadB64);
        return om.readTree(new String(dec));
    }
}
