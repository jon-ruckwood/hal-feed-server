package com.qmetric.feed
import spock.lang.Specification

import static com.google.common.collect.Lists.newArrayList

class LinksTest extends Specification {

    def "should expose all links"()
    {
        expect:
        newArrayList(new Links(newArrayList(new Link("", "", true))).all()) == [new Link("", "", true)]
    }

    def "should expose summarised links"()
    {
        expect:
        final linkForSummary = new Link("a", "/a", true)
        final linkNotForSummary = new Link("b", "/b", false)
        newArrayList(new Links(newArrayList(linkForSummary, linkNotForSummary)).allForSummary()) == [linkForSummary]
    }
}
