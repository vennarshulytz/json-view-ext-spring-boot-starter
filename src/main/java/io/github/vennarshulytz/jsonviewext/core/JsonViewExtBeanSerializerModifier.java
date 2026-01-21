package io.github.vennarshulytz.jsonviewext.core;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.github.vennarshulytz.jsonviewext.model.FilterContext;
import io.github.vennarshulytz.jsonviewext.model.FilterRule;
import io.github.vennarshulytz.jsonviewext.sensitive.SensitiveHandler;
import io.github.vennarshulytz.jsonviewext.sensitive.SensitiveType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * 自定义 Bean 序列化修改器，实现字段过滤和脱敏
 *
 * @author vennarshulytz
 * @since 1.0.0
 */
public class JsonViewExtBeanSerializerModifier extends BeanSerializerModifier {

    @Override
    public JsonSerializer<?> modifySerializer(SerializationConfig config,
                                              BeanDescription beanDesc,
                                              JsonSerializer<?> serializer) {
        if (serializer instanceof BeanSerializerBase) {
            return new JsonViewExtBeanSerializer((BeanSerializerBase) serializer, beanDesc.getBeanClass());
        }
        return serializer;
    }

    /**
     * 自定义 Bean 序列化器
     */
    public static class JsonViewExtBeanSerializer extends StdSerializer<Object> {

        private static final Logger log = LoggerFactory.getLogger(JsonViewExtBeanSerializer.class);

        private final BeanSerializerBase defaultSerializer;
        private final Class<?> beanClass;

        public JsonViewExtBeanSerializer(BeanSerializerBase defaultSerializer, Class<?> beanClass) {
            super(Object.class);
            this.defaultSerializer = defaultSerializer;
            this.beanClass = beanClass;
        }

        @Override
        public void serialize(Object value, JsonGenerator gen, SerializerProvider provider)
                throws IOException {
            FilterContext context = JsonViewExtContextHolder.getContext();

            // 如果没有过滤上下文，使用默认序列化
            if (context == null || !context.hasRules()) {
                defaultSerializer.serialize(value, gen, provider);
                return;
            }

            // 获取当前路径
            String currentPath = PathTracker.getCurrentPath();

            // 检查是否有针对当前类的规则
            FilterRule applicableRule = context.getApplicableRule(beanClass, currentPath);

            if (applicableRule == null) {
                // 没有规则，使用默认序列化
                defaultSerializer.serialize(value, gen, provider);
                return;
            }

            // 有规则，进行字段过滤序列化
            serializeWithFilter(value, gen, provider, context, currentPath, applicableRule);
        }

        private void serializeWithFilter(Object value, JsonGenerator gen,
                                         SerializerProvider provider,
                                         FilterContext context,
                                         String currentPath,
                                         FilterRule rule) throws IOException {
            gen.writeStartObject();

            Iterator<PropertyWriter> props = defaultSerializer.properties();
            while (props.hasNext()) {
                PropertyWriter prop = props.next();
                String propName = prop.getName();

                // 检查字段是否应该被序列化
                FilterContext.FieldSerializationResult result = checkFieldSerialization(
                        context, currentPath, propName, rule);

                if (!result.shouldSerialize()) {
                    continue;
                }

                try {
                    Object propValue = getPropertyValue(value, prop);

                    // 处理脱敏
                    if (result.hasSensitiveType() && propValue instanceof String) {
                        String desensitized = SensitiveHandler.desensitize(
                                result.getSensitiveType(), (String) propValue);
                        gen.writeStringField(propName, desensitized);
                        continue;
                    }

                    // 处理嵌套对象
                    if (propValue != null) {
                        String newPath = currentPath.isEmpty() ? propName : currentPath + "." + propName;

                        if (propValue instanceof Collection) {
                            serializeCollection(propName, (Collection<?>) propValue,
                                    gen, provider, newPath);
                        } else if (propValue.getClass().isArray()) {
                            serializeArray(propName, propValue, gen, provider, newPath);
                        } else if (isComplexType(propValue.getClass())) {
                            serializeNestedObject(propName, propValue, gen, provider, newPath);
                        } else {
                            prop.serializeAsField(value, gen, provider);

                        }
                    } else {
                        prop.serializeAsField(value, gen, provider);
                    }
                } catch (Exception e) {
                    log.warn("Error serializing property: {}", propName, e);
                    // 尝试使用默认方式写入
                    try {
                        prop.serializeAsField(value, gen, provider);
                    } catch (Exception ex) {
                        // 忽略无法序列化的字段
                        log.debug("Skipping unserializable property: {}", propName);
                    }
                }
            }

            gen.writeEndObject();
        }

