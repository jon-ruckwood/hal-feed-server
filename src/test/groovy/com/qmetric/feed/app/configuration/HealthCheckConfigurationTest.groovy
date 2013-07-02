package com.qmetric.feed.app.configuration

import com.codahale.metrics.health.HealthCheckRegistry
import com.qmetric.feed.app.configuration.DBHealthCheckBuilder
import com.qmetric.feed.app.configuration.HealthCheckConfiguration
import com.qmetric.spark.metrics.DBHealthCheck
import spock.lang.Specification

class HealthCheckConfigurationTest extends Specification
{

    def healthCheckRegistry = Mock(HealthCheckRegistry)
    def dbHealthCheckBuilder = Mock(DBHealthCheckBuilder)
    def dbHealthCheck = Mock(DBHealthCheck)
    def healthCheckConfiguration = new HealthCheckConfiguration(healthCheckRegistry, dbHealthCheckBuilder)


    def "DBHealthCheck is registered with healthCheckRegistry"()
    {
        given:
        dbHealthCheckBuilder.build() >> dbHealthCheck

        when:
        healthCheckConfiguration.configure();

        then:
        1 * healthCheckRegistry.register("Database", dbHealthCheck)

    }
}
