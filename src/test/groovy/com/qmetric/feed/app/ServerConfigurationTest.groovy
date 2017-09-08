package com.qmetric.feed.app

import com.qmetric.feed.domain.FeedEntryLink
import com.yammer.dropwizard.testing.junit.DropwizardServiceRule
import spock.lang.Specification

import static com.google.common.collect.Lists.newArrayList

class ServerConfigurationTest extends Specification {

    def server

    def "should parse full configuration"()
    {
        when:
        final config = loadConfig("/config-samples/test-full-server-config.yml")

        then:
        config.publicBaseUrl == 'http://www.domain.com'
        config.feedName == 'Test feed'
        newArrayList(config.feedEntryLinks.additionalLinksForFeedEntry()) == [new FeedEntryLink("other", 'http://other.com/feed'), new FeedEntryLink("other2", 'http://other2.com/feed')]
        config.getDefaultEntriesPerPage() == 20
        config.hiddenPayloadAttributes.all() == ['someIdToHide1', 'someIdToHide2']
        config.getPayloadValidationRules().all().iterator().hasNext()
        config.databaseConfiguration.collect { [it.driverClass, it.url, it.user, it.password] }.first() == ['org.hsqldb.jdbcDriver', 'jdbc:hsqldb:mem:feed', "sa", '']
    }

    def "should parse minimal configuration"()
    {
        when:
        final config = loadConfig("/config-samples/test-minimal-server-config.yml")

        then:
        config.feedName == 'Test feed'
        config.feedEntryLinks.additionalLinksForFeedEntry().isEmpty()
        config.getDefaultEntriesPerPage() == 10
        config.hiddenPayloadAttributes.all().isEmpty()
        !config.getPayloadValidationRules().all().iterator().hasNext()
        config.databaseConfiguration.collect { [it.driverClass, it.url, it.user, it.password] }.first() == ['org.hsqldb.jdbcDriver', 'jdbc:hsqldb:mem:feed', "sa", '']
    }

    def cleanup()
    {
        server.jettyServer.stop()
    }

    private ServerConfiguration loadConfig(final configPath)
    {
        server = new DropwizardServiceRule<ServerConfiguration>(Main.class, this.getClass().getResource(configPath).path)
        //noinspection GroovyAccessibility
        server.startIfRequired()
        server.configuration
    }
}