        private FilterContext.FieldSerializationResult checkFieldSerialization(FilterContext context,
                                                                               String currentPath,
                                                                               String propName,
                                                                               FilterRule rule) {
            boolean shouldSerialize;
            Class<? extends SensitiveType> sensitiveType = null;

            if (rule.isInclude()) {
                shouldSerialize = rule.getProps().contains(propName);
                if (shouldSerialize) {
                    sensitiveType = rule.getSensitiveProps().get(propName);
                }
            } else {
                shouldSerialize = !rule.getProps().contains(propName);
                if (shouldSerialize) {
                    sensitiveType = rule.getSensitiveProps().get(propName);
                }
            }

            return new FilterContext.FieldSerializationResult(shouldSerialize, sensitiveType);
        }

        private Object getPropertyValue(Object bean, PropertyWriter prop) throws Exception {
            if (prop instanceof BeanPropertyWriter) {
                BeanPropertyWriter bpw = (BeanPropertyWriter) prop;
                return bpw.get(bean);
            }
            return null;
        }

        private void writeProperty(PropertyWriter prop, String propName, Object propValue,
                                   JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeFieldName(propName);
            provider.defaultSerializeValue(propValue, gen);
        }

        private void serializeCollection(String propName, Collection<?> collection,
                                         JsonGenerator gen, SerializerProvider provider,
                                         String basePath) throws IOException {
            gen.writeFieldName(propName);
            gen.writeStartArray();

            for (Object item : collection) {
                if (item == null) {
                    gen.writeNull();
                } else if (isComplexType(item.getClass())) {
                    // 对于集合元素，路径使用集合属性名
                    PathTracker.pushPath(basePath);
                    try {
                        provider.defaultSerializeValue(item, gen);
                    } finally {
                        PathTracker.popPath();
                    }
                } else {
                    provider.defaultSerializeValue(item, gen);
                }
            }

            gen.writeEndArray();
        }

        private void serializeArray(String propName, Object array,
                                    JsonGenerator gen, SerializerProvider provider,
                                    String basePath) throws IOException {
            gen.writeFieldName(propName);
            gen.writeStartArray();

            int length = java.lang.reflect.Array.getLength(array);
            for (int i = 0; i < length; i++) {
                Object item = java.lang.reflect.Array.get(array, i);
                if (item == null) {
                    gen.writeNull();
                } else if (isComplexType(item.getClass())) {
                    PathTracker.pushPath(basePath);
                    try {
                        provider.defaultSerializeValue(item, gen);
                    } finally {
                        PathTracker.popPath();
                    }
                } else {
                    provider.defaultSerializeValue(item, gen);
                }
            }

            gen.writeEndArray();
        }

        private void serializeNestedObject(String propName, Object value,
                                           JsonGenerator gen, SerializerProvider provider,
                                           String newPath) throws IOException {
            gen.writeFieldName(propName);
            PathTracker.pushPath(newPath);
            try {
                provider.defaultSerializeValue(value, gen);
            } finally {
                PathTracker.popPath();
            }
        }

        private boolean isComplexType(Class<?> clazz) {
            return !clazz.isPrimitive()
                    && !clazz.getName().startsWith("java.lang")
                    && !clazz.getName().startsWith("java.math")
                    && !clazz.getName().startsWith("java.time")
                    && !clazz.isEnum()
                    && !Number.class.isAssignableFrom(clazz)
                    && !CharSequence.class.isAssignableFrom(clazz)
                    && !Date.class.isAssignableFrom(clazz);
        }
    }

    /**
     * 路径追踪器
     */
    public static class PathTracker {
        private static final ThreadLocal<Deque<String>> PATH_STACK =
                ThreadLocal.withInitial(ArrayDeque::new);

        public static void pushPath(String path) {
            PATH_STACK.get().push(path);
        }

        public static String popPath() {
            Deque<String> stack = PATH_STACK.get();
            return stack.isEmpty() ? "" : stack.pop();
        }

        public static String getCurrentPath() {
            Deque<String> stack = PATH_STACK.get();
            return stack.isEmpty() ? "" : stack.peek();
        }

        public static void clear() {
            PATH_STACK.get().clear();
        }

        public static void reset() {
            PATH_STACK.remove();
        }
    }
}
