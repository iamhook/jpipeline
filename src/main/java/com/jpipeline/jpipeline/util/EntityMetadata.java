package com.jpipeline.jpipeline.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

}
