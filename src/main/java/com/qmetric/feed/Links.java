package com.qmetric.feed;

import com.google.common.base.Predicate;

import java.util.Collection;
import java.util.Collections;

import static com.google.common.collect.Collections2.filter;
import static java.util.Collections.unmodifiableCollection;

public class Links
{
    public static final Links NO_LINKS = new Links(Collections.<Link>emptySet());

    private final Collection<Link> links;

    public Links(final Collection<Link> links)
    {
        this.links = unmodifiableCollection(links);
    }

    public Collection<Link> all()
    {
        return links;
    }

    public Collection<Link> allForSummary()
    {
        return filter(links, new Predicate<Link>()
        {
            @Override public boolean apply(final Link input)
            {
                return input.includeInSummary;
            }
        });
    }
}
