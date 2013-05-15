package com.qmetric.feed;

import java.net.URI;

public interface FeedUriFactory
{
    URI createForFeedEntry(Id id);

    URI createForFeed();
}
