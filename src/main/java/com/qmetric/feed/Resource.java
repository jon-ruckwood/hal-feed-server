package com.qmetric.feed;

import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;

public class Resource
{
    public final Map<String, String> attributes;

    public Resource(final Map<String, String> attributes)
    {
        this.attributes = unmodifiableMap(attributes);
    }

    @Override
    public int hashCode()
    {
        return reflectionHashCode(this);
    }

    @Override
    public boolean equals(final Object obj)
    {
        return reflectionEquals(this, obj);
    }
}
