package com.qmetric.feed.app;

import com.google.common.base.Optional;
import com.qmetric.feed.domain.FeedEntries;
import com.qmetric.feed.domain.FeedEntry;
import com.qmetric.feed.domain.FeedStore;
import com.qmetric.feed.domain.Id;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.collect.ImmutableSortedSet.orderedBy;
import static java.util.Collections.synchronizedMap;

public class InMemoryStore implements FeedStore
{
    private final Map<Id, FeedEntry> feed = synchronizedMap(new HashMap<Id, FeedEntry>());

    @Override public FeedEntries retrieveAll()
    {
        return new FeedEntries(orderedBy(new Comparator<FeedEntry>()
        {
            @Override public int compare(final FeedEntry entry1, final FeedEntry entry2)
            {
                return entry2.publishedDate.compareTo(entry1.publishedDate);
            }
        }).addAll(feed.values()).build().asList());
    }

    @Override public Optional<FeedEntry> retrieveBy(final Id id)
    {
        return Optional.fromNullable(feed.get(id));
    }

    @Override public void add(final FeedEntry entry)
    {
        feed.put(entry.id, entry);
    }
}
