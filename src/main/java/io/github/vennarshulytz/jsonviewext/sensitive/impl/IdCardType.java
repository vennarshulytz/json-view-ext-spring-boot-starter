package io.github.vennarshulytz.jsonviewext.sensitive.impl;

import io.github.vennarshulytz.jsonviewext.sensitive.SensitiveType;

/**
 * 身份证号脱敏处理器
 * 保留前6位和后4位，中间用*号替换
 *
 * @author vennarshulytz
 * @since 1.0.0
 */
public class IdCardType implements SensitiveType {
    private static final int PREFIX_LENGTH = 6;
    private static final int SUFFIX_LENGTH = 4;
    private static final char MASK_CHAR = '*';

    @Override
    public String desensitize(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        int length = value.length();
        if (length <= PREFIX_LENGTH + SUFFIX_LENGTH) {
            return value;
        }

        StringBuilder sb = new StringBuilder(length);
        sb.append(value, 0, PREFIX_LENGTH);

        int maskLength = length - PREFIX_LENGTH - SUFFIX_LENGTH;
        for (int i = 0; i < maskLength; i++) {
            sb.append(MASK_CHAR);
        }

        sb.append(value.substring(length - SUFFIX_LENGTH));
        return sb.toString();
    }

    @Override
    public String getTypeName() {
        return "ID_CARD";
    }
}
