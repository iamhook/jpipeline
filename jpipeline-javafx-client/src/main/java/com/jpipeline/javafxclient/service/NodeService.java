package com.jpipeline.javafxclient.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpipeline.common.dto.NodeDTO;
import com.jpipeline.common.entity.Node;
import com.jpipeline.common.util.NodeConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NodeService {

    private static ObjectMapper OM = new ObjectMapper();
    private static Logger log = LoggerFactory.getLogger(NodeService.class);

    private static RSocketService rSocketService = new RSocketService();
    private static HttpService httpService = new HttpService("localhost", 9544);
    private static Map<String, NodeConfig> configsCache = new ConcurrentHashMap<>();

    public static List<String> getNodeTypes() {
        try {
            String response = httpService.get("/api/nodesupport/types").body();
            return OM.readValue(response, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        return Collections.emptyList();
    }

    public static NodeDTO createNewNode(String nodeType) {
        try {
            String response = httpService.get("/api/nodesupport/" + nodeType + "/create").body();
            return OM.readValue(response, new TypeReference<>() {
            });
        } catch (Exception e) {
            log.error(e.toString(), e);
        }

        return null;
    }

    public static NodeConfig getNodeConfig(String nodeType) {
        return configsCache.computeIfAbsent(nodeType, s -> {
            try {
                String response = httpService.get("/api/nodesupport/" + nodeType + "/config").body();
                return OM.readValue(response, new TypeReference<>() {
                });
            } catch (Exception e) {
                log.error(e.toString(), e);
            }
            return null;
        });
    }

    public static Flux<Node.NodeStatus> getStatusStream(String nodeId) throws JsonProcessingException {
        return rSocketService.requestStream("", "/node/"+nodeId, Node.NodeSignal.class)
                .map(nodeSignal -> OM.convertValue(nodeSignal.getBody(), Node.NodeStatus.class))
                .onErrorContinue((throwable, o) -> {});
    }

    public static boolean checkIsAlive() {
        try {
            httpService.get("/api/service/checkIsAlive");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
