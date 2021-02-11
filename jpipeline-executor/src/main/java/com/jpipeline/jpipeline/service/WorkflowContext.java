package com.jpipeline.jpipeline.service;

import com.jpipeline.jpipeline.JpipelineApplication;
import com.jpipeline.jpipeline.entity.Node;
import com.jpipeline.jpipeline.util.CJson;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WorkflowContext {

    private Map<UUID, Node> nodeMap = new HashMap<>();

    @Value("${jpipeline.workflow-config-path}")
    private String configPath;

    @Autowired
    private NodeSupportService nodeSupportService;

    @Autowired
    private ApplicationArguments applicationArguments;

    @PostConstruct
    private void postContruct() throws Exception {
        List<String> args = Arrays.asList(applicationArguments.getSourceArgs());
        if (args.contains("deploy")) {
            deploy();
        }
    }

    public void deploy() throws Exception {
        File configFile = new File(configPath);
        if (configFile.exists()) {
            String jsonString = Files.readString(configFile.toPath(), Charset.defaultCharset());
            CJson config = CJson.fromJson(jsonString);
            deploy(config);
        } else {
            throw new Exception("Config does not exist!");
        }
    }

    public void deploy(CJson config) {
        saveConfig(config);
        List<Map> nodes = config.getList("nodes");

        List<Node> collect = nodes.stream()
                .map(map -> nodeSupportService.fromJson(new CJson(map)))
                .filter(node -> node.getActive())
                .collect(Collectors.toList());
        deploy(collect);
    }

    public void deploy(List<? extends Node> nodes) {
        nodes.forEach(node -> nodeMap.put(node.getId(), node));
        nodes.forEach(node -> node.getWires().forEach(wire -> {
            if (nodeMap.containsKey(wire)) {
                node.subscribe(nodeMap.get(wire));
            }
        }));

        nodes.forEach(Node::init);
    }

    @SneakyThrows
    private void saveConfig(CJson config) {
        FileOutputStream fos = new FileOutputStream(configPath);
        fos.write(config.toJson().getBytes(Charset.defaultCharset()));
    }

}
