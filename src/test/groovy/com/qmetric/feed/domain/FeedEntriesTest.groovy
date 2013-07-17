package com.qmetric.feed.domain

import spock.lang.Specification

import static java.util.Collections.singletonList

class FeedEntriesTest extends Specification {

    def "should hold collection of entries"()
    {
        expect:
        final entry = new FeedEntry(Id.of("1"), null, null)
        final entriesList = [entry]
        new FeedEntries(entriesList).all() == entriesList
        new FeedEntries(entriesList).first().get() == entry
        new FeedEntries(entriesList).last().get() == entry
    }

    def "should know whether more entries exist on previous/ next pages when at least one entry on current page"()
    {
        expect:
        !new FeedEntries([], true, true).earlierExists
        !new FeedEntries([], true, true).laterExists
        new FeedEntries([Mock(FeedEntry)], true, true).earlierExists
        new FeedEntries([Mock(FeedEntry)], true, true).laterExists
    }

    def "should evaluate equality"()
    {
        expect:
        new FeedEntries(singletonList(new FeedEntry(Id.of("1"), null, null))) == new FeedEntries([new FeedEntry(Id.of("1"), null, null)])
        new FeedEntries(singletonList(new FeedEntry(Id.of("1"), null, null))) != new FeedEntries([new FeedEntry(Id.of("2"), null, null)])
    }
}
