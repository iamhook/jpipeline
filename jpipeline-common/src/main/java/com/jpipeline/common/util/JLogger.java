package com.jpipeline.common.util;

import com.jpipeline.common.entity.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JLogger {

    private Logger logger;
    private Node node;

    public JLogger(Node node) {
        this.logger = LoggerFactory.getLogger(node.getType() + "@" + node.getId());
        this.node = node;
    }

    public void debug(Object msg) {
        logger.debug(msg.toString());
        sendDebugSignal(msg);
    }

    public void debug(boolean sendToClient, Object msg) {
        logger.debug(msg.toString());
        if (sendToClient) sendDebugSignal(msg);
    }

    public void debug(boolean sendToClient, String format, Object... arguments) {
        logger.debug(format, arguments);
        String msg = String.format(format.replaceAll("\\{}", "%s"), arguments);
        if (sendToClient) sendDebugSignal(msg);
    }

    public void info(Object msg) {
        logger.info(msg.toString());
        sendDebugSignal(msg);
    }

    public void info(String format, Object... arguments) {
        logger.info(format, arguments);
        String msg = String.format(format.replaceAll("\\{}", "%s"), arguments);
        sendDebugSignal(msg);
    }

    public void error(Object msg) {
        logger.error(msg.toString());
        sendErrorSignal(msg);
    }

    public void error(String msg, Throwable t) {
        logger.error(msg, t);
        sendErrorSignal(msg);
    }

    public void error(String format, Object... arguments) {
        logger.error(format, arguments);
        String msg = String.format(format.replaceAll("\\{}", "%s"), arguments);
        sendErrorSignal(msg);
    }

    private void sendDebugSignal(Object message) {
        node.sendSignal(new Node.NodeSignal(Node.SignalType.DEBUG, message, node.getId()));
    }

    private void sendErrorSignal(Object message) {
        node.sendSignal(new Node.NodeSignal(Node.SignalType.ERROR, message, node.getId()));
    }

}
