package com.jpipeline.manager.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpipeline.common.WorkflowConfig;
import com.jpipeline.manager.AuthService;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/api/manager")
public class ManagerController {

    private static final ObjectMapper OM = new ObjectMapper();

    private static final Logger log = LoggerFactory.getLogger(ManagerController.class);

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Autowired
    private HttpClient httpClient;

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
    public Boolean startExecutor() throws Exception {
        log.info("Run '{}'", runCommand);

        runCommand = runCommand + " --jwtSecret=" + AuthService.getJwtSecret();

        ProcessBuilder ps = new ProcessBuilder(runCommand.split("\\s"));

        ps.redirectErrorStream(true);

        Process pr = ps.start();

        BufferedReader in = new BufferedReader(new InputStreamReader(pr.getInputStream()));

        executor.submit(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    System.out.println(line);
                }
                pr.waitFor();
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        });

        return true;
    }

    @PostMapping("/stop")
    public boolean stopExecutor() {
        try {
            HttpGet httpGet = new HttpGet("http://localhost:" + executorPort + "/api/service/shutdown");
            HttpResponse response = httpClient.execute(httpGet);

            if (response.getStatusLine().getStatusCode() == 200) {
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    @PreDestroy
    public void onExit() {
        stopExecutor();
    }
}
