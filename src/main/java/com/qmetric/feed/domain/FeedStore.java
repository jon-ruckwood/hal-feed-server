package com.qmetric.feed.domain;

import com.google.common.base.Optional;

public interface FeedStore
{
    FeedEntries retrieveBy(FeedRestrictionCriteria restriction);

    Optional<FeedEntry> retrieveBy(Id id);

    FeedEntry store(FeedEntry feedEntry);
}
