package com.jpipeline.jpipeline.http;

import com.jpipeline.jpipeline.entity.Node;
import org.reflections.Reflections;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/node")
public class NodeController {

    @GetMapping("/types")
    public List<String> getTypes() {
        Reflections reflections = new Reflections("com.jpipeline.jpipeline");
        ArrayList<Class<? extends Node>> nodeClasses = new ArrayList<>(reflections.getSubTypesOf(Node.class));
        return nodeClasses.stream().map(this::getNodeType).collect(Collectors.toList());
    }

    private String getNodeType(Class<? extends Node> clazz) {
        try {
            return (String)clazz.getMethod("getType").invoke(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
