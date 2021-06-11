package com.jpipeline.javafxclient.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpipeline.common.util.CJson;
import com.jpipeline.common.util.JController;
import com.jpipeline.javafxclient.service.NodeService;
import com.sun.javafx.webkit.WebConsoleListener;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebView;
import lombok.Setter;
import netscape.javascript.JSObject;

import java.io.File;

public class HtmlNodeEditController extends JController {

    private static final ObjectMapper OM = new ObjectMapper();

    static {
        com.sun.javafx.webkit.WebConsoleListener.setDefaultListener((view, message, lineNumber, sourceId) -> {
            System.out.println(message + "[at " + lineNumber + "]");
        });
    }

    @Setter
    private Pane rootPane;

    @FXML
    public WebView webView;

    private JSObject bridge;

    @Override
    public void onInit() {
        CJson nodeJson = OM.convertValue(node, CJson.class);
        CJson nodeConfigJson = OM.convertValue(nodeConfig, CJson.class);

        //String htmlPath = NodeService.getNodeHtml(node.getType());
        String htmlPath = "tmp/SwitchNode.html";

        bridge = (JSObject) webView.getEngine()
                .executeScript("window");

        bridge.setMember("node", nodeJson.toJson());
        bridge.setMember("nodeConfig", nodeConfigJson.toJson());
        webView.getEngine().executeScript("node = JSON.parse(node)");
        webView.getEngine().executeScript("nodeConfig = JSON.parse(nodeConfig)");

        webView.getEngine().load(new File(htmlPath).toURI().toString());

    }

    @Override
    public void onClose() {
        CJson nodeJson = CJson.fromJson((String)webView.getEngine().executeScript("JSON.stringify(node)"));

        node.setProperties(nodeJson.getJson("properties"));

        return;
    }
}
