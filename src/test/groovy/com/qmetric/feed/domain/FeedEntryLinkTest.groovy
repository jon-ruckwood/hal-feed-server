package com.qmetric.feed.domain

import spock.lang.Specification

class FeedEntryLinkTest extends Specification {

    def "should evaluate equality"()
    {
        expect:
        new FeedEntryLink("rel", "/href", true) == new FeedEntryLink("rel", "/href", true)
        new FeedEntryLink("rel", "/href", true) != new FeedEntryLink("diff", "/href", true)
        new FeedEntryLink("rel", "/href", true) != new FeedEntryLink("rel", "/diff", true)
        new FeedEntryLink("rel", "/href", true) != new FeedEntryLink("rel", "/href", false)
    }
}
