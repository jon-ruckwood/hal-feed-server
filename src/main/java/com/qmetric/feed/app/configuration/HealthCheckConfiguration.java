package com.qmetric.feed.app.configuration;

import com.codahale.metrics.health.HealthCheckRegistry;
import com.qmetric.spark.metrics.HealthCheckRoute;

import static spark.Spark.get;

public class HealthCheckConfiguration
{

    private final HealthCheckRegistry healthCheckRegistry;

    private final DBHealthCheckBuilder healthCheckBuilder;

    public HealthCheckConfiguration(final HealthCheckRegistry healthCheckRegistry, final DBHealthCheckBuilder healthCheckBuilder)
    {
        this.healthCheckRegistry = healthCheckRegistry;
        this.healthCheckBuilder = healthCheckBuilder;
    }

    public void configure()
    {

        healthCheckRegistry.register("Database", healthCheckBuilder.build());

        get(new HealthCheckRoute(healthCheckRegistry));
    }
}
