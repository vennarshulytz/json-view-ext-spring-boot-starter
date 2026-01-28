package io.github.vennarshulytz.jsonviewext.sensitive.impl;


import io.github.vennarshulytz.jsonviewext.sensitive.SensitiveType;

import static io.github.vennarshulytz.jsonviewext.constant.DesensitizationConstants.MASK_CHAR;

/**
 * 邮箱脱敏处理器
 * 保留邮箱前3个字符和@后的域名
 *
 * @author vennarshulytz
 * @since 1.0.0
 */
public class EmailType implements SensitiveType {

    private static final int PREFIX_LENGTH = 3;

    @Override
    public String desensitize(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        int atIndex = value.indexOf('@');
        if (atIndex <= 0) {
            return value;
        }

        String localPart = value.substring(0, atIndex);
        String domainPart = value.substring(atIndex);

        if (localPart.length() <= PREFIX_LENGTH) {
            return value;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(localPart, 0, PREFIX_LENGTH);

        int maskLength = localPart.length() - PREFIX_LENGTH;
        for (int i = 0; i < maskLength; i++) {
            sb.append(MASK_CHAR);
        }

        sb.append(domainPart);
        return sb.toString();
    }

    @Override
    public String getTypeName() {
        return "EMAIL";
    }
}
