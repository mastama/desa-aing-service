package com.yolifay.infrastructure.adapter.out.security;

import com.yolifay.domain.port.out.ClockPortOut;
import com.yolifay.domain.port.out.IdGeneratorPortOut;
import com.yolifay.domain.port.out.JwtProviderPortOut;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
public class JwtProviderAdapter implements JwtProviderPortOut {

    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private final ClockPortOut clock;
    private final IdGeneratorPortOut idGen;
    private final long accessTtlSeconds;
    private final long refreshTtlSeconds;
    private final String issuer;

    public JwtProviderAdapter(
            JwtProps props,
            ClockPortOut clock,
            IdGeneratorPortOut idGen,
            ResourceLoader loader
    ) throws Exception {
        // baca isi PEM dari path (classpath/file:) atau fallback ke field *Pem
        String privatePem = resolvePem(props.getPrivateKeyPath(), props.getPrivateKeyPem(), loader);
        String publicPem  = resolvePem(props.getPublicKeyPath(),  props.getPublicKeyPem(),  loader);

        this.privateKey = loadPrivate(privatePem);
        this.publicKey  = loadPublic(publicPem);
        this.accessTtlSeconds = props.getAccessTtlSeconds();
        this.refreshTtlSeconds = props.getRefreshTtlSeconds();
        this.issuer = props.getIssuer();
        this.clock = clock;
        this.idGen = idGen;
    }

    @Override
    public IssuedToken issueAccess(Long userId, List<String> roles, long version) {
        var now = clock.now();
        var exp = now.plusSeconds(accessTtlSeconds);
        var jti = idGen.newJti();
        var token = Jwts.builder()
                .setIssuer(issuer)
                .setSubject(String.valueOf(userId))
                .setId(jti)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .claim("roles", roles)
                .claim("ver", version)
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
        return new IssuedToken(token, jti, exp);
    }

    @Override
    public IssuedToken issueRefresh(Long userId, long version) {
        var now = clock.now();
        var exp = now.plusSeconds(refreshTtlSeconds);
        var jti = idGen.newJti();
        var token = Jwts.builder()
                .setIssuer(issuer)
                .setSubject(String.valueOf(userId))
                .setId(jti)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .claim("ver", version)
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
        return new IssuedToken(token, jti, exp);
    }

    @Override
    public Map<String, Object> validateAndGetClaims(String token) {
        var parser = Jwts.parserBuilder().setSigningKey(publicKey).build();
        var c = parser.parseClaimsJws(token).getBody();
        return Map.of(
                "sub", c.getSubject(),
                "jti", c.getId(),
                "iat", c.getIssuedAt().toInstant(),
                "exp", c.getExpiration().toInstant(),
                "roles", c.get("roles"),
                "ver", c.get("ver")
        );
    }

    // ==== helpers ====

    private static String resolvePem(String path, String pemFallback, ResourceLoader loader) throws Exception {
        if (path != null && !path.isBlank()) {
            Resource res = loader.getResource(path); // mendukung classpath:, file:, http:
            try (InputStream is = res.getInputStream()) {
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
        }
        if (pemFallback != null && !pemFallback.isBlank()) return pemFallback;
        throw new IllegalArgumentException("JWT key is not configured (path or pem is required).");
    }

    private static PrivateKey loadPrivate(String pem) throws Exception {
        if (pem.contains("BEGIN RSA PRIVATE KEY"))
            throw new IllegalArgumentException("PKCS#1 terdeteksi. Konversi ke PKCS#8 (BEGIN PRIVATE KEY).");
        byte[] der = extractDer(pem, "PRIVATE KEY");
        return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(der));
    }

    private static PublicKey loadPublic(String pem) throws Exception {
        byte[] der = extractDer(pem, "PUBLIC KEY");
        return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(der));
    }

    private static byte[] extractDer(String pem, String type) {
        String start = "-----BEGIN " + type + "-----";
        String end   = "-----END " + type + "-----";
        int s = pem.indexOf(start), e = pem.indexOf(end);
        if (s < 0 || e < 0) throw new IllegalArgumentException("PEM header " + type + " tidak ditemukan.");
        String base64 = pem.substring(s + start.length(), e);
        base64 = base64.replace("\r","").replace("\n","").replace(" ","").trim();
        return Base64.getMimeDecoder().decode(base64);
    }
}