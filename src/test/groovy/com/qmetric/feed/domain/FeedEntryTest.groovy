package com.qmetric.feed.domain

import org.joda.time.DateTime
import spock.lang.Specification

class FeedEntryTest extends Specification {

    def "should evaluate equality"()
    {
        expect:
        final publishedDate = new DateTime()
        new FeedEntry(Id.of("1"), publishedDate, new Payload(["stuff": "1234"])) == new FeedEntry(Id.of("1"), publishedDate, new Payload(["stuff": "1234"]))
        new FeedEntry(Id.of("1"), publishedDate, new Payload(["stuff": "1234"])) != new FeedEntry(Id.of("2"), publishedDate, new Payload(["stuff": "1234"]))
        new FeedEntry(Id.of("1"), publishedDate, new Payload([:])) != new FeedEntry(Id.of("1"), publishedDate, new Payload(["stuff": "1234"]))
        new FeedEntry(Id.of("1"), publishedDate, new Payload(["stuff": "1234"])) != new FeedEntry(Id.of("1"), publishedDate.plusDays(1), new Payload(["stuff": "1234"]))
    }
}
