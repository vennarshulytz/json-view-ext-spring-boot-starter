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

    /**
     * 获取适用的规则（优先匹配精确路径，其次匹配通用规则）
     */
    public FilterRule getApplicableRule(Class<?> clazz, String currentPath) {
        // 优先查找 include 规则
        FilterRule includeRule = findMatchingRule(includeRules, clazz, currentPath);
        if (includeRule != null) {
            return includeRule;
        }

        // 其次查找 exclude 规则
        return findMatchingRule(excludeRules, clazz, currentPath);
    }

    /**
     * 查找匹配的规则
     */
    private FilterRule findMatchingRule(Map<Class<?>, Map<String, FilterRule>> rulesMap,
                                        Class<?> clazz, String currentPath) {
        Map<String, FilterRule> classRules = rulesMap.get(clazz);
        if (classRules == null || classRules.isEmpty()) {
            return null;
        }

        // 优先匹配精确路径
        FilterRule exactMatch = classRules.get(currentPath);
        if (exactMatch != null) {
            return exactMatch;
        }

        // 查找通用规则（无路径）
        return classRules.get("");
    }

    public boolean hasRules() {
        return !includeRules.isEmpty() || !excludeRules.isEmpty();
    }

    @Override
    public String toString() {
        return "FilterContext{" +
                "includeRules=" + includeRules +
                ", excludeRules=" + excludeRules +
                '}';
    }
}
