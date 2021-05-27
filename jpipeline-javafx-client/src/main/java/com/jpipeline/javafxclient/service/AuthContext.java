package com.jpipeline.javafxclient.service;

import lombok.Getter;
import lombok.Setter;

public class AuthContext {

    @Getter @Setter
    private static String managerHost;

    @Getter @Setter
    private static String username;

    @Getter @Setter
    private static String password;


}
