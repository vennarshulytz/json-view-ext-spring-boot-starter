package io.github.vennarshulytz.jsonviewext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.github.vennarshulytz.jsonviewext.core.JsonViewExtContextHolder;
import io.github.vennarshulytz.jsonviewext.core.JsonViewExtModule;
import io.github.vennarshulytz.jsonviewext.model.FilterContext;
import io.github.vennarshulytz.jsonviewext.model.FilterRule;
import io.github.vennarshulytz.jsonviewext.sensitive.SensitiveHandler;
import io.github.vennarshulytz.jsonviewext.sensitive.SensitiveType;
import io.github.vennarshulytz.jsonviewext.sensitive.impl.EmailType;
import io.github.vennarshulytz.jsonviewext.sensitive.impl.IdCardType;
import io.github.vennarshulytz.jsonviewext.sensitive.impl.PhoneType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


/**
 * 测试 JsonViewExt 功能
 *
 * @author vennarshulytz
 * @since 1.0.0
 */
// @SpringBootTest
public class JsonViewExtTests {

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JsonViewExtModule());
    }

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

    @Test
    public void testFilterContext() {
        FilterContext context = new FilterContext();

        FilterRule includeRule = new FilterRule(
                TestEntity.class, "",
                ImmutableSet.of("name", "value"), true, ImmutableMap.of());

        context.addIncludeRule(includeRule);

        assertTrue(context.hasRules());

        FilterRule found = context.getApplicableRule(TestEntity.class, "");
        assertNotNull(found);
        assertEquals(ImmutableSet.of("name", "value"), found.getProps());
    }

    @Test
    public void testFilterContextWithPath() {
        FilterContext context = new FilterContext();

        FilterRule generalRule = new FilterRule(
                TestEntity.class, "",
                ImmutableSet.of("id", "name"), true, ImmutableMap.of());

        FilterRule specificRule = new FilterRule(
                TestEntity.class, "nested.entity",
                ImmutableSet.of("name", "value"), true, ImmutableMap.of());

        context.addIncludeRule(generalRule);
        context.addIncludeRule(specificRule);

        // 精确路径匹配
        FilterRule found = context.getApplicableRule(TestEntity.class, "nested.entity");
        assertNotNull(found);
        assertEquals(ImmutableSet.of("name", "value"), found.getProps());

        // 通用规则匹配
        FilterRule generalFound = context.getApplicableRule(TestEntity.class, "other.path");
        assertNotNull(generalFound);
        assertEquals(ImmutableSet.of("id", "name"), generalFound.getProps());
    }

    @Test
    public void testFilterContextWithWildcardPath() {
        FilterContext context = new FilterContext();

        FilterRule wildcardRule = new FilterRule(
                TestEntity.class, "items.*.detail",
                ImmutableSet.of("name"), true, ImmutableMap.of());

        context.addIncludeRule(wildcardRule);

        FilterRule found = context.getApplicableRule(TestEntity.class, "items.0.detail");
        assertNotNull(found);
        assertEquals(ImmutableSet.of("name"), found.getProps());
    }

    @Test
    public void testSerializationWithFilter() throws Exception {
        TestEntity entity = new TestEntity();
        entity.setId("123");
        entity.setName("Test");
        entity.setValue("Secret");

        FilterContext context = new FilterContext();
        FilterRule rule = new FilterRule(
                TestEntity.class, "",
                ImmutableSet.of("id", "name"), true, ImmutableMap.of());
        context.addIncludeRule(rule);

        try {
            JsonViewExtContextHolder.setContext(context);
            String json = objectMapper.writeValueAsString(entity);

            assertTrue(json.contains("id"));
            assertTrue(json.contains("name"));
            assertFalse(json.contains("Secret"));
        } finally {
            JsonViewExtContextHolder.clear();
        }
    }

    @Test
    public void testSerializationWithWildcardCollectionFilter() throws Exception {
        Holder holder = new Holder();
        holder.setItems(Arrays.asList(
                new NestedEntity(new TestEntity("1", "A", "SecretA")),
                new NestedEntity(new TestEntity("2", "B", "SecretB"))));

        FilterContext context = new FilterContext();
        context.addIncludeRule(new FilterRule(
                Holder.class, "", ImmutableSet.of("items"), true, ImmutableMap.of()));
        context.addIncludeRule(new FilterRule(
                NestedEntity.class, "items.*", ImmutableSet.of("entity"), true, ImmutableMap.of()));
        context.addIncludeRule(new FilterRule(
                TestEntity.class, "items.*.entity", ImmutableSet.of("id", "name"), true, ImmutableMap.of()));

        try {
            JsonViewExtContextHolder.setContext(context);
            String json = objectMapper.writeValueAsString(holder);

            assertTrue(json.contains("items"));
            assertTrue(json.contains("A"));
            assertTrue(json.contains("B"));
            assertFalse(json.contains("SecretA"));
            assertFalse(json.contains("SecretB"));
        } finally {
            JsonViewExtContextHolder.clear();
        }
    }

    @Test
    public void testSerializationWithMapAndOptionalFilter() throws Exception {
        Holder holder = new Holder();
        holder.setMap(ImmutableMap.of("primary", new TestEntity("1", "A", "Secret")));
        holder.setOptional(Optional.of(new TestEntity("2", "B", "Hidden")));

        FilterContext context = new FilterContext();
        context.addIncludeRule(new FilterRule(
                Holder.class, "", ImmutableSet.of("map", "optional"), true, ImmutableMap.of()));
        context.addIncludeRule(new FilterRule(
                TestEntity.class, "map.primary", ImmutableSet.of("name"), true, ImmutableMap.of()));
        context.addIncludeRule(new FilterRule(
                TestEntity.class, "optional", ImmutableSet.of("id"), true, ImmutableMap.of()));

        try {
            JsonViewExtContextHolder.setContext(context);
            String json = objectMapper.writeValueAsString(holder);

            assertTrue(json.contains("primary"));
            assertTrue(json.contains("A"));
            assertTrue(json.contains("2"));
            assertFalse(json.contains("Secret"));
            assertFalse(json.contains("Hidden"));
            assertFalse(json.contains("\"id\":\"1\""));
            assertFalse(json.contains("\"name\":\"B\""));
        } finally {
            JsonViewExtContextHolder.clear();
        }
    }

    @Test
    public void testSerializationWithPageLikeContentFilter() throws Exception {
        PageLike page = new PageLike(Arrays.asList(
                new TestEntity("1", "A", "SecretA"),
                new TestEntity("2", "B", "SecretB")),
                2);

        FilterContext context = new FilterContext();
        context.addIncludeRule(new FilterRule(
                PageLike.class, "", ImmutableSet.of("content", "total"), true, ImmutableMap.of()));
        context.addIncludeRule(new FilterRule(
                TestEntity.class, "content.*", ImmutableSet.of("name"), true, ImmutableMap.of()));

        try {
            JsonViewExtContextHolder.setContext(context);
            String json = objectMapper.writeValueAsString(page);

            assertTrue(json.contains("content"));
            assertTrue(json.contains("total"));
            assertTrue(json.contains("A"));
            assertTrue(json.contains("B"));
            assertFalse(json.contains("SecretA"));
            assertFalse(json.contains("SecretB"));
        } finally {
            JsonViewExtContextHolder.clear();
        }
    }

    @Test
    public void testSensitiveHandlerWithRegisteredCustomBean() {
        SensitiveHandler.registerHandler(CustomSensitiveType.class, new CustomSensitiveType("bean"));

        String result = SensitiveHandler.desensitize(CustomSensitiveType.class, "value");

        assertEquals("bean:value", result);
    }

    public static class CustomSensitiveType implements SensitiveType {
        private final String prefix;

        public CustomSensitiveType() {
            this("default");
        }

        public CustomSensitiveType(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public String desensitize(String value) {
            return prefix + ":" + value;
        }
    }

    // 测试实体类
    public static class TestEntity {
        private String id;
        private String name;
        private String value;

        public TestEntity() {
        }

        public TestEntity(String id, String name, String value) {
            this.id = id;
            this.name = name;
            this.value = value;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
    }

    public static class NestedEntity {
        private TestEntity entity;

        public NestedEntity() {
        }

        public NestedEntity(TestEntity entity) {
            this.entity = entity;
        }

        public TestEntity getEntity() { return entity; }
        public void setEntity(TestEntity entity) { this.entity = entity; }
    }

    public static class Holder {
        private List<NestedEntity> items;
        private Map<String, TestEntity> map;
        private Optional<TestEntity> optional;

        public List<NestedEntity> getItems() { return items; }
        public void setItems(List<NestedEntity> items) { this.items = items; }

        public Map<String, TestEntity> getMap() { return map; }
        public void setMap(Map<String, TestEntity> map) { this.map = map; }

        public Optional<TestEntity> getOptional() { return optional; }
        public void setOptional(Optional<TestEntity> optional) { this.optional = optional; }
    }

    public static class PageLike {
        private List<TestEntity> content;
        private long total;

        public PageLike() {
        }

        public PageLike(List<TestEntity> content, long total) {
            this.content = content;
            this.total = total;
        }

        public List<TestEntity> getContent() { return content; }
        public void setContent(List<TestEntity> content) { this.content = content; }

        public long getTotal() { return total; }
        public void setTotal(long total) { this.total = total; }
    }
}
