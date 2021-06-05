package com.jpipeline.javafxclient.service;

import lombok.Data;

import java.io.Serializable;

@Data
public class JConnection implements Serializable {

    private String hostname;
    private Integer port;
    private String username;
    private String password;

    public JConnection() {}

    @Override
    public String toString() {
        if (username != null && !username.isEmpty())
            return username + "@" + hostname + (port == null? "" : ":" + port);
        else
            return hostname + (port == null? "" : ":" + port);
    }

}
