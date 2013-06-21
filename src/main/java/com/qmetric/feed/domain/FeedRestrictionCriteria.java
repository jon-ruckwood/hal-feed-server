package com.qmetric.feed.domain;

import com.google.common.base.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;

public class FeedRestrictionCriteria
{
    public final Filter filter;

    public final int limit;

    public FeedRestrictionCriteria(final Filter filter, final int limit)
    {
        checkArgument(limit > 0, "Max results limit must be more than 0");
        this.filter = filter;
        this.limit = limit;
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

    public static class Filter
    {
        public final Optional<Id> earlierThan;

        public final Optional<Id> laterThan;

        public Filter(final Optional<Id> earlierThan, final Optional<Id> laterThan)
        {
            checkArgument(!earlierThan.isPresent() || !laterThan.isPresent(), "Supply an earlier or later than condition, not both");
            this.earlierThan = earlierThan;
            this.laterThan = laterThan;
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
}
