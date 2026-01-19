package io.github.vennarshulytz.jsonviewext.core;

import io.github.vennarshulytz.jsonviewext.annotation.JsonFilterExt;
import io.github.vennarshulytz.jsonviewext.annotation.JsonViewExt;
import io.github.vennarshulytz.jsonviewext.annotation.Sensitive;
import io.github.vennarshulytz.jsonviewext.model.FilterContext;
import io.github.vennarshulytz.jsonviewext.model.FilterRule;
import io.github.vennarshulytz.jsonviewext.sensitive.SensitiveType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 过滤规则注册中心，负责解析注解并缓存规则
 *
 * @author vennarshulytz
 * @since 1.0.0
 */
public class FilterRuleRegistry {

    private static final Logger log = LoggerFactory.getLogger(FilterRuleRegistry.class);

    /**
     * 方法级别的规则缓存
     */
    private final Map<Method, FilterContext> methodRuleCache = new ConcurrentHashMap<>();

    /**
     * 解析并缓存方法的过滤规则
     */
    public FilterContext getOrCreateContext(Method method) {
        return methodRuleCache.computeIfAbsent(method, this::parseAnnotation);
    }

    /**
     * 解析 @JsonViewExt 注解
     */
    private FilterContext parseAnnotation(Method method) {
        JsonViewExt annotation = method.getAnnotation(JsonViewExt.class);
        if (annotation == null) {
            return new FilterContext();
        }

        FilterContext context = new FilterContext();

        // 解析 include 规则（后定义的覆盖先定义的）
        JsonFilterExt[] includes = annotation.include();
        for (JsonFilterExt filter : includes) {
            FilterRule rule = parseFilterRule(filter, true);
            context.addIncludeRule(rule);
            log.debug("Parsed include rule: {}", rule);
        }

        // 解析 exclude 规则（后定义的覆盖先定义的）
        JsonFilterExt[] excludes = annotation.exclude();
        for (JsonFilterExt filter : excludes) {
            FilterRule rule = parseFilterRule(filter, false);
            context.addExcludeRule(rule);
            log.debug("Parsed exclude rule: {}", rule);
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
        return method.isAnnotationPresent(JsonViewExt.class);
    }

}
