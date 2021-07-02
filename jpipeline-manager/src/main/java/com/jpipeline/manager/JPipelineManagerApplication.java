package com.jpipeline.manager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.jpipeline")
public class JPipelineManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(JPipelineManagerApplication.class, args);

    }

}
