package com.qmetric.feed;

import com.google.common.base.Optional;

public interface FeedStore
{
    // todo pagination solution needed
    FeedEntries retrieveAll();

    Optional<FeedEntry> retrieveBy(Id id);

    void add(FeedEntry feedEntry);
}
