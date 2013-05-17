package com.qmetric.feed
import com.google.common.collect.ImmutableMap
import groovy.json.JsonSlurper
import org.joda.time.DateTime
import spock.lang.Specification

import static com.google.common.collect.Lists.newArrayList
import static com.google.common.collect.Sets.newHashSet
import static com.qmetric.feed.Links.NO_LINKS
import static com.theoryinpractise.halbuilder.api.RepresentationFactory.HAL_JSON
import static java.util.Collections.*

class HalFeedRepresentationFactoryTest extends Specification {

    final jsonSlurper = new JsonSlurper()

    final feedUri = new URI('http://localhost:1234/test-feed')

    final entry1 = new FeedEntry(Id.of("1"), new DateTime(2013, 5, 13, 11, 2, 32), new Resource(ImmutableMap.of("stuff", "aaaa", "value", "2000")))

    final entry2 = new FeedEntry(Id.of("2"), new DateTime(2013, 5, 14, 11, 2, 32), new Resource(ImmutableMap.of("stuff", "bbbb", "value", "1000")))

    def "should return hal+json representation of entries"()
    {
        given:
        final factory = new HalFeedRepresentationFactory(feedUri, NO_LINKS)
        final entries = new FeedEntries([entry2, entry1])

        when:
        final hal = factory.format(entries)

        then:
        jsonSlurper.parseText(hal.toString(HAL_JSON)) == jsonSlurper.parseText(this.getClass().getResource('/assets/expectedHalWithMultipleEntries.json').text)
    }

    def "should return hal+json representation of entries with restricted resource attributes for summary"()
    {
        given:
        final factory = new HalFeedRepresentationFactory(feedUri, NO_LINKS, singleton('stuff'))
        final entries = new FeedEntries([entry2, entry1])

        when:
        final hal = factory.format(entries)

        then:
        jsonSlurper.parseText(hal.toString(HAL_JSON)) == jsonSlurper.parseText(this.getClass().getResource('/assets/expectedHalWithMultipleSummarisedEntries.json').text)
    }

    def "should return hal+json representation of entries with custom links"()
    {
        given:
        final factory = new HalFeedRepresentationFactory(feedUri, new Links(newHashSet(new Link("someLink", "http://not-included", false), new Link("someLinkWithNamedParam", "http://other-feed/{stuff}", true))))
        final entries = new FeedEntries([entry2, entry1])

        when:
        final hal = factory.format(entries)

        then:
        jsonSlurper.parseText(hal.toString(HAL_JSON)) == jsonSlurper.parseText(this.getClass().getResource('/assets/expectedHalWithMultipleEntriesWithCustomLinks.json').text)
    }

    def "should return hal+json representation of entry"()
    {
        given:
        final factory = new HalFeedRepresentationFactory(feedUri, NO_LINKS)
        final entry = entry1

        when:
        final hal = factory.format(entry)

        then:
        jsonSlurper.parseText(hal.toString(HAL_JSON)) == jsonSlurper.parseText(this.getClass().getResource('/assets/expectedHalWithSingleEntry.json').text)
    }

    def "should return hal+json representation of entry with complex properties"()
    {
        given:
        final factory = new HalFeedRepresentationFactory(feedUri, NO_LINKS)
        final entry = new FeedEntry(Id.of("1"), new DateTime(2013, 5, 13, 11, 2, 32), new Resource(ImmutableMap.of("nested", singletonMap("stuff", "aaaa"), "arr", newArrayList("a", "b", "c"))))

        when:
        final hal = factory.format(entry)

        then:
        jsonSlurper.parseText(hal.toString(HAL_JSON)) == jsonSlurper.parseText(this.getClass().getResource('/assets/expectedHalWithSingleEntryWithComplexProperties.json').text)
    }

    def "should return hal+json representation of entry with custom links"()
    {
        given:
        final factory = new HalFeedRepresentationFactory(feedUri, new Links(newHashSet(new Link("someLink", "http://other-feed"), new Link("someLinkWithNamedParam", "http://other-feed/{someId}"))))
        final entry = new FeedEntry(Id.of("1"), new DateTime(2013, 5, 13, 11, 2, 32), new Resource(singletonMap("someId", "s1234")))

        when:
        final hal = factory.format(entry)

        then:
        jsonSlurper.parseText(hal.toString(HAL_JSON)) == jsonSlurper.parseText(this.getClass().getResource('/assets/expectedHalWithSingleEntryWithCustomLinks.json').text)
    }

    def "should apply templated attr in returned hal+json representation with custom link with unresolved named parameter"()
    {
        given:
        final factory = new HalFeedRepresentationFactory(feedUri, new Links(newHashSet(new Link("someLink", "http://other-feed/{unresolved}"))))
        final entry = new FeedEntry(Id.of("1"), new DateTime(2013, 5, 13, 11, 2, 32), new Resource(emptyMap()))

        when:
        final hal = factory.format(entry)

        then:
        jsonSlurper.parseText(hal.toString(HAL_JSON)) == jsonSlurper.parseText(this.getClass().getResource('/assets/expectedHalWithTemplatedCustomLink.json').text)
    }
}
