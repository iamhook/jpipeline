package com.jpipeline.jpipeline.http;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/workflow")
public class WorkflowController {

    @GetMapping
    public Mono<String> test() {
        return Mono.just("Hello, World!");
    }

    @PostMapping
    public Mono<String> deploy(@RequestBody Map<String, Object> object) {
        return Mono.just("Deployed!");
    }

}
