package com.qmetric.feed.domain;

import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

public class Resource
{
    public final Map<String, Object> attributes;

    public Resource(final Map<String, Object> attributes)
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

    @Override
    public String toString()
    {
        return reflectionToString(this);
    }
}
