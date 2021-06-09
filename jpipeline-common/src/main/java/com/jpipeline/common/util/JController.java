package com.jpipeline.common.util;

import com.jpipeline.common.dto.NodeDTO;

public abstract class JController {

    private NodeDTO node;

    public void setNodeDTO(NodeDTO node) {
        this.node = node;
    }

    public CJson getNodeProperties() {
        return node.getProperties();
    }

    public abstract void onInit();

    public abstract void onClose();


}
