package com.jpipeline.manager.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpipeline.common.WorkflowConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@RestController
@RequestMapping("/api/manager")
public class ManagerController {

    private static final ObjectMapper OM = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(ManagerController.class);
    HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${jpipeline.workflowConfigPath}")
    private String configPath;

    @Value("${jpipeline.executor.runCommand}")
    private String runCommand;

    @Value("${jpipeline.executor.port}")
    private Integer executorPort;

    @PostConstruct
    private void init() throws Exception {
        startExecutor();
    }

    @GetMapping("/checkIsAlive")
    public void checkIsAlive() {}

    @PostMapping("/deploy")
    public void deploy(@RequestBody WorkflowConfig config) throws Exception {
        saveConfig(config);
        restartExecutor();
    }

    private void saveConfig(WorkflowConfig config) throws IOException {
        FileOutputStream fos = new FileOutputStream(configPath);
        fos.write(OM.writeValueAsBytes(config));
        log.info("Save config file to {}", configPath);
    }

    public void restartExecutor() throws Exception {
        stopExecutor();
        Thread.sleep(500);
        startExecutor();
    }

    @PostMapping("/start")
    public void startExecutor() throws Exception {
        log.info("Run '{}'", runCommand);
        log.info("JPipelineExecutor started");
    }

    @PostMapping("/stop")
    public boolean stopExecutor() throws IOException, InterruptedException {
        try {
            HttpResponse<String> send = httpClient.send(HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create("http://localhost:" + executorPort + "/api/service/shutdown"))
                    .build(), HttpResponse.BodyHandlers.ofString());

            if (send.statusCode() == 200) {
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @PreDestroy
    public void onExit() throws IOException, InterruptedException {
        stopExecutor();
    }
}
