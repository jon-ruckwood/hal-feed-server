package com.qmetric.feed.app.resource

import com.google.common.base.Function
import com.google.common.base.Optional
import com.qmetric.feed.domain.*
import com.sun.jersey.api.uri.UriBuilderImpl
import com.theoryinpractise.halbuilder.api.Link
import com.theoryinpractise.halbuilder.api.Representation
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import javax.annotation.Nullable
import javax.ws.rs.core.UriInfo

import static com.qmetric.feed.domain.FeedRestrictionCriteria.Filter
import static com.theoryinpractise.halbuilder.api.RepresentationFactory.HAL_JSON
import static org.joda.time.DateTime.now

class FeedResourceTest extends Specification {

    @Shared def responseBody = "response body"

    @Shared def selfLinkHref = "/self"

    @Shared def entries = new FeedEntries([])

    @Shared def feedEntry = new FeedEntry(Id.of("1"), now(), new Payload(["stuff": "1234"]))

    final Feed feed = Mock(Feed)

    final FeedRepresentationFactory<Representation> feedRepresentationFactory = Mock(FeedRepresentationFactory)

    final Representation representation = Mock(Representation)

    final Link link = Mock(Link)

    final feedResource = new FeedResource('http://localhost:8080', feed, feedRepresentationFactory, 10)

    def setup()
    {
        representation.toString(HAL_JSON) >> responseBody
        feedRepresentationFactory.format(_ as URI, feedEntry) >> representation
        feedRepresentationFactory.format(_ as URI, entries) >> representation
    }

    def "should return 201 with response body representation of published entry"()
    {
        given:
        feed.publish(feedEntry.payload) >> feedEntry
        link.getHref() >> selfLinkHref
        representation.getResourceLink() >> link

        when:
        final response = feedResource.postEntry(null, null, feedEntry.payload)

        then:
        response.entity == responseBody
        response.status == 201
        response.metadata.get("Location").first() == new URI(selfLinkHref)
    }

    def "should return 400 when attempting to publish invalid entry to feed"()
    {
        given:
        feed.publish(feedEntry.payload) >> { throw new IllegalArgumentException("Illegal argument") }

        when:
        final response = feedResource.postEntry(null, null, feedEntry.payload)

        then:
        response.entity == "Illegal argument"
        response.status == 400
    }

    def "should return response body representation for existing feed entry"()
    {
        given:
        feed.retrieveBy(feedEntry.id) >> Optional.of(feedEntry)

        when:
        final response = feedResource.getEntry(null, null, feedEntry.id.toString())

        then:
        response.entity == "response body"
        response.status == 200
    }

    def "should return 404 with empty response body when no existing feed entry found"()
    {
        given:
        feed.retrieveBy(feedEntry.id) >> Optional.absent()

        when:
        final response = feedResource.getEntry(null, null, feedEntry.id.toString())

        then:
        response.status == 404
        response.entity == "Feed entry not found"
    }

    @Unroll def "should return expected response for feed page request"()
    {
        given:
        if (error)
        {
            feed.retrieveBy(criteria) >> { throw new IllegalArgumentException("Illegal argument") }
        }
        else
        {
            feed.retrieveBy(criteria) >> pageResult
        }

        when:
        final response = feedResource.getPage(null, null, optionalIdToOptionalStr(criteria.filter.earlierThan), optionalIdToOptionalStr(criteria.filter.laterThan), Optional.of(criteria.limit))

        then:
        response.status == expectedStatus
        response.entity == expectedBody

        where:
        criteria                                                                                | pageResult | error | expectedBody       | expectedStatus
        new FeedRestrictionCriteria(new Filter(Optional.absent(), Optional.absent()), 10)       | entries    | false | responseBody       | 200
        new FeedRestrictionCriteria(new Filter(Optional.of(Id.of("5")), Optional.absent()), 10) | entries    | false | responseBody       | 200
        new FeedRestrictionCriteria(new Filter(Optional.absent(), Optional.of(Id.of("5"))), 10) | entries    | false | responseBody       | 200
        new FeedRestrictionCriteria(new Filter(Optional.absent(), Optional.absent()), 10)       | _          | true  | "Illegal argument" | 400
    }

    def "getEntry should use URI from request when resource is not configured with a 'publicBaseUrl'"()
    {
        given:
        def uri = new URI('http://www.example.org/')
        def uriInfo = Mock(UriInfo)
        uriInfo.getBaseUriBuilder() >> new UriBuilderImpl().uri(uri)

        and:
        feed.retrieveBy(feedEntry.id) >> Optional.of(feedEntry)

        and:
        def feedResourceNoPublicBaseUrl = new FeedResource(null, feed, feedRepresentationFactory, 10)

        when:
        feedResourceNoPublicBaseUrl.getEntry(uriInfo, null, feedEntry.id.toString())

        then:
        1 * feedRepresentationFactory.format(new URI('http://www.example.org/feed'), feedEntry) >> representation
    }

    def "getPage should use URI from request when resource is not configured with a 'publicBaseUrl'"()
    {
        given:
        def uri = new URI('http://www.example.org/')
        def uriInfo = Mock(UriInfo)
        uriInfo.getBaseUriBuilder() >> new UriBuilderImpl().uri(uri)

        and:
        feed.retrieveBy(_ as FeedRestrictionCriteria) >> entries

        and:
        def feedResourceNoPublicBaseUrl = new FeedResource(null, feed, feedRepresentationFactory, 10)

        when:
        feedResourceNoPublicBaseUrl.getPage(uriInfo, null, Optional.absent(), Optional.of('1'), Optional.of(10))

        then:
        1 * feedRepresentationFactory.format(new URI('http://www.example.org/feed'), entries) >> representation
    }

    def "postEntry should use URI from request when resource is not configured with a 'publicBaseUrl'"()
    {
        given:
        def uri = new URI('http://www.example.org/')
        def uriInfo = Mock(UriInfo)
        uriInfo.getBaseUriBuilder() >> new UriBuilderImpl().uri(uri)

        and:
        feed.publish(_ as Payload) >> feedEntry
        link.getHref() >> selfLinkHref
        representation.getResourceLink() >> link

        and:
        def feedResourceNoPublicBaseUrl = new FeedResource(null, feed, feedRepresentationFactory, 10)

        when:
        feedResourceNoPublicBaseUrl.postEntry(uriInfo, null, feedEntry.payload)

        then:
        1 * feedRepresentationFactory.format(new URI('http://www.example.org/feed'), feedEntry) >> representation
    }

    private static Optional<String> optionalIdToOptionalStr(final Optional<Id> optionalId)
    {
        optionalId.transform(new Function<Id, String>() {
            @Override String apply(@Nullable final Id input)
            {
                return input.toString()
            }
        })
    }
}
