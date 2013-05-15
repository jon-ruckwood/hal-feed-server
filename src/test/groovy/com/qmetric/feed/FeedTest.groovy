package com.qmetric.feed

import org.joda.time.DateTime
import spock.lang.Specification

import static java.util.Collections.emptyMap

class FeedTest extends Specification {

    final feedStore = Mock(FeedStore)

    final idFactory = Mock(IdFactory)

    final publishedDateProvider = Mock(PublishedDateProvider)

    final feed = new Feed(feedStore, idFactory, publishedDateProvider)

    def "should retrieve all feed entries"()
    {
        when:
        feed.retrieveAll()

        then:
        1 * feedStore.retrieveAll()
    }

    def "should retrieve of specific feed entry"()
    {
        when:
        feed.retrieve(new Id("1"))

        then:
        1 * feedStore.retrieve(new Id("1"))
    }

    def "should create feed entry and publish"()
    {
        given:
        final expectedGeneratedId = new Id("1")
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
