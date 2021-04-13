package com.jpipeline.jpipeline.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpipeline.common.WorkflowConfig;
import com.jpipeline.common.dto.NodeDTO;
import com.jpipeline.common.entity.Node;
import com.jpipeline.common.util.CJson;
import com.jpipeline.common.util.exception.NotFoundException;
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
public class WorkflowService {

    private static final Logger log = LoggerFactory.getLogger(WorkflowService.class);

    private static final ObjectMapper OM = new ObjectMapper();

    private Map<UUID, Node> nodeMap = new HashMap<>();
    private Map<UUID, NodeDTO> nodeDTOMap = new HashMap<>();

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
            deploy(OM.readValue(jsonString, WorkflowConfig.class));
        } else {
            throw new Exception("Config does not exist!");
        }
    }

    public void deploy(WorkflowConfig config) {
        List<NodeDTO> nodes = config.getNodes();

        if (nodes != null) {
            List<Node> collect = nodes.stream()
                    .map(nodeDTO -> {
                        Node node = nodeSupportService.fromDTO(nodeDTO);
                        nodeDTOMap.put(node.getId(), nodeDTO);
                        return node;
                    })
                    .filter(node -> node.getActive())
                    .collect(Collectors.toList());
            deploy(collect);
        } else {
            log.error("Nodes is null in config file");
        }
    }

    public void deploy(List<? extends Node> nodes) {
        nodes.forEach(node -> nodeMap.put(node.getId(), node));
        nodes.forEach(node -> getWires(node.getId()).stream().map(UUID::fromString).forEach(wire -> {
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

    private Set<String> getWires(UUID uuid) {
        return nodeDTOMap.get(uuid).getWires();
    }

}
