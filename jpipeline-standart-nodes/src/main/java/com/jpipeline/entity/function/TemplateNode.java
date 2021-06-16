package com.jpipeline.entity.function;


import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.jpipeline.common.entity.Node;
import com.jpipeline.common.util.JPMessage;
import com.jpipeline.common.util.annotations.NodeProperty;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.UUID;

public class TemplateNode extends Node {

    @NodeProperty
    private String template;

    public TemplateNode(UUID id) {
        super(id);
    }

    private Mustache mustache;

    @Override
    public void onInit() {
        MustacheFactory mf = new DefaultMustacheFactory();
        mustache = mf.compile(new StringReader(template), "templte");
    }

    @Override
    public void onInput(JPMessage message) {
        mustache(message);
    }

    private void mustache(JPMessage message) {
        Writer writer = mustache.execute(new StringWriter(), message);
        String result = writer.toString();
        send(message.setPayload(result));
    }

}
