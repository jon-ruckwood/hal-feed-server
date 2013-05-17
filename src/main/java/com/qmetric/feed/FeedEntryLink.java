package com.qmetric.feed;

import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;

public class FeedEntryLink
{
    public final String rel;

    public final String href;

    public final boolean includeInSummarisedFeedEntry;

    public FeedEntryLink(final String rel, final String href)
    {
        this(rel, href, true);
    }

    public FeedEntryLink(final String rel, final String href, final boolean includeInSummarisedFeedEntry)
    {
        this.rel = rel;
        this.href = href;
        this.includeInSummarisedFeedEntry = includeInSummarisedFeedEntry;
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
