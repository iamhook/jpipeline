package com.jpipeline.entity.function;


import com.jpipeline.common.entity.Node;
import com.jpipeline.common.util.CJson;
import com.jpipeline.common.util.JPMessage;
import com.jpipeline.common.util.annotations.NodeProperty;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;

import java.util.List;
import java.util.UUID;

public class FunctionNode extends Node {

    @NodeProperty
    private String function;

    public FunctionNode(UUID id) {
        super(id);
    }

    @Override
    public void onInit() {

    }

    @Override
    public void onInput(JPMessage message) {

        Binding binding = new Binding();
        binding.setVariable("message", message);
        CompilerConfiguration conf = new CompilerConfiguration();
        ImportCustomizer importCustomizer = new ImportCustomizer();
        importCustomizer.addStarImports("java.util");
        importCustomizer.addStarImports("java.lang");
        conf.addCompilationCustomizers(importCustomizer);
        GroovyShell shell = new GroovyShell(Thread.currentThread().getContextClassLoader(), binding, conf);
        JPMessage result = (JPMessage) shell.evaluate(function);

        send(result);
    }

}
