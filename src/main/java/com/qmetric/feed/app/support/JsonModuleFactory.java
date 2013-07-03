package com.qmetric.feed.app.support;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.qmetric.feed.domain.FeedEntryLink;
import com.qmetric.feed.domain.Payload;

import java.io.IOException;
import java.util.Map;

import static com.fasterxml.jackson.core.Version.unknownVersion;

public class JsonModuleFactory
{
    public static Module create()
    {
        final SimpleModule module = new SimpleModule("jacksonConfig", unknownVersion());

        module.addSerializer(Payload.class, new PayloadJsonMapper.Serializer());
        module.addDeserializer(Payload.class, new PayloadJsonMapper.Deserializer());
        module.addDeserializer(FeedEntryLink.class, new LinkJsonMapper.Deserializer());

        return module;
    }

    private static class PayloadJsonMapper
    {
        private static class Serializer extends JsonSerializer<Payload>
        {
            @Override public void serialize(final Payload payload, final JsonGenerator jsonGenerator, final SerializerProvider serializerProvider) throws IOException
            {
                jsonGenerator.writeObject(payload);
            }
        }

        private static class Deserializer extends JsonDeserializer<Payload>
        {
            @Override @SuppressWarnings("unchecked")
            public Payload deserialize(final JsonParser jsonParser, final DeserializationContext deserializationContext) throws IOException
            {
                return new Payload(jsonParser.readValueAs(Map.class));
            }
        }
    }

    private static class LinkJsonMapper
    {
        private static class Deserializer extends JsonDeserializer<FeedEntryLink>
        {
            @Override
            public FeedEntryLink deserialize(final JsonParser jsonParser, final DeserializationContext deserializationContext) throws IOException
            {
                @SuppressWarnings("unchecked")
                final Map<String, String> map = (Map<String, String>) new ObjectMapper().readValue(jsonParser, Map.class);

                return new FeedEntryLink(map.get("rel"), map.get("href"));
            }
        }
    }
}
