package com.jpipeline.javafxclient.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpipeline.common.WorkflowConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManagerService {

    private static ObjectMapper OM = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(ManagerService.class);
    private static HttpService httpService;

    public static void createHttpService() {
        httpService = new HttpService(AuthContext.getConnection().getHostname());
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
        } catch (Exception e) {
            return false;
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
