package com.jpipeline.javafxclient.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpipeline.common.WorkflowConfig;
import com.jpipeline.common.dto.NodeDTO;
import com.jpipeline.common.entity.Node;
import com.jpipeline.common.service.RSocketService;
import com.jpipeline.common.util.NodeTypeConfig;
import com.jpipeline.javafxclient.context.JContext;
import lombok.Setter;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NodeService {

    private static final ObjectMapper OM = new ObjectMapper();
    private static Logger log = LoggerFactory.getLogger(NodeService.class);

    @Setter
    private static RSocketService rSocketService;

    @Setter
    private static HttpService httpService;

    private static Map<String, NodeTypeConfig> nodeConfigsCache = new ConcurrentHashMap<>();
    private static Map<String, String> nodeFxmlCache = new ConcurrentHashMap<>();
    private static Map<String, String> nodeHtmlCache = new ConcurrentHashMap<>();
    private static Map<String, String> nodeFxmlControllerCache = new ConcurrentHashMap<>();

    public static void clearCache() {
        nodeFxmlCache.clear();
        nodeConfigsCache.clear();
    }


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

    public static NodeTypeConfig getNodeConfig(String nodeType) {
        return nodeConfigsCache.computeIfAbsent(nodeType, s -> {
            try {
                String response = EntityUtils.toString(httpService.get("/proxy/api/nodesupport/" + nodeType + "/config").getEntity());
                return OM.readValue(response, new TypeReference<>() {});
            } catch (Exception e) {
                log.error(e.toString(), e);
            }
            return null;
        });
    }

    public static String getNodeFxml(String nodeType) {
        return getNodeResource("fxml", nodeType, nodeFxmlCache);
    }

    public static String getNodeHtml(String nodeType) {
        return getNodeResource("html", nodeType, nodeHtmlCache);
    }

    public static String getNodeGroovyController(String nodeType) {
        return getNodeResource("controller", nodeType, nodeFxmlControllerCache);
    }

    public static String getNodeResource(String resourceType, String nodeType, Map<String, String> cache) {
        return cache.computeIfAbsent(nodeType, s -> {
            try {
                HttpResponse response = httpService.get("/proxy/api/nodesupport/" + nodeType + "/" + resourceType);
                String disposition = response.getFirstHeader("Content-Disposition").getValue();
                String fileName = disposition.replaceFirst(".*filename\\*=.*''([\\w.]*)$", "$1");
                String path = JContext.getExtResourcesFolder() + fileName;
                FileUtils.write(new File(path), EntityUtils.toString(response.getEntity()), Charset.defaultCharset());
                return path;
            } catch (Exception e) {
                log.error(e.toString(), e);
                return null;
            }
        });
    }

    private static Flux<Node.NodeSignal> statusStream;
    private static Flux<Node.NodeSignal> debugStream;
    private static Flux<Node.NodeSignal> errorsStream;

    public static void clearRSocketStreams() {
        statusStream = null;
        debugStream = null;
        errorsStream = null;
    }

    private static Flux<Node.NodeSignal> getSignalStream(String route) {
        return rSocketService.requestStream("", route, Node.NodeSignal.class)
                .onErrorContinue((throwable, o) -> {});
    }

    public static Flux<Node.NodeSignal> getStatusStream() {
        if (statusStream == null) {
            statusStream = getSignalStream("/status");
        }
        return statusStream;
    }

    public static Flux<Node.NodeSignal> getDebugStream() {
        if (debugStream == null) {
            debugStream = getSignalStream("/debug");
        }
        return debugStream;
    }

    public static Flux<Node.NodeSignal> getErrorsStream() {
        if (errorsStream == null) {
            errorsStream = getSignalStream("/errors");
        }
        return errorsStream;
    }

    public static boolean checkIsAlive() {
        try {
            return Boolean.parseBoolean(EntityUtils.toString(httpService.get("/proxy/api/service/checkIsAlive").getEntity()));
        } catch (Exception e) {
            return false;
        }
    }

}
