package com.jpipeline.jpipeline.http;

import com.jpipeline.common.WorkflowConfig;
import com.jpipeline.jpipeline.service.WorkflowService;
import com.jpipeline.security.AuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/service")
public class ServiceController {

    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private AuthFilter authFilter;

    @PostMapping
    public void setJwtSecret(@RequestParam String jwtSecret) {
        authFilter.setJwtSecret(jwtSecret);
    }

    @GetMapping("/checkIsAlive")
    public boolean checkIsAlive() {
        return true;
    }

    @GetMapping("/shutdown")
    public void shutdown() {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.schedule(() -> {
            System.exit(0);
        }, 100, TimeUnit.MILLISECONDS);
    }

    @GetMapping("/config")
    public WorkflowConfig getConfig() {
        return workflowService.getConfig();
    }

}
