package com.qmetric.feed.domain;

import com.google.common.base.Optional;

public interface FeedStore
{
    // todo pagination solution needed
    FeedEntries retrieveAll();

    Optional<FeedEntry> retrieveBy(Id id);

    FeedEntry store(FeedEntry feedEntry);
}
