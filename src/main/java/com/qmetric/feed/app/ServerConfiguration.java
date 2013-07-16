package com.qmetric.feed.app;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.qmetric.feed.app.resource.FeedResource;
import com.qmetric.feed.domain.FeedEntryLink;
import com.qmetric.feed.domain.HiddenPayloadAttributes;
import com.qmetric.feed.domain.Links;
import com.qmetric.feed.domain.validation.MandatoryPayloadAttributesRule;
import com.qmetric.feed.domain.validation.PayloadValidationRule;
import com.qmetric.feed.domain.validation.PayloadValidationRules;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.db.DatabaseConfiguration;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import java.util.Collection;
import java.util.Collections;

import static java.util.Collections.emptyList;

public class ServerConfiguration extends Configuration
{
    @NotEmpty @JsonProperty
    private String publicBaseUrl;

    @NotEmpty @JsonProperty
    private String feedName;

    @JsonProperty
    private Collection<FeedEntryLink> feedEntryLinks = emptyList();

    @JsonProperty
    private Collection<String> hiddenPayloadAttributes = emptyList();

    @JsonProperty
    private Validation validation = new Validation();

    @Valid @NotNull @JsonProperty
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

    public PayloadValidationRules getPayloadValidationRules()
    {
        return validation.mandatoryPayloadAttributes.isEmpty() ? new PayloadValidationRules(Collections.<PayloadValidationRule>emptyList()) :
               new PayloadValidationRules(Collections.<PayloadValidationRule>singleton(new MandatoryPayloadAttributesRule(validation.mandatoryPayloadAttributes)));
    }

    public String getFeedName()
    {
        return feedName;
    }

    public DatabaseConfiguration getDatabaseConfiguration()
    {
        return databaseConfiguration;
    }

    public class Validation
    {
        @JsonProperty
        public Collection<String> mandatoryPayloadAttributes = emptyList();
    }
}
