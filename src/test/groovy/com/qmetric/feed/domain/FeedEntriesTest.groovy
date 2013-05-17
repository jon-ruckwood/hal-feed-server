package com.qmetric.feed.domain

import spock.lang.Specification

import static java.util.Collections.singletonList

class FeedEntriesTest extends Specification {

    def "should hold collection of entries"()
    {
        expect:
        final entriesList = singletonList(new FeedEntry(Id.of("1"), null, null))
        new FeedEntries(entriesList).all() == entriesList
    }

    def "should evaluate equality"()
    {
        expect:
        new FeedEntries(singletonList(new FeedEntry(Id.of("1"), null, null))) == new FeedEntries(singletonList(new FeedEntry(Id.of("1"), null, null)))
        new FeedEntries(singletonList(new FeedEntry(Id.of("1"), null, null))) != new FeedEntries(singletonList(new FeedEntry(Id.of("2"), null, null)))
    }
}
