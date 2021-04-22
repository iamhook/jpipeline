package com.jpipeline.jpipeline.http;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/service")
public class ServiceController {

    @GetMapping("/checkIsAlive")
    public void checkIsAlive() {}

    @GetMapping("/shutdown")
    public void shutdown() {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.schedule(() -> {
            System.exit(0);
        }, 100, TimeUnit.MILLISECONDS);
    }

}
