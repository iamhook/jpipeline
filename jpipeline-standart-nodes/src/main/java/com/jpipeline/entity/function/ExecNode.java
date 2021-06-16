package com.jpipeline.entity.function;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.jpipeline.common.entity.Node;
import com.jpipeline.common.util.JPMessage;
import com.jpipeline.common.util.annotations.NodeProperty;

import java.io.*;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ExecNode extends Node {

    @NodeProperty
    private String commandTemplate;

    private Mustache mustache;

    public ExecNode(UUID id) {
        super(id);
    }

    @Override
    public void onInit() {
        MustacheFactory mf = new DefaultMustacheFactory();
        mustache = mf.compile(new StringReader(commandTemplate), "command");
    }

    @Override
    public void onInput(JPMessage message) {
        try {
            Runtime run = Runtime.getRuntime();
            Process pr = run.exec(mustache(message));
            pr.waitFor();
            BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line=buf.readLine())!=null) {
                output.append(line + "\n");
            }

            send(message.setPayload(output.toString()));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private String mustache(JPMessage message) {
        Writer writer = mustache.execute(new StringWriter(), message);
        return writer.toString();
    }

}
