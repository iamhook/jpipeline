package com.jpipeline.jpipeline.entity;

import com.jpipeline.jpipeline.util.NodePropertyConfig;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ExecNode extends Node {

    public ExecNode(UUID id) {
        super(id);
    }

    @Override
    public void onInit() {

    }

    @Override
    public void onInput(Object message) {
        try {
            Runtime run = Runtime.getRuntime();
            Process pr = run.exec(command());
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

    public static List<NodePropertyConfig> nodePropertyConfigs() {
        return Arrays.asList(
                new NodePropertyConfig("command", String.class, null, true)
        );
    }

    private String command() {
        return properties.getString("command");
    }
}
