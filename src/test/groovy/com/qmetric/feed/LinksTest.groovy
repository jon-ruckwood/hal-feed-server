package com.qmetric.feed
import spock.lang.Specification

import static com.google.common.collect.Lists.newArrayList

class LinksTest extends Specification {

    def "should expose all links"()
    {
        expect:
        newArrayList(new Links(newArrayList(new FeedEntryLink("", "", true))).forFeedEntry()) == [new FeedEntryLink("", "", true)]
    }

    def "should expose summarised links"()
    {
        expect:
        final linkToIncludeInSummarisedFeedEntry = new FeedEntryLink("a", "/a", true)
        final linkToExcludeFromSummarisedFeedEntry = new FeedEntryLink("b", "/b", false)
        newArrayList(new Links(newArrayList(linkToIncludeInSummarisedFeedEntry, linkToExcludeFromSummarisedFeedEntry)).forSummarisedFeedEntry()) == [linkToIncludeInSummarisedFeedEntry]
    }
}
