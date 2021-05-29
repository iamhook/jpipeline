package com.jpipeline.javafxclient;

import com.jpipeline.javafxclient.service.JConnection;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.util.*;
import java.util.concurrent.ConcurrentMap;

public class PropertiesStore {

    private static final String PATH = "jpipeline-props.db";

    public static Set<JConnection> getConnections() {

        DB db = DBMaker.fileDB(PATH).make();
        try {
            ConcurrentMap map = db.hashMap("store").createOrOpen();
            Set<JConnection> connections = (Set<JConnection>) map.computeIfAbsent("connections", o -> new HashSet<>());

            return connections;
        } finally {
            db.close();
        }
    }

    public static void saveConnection(JConnection connection) {
        DB db = DBMaker.fileDB(PATH).make();
        try {
            ConcurrentMap map = db.hashMap("store").createOrOpen();
            Set<JConnection> connections = (Set<JConnection>) map.computeIfAbsent("connections", o -> new HashSet<>());
            connections.add(connection);
            map.put("connections", connections);
        } finally {
            db.close();
        }
    }

    public static void deleteConnection(JConnection connection) {
        DB db = DBMaker.fileDB(PATH).make();
        try {
            ConcurrentMap map = db.hashMap("store").createOrOpen();
            List<JConnection> connections = (List<JConnection>) map.computeIfAbsent("connections", o -> new ArrayList<JConnection>());
            connections.remove(connection);
            map.put("connections", connections);
        } finally {
            db.close();
        }
    }

}
