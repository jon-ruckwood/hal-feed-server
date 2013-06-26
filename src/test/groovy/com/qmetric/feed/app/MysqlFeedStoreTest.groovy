package com.qmetric.feed.app
import com.google.common.base.Optional
import com.googlecode.flyway.core.Flyway
import com.mchange.v2.c3p0.ComboPooledDataSource
import com.qmetric.feed.domain.*
import groovy.sql.Sql
import spock.lang.Shared
import spock.lang.Specification

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

        store = new MysqlFeedStore(dataSource, new PayloadSerializationMapper())
    }

    def cleanup()
    {
        sql.execute("delete from feed")
    }

    def "should retrieve feed entry by id"()
    {
        given:
        final all = setupPageOfEntries(2)

        expect:
        store.retrieveBy(all.get(1).id).get() == all.get(1)
        !store.retrieveBy(laterId(all.get(0).id)).isPresent()
        !store.retrieveBy(Id.of("nonNumbericUnknownId")).isPresent()
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

    def "should retrieve page of latest feed entries limited to max results in descending insertion order"()
    {
        given:
        final all = setupPageOfEntries(10)

        expect:
        store.retrieveBy(criteria(1)) == new FeedEntries(all.subList(0, 1), true, false)
        store.retrieveBy(criteria(3)) == new FeedEntries(all.subList(0, 3), true, false)
        store.retrieveBy(criteria(10)) == new FeedEntries(all, false, false)
        store.retrieveBy(criteria(11)) == new FeedEntries(all, false, false)
    }

    def "should retrieve page of feed entries occurring before a given entry in descending insertion order"()
    {
        given:
        final all = setupPageOfEntries(10)

        expect:
        store.retrieveBy(criteria(opt(all.get(5).id), absent(), 3)) == new FeedEntries(all.subList(6, 9), true, true)
        store.retrieveBy(criteria(opt(all.get(5).id), absent(), 11)) == new FeedEntries(all.subList(6, 10), false, true)
        store.retrieveBy(criteria(opt(all.get(0).id), absent(), 3)) == new FeedEntries(all.subList(1, 4), true, true)
        store.retrieveBy(criteria(opt(laterId(all.get(0).id)), absent(), 3)) == new FeedEntries(all.subList(0, 3), true, false)
        store.retrieveBy(criteria(opt(all.get(6).id), absent(), 3)) == new FeedEntries(all.subList(7, 10), false, true)
        store.retrieveBy(criteria(opt(earlierId(all.get(9).id)), absent(), 3)) == new FeedEntries([], false, false)
    }

    def "should retrieve page of feed entries occurring after a given entry in descending insertion order"()
    {
        given:
        final all = setupPageOfEntries(10)

        expect:
        store.retrieveBy(criteria(absent(), opt(all.get(5).id), 3)) == new FeedEntries(all.subList(2, 5), true, true)
        store.retrieveBy(criteria(absent(), opt(all.get(5).id), 11)) == new FeedEntries(all.subList(0, 5), true, false)
        store.retrieveBy(criteria(absent(), opt(all.get(9).id), 3)) == new FeedEntries(all.subList(6, 9), true, true)
        store.retrieveBy(criteria(absent(), opt(earlierId(all.get(9).id)), 3)) == new FeedEntries(all.subList(7, 10), false, true)
        store.retrieveBy(criteria(absent(), opt(all.get(3).id), 3)) == new FeedEntries(all.subList(0, 3), true, false)
        store.retrieveBy(criteria(absent(), opt(laterId(all.get(0).id)), 3)) == new FeedEntries([], false, false)
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
        final DataSource datasource = new ComboPooledDataSource()
        datasource.setDriverClass('org.hsqldb.jdbcDriver')
        datasource.setJdbcUrl("jdbc:hsqldb:mem:feed")
        datasource.setUser('sa')
        datasource.setPassword('')
        datasource
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

    private static Id laterId(final id)
    {
        Id.of(String.valueOf(Integer.valueOf(id.toString()) + 1))
    }

    private static Id earlierId(final id)
    {
        Id.of(String.valueOf(Integer.valueOf(id.toString()) - 1))
    }
}
