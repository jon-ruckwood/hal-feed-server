package com.qmetric.feed.domain

import spock.lang.Specification

import static com.google.common.collect.Lists.newArrayList

class LinksTest extends Specification {

    final customizedSelfLinkForFeedEntry = new FeedEntryLink("self", "/a")

    final additionalLinkForFeedEntry = new FeedEntryLink("custom", "/b")

    def "should expose additional links for entry"()
    {
        expect:
        newArrayList(new Links(newArrayList(customizedSelfLinkForFeedEntry, additionalLinkForFeedEntry)).additionalLinksForFeedEntry()) == [additionalLinkForFeedEntry]
    }

    def "should expose customized self link for entry"()
    {
        expect:
        new Links(newArrayList(additionalLinkForFeedEntry, customizedSelfLinkForFeedEntry)).customizedSelfLinkForFeedEntry().get() == customizedSelfLinkForFeedEntry
    }
}
