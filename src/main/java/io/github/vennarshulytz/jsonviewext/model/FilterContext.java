package io.github.vennarshulytz.jsonviewext.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 过滤上下文，存储当前请求的所有过滤规则
 *
 * @author vennarshulytz
 * @since 1.0.0
 */
public class FilterContext {

    /**
     * 包含规则映射: Class -> (fieldPath -> FilterRule)
     */
    private final Map<Class<?>, Map<String, FilterRule>> includeRules;

    /**
     * 排除规则映射: Class -> (fieldPath -> FilterRule)
     */
    private final Map<Class<?>, Map<String, FilterRule>> excludeRules;

    public FilterContext() {
        this.includeRules = new ConcurrentHashMap<>();
        this.excludeRules = new ConcurrentHashMap<>();
    }

    public void addIncludeRule(FilterRule rule) {
        includeRules.computeIfAbsent(rule.getTargetClass(), k -> new ConcurrentHashMap<>())
                .put(rule.getFieldPath(), rule);
    }

    public void addExcludeRule(FilterRule rule) {
        excludeRules.computeIfAbsent(rule.getTargetClass(), k -> new ConcurrentHashMap<>())
                .put(rule.getFieldPath(), rule);
    }

    @Override
    public String toString() {
        return "FilterContext{" +
                "includeRules=" + includeRules +
                ", excludeRules=" + excludeRules +
                '}';
    }
}
