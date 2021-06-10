package com.jpipeline.common.util;

import com.jpipeline.common.dto.NodeDTO;
import lombok.Setter;

public abstract class JController {

    @Setter
    protected Runnable addOutputCallback;
    @Setter
    protected NodeDTO node;
    @Setter
    protected NodeConfig nodeConfig;

    public CJson getNodeProperties() {
        return node.getProperties();
    }

    public abstract void onInit();

    public abstract void onClose();

    public void addOutput() {
        addOutputCallback.run();
    }

}
