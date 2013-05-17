package com.qmetric.feed.app

import com.qmetric.feed.domain.*
import org.joda.time.DateTime
import spock.lang.Specification

import static java.util.Collections.emptyMap
import static org.joda.time.DateTime.now

class InMemoryStoreTest extends Specification {

    final idFactory = Mock(IdFactory)

    final publishedDateProvider = Mock(PublishedDateProvider)

    final inMemoryStore = new InMemoryStore()

    def "should retrieve all feed entries in descending order of date published to feed"()
    {
        given:
        final notLatestOrEarliestEntry = new FeedEntry(Id.of("notLatestOrEarliest id"), new DateTime(2013, 2, 1, 0, 0, 0, 0), new Resource(emptyMap()))
        final latestEntry = new FeedEntry(Id.of("latest id"), new DateTime(2013, 3, 1, 0, 0, 0, 0), new Resource(emptyMap()))
        final earliestEntry = new FeedEntry(Id.of("earliest id"), new DateTime(2013, 1, 1, 0, 0, 0, 0), new Resource(emptyMap()))

        publishedDateProvider.getPublishedDate() >>> [notLatestOrEarliestEntry.publishedDate, latestEntry.publishedDate, earliestEntry.publishedDate]
        idFactory.create() >>> [notLatestOrEarliestEntry.id, latestEntry.id, earliestEntry.id]

        inMemoryStore.add(notLatestOrEarliestEntry)
        inMemoryStore.add(latestEntry)
        inMemoryStore.add(earliestEntry)

        when:
        final entries = inMemoryStore.retrieveAll()

        then:
        entries.all() == [latestEntry, notLatestOrEarliestEntry, earliestEntry]
    }

    def "should retrieve single feed entry by id"()
    {
        given:
        final resource = new Resource(emptyMap())
        final expectedPublishedDate = now()
        final expectedId = Id.of("1")
        final expectedFeedEntry = new FeedEntry(expectedId, expectedPublishedDate, resource)
        publishedDateProvider.getPublishedDate() >> expectedPublishedDate
        idFactory.create() >>> [expectedId]
        inMemoryStore.add(expectedFeedEntry)

        expect:
        inMemoryStore.retrieveBy(Id.of("1")).get() == expectedFeedEntry
        !inMemoryStore.retrieveBy(Id.of("2")).isPresent()
    }

    def "should add entry to feed"()
    {
        given:
        final entry = new FeedEntry(Id.of("1"), now(), new Resource(emptyMap()))

        when:
        inMemoryStore.add(entry)

        then:
        entry == inMemoryStore.retrieveBy(Id.of("1")).get()
    }
}
