package com.qmetric.feed.domain
import com.google.common.collect.ImmutableMap
import spock.lang.Specification

class ResourceTest extends Specification {

    def "should evaluate equality"()
    {
        expect:
        new Resource(ImmutableMap.of("stuff", "1234")) == new Resource(ImmutableMap.of("stuff", "1234"))
        new Resource(ImmutableMap.of("stuff", "1234")) != new Resource(ImmutableMap.of("stuff", "4321"))
    }
}
