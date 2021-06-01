package com.jpipeline.manager.http;

import com.jpipeline.manager.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Value("#{'${jpipeline.users}'.split('\\s')}")
    private List<String> users;

    @Autowired
    private AuthService authService;

    @Value("${jpipeline.jwt-cookie-name}")
    private String JWT_COOKIE;

    @GetMapping("/login")
    private boolean login(@RequestParam String username, @RequestParam String password, ServerWebExchange exchange) {
        String token = authService.createJWT(username);

        if (!users.contains(username + ":" + password)) {
            throw new RuntimeException("Incorrect login or password");
        }

        ServerHttpResponse response = exchange.getResponse();
        response.addCookie(ResponseCookie
                .from(JWT_COOKIE, token)
                .path("/")
                .build());

        return true;
    }

}
