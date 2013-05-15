package com.qmetric.feed

import org.joda.time.DateTime
import spock.lang.Specification

import static java.util.Collections.emptyMap
import static java.util.Collections.singletonMap

class FeedEntryTest extends Specification {

    def "should evaluate equality"()
    {
        expect:
        final publishedDate = new DateTime()
        new FeedEntry(new Id("1"), publishedDate, new Resource(singletonMap("stuff", "1234"))) == new FeedEntry(new Id("1"), publishedDate, new Resource(singletonMap("stuff", "1234")))
        new FeedEntry(new Id("1"), publishedDate, new Resource(singletonMap("stuff", "1234"))) != new FeedEntry(new Id("2"), publishedDate, new Resource(singletonMap("stuff", "1234")))
        new FeedEntry(new Id("1"), publishedDate, new Resource(emptyMap())) != new FeedEntry(new Id("1"), publishedDate, new Resource(singletonMap("stuff", "1234")))
        new FeedEntry(new Id("1"), publishedDate, new Resource(singletonMap("stuff", "1234"))) != new FeedEntry(new Id("1"), publishedDate.plusDays(1), new Resource(singletonMap("stuff", "1234")))
    }
}
