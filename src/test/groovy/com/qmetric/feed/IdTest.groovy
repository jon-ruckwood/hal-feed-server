package com.qmetric.feed

import spock.lang.Specification

class IdTest extends Specification {

    def "should return string representation of id"()
    {
        expect:
        new Id("1").asString() == "1"
    }

    def "should evaluate equality"()
    {
        expect:
        new Id("1") == new Id("1")
        new Id("1") != new Id("2")
    }
}
