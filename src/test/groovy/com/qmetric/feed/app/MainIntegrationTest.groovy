package com.qmetric.feed.app

import com.mchange.v2.c3p0.ComboPooledDataSource
import groovy.sql.Sql
import spock.lang.Shared
import spock.lang.Specification

import javax.sql.DataSource

class MainIntegrationTest extends Specification
{
    @Shared def dataSource = initDataSource();

    @Shared def sql = new Sql(dataSource)

    @Shared def configuration

    @Shared def testUtil

    def PAYLOAD = "{\"key\" : \"value\"}"

    def setupSpec()
    {
        sql.execute("SET DATABASE SQL SYNTAX MYS TRUE");
        configuration = Configuration.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("config-samples/integration-test-server-config.yml"));
        testUtil = new SparkTestUtil(configuration.localPort);
        new Main(configuration, dataSource).start();
    }


    def "ping should return 200 OK"()
    {
        when:
        SparkTestUtil.UrlResponse response = testUtil.doMethod("GET", "/ping", null);

        then:
        response.status == 200
    }

    def "should retrieve existing FEED by id"()
    {
        given:
        insertEntry()

        when:
        SparkTestUtil.UrlResponse response = testUtil.doMethod("GET", "/test-feed/1", null);

        then:
        response.status == 200
    }

    def "should publish to feed"()
    {
        when:
        SparkTestUtil.UrlResponse response = testUtil.doMethod("POST", "/test-feed", PAYLOAD);

        then:
        response.status == 201
    }

    private void insertEntry()
    {
        sql.execute(String.format("INSERT INTO feed (id, published_date, payload) VALUES ( 1, CURDATE(), '%s')", PAYLOAD))

    }

    private static DataSource initDataSource()
    {
        final DataSource dataSource = new ComboPooledDataSource()
        dataSource.setDriverClass('org.hsqldb.jdbcDriver')
        dataSource.setJdbcUrl("jdbc:hsqldb:mem:feed")
        dataSource.setUser('sa')
        dataSource.setPassword('')
        dataSource
    }
}
