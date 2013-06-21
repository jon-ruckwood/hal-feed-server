package com.qmetric.feed.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qmetric.feed.domain.Payload;

import java.io.IOException;
import java.util.Map;

public class PayloadSerializationMapper
{
    private final ObjectMapper payloadJsonMapper;

    public PayloadSerializationMapper()
    {
        this.payloadJsonMapper = new ObjectMapper();
    }

    public String serializePayloadOf(final Payload payload) throws IOException
    {
        return payloadJsonMapper.writeValueAsString(payload.attributes);
    }

    public Payload deserializePayload(final String payload)
    {
        try
        {
            //noinspection unchecked
            return new Payload(payloadJsonMapper.readValue(payload, Map.class));
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException(e);
        }
    }
}
