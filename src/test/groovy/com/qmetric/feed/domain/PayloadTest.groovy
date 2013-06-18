package com.qmetric.feed.domain
import com.google.common.collect.ImmutableMap
import spock.lang.Specification

class PayloadTest extends Specification {

    def "should evaluate equality"()
    {
        expect:
        new Payload(ImmutableMap.of("stuff", "1234")) == new Payload(ImmutableMap.of("stuff", "1234"))
        new Payload(ImmutableMap.of("stuff", "1234")) != new Payload(ImmutableMap.of("stuff", "4321"))
    }
}
