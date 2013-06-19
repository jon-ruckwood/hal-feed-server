package com.qmetric.feed.app

import com.googlecode.flyway.core.Flyway
import com.mchange.v2.c3p0.ComboPooledDataSource
import com.qmetric.feed.domain.FeedEntry
import com.qmetric.feed.domain.Id
import com.qmetric.feed.domain.Payload
import groovy.sql.Sql
import org.joda.time.DateTime
import spock.lang.Shared
import spock.lang.Specification

import javax.sql.DataSource

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

        store = new MysqlFeedStore(dataSource)
    }

    def cleanup()
    {
        sql.execute("delete from feed")
    }

    def "should retrieve feed entry by id"()
    {
        given:
        final feedEntry = store.store(new FeedEntry(now(), new Payload(['test': '1234'])))

        expect:
        store.retrieveBy(feedEntry.id).get() == feedEntry
        !store.retrieveBy(Id.of(String.valueOf(Integer.valueOf(feedEntry.id.toString()) + 1))).isPresent()
        !store.retrieveBy(Id.of("nonNumbericUnknownId")).isPresent()
    }

    def "should retrieve feed entries ordered by descending publish date"()
    {
        given:
        final notLatestOrEarliestEntry = store.store(new FeedEntry(new DateTime(2013, 2, 1, 0, 0, 0, 0), new Payload(emptyMap())))
        final latestEntry = store.store(new FeedEntry(new DateTime(2013, 3, 1, 0, 0, 0, 0), new Payload(emptyMap())))
        final earliestEntry = store.store(new FeedEntry(new DateTime(2013, 1, 1, 0, 0, 0, 0), new Payload(emptyMap())))

        when:
        final entries = store.retrieveAll().all()

        then:
        entries == [latestEntry, notLatestOrEarliestEntry, earliestEntry]
    }

    def "should retrieve feed entries in descending id order when same publish date"()
    {
        given:
        final earliestEntryWithSameDate = store.store(new FeedEntry(new DateTime(2013, 1, 1, 0, 0, 0, 0), new Payload(emptyMap())))
        final latestEntryWithSameDate = store.store(new FeedEntry(new DateTime(2013, 1, 1, 0, 0, 0, 0), new Payload(emptyMap())))

        when:
        final entries = store.retrieveAll().all()

        then:
        entries == [latestEntryWithSameDate, earliestEntryWithSameDate]
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
}
