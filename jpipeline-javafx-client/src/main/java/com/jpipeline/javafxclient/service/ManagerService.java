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
        httpService = new HttpService(AuthContext.getManagerHost());
    }

    public static void deploy(WorkflowConfig config) {
        try {
            httpService.post("/api/manager/deploy", OM.writeValueAsString(config));
        } catch (Exception e) {
            log.error(e.toString());
            throw new RuntimeException(e);
        }
    }

    public static boolean checkIsAlive() {
        try {
            httpService.get("/checkIsAlive");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
