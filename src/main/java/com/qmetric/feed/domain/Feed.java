package com.qmetric.feed.domain;

import com.google.common.base.Optional;
import com.qmetric.feed.domain.validation.PayloadValidationRules;

public class Feed
{
    private final FeedStore store;

    private final PublishedDateProvider publishedDateProvider;

    private final PayloadValidationRules payloadValidationRules;

    public Feed(final FeedStore store, final PayloadValidationRules payloadValidationRules)
    {
        this(store, new PublishedDateProvider(), payloadValidationRules);
    }

    Feed(final FeedStore store, final PublishedDateProvider publishedDateProvider, final PayloadValidationRules payloadValidationRules)
    {
        this.store = store;
        this.publishedDateProvider = publishedDateProvider;
        this.payloadValidationRules = payloadValidationRules;
    }

    public FeedEntries retrieveBy(final FeedRestrictionCriteria criteria)
    {
        return store.retrieveBy(criteria);
    }

    public Optional<FeedEntry> retrieveBy(final Id id)
    {
        return store.retrieveBy(id);
    }

    public FeedEntry publish(final Payload payload)
    {
        payloadValidationRules.checkValid(payload);

        return store.store(new FeedEntry(publishedDateProvider.getPublishedDate(), payload));
    }
}
