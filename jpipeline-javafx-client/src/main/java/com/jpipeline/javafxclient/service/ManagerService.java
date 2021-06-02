package com.jpipeline.javafxclient.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpipeline.common.WorkflowConfig;
import com.jpipeline.common.util.ErrorMessage;
import com.jpipeline.common.util.exception.CustomException;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ManagerService {

    private static ObjectMapper OM = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(ManagerService.class);
    private static HttpService httpService;

    public static void createHttpService() {
        httpService = new HttpService(AuthContext.getConnection().getHostname());
        NodeService.setHttpService(httpService);
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
            HttpResponse response = httpService.get("/api/auth/login?username=" + connection.getUsername() + "&password=" + connection.getPassword());
            if (response.getStatusLine().getStatusCode() == 200) {
                return true;
            } else {
                ErrorMessage errorMessage = OM.readValue(EntityUtils.toString(response.getEntity()), ErrorMessage.class);
                throw new CustomException(errorMessage.getMessage());
            }
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
