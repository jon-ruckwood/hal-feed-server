package com.qmetric.feed.app

import spock.lang.Specification

class UUIDFactoryTest extends Specification {

    def "should create uuid"()
    {
        expect:
        new UUIDFactory().create() != new UUIDFactory().create()
    }
}
