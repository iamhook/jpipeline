package com.jpipeline.jpipeline.service;

import com.jpipeline.jpipeline.entity.Node;
import com.jpipeline.jpipeline.util.CJson;
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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class NodeSupportService {

    protected static final Logger log = LoggerFactory.getLogger(NodeSupportService.class);

    @Value("${jpipeline.nodesPackage}")
    private String nodesPackage;

    @Autowired
    private ResourceLoader resourceLoader;

    public List<String> getNodeTypes() {
        Reflections reflections = new Reflections(nodesPackage);
        ArrayList<Class<? extends Node>> nodeClasses = new ArrayList<>(reflections.getSubTypesOf(Node.class));
        return nodeClasses.stream().map(Class::getSimpleName).collect(Collectors.toList());
    }

    public List<CJson> getPropertiesByNodeType(Class<? extends Node> nodeClass) {
        try {
            final Resource resource = resourceLoader.getResource("classpath:node-configs/" +
                    nodeClass.getSimpleName() + ".conf.json");
            byte[] configBinaryData = FileCopyUtils.copyToByteArray(resource.getInputStream());
            CJson config = CJson.fromJson(new String(configBinaryData, Charset.defaultCharset()));
            return config.getJsonList("properties");
        } catch (Exception e) {
            log.warn(e.toString());
        }
        return new ArrayList<>();
    }

    @SneakyThrows
    public List<CJson> getPropertiesByNodeType(String type) {
        Class<Node> nodeClass = (Class<Node>) Class.forName(nodesPackage+"."+type);
        return getPropertiesByNodeType(nodeClass);
    }

    @SneakyThrows
    public Object createNew(String type) {
        Class<?> nodeClass = Class.forName(nodesPackage+"."+type);
        Constructor<?> constructor = nodeClass.getConstructor(UUID.class);
        return constructor.newInstance(UUID.randomUUID());
    }

    @SneakyThrows
    public Node fromJson(CJson config) {
        UUID id = UUID.fromString(config.getString("id"));
        String type = config.getString("type");
        Boolean active = config.getBoolean("active");

        Class<Node> nodeClass = (Class<Node>) Class.forName(nodesPackage+"."+type);
        Constructor<Node> constructor = nodeClass.getConstructor(UUID.class);
        Node node = constructor.newInstance(id);

        if (active != null) node.setActive(active);

        CJson properties = node.getProperties();
        CJson propertiesJson = config.getJson("properties");

        List<CJson> propConfigs = getPropertiesByNodeType(nodeClass);
        for (CJson propertyConfig : propConfigs) {
            String propertyName = propertyConfig.getString("name");
            if (propertiesJson.containsKey(propertyName)) {
                properties.put(propertyName, propertiesJson.get(propertyName));
            } else if (propertyConfig.getBoolean("required")) {
                log.error("Property {} is required, but wasn't provided", propertyName);
            }
        }

        return node;
    }

}
