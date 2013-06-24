package com.qmetric.feed.app;

import com.codahale.metrics.MetricRegistry;
import com.googlecode.flyway.core.Flyway;
import com.qmetric.feed.app.routes.PingRoute;
import com.qmetric.feed.app.routes.PublishToFeedRoute;
import com.qmetric.feed.app.routes.RetrieveAllFromFeedRoute;
import com.qmetric.feed.app.routes.RetrieveEntryFromFeedRoute;
import com.qmetric.feed.app.routes.RetrieveFromFeedRoute;
import com.qmetric.feed.domain.Feed;
import com.qmetric.feed.domain.FeedRepresentationFactory;
import com.qmetric.feed.domain.FeedStore;
import com.qmetric.spark.metrics.MetricsRoute;
import com.qmetric.spark.metrics.RouteMeterWrapper;
import com.qmetric.spark.metrics.RouteTimerWrapper;
import com.theoryinpractise.halbuilder.api.Representation;
import spark.Filter;
import spark.Request;
import spark.Response;

import javax.sql.DataSource;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;

import static com.theoryinpractise.halbuilder.api.RepresentationFactory.HAL_JSON;
import static java.lang.String.format;
import static spark.Spark.after;
import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.setPort;

public class Main
{
    private static final String DEFAULT_CONF_FILE = "/usr/local/config/hal-feed-server/server-config.yml";

    private final Configuration configuration;

    private final MetricRegistry metricRegistry = new MetricRegistry();

    public Main(final Configuration configuration)
    {
        this.configuration = configuration;
    }

    public static void main(String[] args) throws IOException, URISyntaxException
    {
        new Main(Configuration.load(new FileInputStream(System.getProperty("conf", DEFAULT_CONF_FILE)))).start();
    }

    public void start() throws URISyntaxException, IOException
    {
        final FeedStore store = initFeedStore();

        final Feed feed = new Feed(store);

        final FeedRepresentationFactory<Representation> feedResponseFactory = new HalFeedRepresentationFactory(configuration.feedSelfLink, configuration.feedEntryLinks);

        configureSpark(feed, feedResponseFactory);
    }

    private FeedStore initFeedStore()
    {
        final DataSource dataSource = DataSourceFactory.create(configuration.dataSourceConfiguration);

        migratePendingDatabaseSchemaChanges(dataSource);

        return new MysqlFeedStore(dataSource, new PayloadSerializationMapper());
    }

    private void migratePendingDatabaseSchemaChanges(final DataSource dataSource)
    {
        final Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.migrate();
    }

    private void configureSpark(final Feed feed, final FeedRepresentationFactory<Representation> feedResponseFactory)
    {
        final String contextPath = configuration.feedSelfLink.getPath();

        setPort(configuration.localPort);

        after(new Filter()
        {
            @Override
            public void handle(Request request, Response response)
            {
                response.type(HAL_JSON);
            }
        });

        get(new PingRoute("/ping"));

        // todo: remove once experimental pagination feature fully tested
        get(new RetrieveAllFromFeedRoute(contextPath, feed, feedResponseFactory));

        // todo drop the /experimental path once retrievalAll route removed
        get(new RetrieveFromFeedRoute(format("%s/experimental", contextPath), feed, feedResponseFactory));

        get(new RetrieveEntryFromFeedRoute(format("%s/:id", contextPath), feed, feedResponseFactory));

        configurePostPublishToFeedRoute(contextPath, feed, feedResponseFactory);

        get(new MetricsRoute(metricRegistry));
    }

    private void configurePostPublishToFeedRoute(final String contextPath, final Feed feed, final FeedRepresentationFactory<Representation> feedResponseFactory)
    {

        final PublishToFeedRoute publishToFeedRoute = new PublishToFeedRoute(contextPath, feed, feedResponseFactory, new PayloadSerializationMapper());
        decorateRouteForMetrics(publishToFeedRoute, contextPath);
    }

    private void decorateRouteForMetrics(final PublishToFeedRoute publishToFeedRoute, final String contextPath)
    {
        post(new RouteMeterWrapper(contextPath, metricRegistry, new RouteTimerWrapper(contextPath, metricRegistry, publishToFeedRoute)));
    }
}
