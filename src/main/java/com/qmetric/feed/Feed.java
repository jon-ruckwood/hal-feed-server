package com.qmetric.feed;

import com.google.common.base.Optional;

public class Feed
{
    private final FeedStore store;

    private final IdFactory idFactory;

    private final PublishedDateProvider publishedDateProvider;

    public Feed(final FeedStore store, final IdFactory idFactory)
    {
        this(store, idFactory, new PublishedDateProvider());
    }

    Feed(final FeedStore store, final IdFactory idFactory, final PublishedDateProvider publishedDateProvider)
    {
        this.store = store;
        this.idFactory = idFactory;
        this.publishedDateProvider = publishedDateProvider;
    }

    // todo pagination solution needed
    public FeedEntries retrieveAll()
    {
        return store.retrieveAll();
    }

    public Optional<FeedEntry> retrieve(final Id id)
    {
        return store.retrieveBy(id);
    }

    public FeedEntry publish(final Resource resource)
    {
        final FeedEntry feedEntry = new FeedEntry(idFactory.create(), publishedDateProvider.getPublishedDate(), resource);

        store.add(feedEntry);

        return feedEntry;
    }
}
