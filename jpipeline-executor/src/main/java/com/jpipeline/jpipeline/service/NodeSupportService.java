package com.jpipeline.jpipeline.service;

import com.jpipeline.jpipeline.entity.Node;

import com.jpipeline.jpipeline.util.CJson;
import com.jpipeline.jpipeline.util.EntityMetadata;
import com.jpipeline.jpipeline.util.annotations.NodeButton;
import com.jpipeline.jpipeline.util.annotations.NodeProperty;
import lombok.SneakyThrows;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class NodeSupportService {

    @Value("${jpipeline.nodes-package}")
    private String nodesPackage;

    public List<String> getNodeTypes() {
        Reflections reflections = new Reflections(nodesPackage);
        ArrayList<Class<? extends Node>> nodeClasses = new ArrayList<>(reflections.getSubTypesOf(Node.class));
        return nodeClasses.stream().map(Class::getSimpleName).collect(Collectors.toList());
    }

    public List<Field> getPropertiesByNodeType(Class<? extends Node> nodeClass) {
        List<Field> fields = EntityMetadata.findFields(nodeClass);
        return fields.stream()
                .filter(field -> field.getAnnotation(NodeProperty.class) != null)
                .collect(Collectors.toList());
    }

    @SneakyThrows
    public List<String> getPropertyNamesByNodeType(Class<? extends Node> nodeClass) {
        return getPropertiesByNodeType(nodeClass).stream().map(Field::getName).collect(Collectors.toList());
    }

    @SneakyThrows
    public List<String> getPropertyNamesByNodeType(String type) {
        Class<Node> nodeClass = (Class<Node>) Class.forName(nodesPackage+"."+type);
        return getPropertyNamesByNodeType(nodeClass);
    }

    public List<Method> getButtonsByNodeType(Class<? extends Node> nodeClass) {
        List<Method> methods = EntityMetadata.findMethods(nodeClass);
        return methods.stream()
                .filter(field -> field.getAnnotation(NodeButton.class) != null)
                .collect(Collectors.toList());
    }

    @SneakyThrows
    public List<String> getButtonNamesByNodeType(Class<? extends Node> nodeClass) {
        return getButtonsByNodeType(nodeClass).stream().map(Method::getName).collect(Collectors.toList());
    }

    @SneakyThrows
    public List<String> getButtonNamesByNodeType(String type) {
        Class<Node> nodeClass = (Class<Node>) Class.forName(nodesPackage+"."+type);
        return getButtonNamesByNodeType(nodeClass);
    }

    @SneakyThrows
    public Object createNew(String type) {
        Class<?> nodeClass = Class.forName(nodesPackage+"."+type);
        Constructor<?> constructor = nodeClass.getConstructor(UUID.class);
        return constructor.newInstance(UUID.randomUUID());
    }

    @SneakyThrows
    public Node fromJson(CJson json) {
        UUID id = UUID.fromString(json.getString("id"));
        String type = json.getString("type");
        Boolean active = json.getBoolean("active");
        List<String> wires = json.getList("wires");

        Class<Node> nodeClass = (Class<Node>) Class.forName(nodesPackage+"."+type);
        Constructor<Node> constructor = nodeClass.getConstructor(UUID.class);
        Node node = constructor.newInstance(id);

        if (active != null) node.setActive(active);
        node.setWires(wires.stream().map(UUID::fromString).collect(Collectors.toSet()));

        List<Field> fields = getPropertiesByNodeType(nodeClass);
        for (Field field : fields) {
            String fieldName = field.getName();
            if (json.containsKey(fieldName)) {
                field.setAccessible(true);
                field.set(node, json.get(fieldName));
            }
        }

        return node;
    }

}
