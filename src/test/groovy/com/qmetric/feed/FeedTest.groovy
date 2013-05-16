package com.qmetric.feed

import com.google.common.base.Optional
import org.joda.time.DateTime
import spock.lang.Specification

import static java.util.Collections.emptyList
import static java.util.Collections.emptyMap

class FeedTest extends Specification {

    final feedStore = Mock(FeedStore)

    final idFactory = Mock(IdFactory)

    final publishedDateProvider = Mock(PublishedDateProvider)

    final feed = new Feed(feedStore, idFactory, publishedDateProvider)

    def "should retrieve all feed entries"()
    {
        given:
        final expectedEntries = new FeedEntries(emptyList())
        feedStore.retrieveAll() >> expectedEntries

        when:
        final entries = feed.retrieveAll()

        then:
        entries == expectedEntries
    }

    def "should retrieve of specific feed entry"()
    {
        given:
        final expectedEntry = new FeedEntry(Id.of("1"), null, null)
        feedStore.retrieveBy(expectedEntry.id) >> Optional.of(expectedEntry)

        when:
        final entry = feed.retrieve(expectedEntry.id).get()

        then:
        entry == expectedEntry
    }

    def "should retrieve nothing when specific feed entry does not exist"()
    {
        given:
        feedStore.retrieveBy(Id.of("1")) >> Optional.absent()

        when:
        final entry = feed.retrieve(Id.of("1"))

        then:
        !entry.isPresent()
    }

    def "should create feed entry and publish"()
    {
        given:
        final expectedGeneratedId = Id.of("1")
        final expectedPublishDate = new DateTime(2012, 1, 1, 0, 0, 0, 0)
        final resource = new Resource(emptyMap())
        idFactory.create() >> expectedGeneratedId
        publishedDateProvider.publishedDate >> expectedPublishDate

        when:
        feed.publish(resource)

        then:
        1 * feedStore.add(new FeedEntry(expectedGeneratedId, expectedPublishDate, resource))
    }
}
