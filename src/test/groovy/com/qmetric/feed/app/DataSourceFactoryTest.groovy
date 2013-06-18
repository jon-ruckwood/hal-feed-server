package com.qmetric.feed.app

import spock.lang.Specification

class DataSourceFactoryTest extends Specification{

    def "should create data source"()
    {
        expect:
        DataSourceFactory.create(new DataSourceConfiguration("", "", "", ""))
    }
}
