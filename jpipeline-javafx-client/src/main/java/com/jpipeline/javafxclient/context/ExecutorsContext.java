package com.jpipeline.javafxclient.context;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ExecutorsContext {

    private static List<ExecutorService> executors = new ArrayList<>();

    public static ExecutorService newSingleTheadExecutor() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executors.add(executor);
        return executor;
    }

    public static ScheduledExecutorService newSingleThreadScheduledExecutor() {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executors.add(executor);
        return executor;
    }

    public static void shutdownAll() {
        executors.forEach(ExecutorService::shutdown);
    }

}
