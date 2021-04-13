package com.jpipeline.manager.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpipeline.common.WorkflowConfig;
import com.jpipeline.common.dto.NodeDTO;
import com.jpipeline.common.util.CJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/manager")
public class ManagerController {

    private static final ObjectMapper OM = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(ManagerController.class);

    @Value("${jpipeline.workflowConfigPath}")
    private String configPath;

    @Value("${jpipeline.executor.runCommand}")
    private String runCommand;

    private Process executor;

    @PostConstruct
    private void init() throws Exception {
        startExecutor();
    }

    @PostMapping("/deploy")
    public void deploy(@RequestBody WorkflowConfig config) throws Exception {
        saveConfig(config);
        restartExecutor();
    }

    @GetMapping("/config")
    private WorkflowConfig getNodeConfig() throws IOException {
        String jsonString = Files.readString(Path.of(configPath), Charset.defaultCharset());
        return OM.readValue(jsonString, WorkflowConfig.class);
    }

    private void saveConfig(WorkflowConfig config) throws IOException {
        FileOutputStream fos = new FileOutputStream(configPath);
        fos.write(OM.writeValueAsBytes(config));
        log.info("Save config file to {}", configPath);
    }

    public void restartExecutor() throws Exception {
        stopExecutor();
        startExecutor();
    }

    public void startExecutor() throws Exception {
        if (executor != null && executor.isAlive()) {
            throw new Exception("Process is running already");
        }
        log.info("Run '{}'", runCommand);
        executor = Runtime.getRuntime().exec(runCommand);
        log.info("JPipelineExecutor started");
    }

    public void stopExecutor() {
        if (executor != null && executor.isAlive()) {
            executor.destroy();
            log.info("JPipelineExecutor stopped");
        }
    }

    @PreDestroy
    public void onExit() {
        stopExecutor();
    }
}
