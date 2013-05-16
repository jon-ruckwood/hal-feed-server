package com.qmetric.feed
import com.google.common.collect.ImmutableMap
import groovy.json.JsonSlurper
import org.joda.time.DateTime
import spock.lang.Specification

import static com.theoryinpractise.halbuilder.api.RepresentationFactory.HAL_JSON

class HalFeedRepresentationFactoryTest extends Specification {

    final jsonSlurper = new JsonSlurper()

    final uriFactory = Mock(FeedUriFactory)

    final entry1 = new FeedEntry(Id.of("1"), new DateTime(2013, 5, 13, 11, 2, 32), new Resource(ImmutableMap.of("stuff", "aaaa", "value", "2000")))

    final entry2 = new FeedEntry(Id.of("2"), new DateTime(2013, 5, 14, 11, 2, 32), new Resource(ImmutableMap.of("stuff", "bbbb", "value", "1000")))

    final factory = new HalFeedRepresentationFactory(uriFactory, new ResourceAttributesSummaryProvider() {
        @Override Map<String, String> filterAttributesForSummary(final Resource resource)
        {
            return ImmutableMap.builder().put("stuff", resource.attributes.get("stuff")).build();
        }
    })

    def setup() {
        uriFactory.createForFeed() >> new URI("http://localhost:1234/test-feed")
        uriFactory.createForFeedEntry(Id.of("1")) >> new URI("http://localhost:1234/test-feed/1")
        uriFactory.createForFeedEntry(Id.of("2")) >> new URI("http://localhost:1234/test-feed/2")
    }

    def "should return hal+json representation of entries"()
    {
        given:
        final entries = new FeedEntries([entry2, entry1])

        when:
        final hal = factory.format(entries)

        then:
        jsonSlurper.parseText(hal.toString(HAL_JSON)) == jsonSlurper.parseText(this.getClass().getResource('/assets/expectedHalWithMultipleEntries.json').text)
    }

    def "should return hal+json representation of entry"()
    {
        given:
        final entry = entry1

        when:
        final hal = factory.format(entry)

        then:
        jsonSlurper.parseText(hal.toString(HAL_JSON)) == jsonSlurper.parseText(this.getClass().getResource('/assets/expectedHalWithSingleEntry.json').text)
    }
}
