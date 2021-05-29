package com.jpipeline.javafxclient.service;

import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

@Data
public class JConnection implements Serializable {

    private String hostname;
    private String username;
    private String password;

    public JConnection() {}

    @Override
    public String toString() {
        if (username != null && !username.isEmpty())
            return username + "@" + hostname;
        else
            return hostname;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JConnection that = (JConnection) o;
        return hostname.equals(that.hostname) && Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hostname, username);
    }
}
