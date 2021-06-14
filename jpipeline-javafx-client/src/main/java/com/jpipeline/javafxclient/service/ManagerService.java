package com.jpipeline.javafxclient.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpipeline.common.WorkflowConfig;
import com.jpipeline.common.service.RSocketService;
import com.jpipeline.common.util.ManagerMeta;
import com.jpipeline.common.util.exception.CustomException;
import com.jpipeline.javafxclient.context.AuthContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ManagerService {

    private static ObjectMapper OM = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(ManagerService.class);
    private static HttpService httpService;
    private static ManagerMeta meta;

    public static void createHttpService() {
        String hostname = AuthContext.getConnection().getHostname();
        Integer port = AuthContext.getConnection().getPort();
        httpService = new HttpService(hostname, port);
        meta = getMeta();
        NodeService.setHttpService(httpService);
        NodeService.setRSocketService(new RSocketService("ws://" + hostname + ":" + meta.getRsocketPort()));
        NodeService.clearCache();
    }

    private static ManagerMeta getMeta() {
        try {
            String response = EntityUtils.toString(httpService.get("/api/manager/meta").getEntity());
            return OM.readValue(response, new TypeReference<>() {});
        } catch (Exception e) {
            log.error(e.toString());
            throw new RuntimeException(e);
        }
    }

    public static void deploy(WorkflowConfig config) {
        try {
            httpService.post("/api/manager/deploy", OM.writeValueAsString(config));
        } catch (Exception e) {
            log.error(e.toString());
            throw new RuntimeException(e);
        }
    }

    public static boolean login() {
        try {
            JConnection connection = AuthContext.getConnection();
            httpService.get("/api/auth/login?username=" + connection.getUsername() + "&password=" + connection.getPassword());
            return true;
        } catch (IOException e) {
            throw new CustomException(e);
        }
    }

    public static boolean checkIsAlive(String host) {
        try {
            new HttpService(host).get("/api/manager/checkIsAlive");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean checkIsAlive() {
        try {
            httpService.get("/api/manager/checkIsAlive");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
