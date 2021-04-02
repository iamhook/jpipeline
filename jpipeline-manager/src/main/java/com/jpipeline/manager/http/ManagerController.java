package com.jpipeline.manager.http;

import com.jpipeline.common.util.CJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PreDestroy;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

@RestController
@RequestMapping("/api/manager")
public class ManagerController {

    private static final Logger log = LoggerFactory.getLogger(ManagerController.class);

    @Value("${jpipeline.workflowConfigPath}")
    private String configPath;

    @Value("${jpipeline.executor.runCommand}")
    private String runCommand;

    private Process executor;

    @PostMapping("/deploy")
    public void deploy(@RequestBody CJson config) throws Exception {
        saveConfig(config);
        restartExecutor();
    }

    private void saveConfig(CJson config) throws IOException {
        FileOutputStream fos = new FileOutputStream(configPath);
        fos.write(config.toJson().getBytes(Charset.defaultCharset()));
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
