package com.qmetric.feed.domain;

import java.util.Collection;
import java.util.Collections;

import static java.util.Collections.unmodifiableCollection;

public class Links
{
    public static final Links NO_LINKS = new Links(Collections.<FeedEntryLink>emptySet());

    private final Collection<FeedEntryLink> links;

    public Links(final Collection<FeedEntryLink> links)
    {
        this.links = unmodifiableCollection(links);
    }

    public Collection<FeedEntryLink> additionalLinksForFeedEntry()
    {
        return links;
    }
}
