package com.qmetric.feed.app

import spock.lang.Specification


class DataSourceConfigurationTest extends Specification{

    def "should hold expected config"()
    {
        given:
        final driver = "driver"
        final url = "url"
        final username = "username"
        final password = "password"
        final config = new DataSourceConfiguration(driver, url, username, password)

        expect:
        config.driver == driver
        config.url == url
        config.username == username
        config.password == password
    }
}
