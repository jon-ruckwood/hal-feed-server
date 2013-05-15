package com.qmetric.feed;

import org.joda.time.DateTime;

import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

public class FeedEntry
{
    public final Id id;

    public final DateTime publishedDate;

    public final Resource resource;

    public FeedEntry(final Id id, final DateTime publishedDate, final Resource resource)
    {
        this.id = id;
        this.publishedDate = publishedDate;
        this.resource = resource;
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
