package com.jpipeline.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Component
public class AuthFilter implements WebFilter {

    private static final List<String> excludedUrls = Arrays.asList("/api/auth/login", "/api/manager/checkIsAlive");

    @Value("${jpipeline.jwt-cookie-name}")
    private String JWT_COOKIE;

    @Setter
    private String jwtSecret;

    public AuthFilter() {}

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        if (!excludedUrls.contains(request.getPath().toString())) {
            if (jwtSecret == null)
                throw new RuntimeException("Something went wrong (no jwtSecret specified)");

            HttpCookie jwt = request.getCookies().getFirst(JWT_COOKIE);

            if (jwt == null) {
                throw new RuntimeException("Cookie " + JWT_COOKIE + " not found");
            }

            DecodedJWT verifiedToken = JWT.require(Algorithm.HMAC512(jwtSecret.getBytes()))
                    .build()
                    .verify(jwt.getValue());

            if (!verifiedToken.getClaim("token").asString().equals(jwtSecret))
                throw new RuntimeException("Auth failed");
        }

        return chain.filter(exchange);
    }
}
