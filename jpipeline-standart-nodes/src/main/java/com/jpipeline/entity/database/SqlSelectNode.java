package com.jpipeline.entity.database;

import com.jpipeline.common.entity.Node;
import com.jpipeline.common.util.JPMessage;
import com.jpipeline.common.util.annotations.NodeProperty;
import org.sql2o.*;
import org.sql2o.Connection;

import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlSelectNode extends Node {

    @NodeProperty
    private String url;
    @NodeProperty
    private String username;
    @NodeProperty
    private String password;
    @NodeProperty
    private String query;
    @NodeProperty
    private String mode;

    //private Connection connection;
    private Sql2o sql2o;

    public SqlSelectNode(UUID id) {
        super(id);
    }

    @Override
    public void onInit() {
        sql2o = new Sql2o(url, username, password);
        /*try {
           connection  = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            log.error(e.toString(), e);
        }*/
    }

    @Override
    public synchronized void onInput(JPMessage message) {
        long extracted = 0;

        try (Connection open = sql2o.open()) {
            Query query = open.createQuery(this.query);
            Matcher m = Pattern.compile(":[\\w]+")
                    .matcher(this.query);
            while (m.find()) {
                String paramName = m.group().substring(1);
                Object paramValue = message.get(paramName);
                query.addParameter(paramName, paramValue);
            }

            if ("oneRecordOneMessage".equals(mode)) {
                ResultSetIterable<Map<String, Object>> result = query.executeAndFetchLazy((ResultSetHandler<Map<String, Object>>) rs -> {
                    int i = 0;
                    ResultSetMetaData md = rs.getMetaData();
                    int columns = md.getColumnCount();
                    Map<String, Object> row = new HashMap(columns);
                    for (i = 1; i <= columns; ++i) {
                        row.put(md.getColumnName(i), rs.getObject(i));
                    }
                    return row;
                });
                for (Map<String, Object> row : result) {
                    message.setPayload(row);
                    send(message);
                    setStatus(new NodeStatus("extracted " + ++extracted));
                }
            } else {
                List<Map<String, Object>> result = query.executeAndFetchTable().asList();
                extracted = result.size();
                setStatus(new NodeStatus("extracted " + extracted));
                send(message.setPayload(result));
            }
        }
            /*PreparedStatement stmt = connection.prepareStatement(query.replaceAll(":[\\w]+", "?"));

            Matcher m = Pattern.compile(":[\\w]+")
                    .matcher(query);
            int i = 1;
            while (m.find()) {
                String paramName = m.group().substring(1);
                Object paramValue = message.get(paramName);
                if (paramValue instanceof String)
                    stmt.setString(i++, (String) paramValue);
                else if (paramValue instanceof Integer)
                    stmt.setInt(i++, (Integer) paramValue);
                else if (paramValue instanceof Double)
                    stmt.setDouble(i++, (Double) paramValue);
                else if (paramValue instanceof Float)
                    stmt.setFloat(i++, (Float) paramValue);
                else if (paramValue instanceof Boolean)
                    stmt.setBoolean(i++, (Boolean) paramValue);
                else if (paramValue instanceof Short)
                    stmt.setShort(i++, (Short) paramValue);
                else
                    stmt.setString(i++, paramValue.toString());
            }

            ResultSet rs = stmt
                    .executeQuery();
            ResultSetMetaData md = rs.getMetaData();
            int columns = md.getColumnCount();
            long extracted = 0;

            if ("oneRecordOneMessage".equals(mode)) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap(columns);
                    for(i=1; i<=columns; ++i){
                        row.put(md.getColumnName(i),rs.getObject(i));
                    }

                    message.setPayload(row);
                    send(message);
                    setStatus(new NodeStatus("extracted " + ++extracted));
                }
            } else {
                ArrayList<ResultSet> rows = new ArrayList<>();

                while (rs.next()) {
                    rows.add(rs);
                    ++extracted;
                }
                setStatus(new NodeStatus("extracted " + extracted));

                send(message.setPayload(rows));
            }*/

    }
}
