package com.jpipeline.javafxclient.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpService {

    private static Logger log = LoggerFactory.getLogger(HttpService.class);

    private HttpClient httpClient = HttpClient.newHttpClient();

    private String host;
    private Integer port;

    public HttpService(String host, Integer port) {
        this.host = host;
        this.port = port;
    }

    public String get(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://" + host + ":" + port + url))
                .build();

        HttpResponse<String> send = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return send.body();
    }

    public String post(String url, String body) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .uri(URI.create("http://" + host + ":" + port + url))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> send = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return send.body();
    }

}
