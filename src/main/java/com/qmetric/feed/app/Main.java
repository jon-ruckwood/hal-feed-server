package com.qmetric.feed.app;

import com.codahale.metrics.health.HealthCheckRegistry;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.googlecode.flyway.core.Flyway;
import com.qmetric.feed.app.auth.AnonymousAuthenticationProvider;
import com.qmetric.feed.app.auth.BasicAuthenticator;
import com.qmetric.feed.app.auth.Principle;
import com.qmetric.feed.app.resource.FeedResource;
import com.qmetric.feed.app.resource.HealthCheckResource;
import com.qmetric.feed.app.resource.PingResource;
import com.qmetric.feed.app.support.FeedStoreConnectivityHealthCheck;
import com.qmetric.feed.app.support.FeedStorePayloadRepresentation;
import com.qmetric.feed.app.support.JsonModuleFactory;
import com.qmetric.feed.domain.Feed;
import com.qmetric.feed.domain.FeedRepresentationFactory;
import com.qmetric.feed.domain.FeedStore;
import com.sun.jersey.spi.inject.InjectableProvider;
import com.theoryinpractise.halbuilder.api.Representation;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.auth.basic.BasicAuthProvider;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.db.DatabaseConfiguration;
import com.yammer.dropwizard.db.ManagedDataSource;
import com.yammer.dropwizard.db.ManagedDataSourceFactory;
import com.yammer.dropwizard.jdbi.DBIFactory;
import com.yammer.dropwizard.json.ObjectMapperFactory;

public class Main extends Service<ServerConfiguration>
{
    private static final String DEFAULT_CONF_FILE = "/usr/local/config/hal-feed-server/server-config.yml";

    private static final String SERVER_NAME = "HAL Feed Server";

    public static void main(String[] args) throws Exception
    {
        final String configurationPath = System.getProperty("conf", DEFAULT_CONF_FILE);

        new Main().run(new String[] {"server", configurationPath});
    }

    @Override public void initialize(final Bootstrap<ServerConfiguration> configurationBootstrap)
    {
        configureObjectMapperFactory(configurationBootstrap);

        configurationBootstrap.setName(SERVER_NAME);
    }

    @Override public void run(final ServerConfiguration configuration, final Environment environment) throws Exception
    {
        environment.addProvider(getAuthenticationProvider(configuration));

        final FeedStore feedStore = initFeedStore(environment, configuration.getDatabaseConfiguration());

        final FeedRepresentationFactory<Representation> feedResponseFactory =
                new HalFeedRepresentationFactory(configuration.getFeedName(), configuration.getFeedEntryLinks(),
                                                 configuration.getHiddenPayloadAttributes());

        environment.addResource(new PingResource());
        environment.addResource(new HealthCheckResource(configureHealthChecks(feedStore)));
        environment.addResource(new FeedResource(configuration.getPublicBaseUrl(), new Feed(feedStore, configuration.getPayloadValidationRules()), feedResponseFactory, configuration.getDefaultEntriesPerPage()));
    }

    private HealthCheckRegistry configureHealthChecks(final FeedStore feedStore)
    {
        final HealthCheckRegistry healthCheckRegistry = new HealthCheckRegistry();
        healthCheckRegistry.register("feed-store", new FeedStoreConnectivityHealthCheck(feedStore));
        return healthCheckRegistry;
    }

    private void configureObjectMapperFactory(final Bootstrap<ServerConfiguration> configurationBootstrap)
    {
        final ObjectMapperFactory objectMapperFactory = configurationBootstrap.getObjectMapperFactory();
        objectMapperFactory.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        objectMapperFactory.registerModule(JsonModuleFactory.create());
    }

    private FeedStore initFeedStore(final Environment environment, final DatabaseConfiguration databaseConfiguration) throws Exception
    {
        migratePendingDatabaseSchemaChanges(databaseConfiguration);

        return new MysqlFeedStore(new DBIFactory().build(environment, databaseConfiguration, "database"),
                                  new FeedStorePayloadRepresentation(environment.getObjectMapperFactory().build()));
    }

    private void migratePendingDatabaseSchemaChanges(final DatabaseConfiguration databaseConfiguration) throws Exception
    {
        final ManagedDataSource dataSource = new ManagedDataSourceFactory().build(databaseConfiguration);

        final Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.migrate();

        dataSource.stop();
    }

    private InjectableProvider getAuthenticationProvider(final ServerConfiguration configuration)
    {
        if (configuration.getAuthentication().isPresent())
        {
            return new BasicAuthProvider<Principle>(
                    new BasicAuthenticator(configuration.getAuthentication().get().getUsername(),
                                           configuration.getAuthentication().get().getPassword().toCharArray()), "restricted");
        }
        else
        {
            return new AnonymousAuthenticationProvider();
        }
    }
}
