package com.jpipeline.manager;

import com.auth0.jwt.JWT;
import com.jpipeline.security.AuthFilter;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.UUID;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;

@Service
public class AuthService {

    @Autowired
    private Environment env;

    @Autowired
    private AuthFilter authFilter;

    @Getter @Setter
    private static String jwtSecret = UUID.randomUUID().toString();

    @PostConstruct
    private void init() {
        if (Arrays.asList(env.getActiveProfiles()).contains("dev")) {
            jwtSecret = "TEST_TOKEN";
        }

        authFilter.setJwtSecret(jwtSecret);
    }

    public String createJWT(String username) {
        return JWT.create()
                .withSubject(username)
                .withClaim("token", jwtSecret)
                .sign(HMAC512(jwtSecret.getBytes()));
    }

}
