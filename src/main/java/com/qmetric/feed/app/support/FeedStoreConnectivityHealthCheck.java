package com.qmetric.feed.app.support;

import com.codahale.metrics.health.HealthCheck;
import com.qmetric.feed.domain.FeedStore;

import static com.codahale.metrics.health.HealthCheck.Result.healthy;
import static com.codahale.metrics.health.HealthCheck.Result.unhealthy;

public class FeedStoreConnectivityHealthCheck extends HealthCheck
{
    private final FeedStore feedStore;

    public FeedStoreConnectivityHealthCheck(final FeedStore feedStore)
    {
        this.feedStore = feedStore;
    }

    @Override protected Result check() throws Exception
    {
        try
        {
            feedStore.checkConnectivity();

            return healthy();
        }
        catch (final Exception exception)
        {
            return unhealthy("failed to connect to feed store");
        }
    }
}