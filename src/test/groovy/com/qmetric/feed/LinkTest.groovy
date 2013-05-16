package com.qmetric.feed

import spock.lang.Specification

class LinkTest extends Specification {

    def "should evaluate equality"()
    {
        expect:
        new Link("rel", "/href", true) == new Link("rel", "/href", true)
        new Link("rel", "/href", true) != new Link("diff", "/href", true)
        new Link("rel", "/href", true) != new Link("rel", "/diff", true)
        new Link("rel", "/href", true) != new Link("rel", "/href", false)
    }
}
