package com.qmetric.feed.domain;

import org.joda.time.DateTime;

import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

public class FeedEntry
{
    private static final Id ID_NOT_KNOWN_YET = null;

    public final Id id;

    public final DateTime publishedDate;

    public final Payload payload;

    public FeedEntry(final DateTime publishedDate, final Payload payload)
    {
        this(ID_NOT_KNOWN_YET, publishedDate, payload);
    }

    public FeedEntry(final Id id, final DateTime publishedDate, final Payload payload)
    {
        this.id = id;
        this.publishedDate = publishedDate;
        this.payload = payload;
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
