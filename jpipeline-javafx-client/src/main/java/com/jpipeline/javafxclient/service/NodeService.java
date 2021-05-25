package com.jpipeline.javafxclient.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpipeline.common.WorkflowConfig;
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

    public static WorkflowConfig getConfig() {
        try {
            String response = httpService.get("/api/service/config").body();
            return OM.readValue(response, new TypeReference<>() {
            });
        } catch (Exception e) {
            log.error(e.toString());
            throw new RuntimeException(e);
        }
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

    public static void pressButton(String nodeId) {
        try {
            httpService.get("/api/node/" + nodeId + "/pressButton").body();
        } catch (Exception e) {
            log.error(e.toString(), e);
        }
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

    private static Flux<Node.NodeSignal> signalFlux;

    public static void flushSignalFlux() {
        signalFlux = null;
    }

    public static Flux<Node.NodeSignal> getSignalStream() throws JsonProcessingException {
        if (signalFlux == null) {
            signalFlux = rSocketService.requestStream("", "/node", Node.NodeSignal.class)
                    .onErrorContinue((throwable, o) -> {});
        }
        return signalFlux;
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
