package com.qmetric.feed;

import java.util.Map;

public interface FeedEntryPropertiesProvider
{
    Map<String, String> getSummarisedProperties(FeedEntry entry);

    Map<String, String> getProperties(FeedEntry entry);
}
