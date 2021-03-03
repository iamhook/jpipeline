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

import java.lang.reflect.*;
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

        //CJson properties = node.getProperties();
        CJson propertiesJson = nodeDTO.getProperties();

        Map<String, Field> fields = getNodePropertyFields(nodeClass);

        List<PropertyConfig> propConfigs = getPropertyConfigs(nodeClass);

        setPropertyValues(propConfigs, fields, propertiesJson, node);

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

    /*
    * sdfsdf
    *
    * */
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
