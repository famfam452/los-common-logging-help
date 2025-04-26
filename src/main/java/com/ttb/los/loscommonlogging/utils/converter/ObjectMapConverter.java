package com.ttb.los.loscommonlogging.utils.converter;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.lang.reflect.Field;

@Slf4j
public class ObjectMapConverter {
    public static Map<String, String> convert(Object obj) {
        Map<String, String> map = new HashMap<>();
        if (obj == null) {
            return map;
        }

        Class<?> clazz = obj.getClass();
        for (Field field: clazz.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(obj);
                if (value != null) {
                    map.put(field.getName(), value.toString());
                }

            } catch (IllegalAccessException e) {
                log.error("Error : {}", e.getMessage(), e);
            }
        }
        return map;
    }
}
