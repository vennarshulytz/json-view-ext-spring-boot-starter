package io.github.vennarshulytz.jsonviewext;

import io.github.vennarshulytz.jsonviewext.sensitive.SensitiveHandler;
import io.github.vennarshulytz.jsonviewext.sensitive.impl.EmailType;
import io.github.vennarshulytz.jsonviewext.sensitive.impl.IdCardType;
import io.github.vennarshulytz.jsonviewext.sensitive.impl.PhoneType;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.Assert.assertEquals;

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

}
