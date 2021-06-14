package com.jpipeline.javafxclient.context;

import com.jpipeline.javafxclient.service.JConnection;
import lombok.Getter;
import lombok.Setter;

public class AuthContext {

    @Getter @Setter
    private static JConnection connection;

}
