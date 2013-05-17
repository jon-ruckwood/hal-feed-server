package com.qmetric.feed.app.routes

import com.fasterxml.jackson.databind.ObjectMapper
import com.qmetric.feed.domain.*
import com.theoryinpractise.halbuilder.api.Representation
import spark.Request
import spark.Response
import spock.lang.Specification

import static com.theoryinpractise.halbuilder.api.RepresentationFactory.HAL_JSON
import static org.joda.time.DateTime.now

class PublishToFeedRouteTest extends Specification {

    final request = Mock(Request)

    final response = Mock(Response)

    final feed = Mock(Feed)

    final expectedRepresentation = Mock(Representation)

    final feedRepresentationFactory = Mock(FeedRepresentationFactory)

    final route = new PublishToFeedRoute("", feed, feedRepresentationFactory, new ObjectMapper())

    def "should return 201 with response body representation of added entry"()
    {
        given:
        final expectedFeedEntry = new FeedEntry(Id.of("1"), now(), new Resource(Collections.<String, Object>singletonMap("stuff", "1234")))
        request.body() >> "{\"stuff\": \"1234\"}"
        feed.publish(expectedFeedEntry.resource) >> expectedFeedEntry
        feedRepresentationFactory.format(expectedFeedEntry) >> expectedRepresentation
        expectedRepresentation.toString(HAL_JSON) >> "response body"

        when:
        final responseBody = route.handle(request, response)

        then:
        responseBody == "response body"
        1 * response.status(201)
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
