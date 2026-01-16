package io.github.vennarshulytz.jsonviewext.model;

import io.github.vennarshulytz.jsonviewext.sensitive.SensitiveType ;

import java.util.*;

/**
 * 过滤规则模型
 *
 * @author vennarshulytz
 * @since 1.0.0
 */
public class FilterRule {

    private final Class<?> targetClass;
    private final String fieldPath;
    private final Set<String> props;
    private final boolean isInclude;
    private final Map<String, Class<? extends SensitiveType>> sensitiveProps;

    public FilterRule(Class<?> targetClass, String fieldPath, Set<String> props,
                      boolean isInclude, Map<String, Class<? extends SensitiveType>> sensitiveProps) {
        this.targetClass = targetClass;
        this.fieldPath = fieldPath != null ? fieldPath : "";
        this.props = props != null ? Collections.unmodifiableSet(new HashSet<>(props)) : Collections.emptySet();
        this.isInclude = isInclude;
        this.sensitiveProps = sensitiveProps != null ?
                Collections.unmodifiableMap(new HashMap<>(sensitiveProps)) : Collections.emptyMap();
    }

    public Class<?> getTargetClass() {
        return targetClass;
    }

    public String getFieldPath() {
        return fieldPath;
    }

    public Set<String> getProps() {
        return props;
    }

    public boolean isInclude() {
        return isInclude;
    }

    public Map<String, Class<? extends SensitiveType>> getSensitiveProps() {
        return sensitiveProps;
    }

    public boolean hasFieldPath() {
        return fieldPath != null && !fieldPath.isEmpty();
    }

    /**
     * 生成规则的唯一键
     */
    public String generateKey() {
        return targetClass.getName() + "#" + fieldPath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FilterRule that = (FilterRule) o;
        return Objects.equals(targetClass, that.targetClass) &&
                Objects.equals(fieldPath, that.fieldPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetClass, fieldPath);
    }

    @Override
    public String toString() {
        return "FilterRule{" +
                "targetClass=" + targetClass.getSimpleName() +
                ", fieldPath='" + fieldPath + '\'' +
                ", props=" + props +
                ", isInclude=" + isInclude +
                ", sensitiveProps=" + sensitiveProps.keySet() +
                '}';
    }
}
