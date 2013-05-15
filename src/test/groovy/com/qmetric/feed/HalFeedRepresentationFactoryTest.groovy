package com.qmetric.feed
import com.google.common.collect.ImmutableMap
import groovy.json.JsonSlurper
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import spock.lang.Specification

import static com.theoryinpractise.halbuilder.api.RepresentationFactory.HAL_JSON

class HalFeedRepresentationFactoryTest extends Specification {

    final jsonSlurper = new JsonSlurper()

    final uriFactory = Mock(FeedUriFactory)

    final factory = new HalFeedRepresentationFactory(uriFactory, new FeedEntryPropertiesProviderImpl())

    def setup() {
        uriFactory.createForFeed() >> new URI("http://localhost:1234/test-feed")
        uriFactory.createForFeedEntry(new Id("1")) >> new URI("http://localhost:1234/test-feed/1")
        uriFactory.createForFeedEntry(new Id("2")) >> new URI("http://localhost:1234/test-feed/2")
    }

    def "should return hal+json representation of entries"()
    {
        given:
        final entries = [//
                new FeedEntry(new Id("2"), new DateTime(2013, 5, 14, 11, 2, 32), new Resource(ImmutableMap.of("stuff", "bbbb", "value", "1000"))), //
                new FeedEntry(new Id("1"), new DateTime(2013, 5, 13, 11, 2, 32), new Resource(ImmutableMap.of("stuff", "aaaa", "value", "2000"))) //
        ]

        when:
        final hal = factory.format(entries)

        then:
        jsonSlurper.parseText(hal.toString(HAL_JSON)) == jsonSlurper.parseText(this.getClass().getResource('/assets/expectedHalWithMultipleEntries.json').text)
    }

    def "should return hal+json representation of entry"()
    {
        given:
        final entry = new FeedEntry(new Id("2"), new DateTime(2013, 5, 14, 11, 2, 32), new Resource(ImmutableMap.of("stuff", "bbbb", "value", "1000")))

        when:
        final hal = factory.format(entry)

        then:
        jsonSlurper.parseText(hal.toString(HAL_JSON)) == jsonSlurper.parseText(this.getClass().getResource('/assets/expectedHalWithSingleEntry.json').text)
    }

    class FeedEntryPropertiesProviderImpl implements FeedEntryPropertiesProvider
    {
        final dateFormatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss")

        @Override Map<String, String> getSummarisedProperties(final FeedEntry entry)
        {
            return ImmutableMap.builder()
                    .put("stuff", entry.resource.attributes.get("stuff"))
                    .put("published", dateFormatter.print(entry.publishedDate)).build()
        }

        @Override Map<String, String> getProperties(final FeedEntry entry)
        {
            return ImmutableMap.builder()
                    .putAll(entry.resource.attributes)
                    .put("published", dateFormatter.print(entry.publishedDate)).build()
        }
    }
}
