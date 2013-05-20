package com.qmetric.feed.domain

import spock.lang.Specification

class FeedEntryLinkTest extends Specification {

    def "should evaluate equality"()
    {
        expect:
        new FeedEntryLink("rel", "/href") == new FeedEntryLink("rel", "/href")
        new FeedEntryLink("rel", "/href") != new FeedEntryLink("diff", "/href")
        new FeedEntryLink("rel", "/href") != new FeedEntryLink("rel", "/diff")
    }
}
