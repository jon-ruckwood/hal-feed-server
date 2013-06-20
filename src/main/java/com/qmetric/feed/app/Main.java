package com.qmetric.feed.app;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.googlecode.flyway.core.Flyway;
import com.qmetric.feed.app.routes.PingRoute;
import com.qmetric.feed.app.routes.PublishToFeedRoute;
import com.qmetric.feed.app.routes.RetrieveAllFromFeedRoute;
import com.qmetric.feed.app.routes.RetrieveFromFeedRoute;
import com.qmetric.feed.domain.Feed;
import com.qmetric.feed.domain.FeedRepresentationFactory;
import com.qmetric.feed.domain.FeedStore;
import com.theoryinpractise.halbuilder.api.Representation;
import spark.Filter;
import spark.Request;
import spark.Response;
import spark.Route;

import javax.sql.DataSource;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static com.codahale.metrics.MetricRegistry.name;
import static com.theoryinpractise.halbuilder.api.RepresentationFactory.HAL_JSON;
import static java.lang.String.format;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static org.apache.commons.lang3.StringUtils.substringAfterLast;
import static spark.Spark.after;
import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.setPort;

public class Main
{
    private static final String DEFAULT_CONF_FILE = "/usr/local/config/hal-feed-server/server-config.yml";

    private final Configuration configuration;

    private final MetricRegistry metrics = new MetricRegistry();

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

        return new MysqlFeedStore(dataSource);
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

        get(new RetrieveAllFromFeedRoute(contextPath, feed, feedResponseFactory));

        get(new RetrieveFromFeedRoute(format("%s/:id", contextPath), feed, feedResponseFactory));

        post(new PublishToFeedRoute(contextPath, feed, feedResponseFactory, new ObjectMapper()));

        before(new BeforeFilter("/ping", PingRoute.class, "pingRequests"));

        before(new BeforeFilter(contextPath, RetrieveAllFromFeedRoute.class, "retrieveAllFromFeedRequests"));

        before(new BeforeFilter(format("%s/:id", contextPath), RetrieveFromFeedRoute.class, "retrieveFromFeedRequests"));

        before(new BeforeFilter(contextPath, PublishToFeedRoute.class, "PublishToFeedRequests"));

        get(new MetricsRoute("/metrics"));

        get(new MetricsRoute("/metrics/route/%s"));
    }

    class BeforeFilter extends Filter
    {
        private Meter requests;

        BeforeFilter(final String path, final Class<?> klass, final String name)
        {
            super(path);
            requests = metrics.meter(name(klass, name));
        }

        @Override public void handle(final Request request, final Response response)
        {
            requests.mark();
        }
    }

    private class MetricsRoute extends Route
    {
        public MetricsRoute(final String path)
        {
            super(path);
        }

        @Override public Object handle(final Request request, final Response response)
        {
            final Map<String, Object> metricsMap = new HashMap<String, Object>();

            for (final String key : metrics.getMeters().keySet())
            {
                final Map<String, Object> values = new HashMap<String, Object>();
                values.put("count", metrics.getMeters().get(key).getCount());
                values.put("avg", metrics.getMeters().get(key).getMeanRate());
                metricsMap.put(substringAfterLast(key, "."), values);
            }

            try
            {
                return new ObjectMapper().writeValueAsString(metricsMap);
            }
            catch (IOException e)
            {

                response.status(HTTP_BAD_REQUEST);
                return e.getMessage();
            }
        }
    }
}
