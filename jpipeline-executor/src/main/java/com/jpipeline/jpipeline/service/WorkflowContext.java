package com.jpipeline.jpipeline.service;

import com.jpipeline.jpipeline.entity.Node;
import com.jpipeline.jpipeline.util.CJson;
import com.jpipeline.jpipeline.util.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WorkflowContext {

    private static final Logger log = LoggerFactory.getLogger(WorkflowContext.class);

    private Map<UUID, Node> nodeMap = new HashMap<>();

    @Autowired
    private NodeSupportService nodeSupportService;

    @Autowired
    private ApplicationArguments applicationArguments;

    @PostConstruct
    private void init() throws Exception {
        Set<String> optionNames = applicationArguments.getOptionNames();
        if (optionNames.contains("configPath")) {
            deploy(applicationArguments.getOptionValues("configPath").get(0));
        }
    }

    public void deploy(String configPath) throws Exception {
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
        List<Map> nodes = config.getList("nodes");

        if (nodes != null) {
            List<Node> collect = nodes.stream()
                    .map(map -> nodeSupportService.fromJson(new CJson(map)))
                    .filter(node -> node.getActive())
                    .collect(Collectors.toList());
            deploy(collect);
        } else {
            log.error("Nodes is null in config file");
        }
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

    public void pressButton(UUID uuid) throws NotFoundException {
        if (nodeMap.containsKey(uuid))
            nodeMap.get(uuid).pressButton();
        else
            throw new NotFoundException("Node " + uuid + " not found");
    }

}
