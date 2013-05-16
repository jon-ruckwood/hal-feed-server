package com.qmetric.feed;

import com.google.common.base.Optional;

import java.util.Collection;

public interface FeedStore
{
    // todo pagination solution needed
    Collection<FeedEntry> retrieveAll();

    Optional<FeedEntry> retrieve(Id id);

    void add(FeedEntry feedEntry);
}
