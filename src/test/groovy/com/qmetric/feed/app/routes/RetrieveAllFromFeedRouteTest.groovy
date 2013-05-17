package com.qmetric.feed.app.routes

import com.qmetric.feed.domain.Feed
import com.qmetric.feed.domain.FeedEntries
import com.qmetric.feed.domain.FeedRepresentationFactory
import com.theoryinpractise.halbuilder.api.Representation
import spark.Request
import spark.Response
import spock.lang.Specification

import static com.theoryinpractise.halbuilder.api.RepresentationFactory.HAL_JSON
import static java.util.Collections.emptyList

class RetrieveAllFromFeedRouteTest extends Specification {

    final request = Mock(Request)

    final response = Mock(Response)

    final expectedRepresentation = Mock(Representation)

    final feed = Mock(Feed)

    final feedRepresentationFactory = Mock(FeedRepresentationFactory)

    final route = new RetrieveAllFromFeedRoute("", feed, feedRepresentationFactory)

    def "should return response body representation of feed"()
    {
        given:
        final entries = new FeedEntries(emptyList())
        feed.retrieveAll() >> entries
        feedRepresentationFactory.format(entries) >> expectedRepresentation
        expectedRepresentation.toString(HAL_JSON) >> "response body"

        when:
        final responseBody = route.handle(request, response)

        then:
        responseBody == "response body"
    }
}
