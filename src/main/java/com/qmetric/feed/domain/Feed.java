package com.qmetric.feed.domain;

import com.google.common.base.Optional;

public class Feed
{
    private final FeedStore store;

    private final PublishedDateProvider publishedDateProvider;

    public Feed(final FeedStore store)
    {
        this(store, new PublishedDateProvider());
    }

    Feed(final FeedStore store, final PublishedDateProvider publishedDateProvider)
    {
        this.store = store;
        this.publishedDateProvider = publishedDateProvider;
    }

    // todo pagination solution needed
    public FeedEntries retrieveAll()
    {
        return store.retrieveAll();
    }

    public Optional<FeedEntry> retrieveBy(final Id id)
    {
        return store.retrieveBy(id);
    }

    public FeedEntry publish(final Payload payload)
    {
        return store.store(new FeedEntry(publishedDateProvider.getPublishedDate(), payload));
    }
}
