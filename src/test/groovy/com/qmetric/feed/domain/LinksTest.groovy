package com.qmetric.feed.domain

import spock.lang.Specification

import static com.google.common.collect.Lists.newArrayList

class LinksTest extends Specification {

    final additionalLinkForFeedEntry = new FeedEntryLink("custom", "/b")

    def "should expose additional links for entry"()
    {
        expect:
        newArrayList(new Links([additionalLinkForFeedEntry]).additionalLinksForFeedEntry()) == [additionalLinkForFeedEntry]
    }
}
