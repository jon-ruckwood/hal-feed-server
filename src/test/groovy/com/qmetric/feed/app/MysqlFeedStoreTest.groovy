package com.qmetric.feed.app

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.base.Optional
import com.googlecode.flyway.core.Flyway
import com.googlecode.flyway.core.util.jdbc.DriverDataSource
import com.qmetric.feed.app.support.FeedStorePayloadRepresentation
import com.qmetric.feed.domain.*
import groovy.sql.Sql
import org.skife.jdbi.v2.DBI
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import javax.sql.DataSource

import static FeedRestrictionCriteria.Filter
import static com.google.common.base.Optional.absent
import static java.util.Collections.emptyMap
import static org.joda.time.DateTime.now

class MysqlFeedStoreTest extends Specification {

    @Shared MysqlFeedStore store

    @Shared Sql sql

    def setupSpec()
    {
        def dataSource = initDataSource()

        sql = new Sql(dataSource)
        sql.execute("SET DATABASE SQL SYNTAX MYS TRUE");

        initSchema(dataSource)

        store = new MysqlFeedStore(new DBI(dataSource), new FeedStorePayloadRepresentation(new ObjectMapper()))
    }

    def cleanup()
    {
        sql.execute("delete from feed")
    }

    def "should retrieve feed entry by id"()
    {
        given:
        final all = setupPageOfEntries(2)
        final numericIdOfUnknownEntry = increment(all.get(0).id)
        final textualIdOfUnknownEntry = Id.of("nonNumericUnknownId")

        expect:
        store.retrieveBy(all.get(1).id).get() == all.get(1)
        !store.retrieveBy(numericIdOfUnknownEntry.get()).isPresent()
        !store.retrieveBy(textualIdOfUnknownEntry).isPresent()
    }

    def "should retrieve all feed entries in descending insertion order"()
    {
        given:
        final all = setupPageOfEntries(10)

        when:
        final entries = store.retrieveAll().all()

        then:
        entries == all
    }

    def "should retrieve nothing from empty feed"()
    {
        expect:
        store.retrieveAll().all() == []
        store.retrieveBy(criteria(opt(Id.of("1")), absent(), 1)) == new FeedEntries([], false, false)
        store.retrieveBy(criteria(absent(), opt(Id.of("1")), 1)) == new FeedEntries([], false, false)
        store.retrieveBy(criteria(1)) == new FeedEntries([], false, false)
    }

    @Unroll
    def "should retrieve page of latest feed entries limited to max results in descending insertion order"()
    {
        given:
        final all = setupPageOfEntries(10)

        expect:
        store.retrieveBy(criteria(resultsLimit)) == new FeedEntries(all.subList(expectedPage.fromInt, expectedPage.toInt), earlierEntriesExist, laterEntriesExist)

        where:
        resultsLimit | expectedPage | earlierEntriesExist | laterEntriesExist
        1            | 0..1         | true                | false
        3            | 0..3         | true                | false
        10           | 0..10        | false               | false
        11           | 0..10        | false               | false
    }

    @Unroll
    def "should retrieve page of feed entries occurring before a given entry in descending insertion order"()
    {
        given:
        final all = setupPageOfEntries(10)

        expect:
        store.retrieveBy(criteria(opt(all.get(earlierThanEntryAtPosition).id), absent(), resultsLimit)) == new FeedEntries(all.subList(expectedPage.fromInt, expectedPage.toInt), earlierEntriesExist, laterEntriesExist)

        where:
        earlierThanEntryAtPosition | resultsLimit | expectedPage | earlierEntriesExist | laterEntriesExist
        5                          | 3            | 6..9         | true                | true
        5                          | 11           | 6..10        | false               | true
        0                          | 3            | 1..4         | true                | true
        6                          | 3            | 7..10        | false               | true
    }

    @Unroll
    def "should retrieve page of feed entries occurring after a given entry in descending insertion order"()
    {
        given:
        final all = setupPageOfEntries(10)

        expect:
        store.retrieveBy(criteria(absent(), opt(all.get(laterThanEntryAtPosition).id), resultsLimit)) == new FeedEntries(all.subList(expectedPage.fromInt, expectedPage.toInt), earlierEntriesExist, laterEntriesExist)

        where:
        laterThanEntryAtPosition | resultsLimit | expectedPage | earlierEntriesExist | laterEntriesExist
        5                        | 3            | 2..5         | true                | true
        5                        | 11           | 0..5         | true                | false
        9                        | 3            | 6..9         | true                | true
        3                        | 3            | 0..3         | true                | false
    }

    def "should retrieve feed page with criteria id of entry that does not exist"()
    {
        given:
        final all = setupPageOfEntries(10)
        final idOfEarliest = all.get(all.size() - 1).id
        final idOfLatest = all.get(0).id

        expect:
        store.retrieveBy(criteria(increment(idOfLatest), absent(), 3)) == new FeedEntries(all.subList(0, 3), true, false)
        store.retrieveBy(criteria(decrement(idOfEarliest), absent(), 3)) == new FeedEntries([], false, false)
        store.retrieveBy(criteria(absent(), decrement(idOfEarliest), 3)) == new FeedEntries(all.subList(7, 10), false, true)
        store.retrieveBy(criteria(absent(), increment(idOfLatest), 3)) == new FeedEntries([], false, false)
    }

    def "should store feed entry"()
    {
        when:
        final feedEntry = store.store(new FeedEntry(now(), new Payload(['test': '1234', 'inner': ['a': 1]])))

        then:
        store.retrieveBy(feedEntry.id).get() == feedEntry
    }

    private static DataSource initDataSource()
    {
        new DriverDataSource('org.hsqldb.jdbcDriver', 'jdbc:hsqldb:mem:feed', 'sa', '')
    }

    private static initSchema(DataSource dataSource)
    {
        def flyway = new Flyway()
        flyway.setDataSource(dataSource)
        flyway.migrate()
    }

    private List<FeedEntry> setupPageOfEntries(final int numberOfEntries)
    {
        (1..numberOfEntries).collect {store.store(new FeedEntry(now(), new Payload(emptyMap())))}.reverse()
    }

    private static Optional<Id> opt(final Id id)
    {
        Optional.of(id)
    }

    private static FeedRestrictionCriteria criteria(final Optional<Id> earlierThan, final Optional<Id> laterThan, final int limit)
    {
        new FeedRestrictionCriteria(new Filter(earlierThan, laterThan), limit)
    }

    private static FeedRestrictionCriteria criteria(final int limit)
    {
        new FeedRestrictionCriteria(new Filter(absent(), absent()), limit)
    }

    private static Optional<Id> increment(final id)
    {
        Optional.of(Id.of(String.valueOf(Integer.valueOf(id.toString()) + 1)))
    }

    private static Optional<Id> decrement(final Id id)
    {
        Optional.of(Id.of(String.valueOf(Integer.valueOf(id.toString()) - 1)))
    }
}
