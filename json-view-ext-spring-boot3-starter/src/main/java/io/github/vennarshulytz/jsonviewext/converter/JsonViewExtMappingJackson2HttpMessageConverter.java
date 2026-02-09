package io.github.vennarshulytz.jsonviewext.converter;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.vennarshulytz.jsonviewext.core.JsonViewExtBeanSerializerModifier;
import io.github.vennarshulytz.jsonviewext.core.JsonViewExtContextHolder;
import io.github.vennarshulytz.jsonviewext.model.FilteredResponse;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * 自定义 HttpMessageConverter
 *
 * @author vennarshulytz
 * @since 1.1.0
 */
public class JsonViewExtMappingJackson2HttpMessageConverter extends MappingJackson2HttpMessageConverter {

    private final ObjectMapper filterObjectMapper;

    public JsonViewExtMappingJackson2HttpMessageConverter(
            ObjectMapper defaultObjectMapper,
            ObjectMapper filterObjectMapper) {
        super(defaultObjectMapper);
        this.filterObjectMapper = filterObjectMapper;
    }

    @Override
    protected void writeInternal(Object object, Type type, HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException {

        if (object instanceof FilteredResponse) {
            FilteredResponse filteredResponse = (FilteredResponse) object;
            try {
                JsonViewExtContextHolder.setContext(filteredResponse.getContext());
                JsonViewExtBeanSerializerModifier.PathTracker.clear();

                MediaType contentType = outputMessage.getHeaders().getContentType();
                JsonEncoding encoding = getJsonEncoding(contentType);

                try (JsonGenerator generator = filterObjectMapper.getFactory()
                        .createGenerator(outputMessage.getBody(), encoding)) {
                    filterObjectMapper.writeValue(generator, filteredResponse.getData());
                }
            } finally {
                JsonViewExtContextHolder.clear();
                JsonViewExtBeanSerializerModifier.PathTracker.reset();
            }
        } else {
            super.writeInternal(object, type, outputMessage);
        }
    }
}
