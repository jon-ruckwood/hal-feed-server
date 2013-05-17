package com.qmetric.feed;

import com.google.common.base.Predicate;

import java.util.Collection;
import java.util.Collections;

import static com.google.common.collect.Collections2.filter;
import static java.util.Collections.unmodifiableCollection;

public class Links
{
    public static final Links NO_LINKS = new Links(Collections.<FeedEntryLink>emptySet());

    private final Collection<FeedEntryLink> links;

    public Links(final Collection<FeedEntryLink> links)
    {
        this.links = unmodifiableCollection(links);
    }

    public Collection<FeedEntryLink> forFeedEntry()
    {
        return links;
    }

    public Collection<FeedEntryLink> forSummarisedFeedEntry()
    {
        return filter(links, new Predicate<FeedEntryLink>()
        {
            @Override public boolean apply(final FeedEntryLink input)
            {
                return input.includeInSummarisedFeedEntry;
            }
        });
    }
}
