package io.github.vennarshulytz.jsonviewext.annotation;

import java.lang.annotation.*;

/**
 * 字段过滤规则注解
 *
 * @author vennarshulytz
 * @since 1.0.0
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JsonFilterExt {

    /**
     * 目标类型
     */
    Class<?> clazz();

    /**
     * 字段路径，用于精确匹配特定路径下的对象
     * 支持嵌套路径，使用 '.' 分隔，如 "user.address"
     * 为空时表示匹配所有该类型的对象
     */
    String field() default "";

    /**
     * 需要包含/排除的属性名称数组
     */
    String[] props();

    /**
     * 敏感字段脱敏规则
     */
    Sensitive[] sensitives() default {};
}
