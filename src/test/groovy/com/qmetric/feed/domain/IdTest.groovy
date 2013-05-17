package com.qmetric.feed.domain

import spock.lang.Specification

class IdTest extends Specification {

    def "should return string representation of id"()
    {
        expect:
        Id.of("1").toString() == "1"
    }

    def "should evaluate equality"()
    {
        expect:
        Id.of("1") == Id.of("1")
        Id.of("1") != Id.of("2")
    }
}
