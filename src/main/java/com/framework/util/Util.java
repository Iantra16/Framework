package com.framework.util;

import com.framework.annotation.Controller;
import com.framework.annotation.UrlMapping;
import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Util {

    public static List<Class<?>> getAllClasses(String packageName) throws Exception {
        List<Class<?>> classes = new ArrayList<>();
        String path = packageName.replace('.', '/');
        File dir = new File(Thread.currentThread()
                    .getContextClassLoader()
                    .getResource(path).toURI());
        for (File file : dir.listFiles()) {
            if (file.getName().endsWith(".class")) {
                String className = packageName + "."
                    + file.getName().replace(".class", "");
                classes.add(Class.forName(className));
            }
        }
        return classes;
    }

    public static boolean hasAnnotation(Class<?> clazz,
            Class<? extends java.lang.annotation.Annotation> annotation) {
        return clazz.isAnnotationPresent(annotation);
    }

    public static List<String> getControllers(String packageName) throws Exception {
        List<String> controllers = new ArrayList<>();
        for (Class<?> clazz : getAllClasses(packageName)) {
            if (hasAnnotation(clazz, Controller.class)) {
                controllers.add(clazz.getName());
            }
        }
        return controllers;
    }

    public static HashMap<UrlMethod, Mapping> getMappings(String packageName) throws Exception {
        HashMap<UrlMethod, Mapping> urlMappings = new HashMap<>();
        for (Class<?> clazz : getAllClasses(packageName)) {
            if (hasAnnotation(clazz, Controller.class)) {
                for (Method method : clazz.getMethods()) {
                    if (method.isAnnotationPresent(UrlMapping.class)) {
                        
                        String url = method.getAnnotation(UrlMapping.class).value();
                        String httpMethod = method.getAnnotation(UrlMapping.class).method();
                        UrlMethod urlMethod = new UrlMethod(url, httpMethod);

                        // Vérification unicité
                        if (urlMappings.containsKey(urlMethod)) {
                            Mapping existant = urlMappings.get(urlMethod);
                            throw new Exception("URL dupliquée : '" + url 
                                + "' [" + httpMethod + "] déjà enregistrée par "
                                + existant.getClassName() + "." + existant.getMethodName());
                        }

                        urlMappings.put(urlMethod, new Mapping(clazz.getName(), method.getName()));
                    }
                }
            }
        }
        return urlMappings;
    }
}