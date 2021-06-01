package com.jpipeline.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpipeline.common.util.ErrorMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


@Configuration
public class ExceptionHandler implements ErrorWebExceptionHandler {

    private static Logger logger = LoggerFactory.getLogger(ExceptionHandler.class);

    private static ObjectMapper OM = new ObjectMapper();

    @Override
    public Mono<Void> handle(ServerWebExchange serverWebExchange, Throwable ex) {
        DataBufferFactory bufferFactory = serverWebExchange.getResponse().bufferFactory();
        ServerHttpResponse response = serverWebExchange.getResponse();

        ErrorMessage errorMessage = new ErrorMessage("INTERNAL_SERVER_ERROR", ex.toString());

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        DataBuffer dataBuffer = null;
        try {
            dataBuffer = bufferFactory.wrap(OM.writeValueAsBytes(errorMessage));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        logger.error(ex.toString(), ex);

        return response.writeWith(Mono.just(dataBuffer));
    }
}
