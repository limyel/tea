package com.limyel.tea.core.io;

import com.limyel.tea.core.util.ClassPathUtil;
import com.limyel.tea.ioc.util.BeanContainerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.*;
import java.util.*;
import java.util.function.Function;

/**
 * 配置解析器，用于扫描配置文件
 */
public class PropertyResolver {

    private static volatile PropertyResolver INSTANCE;

    private static Logger logger = LoggerFactory.getLogger(PropertyResolver.class);

    public static String CONFIG_PATH = "/tea.properties";

    private Map<String, String> properties = new HashMap<>();
    private Map<Class<?>, Function<String, Object>> converters = new HashMap<>();

    private PropertyResolver() {
        Properties props = new Properties();
        ClassPathUtil.readInputStream(CONFIG_PATH, is -> {
            logger.info("load properties: {}", CONFIG_PATH);
            props.load(is);
            return true;
        });

        // 系统参数
        properties.putAll(System.getenv());
        Set<String> names = props.stringPropertyNames();
        for (String name: names) {
            properties.put(name, props.getProperty(name));
        }
        // 注册 converters
        // todo 剥离
        converters.put(String.class, s -> s);
        converters.put(boolean.class, Boolean::parseBoolean);
        converters.put(Boolean.class, Boolean::valueOf);
        converters.put(byte.class, Byte::parseByte);
        converters.put(Byte.class, Byte::valueOf);
        converters.put(short.class, Short::parseShort);
        converters.put(Short.class, Short::valueOf);
        converters.put(int.class, Integer::parseInt);
        converters.put(Integer.class, Integer::valueOf);
        converters.put(long.class, Long::parseLong);
        converters.put(Long.class, Long::valueOf);
        converters.put(float.class, Float::parseFloat);
        converters.put(Float.class, Float::valueOf);
        converters.put(double.class, Double::parseDouble);
        converters.put(Double.class, Double::valueOf);
        converters.put(LocalDate.class, LocalDate::parse);
        converters.put(LocalTime.class, LocalTime::parse);
        converters.put(LocalDateTime.class, LocalDateTime::parse);
        converters.put(ZonedDateTime.class, ZonedDateTime::parse);
        converters.put(Duration.class, Duration::parse);
        converters.put(ZoneId.class, ZoneId::of);

        BeanContainerUtil.setPropertyResolver(this);
    }

    public static PropertyResolver getInstance() {
        if (INSTANCE == null) {
            synchronized (PropertyResolver.class) {
                if (INSTANCE == null) {
                    INSTANCE = new PropertyResolver();
                }
            }
        }
        return INSTANCE;
    }

    public boolean containsProperty(String key) {
        return properties.containsKey(key);
    }

    public String getProperty(String key) {
        PropertyExpr expr = parsePropertyExpr(key);
        if (expr != null) {
            if (expr.defaultValue() != null) {
                return getProperty(expr.key(), expr.defaultValue());
            } else {
                return getRequiredProperty(expr.key());
            }
        }
        String value = this.properties.get(key);
        if (value != null) {
            return parseValue(value);
        }
        return value;
    }

    public String getProperty(String key, String defaultValue) {
        String value = getProperty(key);
        return value == null ? parseValue(defaultValue) : value;
    }

    public String getRequiredProperty(String key) {
        String value = getProperty(key);
        return Objects.requireNonNull(value, "Property '" + key + "' not found.");
    }

    public <T> T getRequiredProperty(String key, Class<T> clazz) {
        T value = getProperty(key, clazz);
        return Objects.requireNonNull(value, "Property '" + key + "' not found.");
    }

    public <T> T getProperty(String key, Class<T> clazz) {
        String value = getProperty(key);
        if (value == null) {
            return null;
        }
        return convert(clazz, value);
    }

    public <T> T getProperty(String key, Class<T> clazz, T defaultValue) {
        String value = getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        return convert(clazz, value);
    }

    private <T> T convert(Class<?> clazz, String value) {
        Function<String, Object> fn = converters.get(clazz);
        if (fn == null) {
            throw new IllegalArgumentException("Unsupported value type: " + clazz.getName());
        }
        return (T) fn.apply(value);
    }

    private String parseValue(String value) {
        PropertyExpr expr = parsePropertyExpr(value);
        if (expr == null) {
            return value;
        }
        if (expr.defaultValue() != null) {
            return getProperty(expr.key(), expr.defaultValue());
        } else {
            return getRequiredProperty(expr.key());
        }
    }

    private PropertyExpr parsePropertyExpr(String key) {
        if (key.startsWith("${") && key.endsWith("}")) {
            int index = key.indexOf(":");
            if (index > -1) {
                return new PropertyExpr(key.substring(2, index), key.substring(index + 1, key.length() - 1));
            } else {
                return new PropertyExpr(key.substring(2, key.length() - 1), null);
            }
        }
        return null;
    }

    String notEmpty(String key) {
        if (key.isEmpty()) {
            throw new IllegalArgumentException("Invalid key: " + key);
        }
        return key;
    }
}

record PropertyExpr(String key, String defaultValue) {
}