package com.qmetric.feed.app.support

import com.qmetric.feed.domain.FeedStore
import spock.lang.Specification

class FeedStoreConnectivityHealthCheckTest extends Specification {

    final feedStore = Mock(FeedStore)

    final healthCheck = new FeedStoreConnectivityHealthCheck(feedStore)

    def "healthy when connection to feed store ok"()
    {
        expect:
        healthCheck.check().isHealthy()
    }

    def "unhealthy when cannot connect to feed store"()
    {
        given:
        feedStore.checkConnectivity() >> {throw new RuntimeException()}

        expect:
        !healthCheck.check().isHealthy()
    }
}
