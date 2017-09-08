package com.qmetric.feed.domain;

import java.net.URI;

public interface FeedRepresentationFactory<T>
{
    T format(URI feedUri, FeedEntries entries);

    T format(URI feedUri, FeedEntry entry);
}
