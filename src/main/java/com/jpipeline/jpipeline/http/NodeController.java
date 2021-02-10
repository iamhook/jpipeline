package com.jpipeline.jpipeline.http;

import com.jpipeline.jpipeline.service.NodeSupportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/node")
public class NodeController {

    @Autowired
    private NodeSupportService nodeSupportService;

    @GetMapping("/types")
    public List<String> getTypes() {
        return nodeSupportService.getNodeTypes();
    }

    @GetMapping("/{type}/properties")
    public List<String> getProperties(@PathVariable String type) throws ClassNotFoundException {
        return nodeSupportService.getPropertiesByNodeType(type);
    }



}
