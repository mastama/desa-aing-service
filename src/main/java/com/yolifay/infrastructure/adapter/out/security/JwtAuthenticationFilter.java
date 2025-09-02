package com.yolifay.infrastructure.adapter.out.security;

import com.yolifay.domain.port.out.JwtProviderPortOut;
import com.yolifay.domain.port.out.TokenStorePortOut;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class JwtAuthenticationFilter extends GenericFilter {

    private final JwtProviderPortOut jwt;
    private final TokenStorePortOut tokens;

    public JwtAuthenticationFilter(JwtProviderPortOut jwt, TokenStorePortOut tokens) {
        this.jwt = jwt;
        this.tokens = tokens;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        var request = (HttpServletRequest) req;
        var auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            var token = auth.substring(7);
            try {
                Map<String,Object> claims = jwt.validateAndGetClaims(token);
                var jti = (String) claims.get("jti");
                if (!tokens.isAccessBlacklisted(jti)) {
                    var userId = String.valueOf(claims.get("sub"));
                    var roles = (List<String>) claims.getOrDefault("roles", List.of());
                    var authorities = roles.stream().map(SimpleGrantedAuthority::new).toList();

                    var authentication = new AbstractAuthenticationToken(authorities) {
                        @Override public Object getCredentials() { return token; }
                        @Override public Object getPrincipal() { return userId; }
                    };
                    authentication.setAuthenticated(true);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception ignored) { /* token invalid/expired -> anonymous */ }
        }
        chain.doFilter(req, res);
    }
}
