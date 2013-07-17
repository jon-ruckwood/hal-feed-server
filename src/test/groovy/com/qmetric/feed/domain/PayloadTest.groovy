package com.qmetric.feed.domain

import spock.lang.Specification

class PayloadTest extends Specification {

    def "should evaluate equality"()
    {
        expect:
        new Payload(["stuff": "1234"]) == new Payload(["stuff": "1234"])
        new Payload(["stuff": "1234"]) != new Payload(["stuff": "4321"])
    }
}
