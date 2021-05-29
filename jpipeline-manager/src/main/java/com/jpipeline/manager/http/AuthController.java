package com.jpipeline.manager.http;

import com.auth0.jwt.JWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import java.util.Date;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Value("${jpipeline.jwt-secret}")
    private String JWT_SECRET;

    @Value("${jpipeline.jwt-cookie-name}")
    private String JWT_COOKIE;

    @GetMapping("/login")
    private boolean login(@RequestParam String username, @RequestParam String password, ServerWebExchange exchange) {
        String token = JWT.create()
                .withSubject(username)
                //.withExpiresAt(new Date(System.currentTimeMillis() + 100000))
                .sign(HMAC512(JWT_SECRET.getBytes()));

        ServerHttpResponse response = exchange.getResponse();
        response.addCookie(ResponseCookie
                .from(JWT_COOKIE, token)
                .path("/")
                .build());

        return true;
    }

}
