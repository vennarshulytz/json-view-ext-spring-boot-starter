package io.github.vennarshulytz.jsonviewext;

import io.github.vennarshulytz.jsonviewext.sensitive.SensitiveHandler;
import io.github.vennarshulytz.jsonviewext.sensitive.impl.IdCardType;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.Assert.assertEquals;

@SpringBootTest
public class JsonViewExtTests {

    @Test
    public void testIdCardDesensitize() {
        String original = "123456789012345678";
        String expected = "123456********5678";

        IdCardType idCardType = new IdCardType();
        String result = idCardType.desensitize(original);

        assertEquals(expected, result);
    }

    @Test
    public void testSensitiveHandler() {
        String original = "123456789012345678";
        String result = SensitiveHandler.desensitize(IdCardType.class, original);

        assertEquals("123456********5678", result);
    }

}
