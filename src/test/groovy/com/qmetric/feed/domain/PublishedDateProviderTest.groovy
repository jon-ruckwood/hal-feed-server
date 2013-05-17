package com.qmetric.feed.domain

import spock.lang.Specification

class PublishedDateProviderTest extends Specification {

    def "should create published date"()
    {
        expect:
        new PublishedDateProvider().getPublishedDate()
    }
}
