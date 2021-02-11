package com.jpipeline.jpipeline.http;

import com.jpipeline.jpipeline.service.NodeSupportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/nodesupport")
public class NodeSupportController {

    @Autowired
    private NodeSupportService nodeSupportService;

    @GetMapping("/types")
    public List<String> getTypes() {
        return nodeSupportService.getNodeTypes();
    }

    @GetMapping("/{type}/properties")
    public List<String> getProperties(@PathVariable String type) {
        return nodeSupportService.getPropertyNamesByNodeType(type);
    }

    @GetMapping("/{type}/buttons")
    public List<String> getButtons(@PathVariable String type) {
        return nodeSupportService.getButtonNamesByNodeType(type);
    }

    @GetMapping("/{type}/create")
    public Object createNew(@PathVariable String type) {
        return nodeSupportService.createNew(type);
    }


}
