package com.jpipeline.javafxclient.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpipeline.common.util.ErrorMessage;
import com.jpipeline.common.util.exception.CustomException;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class HttpService {

    private static final ObjectMapper OM = new ObjectMapper();

    private static Logger log = LoggerFactory.getLogger(HttpService.class);

    CookieStore cookieStore = new BasicCookieStore();
    private HttpClient httpClient = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();

    private String host;

    public HttpService(String hostname, Integer port) {
        this.host = hostname + (port == null? "" : ":" + port);
    }

    public HttpService(String host) {
        this.host = host;
    }

    public HttpResponse get(String url) throws IOException {
        HttpResponse response = httpClient.execute(new HttpGet("http://" + host + url));
        if (response.getStatusLine().getStatusCode() == 200) {
            return response;
        } else {
            ErrorMessage errorMessage = OM.readValue(EntityUtils.toString(response.getEntity()), ErrorMessage.class);
            throw new CustomException(errorMessage.getMessage());
        }
    }

    public HttpResponse post(String url, String body) throws IOException {
        HttpPost httpPost = new HttpPost("http://" + host + url);
        StringEntity stringEntity = new StringEntity(body);
        httpPost.setEntity(stringEntity);
        httpPost.addHeader("Content-Type", "application/json");

        HttpResponse response = httpClient.execute(httpPost);
        if (response.getStatusLine().getStatusCode() == 200) {
            return response;
        } else {
            ErrorMessage errorMessage = OM.readValue(EntityUtils.toString(response.getEntity()), ErrorMessage.class);
            throw new CustomException(errorMessage.getMessage());
        }
    }

    public String getCookie(String name) {
        return cookieStore.getCookies().stream().filter(cookie -> cookie.getName().equals(name)).map(Cookie::getValue).findFirst().orElse(null);
    }

}
