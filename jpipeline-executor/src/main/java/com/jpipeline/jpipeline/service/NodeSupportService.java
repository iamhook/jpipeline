package com.jpipeline.jpipeline.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpipeline.jpipeline.PropertyConfig;
import com.jpipeline.jpipeline.dto.NodeDTO;
import com.jpipeline.jpipeline.entity.Node;
import com.jpipeline.jpipeline.util.CJson;
import com.jpipeline.jpipeline.util.EntityMetadata;
import com.jpipeline.jpipeline.util.annotations.NodeProperty;
import lombok.SneakyThrows;
import org.reflections8.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NodeSupportService {

    protected static final Logger log = LoggerFactory.getLogger(NodeSupportService.class);

    private static final ObjectMapper OM = new ObjectMapper();

    @Value("${jpipeline.nodesPackage}")
    private String nodesPackage;

    @Autowired
    private ResourceLoader resourceLoader;

    public List<String> getNodeTypes() {
        Reflections reflections = new Reflections(nodesPackage);
        ArrayList<Class<? extends Node>> nodeClasses = new ArrayList<>(reflections.getSubTypesOf(Node.class));
        return nodeClasses.stream().map(Class::getSimpleName).collect(Collectors.toList());
    }

    public Map<String, Field> getNodePropertyFields(Class<? extends Node> nodeClass) {
        return EntityMetadata.findFields(nodeClass).stream()
                .filter(f -> f.getAnnotation(NodeProperty.class) != null)
                .collect(Collectors.toMap(Field::getName, f -> f));
    }

    public List<PropertyConfig> getPropertyConfigs(Class<? extends Node> nodeClass) {
        try {
            final Resource resource = resourceLoader.getResource("classpath:node-configs/" +
                    nodeClass.getSimpleName() + ".conf.json");
            byte[] configBinaryData = FileCopyUtils.copyToByteArray(resource.getInputStream());
            CJson config = CJson.fromJson(new String(configBinaryData, Charset.defaultCharset()));
            return config.getJsonList("properties").stream()
                    .map(json -> {
                        try {
                            return OM.readValue(json.toJson(), PropertyConfig.class);
                        } catch (JsonProcessingException e) {
                            log.error(e.getMessage(), e);
                            return null;
                        }
                    }).filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn(e.toString());
        }
        return new ArrayList<>();
    }

    @SneakyThrows
    public List<PropertyConfig> getPropertyConfigs(String type) {
        Class<Node> nodeClass = (Class<Node>) Class.forName(nodesPackage+"."+type);
        return getPropertyConfigs(nodeClass);
    }

    @SneakyThrows
    public NodeDTO createNew(String type) {
        Class<? extends Node> nodeClass = (Class<? extends Node>) Class.forName(nodesPackage+"."+type);
        Constructor<? extends Node> constructor = nodeClass.getConstructor(UUID.class);
        Node node = constructor.newInstance(UUID.randomUUID());
        CJson properties = new CJson();
        List<PropertyConfig> propertyConfigs = getPropertyConfigs(nodeClass);
        propertyConfigs.forEach(config -> {
            String name = config.getName();
            Object defaultValue = config.getDefaultValue();
            properties.put(name, defaultValue);
        });
        return NodeDTO.builder()
                .id(node.getId().toString())
                .type(node.getType())
                .active(true)
                .properties(properties)
                .wires(new ArrayList<>())
                .build();
    }

    @SneakyThrows
    public Node fromDTO(NodeDTO nodeDTO) {
        UUID id = UUID.fromString(nodeDTO.getId());
        String type = nodeDTO.getType();
        Boolean active = nodeDTO.getActive();

        Class<Node> nodeClass = (Class<Node>) Class.forName(nodesPackage+"."+type);
        Constructor<Node> constructor = nodeClass.getConstructor(UUID.class);
        Node node = constructor.newInstance(id);

        if (active != null) node.setActive(active);

        CJson properties = node.getProperties();
        CJson propertiesJson = nodeDTO.getProperties();

        Map<String, Field> fields = getNodePropertyFields(nodeClass);

        List<PropertyConfig> propConfigs = getPropertyConfigs(nodeClass);
        for (PropertyConfig propertyConfig : propConfigs) {
            String propertyName = propertyConfig.getName();
            if (propertiesJson.containsKey(propertyName)) {
                properties.put(propertyName, propertiesJson.get(propertyName));
            } else if (propertyConfig.isRequired()) {
                log.error("Property {} is required, but wasn't provided", propertyName);
            }
            if (fields.containsKey(propertyName)) {
                Field field = fields.get(propertyName);
                setPropertyValue(node, propertyName, field, propertiesJson, propertyConfig);
            }
        }

        return node;
    }

    private void setPropertyValue(Node node, String propertyName, Field field, CJson propertiesJson, PropertyConfig propertyConfig) {
        try {
            field.setAccessible(true);
            if (propertyConfig.isComplex()) {
                field.set(node, OM.readValue(propertiesJson.getJson(propertyName).toJson(), field.getType()));
            } else if (propertyConfig.isString()) {
                field.set(node, propertiesJson.getString(propertyName));
            } else {
                field.set(node, OM.readValue(propertiesJson.getBytes(propertyName), field.getType()));
            }

        } catch (Exception e) {
            log.error(e.toString());
        }
    }

}
