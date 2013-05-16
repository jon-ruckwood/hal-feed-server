package com.qmetric.feed;

public interface FeedRepresentationFactory<T>
{
    T format(FeedEntries entries);

    T format(FeedEntry entry);
}
