package com.jpipeline.manager;

import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ManagerConfig {

    @Value("${jpipeline.jwt-cookie-name}")
    private String JWT_COOKIE;

    @Bean
    public HttpClient httpClient(AuthService authService) {
        String jwt = authService.createJWT("executor");

        RequestConfig globalConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.BEST_MATCH).build();
        CookieStore cookieStore = new BasicCookieStore();
        HttpClientContext context = HttpClientContext.create();
        context.setCookieStore(cookieStore);

        BasicClientCookie authCookie = new BasicClientCookie(JWT_COOKIE, jwt);
        authCookie.setDomain("localhost");
        authCookie.setPath("/");

        cookieStore.addCookie(authCookie);

        return HttpClients.custom()
                .setDefaultRequestConfig(globalConfig)
                .setDefaultCookieStore(cookieStore)
                .build();
    }

}
