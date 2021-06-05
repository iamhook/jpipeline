package com.jpipeline.jpipeline.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpipeline.common.util.NodeConfig;
import com.jpipeline.common.util.PropertyConfig;
import com.jpipeline.common.dto.NodeDTO;
import com.jpipeline.common.entity.Node;
import com.jpipeline.common.util.CJson;
import com.jpipeline.common.util.EntityMetadata;
import com.jpipeline.common.util.annotations.NodeProperty;
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

import java.lang.reflect.*;
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

    private NodeConfig getNodeConfig(Class<? extends Node> nodeClass) {
        try {
            final Resource resource = resourceLoader.getResource("classpath:node-configs/" +
                    nodeClass.getSimpleName() + ".conf.json");
            byte[] configBinaryData = FileCopyUtils.copyToByteArray(resource.getInputStream());
            return OM.readValue(configBinaryData, NodeConfig.class);
        } catch (Exception e) {
            log.error(e.toString(), e);
            return null;
        }

    }

    private Resource getNodeFxml(Class<? extends Node> nodeClass) {
        try {
            final Resource resource = resourceLoader.getResource("classpath:node-fxml/" +
                    nodeClass.getSimpleName() + ".fxml");
            return resource;
        } catch (Exception e) {
            log.error(e.toString(), e);
            return null;
        }

    }

    @SneakyThrows
    public NodeConfig getNodeConfig(String type) {
        Class<Node> nodeClass = (Class<Node>) Class.forName(nodesPackage+"."+type);
        return getNodeConfig(nodeClass);
    }

    @SneakyThrows
    public Resource getNodeFxml(String type) {
        Class<Node> nodeClass = (Class<Node>) Class.forName(nodesPackage+"."+type);
        return getNodeFxml(nodeClass);
    }

    @SneakyThrows
    public NodeDTO createNew(String type) {
        Class<? extends Node> nodeClass = (Class<? extends Node>) Class.forName(nodesPackage+"."+type);
        NodeConfig nodeConfig = getNodeConfig(nodeClass);
        Constructor<? extends Node> constructor = nodeClass.getConstructor(UUID.class);
        Node node = constructor.newInstance(UUID.randomUUID());
        CJson properties = new CJson();
        nodeConfig.getProperties().forEach(config -> {
            String name = config.getName();
            Object defaultValue = config.getDefaultValue();
            properties.put(name, defaultValue);
        });
        return NodeDTO.builder()
                .id(node.getId().toString())
                .type(node.getType())
                .active(true)
                .color(nodeConfig.getColor())
                .properties(properties)
                .wires(new HashSet<>())
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

        CJson propertiesJson = nodeDTO.getProperties();

        Map<String, Field> fields = getNodePropertyFields(nodeClass);

        NodeConfig nodeConfig = getNodeConfig(nodeClass);

        setPropertyValues(nodeConfig.getProperties(), fields, propertiesJson, node);

        return node;
    }

    private void setPropertyValues(List<PropertyConfig> propConfigs, Map<String, Field> objectFields, CJson propertiesJson, Object object) {
        for (PropertyConfig propertyConfig : propConfigs) {
            String propertyName = propertyConfig.getName();
            // TODO check if required
            if (objectFields.containsKey(propertyName)) {
                Field field = objectFields.get(propertyName);
                setPropertyValue(object, propertyName, field, propertiesJson, propertyConfig);
            }
        }
    }

    private void setPropertyValue(Object object, String propertyName, Field field, CJson propertiesJson, PropertyConfig propertyConfig) {
        try {
            field.setAccessible(true);

            if (propertyConfig.isMultiple()) {
                List<Object> values = propertiesJson.getList(propertyName);
                Class fieldGenericType = (Class) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                if (propertyConfig.isComplex()) {
                    field.set(object, values.stream()
                            .map(o -> createComplexFieldValue(fieldGenericType, new CJson((Map)o), propertyConfig))
                            .filter(Objects::nonNull).collect(Collectors.toList()));
                } else if (propertyConfig.isString()){
                    field.set(object, propertiesJson.getList(propertyName));
                } else {
                    List<Object> list = new ArrayList<>();
                    for (Object value : values) {
                        list.add(OM.readValue(value.toString(), fieldGenericType));
                    }
                    field.set(object, list);
                }
            } else {
                if (propertyConfig.isComplex()) {
                    CJson propertyJson = propertiesJson.getJson(propertyName);
                    Object complexFieldValue = createComplexFieldValue(field.getType(), propertyJson, propertyConfig);
                    field.set(object, complexFieldValue);
                } else if (propertyConfig.isString()) {
                    field.set(object, propertiesJson.getString(propertyName));
                } else {
                    field.set(object, OM.readValue(propertiesJson.getString(propertyName), field.getType()));
                }
            }

        } catch (Exception e) {
            log.error(e.toString());
        }
    }

    private Object createComplexFieldValue(Class fieldType, CJson propertyJson, PropertyConfig propertyConfig) {
        try {
            Map<String, Field> nestedFields = EntityMetadata.findFields(fieldType).stream()
                    .collect(Collectors.toMap(Field::getName, f -> f));
            Constructor<?> fieldConstructor = fieldType.getConstructor();
            fieldConstructor.setAccessible(true);
            Object fieldValue = fieldConstructor.newInstance();

            List<PropertyConfig> nestedPropertyConfigs = propertyConfig.getNested();

            setPropertyValues(nestedPropertyConfigs, nestedFields, propertyJson, fieldValue);

            return fieldValue;
        } catch (NoSuchMethodException e) {
            log.error("Complex field should have explicit no-args constructor: {}", e.toString());
        } catch (Exception e) {
            log.error(e.toString());
        }
        return null;
    }

}
