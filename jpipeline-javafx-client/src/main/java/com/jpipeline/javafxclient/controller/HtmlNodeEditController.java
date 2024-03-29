package com.jpipeline.javafxclient.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.jpipeline.common.util.CJson;
import com.jpipeline.common.util.JController;
import com.jpipeline.javafxclient.service.NodeService;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebView;
import lombok.Setter;
import netscape.javascript.JSObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class HtmlNodeEditController extends JController {

    private static final Logger log = LoggerFactory.getLogger(HtmlNodeEditController.class);

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
        CJson nodeConfigJson = OM.convertValue(nodeTypeConfig, CJson.class);

        String htmlPath = NodeService.getNodeHtml(node.getType());
        //String htmlPath = JContext.getExtResourcesFolder() + node.getType() + ".html";

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
        try {
            CJson nodeJson = CJson.fromJson((String)webView.getEngine().executeScript("JSON.stringify(node)"));

            ObjectReader objectReader = OM.readerForUpdating(node);
            objectReader.readValue(nodeJson.toJson());

            node.modelChanged();
        } catch (Exception e) {
            log.error(e.toString(), e);
        }

        return;
    }
}
