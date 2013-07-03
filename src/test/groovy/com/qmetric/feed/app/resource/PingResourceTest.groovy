package com.qmetric.feed.app.resource

import spock.lang.Specification

class PingResourceTest extends Specification {

    def "should return 200"()
    {
        given:
        final resource = new PingResource()

        when:
        final response = resource.ping()

        then:
        response.status == 200
    }
}
