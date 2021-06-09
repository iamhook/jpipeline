package com.jpipeline.common.util;

import com.jpipeline.common.dto.NodeDTO;

public abstract class JController {

    protected NodeDTO node;
    protected NodeConfig nodeConfig;

    public void setNode(NodeDTO node) {
        this.node = node;
    }

    public void setNodeConfig(NodeConfig nodeConfig) {
        this.nodeConfig = nodeConfig;
    }

    public NodeConfig getNodeConfig() {
        return nodeConfig;
    }

    public CJson getNodeProperties() {
        return node.getProperties();
    }

    public abstract void onInit();

    public abstract void onClose();


}
