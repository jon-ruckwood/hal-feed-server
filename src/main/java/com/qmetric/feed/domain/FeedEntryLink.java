package com.qmetric.feed.domain;

import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;

public class FeedEntryLink
{
    public final String rel;

    public final String href;

    public FeedEntryLink(final String rel, final String href)
    {
        this.rel = rel;
        this.href = href;
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
