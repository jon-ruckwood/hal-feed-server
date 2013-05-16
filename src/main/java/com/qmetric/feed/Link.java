package com.qmetric.feed;

import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;

public class Link
{
    public final String rel;

    public final String href;

    public final boolean includeInSummary;

    public Link(final String rel, final String href)
    {
        this(rel, href, true);
    }

    public Link(final String rel, final String href, final boolean includeInSummary)
    {
        this.rel = rel;
        this.href = href;
        this.includeInSummary = includeInSummary;
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
