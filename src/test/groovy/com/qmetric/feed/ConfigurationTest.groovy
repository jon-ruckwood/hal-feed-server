package com.qmetric.feed
import com.google.common.collect.Lists
import com.qmetric.feed.domain.FeedEntryLink
import com.qmetric.feed.domain.Links
import spock.lang.Specification

class ConfigurationTest extends Specification {

    def "should parse full configuration"()
    {
        given:
        final fullConfig = Configuration.load(new FileInputStream(new File(this.getClass().getResource('/config-samples/test-full-server-config.yml').toURI())))

        expect:
        fullConfig.publicBaseUrl == 'http://www.domain.com'
        fullConfig.localPort == 5500
        fullConfig.feedName == 'test-feed'
        fullConfig.feedSelfLink == new URI('http://www.domain.com/test-feed')
        Lists.newArrayList(fullConfig.feedEntryLinks.additionalLinksForFeedEntry()) == [new FeedEntryLink("other", 'http://other.com/feed'), new FeedEntryLink("other2", 'http://other2.com/feed')]
    }

    def "should parse minimal configuration"()
    {
        given:
        final minimalConfig = Configuration.load(new FileInputStream(new File(this.getClass().getResource('/config-samples/test-minimal-server-config.yml').toURI())))

        expect:
        minimalConfig.publicBaseUrl == 'http://www.domain.com'
        minimalConfig.localPort == 5500
        minimalConfig.feedName == 'test-feed'
        minimalConfig.feedSelfLink == new URI('http://www.domain.com/test-feed')
        minimalConfig.feedEntryLinks == Links.NO_LINKS
    }
}
