package com.qmetric.feed.app.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qmetric.feed.domain.Payload;

import java.io.IOException;
import java.util.Map;

public class FeedStorePayloadRepresentation
{
    private final ObjectMapper objectMapper;

    public FeedStorePayloadRepresentation(final ObjectMapper objectMapper)
    {
        this.objectMapper = objectMapper;
    }

    public String serialize(final Payload payload) throws IOException
    {
        return objectMapper.writeValueAsString(payload.attributes);
    }

    @SuppressWarnings("unchecked")
    public Payload deserialize(final String payload)
    {
        try
        {
            return new Payload(objectMapper.readValue(payload, Map.class));
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException(e);
        }
    }
}
