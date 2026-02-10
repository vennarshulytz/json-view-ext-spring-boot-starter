package io.github.vennarshulytz.jsonviewext.annotation;

import java.lang.annotation.*;

/**
 * 标记在Controller方法上，用于控制返回对象的JSON序列化字段
 *
 * @author vennarshulytz
 * @since 1.0.0
 */
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface JsonViewExt {

    /**
     * 包含的字段过滤规则，指定要序列化的字段
     * 优先级高于 exclude
     */
    JsonFilterExt[] include() default {};

    /**
     * 排除的字段过滤规则，指定不序列化的字段
     * 优先级低于 include
     */
    JsonFilterExt[] exclude() default {};
}
