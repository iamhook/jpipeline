package com.jpipeline.javafxclient.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpipeline.common.WorkflowConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManagerService {

    private static ObjectMapper OM = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(ManagerService.class);
    private static HttpService httpService = new HttpService("localhost", 9543);

    public static WorkflowConfig getConfig() {
        try {
            String response = httpService.get("/api/manager/config");
            return OM.readValue(response, new TypeReference<WorkflowConfig>() {});
        } catch (Exception e) {
            log.error(e.toString());
            throw new RuntimeException(e);
        }
    }

    public static void deploy(WorkflowConfig config) {
        try {
            String post = httpService.post("/api/manager/deploy", OM.writeValueAsString(config));
            return;
        } catch (Exception e) {
            log.error(e.toString());
            throw new RuntimeException(e);
        }
    }

}
