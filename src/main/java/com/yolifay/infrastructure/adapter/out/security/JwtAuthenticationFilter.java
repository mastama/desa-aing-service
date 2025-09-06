package com.yolifay.infrastructure.adapter.out.security;

import com.yolifay.domain.port.out.JwtProviderPortOut;
import com.yolifay.domain.port.out.TokenStorePortOut;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProviderPortOut jwt;
    private final TokenStorePortOut tokens;

    public JwtAuthenticationFilter(JwtProviderPortOut jwt, TokenStorePortOut tokens) {
        this.jwt = jwt;
        this.tokens = tokens;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        // Jangan overwrite auth yg sudah ada (misal Basic di actuator)
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (auth != null && auth.startsWith("Bearer ")) {
                String token = auth.substring(7);
                try {
                    Map<String, Object> claims = jwt.validateAndGetClaims(token);
                    String jti = (String) claims.get("jti");
                    if (jti != null && !tokens.isAccessBlacklisted(jti)) {
                        String userId = String.valueOf(claims.get("sub"));

                        // Ambil roles & tambahkan prefix ROLE_
                        List<?> raw = (List<?>) claims.getOrDefault("roles", List.of());
                        List<SimpleGrantedAuthority> authorities = raw.stream()
                                .map(Object::toString)
                                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                                .map(SimpleGrantedAuthority::new)
                                .toList();

                        var authentication = new UsernamePasswordAuthenticationToken(userId, token, authorities);
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                } catch (Exception e) {
                    // token invalid/expired/parse error: biarkan anonymous
                    SecurityContextHolder.clearContext();
                }
            }
        }
        chain.doFilter(request, response);
    }
}
