package com.jpipeline.entity;

import com.jpipeline.common.entity.Node;
import com.jpipeline.common.util.JPMessage;
import com.jpipeline.common.util.annotations.NodeProperty;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SqlSelectNode extends Node {

    @NodeProperty
    private String url;
    @NodeProperty
    private String username;
    @NodeProperty
    private String password;
    @NodeProperty
    private String query;

    private Connection connection;

    public SqlSelectNode(UUID id) {
        super(id);
    }

    @Override
    public void onInit() {
        try {
           connection  = DriverManager.getConnection(url, username, password);


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void onInput(JPMessage message) {
        try {
            Statement selectStmt = connection.createStatement();
            ResultSet rs = selectStmt
                    .executeQuery(query);
            ResultSetMetaData md = rs.getMetaData();
            int columns = md.getColumnCount();
            long extracted = 0;
            while (rs.next()) {
                Map row = new HashMap(columns);
                for(int i=1; i<=columns; ++i){
                    row.put(md.getColumnName(i),rs.getObject(i));
                }

                message.setPayload(row);
                send(message);
                setStatus(new NodeStatus("extracted " + ++extracted));
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }
}
