package io.github.vennarshulytz.jsonviewext.model;

import io.github.vennarshulytz.jsonviewext.config.JsonViewExtProperties;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 过滤上下文，存储当前请求的所有过滤规则
 *
 * @author vennarshulytz
 * @since 1.0.0
 */
public class FilterContext {

    public static final FilterContext EMPTY = new FilterContext();

    private static final String COLLECTION_WILDCARD_SEGMENT = ".*";

    /**
     * 包含规则映射: Class -> (fieldPath -> FilterRule)
     */
    private final Map<Class<?>, Map<String, FilterRule>> includeRules;

    /**
     * 排除规则映射: Class -> (fieldPath -> FilterRule)
     */
    private final Map<Class<?>, Map<String, FilterRule>> excludeRules;
    private final JsonViewExtProperties properties;

    public FilterContext() {
        this(JsonViewExtProperties.defaultProperties());
    }

    public FilterContext(JsonViewExtProperties properties) {
        this.includeRules = new ConcurrentHashMap<>();
        this.excludeRules = new ConcurrentHashMap<>();
        this.properties = properties == null ? JsonViewExtProperties.defaultProperties() : properties;
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
        FilterRule includeRule = findMatchingRule(includeRules, clazz, normalizePath(currentPath));
        if (includeRule != null) {
            return includeRule;
        }

        // 其次查找 exclude 规则
        return findMatchingRule(excludeRules, clazz, normalizePath(currentPath));
    }

    /**
     * 查找匹配的规则
     */
    private FilterRule findMatchingRule(Map<Class<?>, Map<String, FilterRule>> rulesMap,
                                        Class<?> clazz, String currentPath) {
        Map<String, FilterRule> classRules = findClassRules(rulesMap, clazz);
        if (classRules == null || classRules.isEmpty()) {
            return null;
        }

        // 优先匹配精确路径
        FilterRule exactMatch = classRules.get(currentPath);
        if (exactMatch != null) {
            return exactMatch;
        }

        String legacyPath = removeCollectionWildcardSegments(currentPath);
        if (!Objects.equals(legacyPath, currentPath)) {
            FilterRule legacyExactMatch = classRules.get(legacyPath);
            if (legacyExactMatch != null) {
                return legacyExactMatch;
            }
        }

        FilterRule wildcardMatch = null;
        int bestSpecificity = -1;
        for (FilterRule rule : classRules.values()) {
            if (!rule.hasWildcardPath()) {
                continue;
            }
            if (pathMatches(rule.getFieldPath(), currentPath)
                    || pathMatches(rule.getFieldPath(), legacyPath)) {
                int specificity = specificity(rule.getFieldPath());
                if (specificity > bestSpecificity) {
                    wildcardMatch = rule;
                    bestSpecificity = specificity;
                }
            }
        }
        if (wildcardMatch != null) {
            return wildcardMatch;
        }

        // 查找通用规则（无路径）
        return classRules.get("");
    }

    private Map<String, FilterRule> findClassRules(Map<Class<?>, Map<String, FilterRule>> rulesMap,
                                                   Class<?> clazz) {
        Map<String, FilterRule> exactRules = rulesMap.get(clazz);
        if (exactRules != null) {
            return exactRules;
        }

        Map<String, FilterRule> matchedRules = null;
        int bestDistance = Integer.MAX_VALUE;
        for (Map.Entry<Class<?>, Map<String, FilterRule>> entry : rulesMap.entrySet()) {
            Class<?> ruleClass = entry.getKey();
            if (ruleClass.isAssignableFrom(clazz)) {
                int distance = inheritanceDistance(clazz, ruleClass);
                if (distance < bestDistance) {
                    matchedRules = entry.getValue();
                    bestDistance = distance;
                }
            }
        }
        return matchedRules;
    }

    private int inheritanceDistance(Class<?> clazz, Class<?> targetClass) {
        if (Objects.equals(clazz, targetClass)) {
            return 0;
        }
        if (targetClass.isInterface()) {
            return clazz == null ? Integer.MAX_VALUE : 1;
        }

        int distance = 0;
        Class<?> current = clazz;
        while (current != null) {
            if (Objects.equals(current, targetClass)) {
                return distance;
            }
            current = current.getSuperclass();
            distance++;
        }
        return Integer.MAX_VALUE;
    }

    private String normalizePath(String path) {
        return path == null ? "" : path;
    }

    private String removeCollectionWildcardSegments(String path) {
        String normalizedPath = normalizePath(path);
        if (normalizedPath.isEmpty()) {
            return normalizedPath;
        }
        if (normalizedPath.endsWith(COLLECTION_WILDCARD_SEGMENT)) {
            normalizedPath = normalizedPath.substring(0,
                    normalizedPath.length() - COLLECTION_WILDCARD_SEGMENT.length());
        }
        return normalizedPath.replace(COLLECTION_WILDCARD_SEGMENT + ".", ".");
    }

    private boolean pathMatches(String pattern, String path) {
        String normalizedPattern = normalizePath(pattern);
        String normalizedPath = normalizePath(path);
        if (Objects.equals(normalizedPattern, normalizedPath)) {
            return true;
        }
        if (normalizedPattern.isEmpty()) {
            return normalizedPath.isEmpty();
        }

        String[] patternParts = normalizedPattern.split("\\.");
        String[] pathParts = normalizedPath.split("\\.");
        if (patternParts.length != pathParts.length) {
            return false;
        }
        for (int i = 0; i < patternParts.length; i++) {
            if (!"*".equals(patternParts[i]) && !Objects.equals(patternParts[i], pathParts[i])) {
                return false;
            }
        }
        return true;
    }

    private int specificity(String pattern) {
        int specificity = 0;
        String[] parts = normalizePath(pattern).split("\\.");
        for (String part : parts) {
            if (!"*".equals(part) && !part.isEmpty()) {
                specificity++;
            }
        }
        return specificity;
    }

    public boolean hasRules() {
        return !includeRules.isEmpty() || !excludeRules.isEmpty();
    }

    public JsonViewExtProperties getProperties() {
        return properties;
    }

    @Override
    public String toString() {
        return "FilterContext{" +
                "includeRules=" + includeRules +
                ", excludeRules=" + excludeRules +
                '}';
    }
}
