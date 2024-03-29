package com.jpipeline.jpipeline.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpipeline.common.util.NodeTypeConfig;
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
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import reactor.core.publisher.Sinks;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class NodeSupportService {

    protected static final Logger log = LoggerFactory.getLogger(NodeSupportService.class);

    private static final ObjectMapper OM = new ObjectMapper();

    private static final String nodesPackage = "com.jpipeline.entity";

    private static final Reflections reflections = new Reflections(nodesPackage);


    @Autowired
    private ResourceLoader resourceLoader;

    private Class<? extends Node> findClass(String type) {
        return reflections.getSubTypesOf(Node.class).stream()
                .filter(aClass -> aClass.getSimpleName().equals(type))
                .findFirst().orElse(null);
    }

    public List<String> getNodeTypes() {
        ArrayList<Class<? extends Node>> nodeClasses = new ArrayList<>(reflections.getSubTypesOf(Node.class));
        return nodeClasses.stream().map(Class::getSimpleName).collect(Collectors.toList());
    }

    public Map<String, Field> getNodePropertyFields(Class<? extends Node> nodeClass) {
        return EntityMetadata.findFields(nodeClass).stream()
                .filter(f -> f.getAnnotation(NodeProperty.class) != null)
                .collect(Collectors.toMap(Field::getName, f -> f));
    }

    private Resource getNodeResource(String location) {
        try {
            final Resource resource = resourceLoader.getResource("classpath:node-resources/" + location);
            return resource;
        } catch (Exception e) {
            log.error(e.toString(), e);
            return null;
        }
    }

    private NodeTypeConfig getNodeConfig(Class<? extends Node> nodeClass) {
        try {
            final Resource resource = getNodeResource(nodeClass.getSimpleName() + "/" + nodeClass.getSimpleName() + ".conf.json");
            byte[] configBinaryData = FileCopyUtils.copyToByteArray(resource.getInputStream());
            NodeTypeConfig nodeTypeConfig = OM.readValue(configBinaryData, NodeTypeConfig.class);
            nodeTypeConfig.setName(nodeClass.getSimpleName());
            return nodeTypeConfig;
        } catch (Exception e) {
            log.error(e.toString(), e);
            return null;
        }

    }

    private Resource getNodeFxml(Class<? extends Node> nodeClass) {
        return getNodeResource(nodeClass.getSimpleName() + "/" +
                nodeClass.getSimpleName() + ".fxml");
    }

    private Resource getNodeHtml(Class<? extends Node> nodeClass) {
        return getNodeResource(nodeClass.getSimpleName() + "/" +
                nodeClass.getSimpleName() + ".html");
    }

    private Resource getNodeGroovyController(Class<? extends Node> nodeClass) {
        return getNodeResource(nodeClass.getSimpleName() + "/" +
                nodeClass.getSimpleName() + "Controller.groovy");
    }

    @SneakyThrows
    public NodeTypeConfig getNodeConfig(String type) {
        Class<? extends Node> nodeClass = findClass(type);
        return getNodeConfig(nodeClass);
    }

    @SneakyThrows
    public Resource getNodeFxml(String type) {
        Class<? extends Node> nodeClass = findClass(type);
        return getNodeFxml(nodeClass);
    }

    @SneakyThrows
    public Resource getNodeHtml(String type) {
        Class<? extends Node> nodeClass = findClass(type);
        return getNodeHtml(nodeClass);
    }

    @SneakyThrows
    public Resource getNodeGroovyController(String type) {
        Class<? extends Node> nodeClass = findClass(type);
        return getNodeGroovyController(nodeClass);
    }

    @SneakyThrows
    public NodeDTO createNew(String type) {
        Class<? extends Node> nodeClass = findClass(type);
        NodeTypeConfig nodeTypeConfig = getNodeConfig(nodeClass);
        Constructor<? extends Node> constructor = nodeClass.getConstructor(UUID.class);
        Node node = constructor.newInstance(UUID.randomUUID());
        CJson properties = new CJson();
        nodeTypeConfig.getProperties().forEach(config -> {
            String name = config.getName();
            Object defaultValue = config.getDefaultValue();
            properties.put(name, defaultValue);
        });
        List<Set<String>> outputs = new ArrayList<>();
        IntStream.range(0, nodeTypeConfig.getOutputs()).forEach(value -> outputs.add(new HashSet<>()));
        return NodeDTO.builder()
                .id(node.getId().toString())
                .type(node.getType())
                .active(true)
                .color(nodeTypeConfig.getColor())
                .properties(properties)
                .outputs(outputs)
                .hasButton(nodeTypeConfig.hasButton())
                .hasInput(nodeTypeConfig.getInputs() > 0)
                .build();
    }

    @SneakyThrows
    public Node fromDTO(NodeDTO nodeDTO) {
        UUID id = UUID.fromString(nodeDTO.getId());
        String type = nodeDTO.getType();
        Boolean active = nodeDTO.getActive();

        Class<? extends Node> nodeClass = findClass(type);
        Constructor<? extends Node> constructor = nodeClass.getConstructor(UUID.class);
        Node node = constructor.newInstance(id);

        if (active != null) node.setActive(active);

        CJson propertiesJson = nodeDTO.getProperties();

        Map<String, Field> fields = getNodePropertyFields(nodeClass);

        NodeTypeConfig nodeTypeConfig = getNodeConfig(nodeClass);

        setPropertyValues(nodeTypeConfig.getProperties(), fields, propertiesJson, node);

        for (Set<String> output : nodeDTO.getOutputs()) {
            node.getSinks().add(Sinks.many().multicast().onBackpressureBuffer());
        }

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
            log.error(e.toString(), e);
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
            log.error(e.toString(), e);
        }
        return null;
    }

}
