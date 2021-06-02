package com.jpipeline.manager.http;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import java.io.IOException;

@RestController
@RequestMapping("/proxy")
public class ExecutorProxyController {

    @Autowired
    private HttpClient httpClient;

    @Value("${jpipeline.executor.port}")
    private Integer executorPort;

    @RequestMapping("/**")
    public ResponseEntity proxy(ServerWebExchange exchange, HttpMethod method) throws IOException {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().subPath(2).toString();
        HttpUriRequest req = RequestBuilder
                .create(method.name())
                .setUri("http://localhost:" + executorPort + path)
                .build();


        HttpResponse response = httpClient.execute(req);

        return ResponseEntity
                .status(response.getStatusLine().getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(EntityUtils.toString(response.getEntity()));
    }

}
