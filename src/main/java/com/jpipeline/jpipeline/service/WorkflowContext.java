package com.jpipeline.jpipeline.service;

import com.jpipeline.jpipeline.entity.Node;
import com.jpipeline.jpipeline.util.CJson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class WorkflowContext {

    @Autowired
    private NodeSupportService nodeSupportService;

    public void build(CJson config) {
        List<Map> nodes = config.getList("nodes");

        List<Node> collect = nodes.stream()
                .map(map -> nodeSupportService.fromJson(new CJson(map)))
                .collect(Collectors.toList());
        build(collect);
    }

    public void build(List<? extends Node> nodes) {
        Map<UUID, Node> nodeMap = new HashMap<>();

        nodes.forEach(node -> nodeMap.put(node.getId(), node));

        nodes.forEach(node -> node.getWires().forEach(wire -> {
            if (nodeMap.containsKey(wire)) {
                node.subscribe(nodeMap.get(wire));
            }
        }));

        nodes.forEach(Node::init);
    }

}
