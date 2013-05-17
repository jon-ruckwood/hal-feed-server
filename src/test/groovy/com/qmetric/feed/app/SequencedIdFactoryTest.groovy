package com.qmetric.feed.app

import spock.lang.Specification

import static java.lang.Integer.parseInt

class SequencedIdFactoryTest extends Specification {

    def "should create unique ids"()
    {
        given:
        final idFactory = new SequencedIdFactory()

        when:
        final id1 = idFactory.create()
        final id2 = idFactory.create()

        then:
        parseInt(id1.toString()) + 1 == parseInt(id2.toString())
    }
}
