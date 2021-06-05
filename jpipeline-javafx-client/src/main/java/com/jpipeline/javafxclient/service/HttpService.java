package com.jpipeline.javafxclient.service;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class HttpService {

    private static Logger log = LoggerFactory.getLogger(HttpService.class);

    private HttpClient httpClient = HttpClientBuilder.create().setDefaultCookieStore(new BasicCookieStore()).build();

    private String host;

    public HttpService(String hostname, Integer port) {
        this.host = hostname + (port == null? "" : ":" + port);
    }

    public HttpService(String host) {
        this.host = host;
    }

    public HttpResponse get(String url) throws IOException {
        HttpResponse execute = httpClient.execute(new HttpGet("http://" + host + url));
        return execute;
    }

    public HttpResponse post(String url, String body) throws IOException {
        HttpPost httpPost = new HttpPost("http://" + host + url);
        StringEntity stringEntity = new StringEntity(body);
        httpPost.setEntity(stringEntity);
        httpPost.addHeader("Content-Type", "application/json");
        return httpClient.execute(httpPost);
    }

}
