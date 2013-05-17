package com.qmetric.feed.app.routes
import spark.Request
import spark.Response
import spock.lang.Specification

class PingRouteTest extends Specification {

    final route = new PingRoute("")

    def "should return pong for ping"()
    {
        expect:
        route.handle(Mock(Request), Mock(Response)) == 'pong'
    }
}
