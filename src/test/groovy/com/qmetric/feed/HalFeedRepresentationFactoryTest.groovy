package com.qmetric.feed
import com.google.common.collect.ImmutableMap
import groovy.json.JsonSlurper
import org.joda.time.DateTime
import spock.lang.Specification

import static com.theoryinpractise.halbuilder.api.RepresentationFactory.HAL_JSON
import static java.util.Collections.singleton

class HalFeedRepresentationFactoryTest extends Specification {

    final jsonSlurper = new JsonSlurper()

    final feedUri = new URI('http://localhost:1234/test-feed')

    final entry1 = new FeedEntry(Id.of("1"), new DateTime(2013, 5, 13, 11, 2, 32), new Resource(ImmutableMap.of("stuff", "aaaa", "value", "2000")))

    final entry2 = new FeedEntry(Id.of("2"), new DateTime(2013, 5, 14, 11, 2, 32), new Resource(ImmutableMap.of("stuff", "bbbb", "value", "1000")))

    def "should return hal+json representation of entries"()
    {
        given:
        final factory = new HalFeedRepresentationFactory(feedUri)
        final entries = new FeedEntries([entry2, entry1])

        when:
        final hal = factory.format(entries)

        then:
        jsonSlurper.parseText(hal.toString(HAL_JSON)) == jsonSlurper.parseText(this.getClass().getResource('/assets/expectedHalWithMultipleEntries.json').text)
    }

    def "should return hal+json representation of entries with restricted resource attributes"()
    {
        given:
        final factory = new HalFeedRepresentationFactory(feedUri, singleton('stuff'))
        final entries = new FeedEntries([entry2, entry1])

        when:
        final hal = factory.format(entries)

        then:
        jsonSlurper.parseText(hal.toString(HAL_JSON)) == jsonSlurper.parseText(this.getClass().getResource('/assets/expectedHalWithMultipleSummarisedEntries.json').text)
    }

    def "should return hal+json representation of entry"()
    {
        given:
        final factory = new HalFeedRepresentationFactory(feedUri)
        final entry = entry1

        when:
        final hal = factory.format(entry)

        then:
        jsonSlurper.parseText(hal.toString(HAL_JSON)) == jsonSlurper.parseText(this.getClass().getResource('/assets/expectedHalWithSingleEntry.json').text)
    }
}
