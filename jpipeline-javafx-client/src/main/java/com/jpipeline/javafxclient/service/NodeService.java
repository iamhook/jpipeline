package com.jpipeline.javafxclient.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpipeline.common.WorkflowConfig;
import com.jpipeline.common.dto.NodeDTO;
import com.jpipeline.common.entity.Node;
import com.jpipeline.common.service.RSocketService;
import com.jpipeline.common.util.NodeConfig;
import lombok.Setter;
import org.apache.http.util.EntityUtils;
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

    private static RSocketService rSocketService = new RSocketService("ws://localhost:7000");

    @Setter
    private static HttpService httpService;

    private static Map<String, NodeConfig> configsCache = new ConcurrentHashMap<>();

    public static List<String> getNodeTypes() {
        try {
            String response = EntityUtils.toString(httpService.get("/proxy/api/nodesupport/types").getEntity());
            return OM.readValue(response, new TypeReference<>() {});
        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        return Collections.emptyList();
    }

    public static WorkflowConfig getConfig() {
        try {
            String response = EntityUtils.toString(httpService.get("/proxy/api/service/config").getEntity());
            return OM.readValue(response, new TypeReference<>() {});
        } catch (Exception e) {
            log.error(e.toString());
            throw new RuntimeException(e);
        }
    }

    public static NodeDTO createNewNode(String nodeType) {
        try {
            String response = EntityUtils.toString(httpService.get("/proxy/api/nodesupport/" + nodeType + "/create").getEntity());
            return OM.readValue(response, new TypeReference<>() {});
        } catch (Exception e) {
            log.error(e.toString(), e);
        }

        return null;
    }

    public static void pressButton(String nodeId) {
        try {
            httpService.get("/proxy/api/node/" + nodeId + "/pressButton");
        } catch (Exception e) {
            log.error(e.toString(), e);
        }
    }

    public static NodeConfig getNodeConfig(String nodeType) {
        return configsCache.computeIfAbsent(nodeType, s -> {
            try {
                String response = EntityUtils.toString(httpService.get("/proxy/api/nodesupport/" + nodeType + "/config").getEntity());
                return OM.readValue(response, new TypeReference<>() {});
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
            return Boolean.parseBoolean(EntityUtils.toString(httpService.get("/proxy/api/service/checkIsAlive").getEntity()));
        } catch (Exception e) {
            return false;
        }
    }

}
