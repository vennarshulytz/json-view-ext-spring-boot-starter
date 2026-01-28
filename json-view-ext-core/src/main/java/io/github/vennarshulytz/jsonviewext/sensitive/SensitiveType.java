package io.github.vennarshulytz.jsonviewext.sensitive;

/**
 * 脱敏类型接口
 *
 * @author vennarshulytz
 * @since 1.0.0
 */
public interface SensitiveType {

    /**
     * 对敏感数据进行脱敏处理
     *
     * @param value 原始值
     * @return 脱敏后的值
     */
    String desensitize(String value);

    /**
     * 获取脱敏类型名称
     *
     * @return 类型名称
     */
    default String getTypeName() {
        return this.getClass().getSimpleName();
    }
}
