package com.qmetric.feed.app.routes

import com.google.common.base.Optional
import com.qmetric.feed.domain.*
import com.theoryinpractise.halbuilder.api.Representation
import spark.Request
import spark.Response
import spock.lang.Specification

import static com.theoryinpractise.halbuilder.api.RepresentationFactory.HAL_JSON
import static java.util.Collections.emptyMap
import static org.joda.time.DateTime.now

class RetrieveEntryFromFeedRouteTest extends Specification {

    final request = Mock(Request)

    final response = Mock(Response)

    final feed = Mock(Feed)

    final expectedRepresentation = Mock(Representation)

    final feedRepresentationFactory = Mock(FeedRepresentationFactory)

    final route = new RetrieveEntryFromFeedRoute("", feed, feedRepresentationFactory)

    def "should return response body representation for existing feed entry"()
    {
        given:
        request.params("id") >> "1"
        final feedEntry = new FeedEntry(Id.of("1"), now(), new Payload(emptyMap()))
        feed.retrieveBy(Id.of("1")) >> Optional.of(feedEntry)
        feedRepresentationFactory.format(feedEntry) >> expectedRepresentation
        expectedRepresentation.toString(HAL_JSON) >> "response body"

        when:
        final responseBody = route.handle(request, response)

        then:
        responseBody == "response body"
        0 * response.status(_)
    }

    def "should return 404 with empty response body when no existing feed entry found"()
    {
        given:
        request.params("id") >> "1"
        feed.retrieveBy(Id.of("1")) >> Optional.absent()

        when:
        final responseBody = route.handle(request, response)

        then:
        responseBody == null
        1 * response.status(404)
    }
}
