package com.jpipeline.jpipeline.util;

import com.jpipeline.jpipeline.entity.Node;
import com.jpipeline.jpipeline.util.annotations.NodeProperty;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EntityMetadata {

    public static List<Field> findFields(Class clazz) {
        List<Field> fields = new ArrayList<>();
        Class currentClass = clazz;
        while (currentClass != null) {

            Field[] declaredFields = currentClass.getDeclaredFields();

            fields.addAll(Arrays.asList(declaredFields));

            currentClass = currentClass.getSuperclass();
        }
        return fields;
    }

    public static List<Method> findMethods(Class clazz) {
        List<Method> methods = new ArrayList<>();
        Class currentClass = clazz;
        while (currentClass != null) {

            Method[] declaredMethods = currentClass.getDeclaredMethods();

            methods.addAll(Arrays.asList(declaredMethods));

            currentClass = currentClass.getSuperclass();
        }
        return methods;
    }

    public static List<NodePropertyConfig> getPropertyConfigs(Class<? extends Node> nodeClass) {
        List<Field> nodePropertyFields = EntityMetadata.findFields(nodeClass).stream()
                .filter(field -> field.getAnnotation(NodeProperty.class) != null)
                .collect(Collectors.toList());

        return nodePropertyFields.stream().map(field -> {
            Class<?> fieldClass = field.getType();
            NodeProperty nodeProperty = field.getAnnotation(NodeProperty.class);
            return new NodePropertyConfig(field.getName(), fieldClass, nodeProperty._default(), nodeProperty.required());
        }).collect(Collectors.toList());
    }

}
