package com.qmetric.feed.domain;

import java.util.List;

import static java.util.Collections.unmodifiableList;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

public class FeedEntries
{
    private final List<FeedEntry> entries;

    public FeedEntries(final List<FeedEntry> entries)
    {
        this.entries = unmodifiableList(entries);
    }

    public List<FeedEntry> all()
    {
        return entries;
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
