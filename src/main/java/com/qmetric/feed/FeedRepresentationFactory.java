package com.qmetric.feed;

import java.util.Collection;

public interface FeedRepresentationFactory<T>
{
    T format(Collection<FeedEntry> entries);

    T format(FeedEntry entry);
}
