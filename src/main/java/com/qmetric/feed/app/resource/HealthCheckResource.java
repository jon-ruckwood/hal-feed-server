package com.qmetric.feed.app.resource;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.google.common.base.Predicate;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.Map;

import static com.google.common.collect.FluentIterable.from;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

@Path("/healthcheck") @Produces(MediaType.APPLICATION_JSON)
public class HealthCheckResource
{
    private final HealthCheckRegistry healthCheckRegistry;

    public HealthCheckResource(final HealthCheckRegistry healthCheckRegistry)
    {
        this.healthCheckRegistry = healthCheckRegistry;
    }

    @GET
    public Response healthcheck()
    {
        final Map<String, HealthCheck.Result> healthCheckResults = healthCheckRegistry.runHealthChecks();

        if (anyUnhealthy(healthCheckResults))
        {
            return Response.status(INTERNAL_SERVER_ERROR).entity(healthCheckResults).build();
        }
        else
        {
            return Response.ok(healthCheckResults).build();
        }
    }

    private boolean anyUnhealthy(Map<String, HealthCheck.Result> healthCheckResults)
    {
        return from(healthCheckResults.entrySet()).anyMatch(new Predicate<Map.Entry<String, HealthCheck.Result>>()
        {
            @Override public boolean apply(final Map.Entry<String, HealthCheck.Result> healthEntry)
            {
                return !healthEntry.getValue().isHealthy();
            }
        });
    }
}