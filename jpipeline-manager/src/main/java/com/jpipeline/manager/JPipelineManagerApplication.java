package com.jpipeline.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import javax.annotation.PostConstruct;
import java.util.Set;

@SpringBootApplication
@ComponentScan(basePackages = "com.jpipeline")
public class JPipelineManagerApplication {

    @Autowired
    private ApplicationArguments applicationArguments;

    @PostConstruct
    private void init() {
        Set<String> optionNames = applicationArguments.getOptionNames();
        if (optionNames.contains("jwtSecret")) {
            AuthService.setJwtSecret(applicationArguments.getOptionValues("jwtSecret").get(0));
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(JPipelineManagerApplication.class, args);

    }

}
