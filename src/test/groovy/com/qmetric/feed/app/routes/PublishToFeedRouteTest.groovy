package com.qmetric.feed.app.routes

import com.qmetric.feed.app.PayloadSerializationMapper
import com.qmetric.feed.domain.*
import com.theoryinpractise.halbuilder.api.Link
import com.theoryinpractise.halbuilder.api.Representation
import spark.Request
import spark.Response
import spock.lang.Specification

import static com.theoryinpractise.halbuilder.api.RepresentationFactory.HAL_JSON
import static org.joda.time.DateTime.now

class PublishToFeedRouteTest extends Specification {

    final selfLinkHref = "/self"

    final link = Mock(Link)

    final request = Mock(Request)

    final response = Mock(Response)

    final feed = Mock(Feed)

    final expectedRepresentation = Mock(Representation)

    final feedRepresentationFactory = Mock(FeedRepresentationFactory)

    final route = new PublishToFeedRoute("", feed, feedRepresentationFactory, new PayloadSerializationMapper())

    def "should return 201 with response body representation of added entry"()
    {
        given:
        final expectedFeedEntry = new FeedEntry(Id.of("1"), now(), new Payload(Collections.<String, Object>singletonMap("stuff", "1234")))
        request.body() >> "{\"stuff\": \"1234\"}"
        feed.publish(expectedFeedEntry.payload) >> expectedFeedEntry
        feedRepresentationFactory.format(expectedFeedEntry) >> expectedRepresentation
        link.getHref() >> selfLinkHref
        expectedRepresentation.getResourceLink() >> link
        expectedRepresentation.toString(HAL_JSON) >> "response body"

        when:
        final responseBody = route.handle(request, response)

        then:
        responseBody == "response body"
        1 * response.status(201)
        1 * response.header("Location", selfLinkHref)
    }

    def "should return 400 with exception in response body when request body contains invalid json"()
    {
        given:
        final requestBodyWithInvalidJson = "{"
        request.body() >> requestBodyWithInvalidJson

        when:
        final responseBody = route.handle(request, response) as String

        then:
        responseBody.contains("Exception")
        1 * response.status(400)
    }
}
