package com.qmetric.feed.app

import com.qmetric.feed.domain.*
import groovy.json.JsonSlurper
import org.joda.time.DateTime
import spock.lang.Specification

import static com.qmetric.feed.domain.Links.NO_LINKS
import static com.theoryinpractise.halbuilder.api.RepresentationFactory.HAL_JSON

class HalFeedRepresentationFactoryTest extends Specification {

    final feedName = "Test feed"

    final jsonSlurper = new JsonSlurper()

    final feedUri = new URI('http://localhost:1234/feed')

    final entry1 = new FeedEntry(Id.of("1"), new DateTime(2013, 5, 13, 11, 2, 32), new Payload(["someId": "aaaa", "value": "2000"]))

    final entry2 = new FeedEntry(Id.of("2"), new DateTime(2013, 5, 14, 11, 2, 32), new Payload(["someId": "bbbb", "value": "1000"]))

    def "should return hal+json representation of entries"()
    {
        given:
        final factory = new HalFeedRepresentationFactory(feedName, NO_LINKS, HiddenPayloadAttributes.NONE)
        final entries = new FeedEntries([entry2, entry1])

        when:
        final hal = factory.format(feedUri, entries)

        then:
        jsonSlurper.parseText(hal.toString(HAL_JSON)) == jsonFrom('/feed-samples/expectedHalWithMultipleEntries.json')
    }

    def "should return hal+json representation of entries with custom links"()
    {
        given:
        final factory = new HalFeedRepresentationFactory(feedName, new Links([new FeedEntryLink("someLink", "http://other"), new FeedEntryLink("someLinkWithNamedParam", "http://other/{someId}")]), HiddenPayloadAttributes.NONE)
        final entries = new FeedEntries([entry2, entry1])

        when:
        final hal = factory.format(feedUri, entries)

        then:
        jsonSlurper.parseText(hal.toString(HAL_JSON)) == jsonFrom('/feed-samples/expectedHalWithMultipleEntriesWithCustomLinks.json')
    }

    def "should return hal+json representation of entry"()
    {
        given:
        final factory = new HalFeedRepresentationFactory(feedName, NO_LINKS, HiddenPayloadAttributes.NONE)
        final entry = entry1

        when:
        final hal = factory.format(feedUri, entry)

        then:
        jsonSlurper.parseText(hal.toString(HAL_JSON)) == jsonFrom('/feed-samples/expectedHalWithSingleEntry.json')
    }

    def "should return hal+json representation of entry with complex payload"()
    {
        given:
        final factory = new HalFeedRepresentationFactory(feedName, NO_LINKS, HiddenPayloadAttributes.NONE)
        final entry = new FeedEntry(Id.of("1"), new DateTime(2013, 5, 13, 11, 2, 32), new Payload(["nested": ["someId": "aaaa"], "arr": ["a", "b", "c"]]))

        when:
        final hal = factory.format(feedUri, entry)

        then:
        jsonSlurper.parseText(hal.toString(HAL_JSON)) == jsonFrom('/feed-samples/expectedHalWithSingleEntryWithComplexPayload.json')
    }

    def "should return hal+json representation of entry with custom links"()
    {
        given:
        final factory = new HalFeedRepresentationFactory(feedName, new Links([new FeedEntryLink("someLink", "http://other"), new FeedEntryLink("someLinkWithNamedParam", "http://other/{someId}/{someNum}")]), HiddenPayloadAttributes.NONE)
        final entry = new FeedEntry(Id.of("1"), new DateTime(2013, 5, 13, 11, 2, 32), new Payload(["someId": "s 12/34", "someNum": 1234]))

        when:
        final hal = factory.format(feedUri, entry)

        then:
        jsonSlurper.parseText(hal.toString(HAL_JSON)) == jsonFrom('/feed-samples/expectedHalWithSingleEntryWithCustomLinks.json')
    }

    def "should apply templated attr in returned hal+json representation with custom link with unresolved named parameter"()
    {
        given:
        final factory = new HalFeedRepresentationFactory(feedName, new Links([new FeedEntryLink("someLink", "http://other/{unresolved}")]), HiddenPayloadAttributes.NONE)
        final entry = new FeedEntry(Id.of("1"), new DateTime(2013, 5, 13, 11, 2, 32), new Payload([:]))

        when:
        final hal = factory.format(feedUri, entry)

        then:
        jsonSlurper.parseText(hal.toString(HAL_JSON)) == jsonFrom('/feed-samples/expectedHalWithTemplatedCustomLink.json')
    }

    def "should return hal+json representation for page of entries with navigational links"()
    {
        given:
        final factory = new HalFeedRepresentationFactory(feedName, NO_LINKS, HiddenPayloadAttributes.NONE)
        final entries = new FeedEntries([entry2, entry1], true, true)

        when:
        final hal = factory.format(feedUri, entries)

        then:
        jsonSlurper.parseText(hal.toString(HAL_JSON)) == jsonFrom('/feed-samples/expectedHalWithEntriesWithNavigationalLinks.json')
    }

    def "should never hide payload attributes for representation of single entry"()
    {
        given:
        final factory = new HalFeedRepresentationFactory(feedName, NO_LINKS, new HiddenPayloadAttributes(["someId", "value"]))
        final entry = entry1

        when:
        final hal = factory.format(feedUri, entry)

        then:
        jsonSlurper.parseText(hal.toString(HAL_JSON)) == jsonFrom('/feed-samples/expectedHalWithSingleEntry.json')
    }

    def "should hide payload attributes for feed representation"()
    {
        given:
        final factory = new HalFeedRepresentationFactory(feedName, NO_LINKS, new HiddenPayloadAttributes(["value"]))
        final entries = new FeedEntries([entry2, entry1])

        when:
        final hal = factory.format(feedUri, entries)

        then:
        jsonSlurper.parseText(hal.toString(HAL_JSON)) == jsonFrom('/feed-samples/expectedHalWithEntriesWithHiddenAttributes.json')
    }

    private jsonFrom(filePath)
    {
        jsonSlurper.parseText(this.getClass().getResource(filePath as String).text)
    }
}
