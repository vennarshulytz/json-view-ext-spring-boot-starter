package io.github.vennarshulytz.jsonviewext;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.github.vennarshulytz.jsonviewext.model.FilterRule;
import io.github.vennarshulytz.jsonviewext.sensitive.SensitiveHandler;
import io.github.vennarshulytz.jsonviewext.sensitive.impl.EmailType;
import io.github.vennarshulytz.jsonviewext.sensitive.impl.IdCardType;
import io.github.vennarshulytz.jsonviewext.sensitive.impl.PhoneType;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.Assert.assertEquals;

/**
 * 测试 JsonViewExt 功能
 *
 * @author vennarshulytz
 * @since 1.0.0
 */
@SpringBootTest
public class JsonViewExtTests {

    @Test
    public void testEmailDesensitize() {
        String original = "3530163057@qq.com";
        String expected = "353*******@qq.com";

        EmailType emailType = new EmailType();
        String result = emailType.desensitize(original);

        assertEquals(expected, result);
    }

    @Test
    public void testSensitiveHandlerWithEmailType() {
        String original = "3530163057@qq.com";
        String result = SensitiveHandler.desensitize(EmailType.class, original);

        assertEquals("353*******@qq.com", result);
    }

    @Test
    public void testIdCardDesensitize() {
        String original = "123456789012345678";
        String expected = "123456********5678";

        IdCardType idCardType = new IdCardType();
        String result = idCardType.desensitize(original);

        assertEquals(expected, result);
    }

    @Test
    public void testSensitiveHandlerWithIdCardType() {
        String original = "123456789012345678";
        String result = SensitiveHandler.desensitize(IdCardType.class, original);

        assertEquals("123456********5678", result);
    }

    @Test
    public void testPhoneDesensitize() {
        String original = "12345678901";
        String expected = "123****8901";

        PhoneType phoneType = new PhoneType();
        String result = phoneType.desensitize(original);

        assertEquals(expected, result);
    }

    @Test
    public void testSensitiveHandlerWithPhoneType() {
        String original = "12345678901";
        String result = SensitiveHandler.desensitize(PhoneType.class, original);

        assertEquals("123****8901", result);
    }

    @Test
    public void testFilterRuleEquality() {
        FilterRule rule1 = new FilterRule(
                String.class, "test",
                ImmutableSet.of("a", "b"), true, ImmutableMap.of());
        FilterRule rule2 = new FilterRule(
                String.class, "test",
                ImmutableSet.of("c", "d"), true, ImmutableMap.of());

        assertEquals(rule1, rule2); // Same class and field path
        assertEquals(rule1.hashCode(), rule2.hashCode());
    }

}
