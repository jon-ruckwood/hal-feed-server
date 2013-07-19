package com.qmetric.feed.domain;

import java.util.Collection;
import java.util.Collections;

public class HiddenPayloadAttributes
{
    public static final HiddenPayloadAttributes NONE = new HiddenPayloadAttributes(Collections.<String>emptyList());

    private final Collection<String> attributes;

    public HiddenPayloadAttributes(final Collection<String> attributes)
    {
        this.attributes = attributes;
    }

    public Collection<String> all()
    {
        return attributes;
    }

    public boolean isHidden(final String payloadAttributeName)
    {
        return attributes.contains(payloadAttributeName);
    }

    public boolean isNotHidden(final String payloadAttributeName)
    {
        return !isHidden(payloadAttributeName);
    }
}
