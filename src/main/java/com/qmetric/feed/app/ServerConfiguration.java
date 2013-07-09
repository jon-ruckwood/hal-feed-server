package com.qmetric.feed.app;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.qmetric.feed.app.resource.FeedResource;
import com.qmetric.feed.domain.FeedEntryLink;
import com.qmetric.feed.domain.HiddenPayloadAttributes;
import com.qmetric.feed.domain.Links;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.db.DatabaseConfiguration;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import java.util.Collection;

import static java.util.Collections.emptyList;

public class ServerConfiguration extends Configuration
{
    @NotEmpty
    @JsonProperty
    private String publicBaseUrl;

    @NotEmpty
    @JsonProperty
    private String feedName;

    @JsonProperty
    private Collection<FeedEntryLink> feedEntryLinks = emptyList();

    @JsonProperty
    private Collection<String> hiddenPayloadAttributes = emptyList();

    @Valid
    @NotNull
    @JsonProperty
    private DatabaseConfiguration databaseConfiguration = new DatabaseConfiguration();

    public String getFeedSelfLink()
    {
        return publicBaseUrl + FeedResource.CONTEXT;
    }

    public Links getFeedEntryLinks()
    {
        return new Links(feedEntryLinks);
    }

    public HiddenPayloadAttributes getHiddenPayloadAttributes()
    {
        return new HiddenPayloadAttributes(hiddenPayloadAttributes);
    }

    public String getFeedName()
    {
        return feedName;
    }

    public DatabaseConfiguration getDatabaseConfiguration()
    {
        return databaseConfiguration;
    }
}
