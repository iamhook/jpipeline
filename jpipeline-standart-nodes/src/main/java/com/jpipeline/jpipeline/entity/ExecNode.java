package com.jpipeline.jpipeline.entity;

import com.jpipeline.common.entity.Node;
import com.jpipeline.common.util.annotations.NodeProperty;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ExecNode extends Node {

    @NodeProperty
    private String command;

    @NodeProperty
    private Collection<String> args;

    public ExecNode(UUID id) {
        super(id);
    }

    @Override
    public void onInit() {
        AtomicInteger i = new AtomicInteger();
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            setStatus(new NodeStatus("Status: " + i.getAndIncrement()));
        }, 1000, 100, TimeUnit.MILLISECONDS);
    }

    @Override
    public void onInput(Object message) {
        try {
            Runtime run = Runtime.getRuntime();
            Process pr = run.exec(command);
            pr.waitFor();
            BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line=buf.readLine())!=null) {
                output.append(line + "\n");
            }

            send(output.toString());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

}
