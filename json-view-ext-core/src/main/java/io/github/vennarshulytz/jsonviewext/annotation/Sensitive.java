package io.github.vennarshulytz.jsonviewext.annotation;


import io.github.vennarshulytz.jsonviewext.sensitive.SensitiveType;

import java.lang.annotation.*;

/**
 * 敏感字段脱敏配置
 *
 * @author vennarshulytz
 * @since 1.0.0
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Sensitive {

    /**
     * 脱敏类型处理器
     */
    Class<? extends SensitiveType> type();

    /**
     * 需要脱敏的属性名称
     */
    String[] props();
}
