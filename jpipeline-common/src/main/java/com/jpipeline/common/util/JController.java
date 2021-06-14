package com.jpipeline.common.util;

import com.jpipeline.common.dto.NodeDTO;
import lombok.Setter;

public abstract class JController {

    @Setter
    protected NodeDTO node;
    @Setter
    protected NodeTypeConfig nodeTypeConfig;

    public CJson getNodeProperties() {
        return node.getProperties();
    }

    public abstract void onInit();

    public abstract void onClose();

}
