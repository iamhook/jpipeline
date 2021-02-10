package com.jpipeline.jpipeline.service;

import com.jpipeline.jpipeline.entity.Node;
import com.jpipeline.jpipeline.util.EntityMetadata;
import com.jpipeline.jpipeline.util.annotations.NodeProperty;
import lombok.SneakyThrows;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NodeSupportService {


    @Value("${jpipeline.nodes-package}")
    private String nodesPackage;

    public List<String> getNodeTypes() {
        Reflections reflections = new Reflections(nodesPackage);
        ArrayList<Class<? extends Node>> nodeClasses = new ArrayList<>(reflections.getSubTypesOf(Node.class));
        return nodeClasses.stream().map(this::getNodeType).collect(Collectors.toList());
    }

    @SneakyThrows
    public List<String> getPropertiesByNodeType(String type) {
        Class<?> nodeClass = Class.forName(nodesPackage+"."+type);
        List<Field> fields = EntityMetadata.findFields(nodeClass);
        return fields.stream()
                .filter(field -> field.getAnnotation(NodeProperty.class) != null)
                .map(Field::getName).collect(Collectors.toList());
    }

    private String getNodeType(Class<? extends Node> clazz) {
        try {
            return (String)clazz.getMethod("getShortType").invoke(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
