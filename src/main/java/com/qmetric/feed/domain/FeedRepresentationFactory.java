package com.qmetric.feed.domain;

public interface FeedRepresentationFactory<T>
{
    T format(FeedEntries entries);

    T format(FeedEntry entry);
}
