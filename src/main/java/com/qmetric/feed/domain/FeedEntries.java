package com.qmetric.feed.domain;

import com.google.common.base.Optional;

import java.util.List;

import static com.google.common.collect.FluentIterable.from;
import static java.util.Collections.unmodifiableList;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

public class FeedEntries
{
    private final List<FeedEntry> entries;

    public final boolean earlierExists;

    public final boolean laterExists;

    public FeedEntries(final List<FeedEntry> entries)
    {
        this(entries, false, false);
    }

    public FeedEntries(final List<FeedEntry> entries, final boolean earlierExists, final boolean laterExists)
    {
        this.entries = unmodifiableList(entries);
        this.earlierExists = isNotEmpty(entries) && earlierExists;
        this.laterExists = isNotEmpty(entries) && laterExists;
    }

    public List<FeedEntry> all()
    {
        return entries;
    }

    public Optional<FeedEntry> first()
    {
        return from(entries).first();
    }

    public Optional<FeedEntry> last()
    {
        return from(entries).last();
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
