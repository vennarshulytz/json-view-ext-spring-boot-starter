package io.github.vennarshulytz.jsonviewext.core;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.vennarshulytz.jsonviewext.annotation.JsonFilterExt;
import io.github.vennarshulytz.jsonviewext.annotation.JsonViewExt;
import io.github.vennarshulytz.jsonviewext.annotation.Sensitive;
import io.github.vennarshulytz.jsonviewext.config.JsonViewExtProperties;
import io.github.vennarshulytz.jsonviewext.model.FilterContext;
import io.github.vennarshulytz.jsonviewext.model.FilterRule;
import io.github.vennarshulytz.jsonviewext.sensitive.SensitiveType;
import io.github.vennarshulytz.jsonviewext.utils.JsonViewExtUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

/**
 * 过滤规则注册中心，负责解析注解并缓存规则
 *
 * @author vennarshulytz
 * @since 1.0.0
 */
public class FilterRuleRegistry {

    private static final Logger log = LoggerFactory.getLogger(FilterRuleRegistry.class);

    private static final Object NULL_SENTINEL = new Object();

    private final Cache<Method, Object> jsonViewExtCache;

    /**
     * 方法级别的规则缓存
     */
    private final Cache<Method, FilterContext> methodRuleCache;
    private final JsonViewExtProperties properties;

    public FilterRuleRegistry(long cacheMaximumSize) {
        this(new JsonViewExtProperties(cacheMaximumSize));
    }

    public FilterRuleRegistry(JsonViewExtProperties properties) {
        if (properties == null) {
            throw new IllegalArgumentException("properties must not be null");
        }
        this.properties = properties;
        this.jsonViewExtCache = createCache(properties.getCacheMaximumSize());
        this.methodRuleCache = createCache(properties.getCacheMaximumSize());
    }

    /**
     * 解析并缓存方法的过滤规则
     */
    public FilterContext getOrCreateContext(Method method) {
        return methodRuleCache.get(method, this::parseAnnotation);
    }

    /**
     * 解析 @JsonViewExt 注解
     */
    private FilterContext parseAnnotation(Method method) {
        JsonViewExt annotation = getJsonViewExtAnnotation(method);
        if (annotation == null) {
            return FilterContext.EMPTY;
        }

        FilterContext context = new FilterContext(properties);

        List<JsonViewExt> resolve = JsonViewExtUtils.resolve(annotation);
        int size = resolve.size();
        for (int i = size - 1; i >= 0; i--) {

            JsonViewExt jsonViewExt = resolve.get(i);

            // 解析 include 规则（后定义的覆盖先定义的）
            JsonFilterExt[] includes = jsonViewExt.include();
            for (JsonFilterExt filter : includes) {
                FilterRule rule = parseFilterRule(filter, true);
                context.addIncludeRule(rule);
                log.debug("Parsed include rule: {}", rule);
            }

            // 解析 exclude 规则（后定义的覆盖先定义的）
            JsonFilterExt[] excludes = jsonViewExt.exclude();
            for (JsonFilterExt filter : excludes) {
                FilterRule rule = parseFilterRule(filter, false);
                context.addExcludeRule(rule);
                log.debug("Parsed exclude rule: {}", rule);
            }
        }

        return context;
    }

    /**
     * 解析单个 @JsonFilterExt 规则
     */
    private FilterRule parseFilterRule(JsonFilterExt filter, boolean isInclude) {
        Class<?> clazz = filter.clazz();
        String field = filter.field();
        Set<String> props = new HashSet<>(Arrays.asList(filter.props()));

        // 解析敏感字段配置
        Map<String, Class<? extends SensitiveType>> sensitiveProps = new HashMap<>();
        for (Sensitive sensitive : filter.sensitives()) {
            Class<? extends SensitiveType> sensitiveType = sensitive.type();
            for (String prop : sensitive.props()) {
                sensitiveProps.put(prop, sensitiveType);
            }
        }

        return new FilterRule(clazz, field, props, isInclude, sensitiveProps);
    }

    /**
     * 判断方法是否有 @JsonViewExt 注解
     */
    public boolean hasJsonViewExtAnnotation(Method method) {
        Object o = jsonViewExtCache.get(method, FilterRuleRegistry::findJsonViewExtAnnotation);
        return o != NULL_SENTINEL;
    }

    public JsonViewExt getJsonViewExtAnnotation(Method method) {
        Object o = jsonViewExtCache.get(method, FilterRuleRegistry::findJsonViewExtAnnotation);
        return o == NULL_SENTINEL ? null : (JsonViewExt) o;
    }


    /**
     * 从方法、类上获取 @JsonViewExt 注解
     */
    public static Object findJsonViewExtAnnotation(Method method) {

        JsonViewExt jsonViewExt = method.getAnnotation(JsonViewExt.class);
        if (jsonViewExt != null) {
            return jsonViewExt;
        }

        for (Annotation annotation : method.getAnnotations()) {
            jsonViewExt = annotation.annotationType().getAnnotation(JsonViewExt.class);
            if (jsonViewExt != null) {
                return jsonViewExt;
            }
        }
        Class<?> declaringClass = method.getDeclaringClass();
        jsonViewExt = declaringClass.getAnnotation(JsonViewExt.class);
        if (jsonViewExt != null) {
            return jsonViewExt;
        }
        for (Annotation annotation : declaringClass.getAnnotations()) {
            jsonViewExt = annotation.annotationType().getAnnotation(JsonViewExt.class);
            if (jsonViewExt != null) {
                return jsonViewExt;
            }
        }

        return NULL_SENTINEL;
    }


    private static <K1, V1> @NonNull Cache<K1, V1> createCache(long cacheMaximumSize) {
        return Caffeine.newBuilder()
                .maximumSize(cacheMaximumSize)
                .build();
    }

}
