package com.jpipeline.jpipeline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class JPipelineExecutorApplication {

    private static final Logger log = LoggerFactory.getLogger(JPipelineExecutorApplication.class);

    // TODO application.properties
    private static Integer managerPort = 9543;
    private static String managerHost = "localhost";

    public static void main(String[] args) {
        SpringApplication.run(JPipelineExecutorApplication.class, args);

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

        executor.scheduleAtFixedRate(() -> checkManagerIsAlive(),
                1000, 1000, TimeUnit.MILLISECONDS);
    }

    private static void checkManagerIsAlive() {
        log.info("Check jpipeline-manager is alive");
        boolean success = true;
        try {
            (new Socket(managerHost, managerPort)).close();
        } catch (UnknownHostException e) {
            log.error(e.toString());
            success = false;
        } catch (IOException e) {
            log.error(e.toString());
            success = false;
        }

        if (!success) {
            log.info("Jpipeline-manager is not alive, exit");
            System.exit(0);
        }
    }

}
