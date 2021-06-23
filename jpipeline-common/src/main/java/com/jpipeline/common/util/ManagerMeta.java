package com.jpipeline.common.util;

import lombok.*;

@Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor
public class ManagerMeta {
    private int rsocketPort;
    private String jwtCookieName;
}
