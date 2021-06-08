package com.jpipeline.common.util;

import com.jpipeline.common.entity.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Sinks;

public class JLogger {

    private Logger logger;
    private Node node;

    public JLogger(Node node) {
        this.logger = LoggerFactory.getLogger(node.getType() + "@" + node.getId());
        this.node = node;
    }

    public void debug(String msg) {
        logger.debug(msg);
    }

    public void debug(String format, Object... arguments) {
        logger.debug(format, arguments);
    }

    public void info(String msg) {
        logger.info(msg);
    }

    public void error(String msg) {
        logger.error(msg);
        sendErrorSignal(msg);
    }

    public void error(String msg, Throwable t) {
        logger.error(msg, t);
        sendErrorSignal(msg);
    }

    public void error(String format, Object... arguments) {
        logger.error(format, arguments);
        // TODO fix
        sendErrorSignal(format);
    }

    private void sendErrorSignal(String error) {
        node.sendSignal(new Node.NodeSignal(Node.SignalType.ERROR, error, node.getId()));
    }

}
