package com.qmetric.feed.app

import com.fasterxml.jackson.databind.ObjectMapper
import com.sun.jersey.api.client.Client
import com.sun.jersey.client.impl.ClientRequestImpl
import com.sun.jersey.core.util.MultivaluedMapImpl
import com.theoryinpractise.halbuilder.DefaultRepresentationFactory
import com.yammer.dropwizard.testing.junit.DropwizardServiceRule
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import javax.ws.rs.core.MultivaluedMap

import static java.util.Collections.emptyMap
import static java.util.Collections.singletonMap
import static org.apache.commons.collections.CollectionUtils.isNotEmpty

class FeedServerIntegrationTest extends Specification {

    @Shared def DropwizardServiceRule<ServerConfiguration> server

    def setupSpec()
    {
        server = new DropwizardServiceRule<ServerConfiguration>(Main.class, this.getClass().getResource("/config-samples/test-integration-server-config.yml").path)
        //noinspection GroovyAccessibility
        server.startIfRequired()
    }

    @Unroll
    def "should post new entry to feed"()
    {
        when:
        final resource = post(appUrl("/feed/"), toJson(payloadAttributes))

        then:
        resource.status == expectedStatus

        where:
        payloadAttributes                       | expectedStatus
        singletonMap("testPayloadAttr", "1234") | 201
        singletonMap("mistyped-testPayloadAttr", "1234") | 400
        emptyMap()                              | 400
    }

    def "should retrieve feed"()
    {
        given:
        post(appUrl("/feed/"), toJson(singletonMap("testPayloadAttr", "1234")))

        when:
        final resource = get(appUrl("/feed/"))

        then:
        resource.status == 200
        isNotEmpty(new DefaultRepresentationFactory().readRepresentation(new InputStreamReader(resource.getEntityInputStream())).getResourcesByRel("entries"))
    }

    def "should retrieve latest page of feed entries"()
    {
        given:
        post(appUrl("/feed/"), toJson(singletonMap("testPayloadAttr", "1234")))

        when:
        final resource = get(appUrl("/feed/experimental"))

        then:
        resource.status == 200
        isNotEmpty(new DefaultRepresentationFactory().readRepresentation(new InputStreamReader(resource.getEntityInputStream())).getResourcesByRel("entries"))
    }

    def "should retrieve existing entry from feed"()
    {
        given:
        final halResponse = new DefaultRepresentationFactory().readRepresentation(new InputStreamReader(post(appUrl("/feed/"), singletonMap("testPayloadAttr", "1234")).getEntityInputStream()))

        when:
        final resource = get(appUrl("/feed/") + halResponse.getValue("_id"))

        then:
        resource.status == 200
    }

    def "should return 404 when requesting an entry that does not exist in feed"()
    {
        when:
        final resource = get(appUrl("/feed/unknown"))

        then:
        resource.status == 404
    }

    @Unroll
    def "should return health check and metrics"()
    {
        when:
        final resource = get(url)

        then:
        resource.status == 200

        where:
        url << [appUrl("/ping"), adminUrl("/ping"), adminUrl("/healthcheck"), adminUrl("/metrics?pretty=true"), adminUrl("/threads")]
    }

    def cleanupSpec()
    {
        //noinspection GroovyAccessibility
        server.jettyServer.stop()
    }

    private static get(final path)
    {
        new Client().handle(new ClientRequestImpl(new URI(path), "GET"))
    }

    private static post(final path, final body)
    {
        final requestHeaders = new MultivaluedMapImpl()
        requestHeaders.putSingle("Content-Type", "application/hal+json")

        new Client().handle(new ClientRequestImpl(new URI(path), "POST", body, requestHeaders as MultivaluedMap<String, Object>))
    }

    private static toJson(body)
    {
        new ObjectMapper().writeValueAsString(body)
    }

    private appUrl(path)
    {
        "http://localhost:" + server.getLocalPort() + path
    }

    private adminUrl(path)
    {
        "http://localhost:" + server.configuration.httpConfiguration.adminPort + path
    }
}
