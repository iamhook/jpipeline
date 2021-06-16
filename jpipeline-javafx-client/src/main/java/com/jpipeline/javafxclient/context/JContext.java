package com.jpipeline.javafxclient.context;

import lombok.Getter;
import lombok.Setter;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JContext {

    @Getter @Setter
    private static Properties properties;

    public static String getExtResourcesFolder() {
        return properties.getProperty("jpipeline.client.externalResourcesFolder");
    }



}
