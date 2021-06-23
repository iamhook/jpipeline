package com.jpipeline.javafxclient.context;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpipeline.javafxclient.service.JConnection;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;

public class PropertiesStore {

    private static final String PATH = "data.json";

    private static final Logger log = LoggerFactory.getLogger(PropertiesStore.class);

    private static final ObjectMapper OM = new ObjectMapper();

    private static StoredData data;

    static {
        try {
            if (new File(PATH).exists())
                data = OM.readValue(FileUtils.readFileToString(new File(PATH), Charset.defaultCharset()), StoredData.class);
            else {
                data = new StoredData();
            }
        } catch (IOException e) {
            log.error(e.toString(), e);
        }
    }

    public static Set<JConnection> getConnections() {
        return data.connections;
    }

    public static void saveConnection(JConnection connection) {

        if (data.connections.contains(connection))
            data.connections.remove(connection);
        data.connections.add(connection);
        saveData();
    }

    public static void deleteConnection(JConnection connection) {
        data.connections.remove(connection);
        saveData();
    }

    private static void saveData() {
        try {
            FileUtils.write(new File(PATH), OM.writeValueAsString(data), Charset.defaultCharset());
        } catch (IOException e) {
            log.error(e.toString(), e);
        }
    }

    @Getter @Setter
    private static class StoredData {
        private Set<JConnection> connections = new HashSet<>();
    }

}
