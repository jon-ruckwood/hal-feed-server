package com.qmetric.feed.app.routes

import com.google.common.base.Optional
import com.qmetric.feed.domain.*
import com.theoryinpractise.halbuilder.api.Representation
import spark.Request
import spark.Response
import spock.lang.Specification

import static com.theoryinpractise.halbuilder.api.RepresentationFactory.HAL_JSON
import static java.util.Collections.emptyList

class RetrieveFromFeedRouteTest extends Specification {

    final expectedBody = "response body"

    final entries = new FeedEntries(emptyList())

    final request = Mock(Request)

    final response = Mock(Response)

    final expectedRepresentation = Mock(Representation)

    final feed = Mock(Feed)

    final feedRepresentationFactory = Mock(FeedRepresentationFactory)

    final route = new RetrieveFromFeedRoute("", feed, feedRepresentationFactory)

    def setup()
    {
        expectedRepresentation.toString(HAL_JSON) >> expectedBody
        feedRepresentationFactory.format(entries) >> expectedRepresentation
    }

    def "should return response body representation of latest feed page"()
    {
        given:
        feed.retrieveBy(new FeedRestrictionCriteria(new FeedRestrictionCriteria.Filter(Optional.absent(), Optional.absent()), 10)) >> entries

        when:
        final responseBody = route.handle(request, response)

        then:
        responseBody == expectedBody
    }

    def "should return response body representation of feed page restricted by earlerThan clause"()
    {
        given:
        request.queryParams("earlierThan") >> "5"
        feed.retrieveBy(new FeedRestrictionCriteria(new FeedRestrictionCriteria.Filter(Optional.of(Id.of("5")), Optional.absent()), 10)) >> entries

        when:
        final responseBody = route.handle(request, response)

        then:
        responseBody == expectedBody
    }

    def "should return response body representation of feed page restricted by laterThan clause"()
    {
        given:
        request.queryParams("laterThan") >> "5"
        feed.retrieveBy(new FeedRestrictionCriteria(new FeedRestrictionCriteria.Filter(Optional.absent(), Optional.of(Id.of("5"))), 10)) >> entries

        when:
        final responseBody = route.handle(request, response)

        then:
        responseBody == expectedBody
    }

    def "should return response body representation of latest feed page with given max results limit"()
    {
        given:
        request.queryParams("limit") >> "25"
        feed.retrieveBy(new FeedRestrictionCriteria(new FeedRestrictionCriteria.Filter(Optional.absent(), Optional.absent()), 25)) >> entries

        when:
        final responseBody = route.handle(request, response)

        then:
        responseBody == expectedBody
    }

    def "should return 400 when illegal arguments given in request"()
    {
        given:
        feed.retrieveBy(_) >> {throw new IllegalArgumentException()}

        when:
        route.handle(request, response)

        then:
        response.status(400)
    }

    def "should return 400 when illegal state while processing request"()
    {
        given:
        feed.retrieveBy(_) >> {throw new IllegalStateException()}

        when:
        route.handle(request, response)

        then:
        response.status(400)
    }
}
