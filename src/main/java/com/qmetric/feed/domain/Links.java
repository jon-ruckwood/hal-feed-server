package com.qmetric.feed.domain;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;

import java.util.Collection;
import java.util.Collections;

import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Iterables.tryFind;
import static java.util.Collections.unmodifiableCollection;

public class Links
{
    public static final Links NO_LINKS = new Links(Collections.<FeedEntryLink>emptySet());

    private static final String SELF_LINK_RELATION = "self";

    private final Collection<FeedEntryLink> links;

    public Links(final Collection<FeedEntryLink> links)
    {
        this.links = unmodifiableCollection(links);
    }

    public Optional<FeedEntryLink> customizedSelfLinkForFeedEntry()
    {
        return tryFind(links, new Predicate<FeedEntryLink>()
        {
            @Override public boolean apply(final FeedEntryLink input)
            {
                return SELF_LINK_RELATION.equals(input.rel);
            }
        });
    }

    public Collection<FeedEntryLink> additionalLinksForFeedEntry()
    {
        return filter(links, new Predicate<FeedEntryLink>()
        {
            @Override public boolean apply(final FeedEntryLink input)
            {
                return !SELF_LINK_RELATION.equals(input.rel);
            }
        });
    }
}
