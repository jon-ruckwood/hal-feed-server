package com.qmetric.feed.app;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;
import com.qmetric.feed.domain.FeedEntryLink;
import com.qmetric.feed.domain.HiddenPayloadAttributes;
import com.qmetric.feed.domain.Links;
import com.qmetric.feed.domain.validation.MandatoryPayloadAttributesRule;
import com.qmetric.feed.domain.validation.PayloadValidationRule;
import com.qmetric.feed.domain.validation.PayloadValidationRules;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.db.DatabaseConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Collections;

import static java.util.Collections.emptyList;

public class ServerConfiguration extends Configuration
{
    @JsonProperty
    private String publicBaseUrl;

    @NotEmpty @JsonProperty
    private String feedName;

    @Valid @JsonProperty
    private Authentication authentication = new Authentication();

    @JsonProperty
    private Collection<FeedEntryLink> feedEntryLinks = emptyList();

    @JsonProperty
    private int defaultEntriesPerPage = 10;

    @JsonProperty
    private Collection<String> hiddenPayloadAttributes = emptyList();

    @JsonProperty
    private Validation validation = new Validation();

    @Valid @NotNull @JsonProperty
    private DatabaseConfiguration databaseConfiguration = new DatabaseConfiguration();

    public String getPublicBaseUrl()
    {
        return publicBaseUrl;
    }

    public Links getFeedEntryLinks()
    {
        return new Links(feedEntryLinks);
    }

    public int getDefaultEntriesPerPage()
    {
        return defaultEntriesPerPage;
    }

    public HiddenPayloadAttributes getHiddenPayloadAttributes()
    {
        return new HiddenPayloadAttributes(hiddenPayloadAttributes);
    }

    public PayloadValidationRules getPayloadValidationRules()
    {
        return validation.required.isEmpty() ? new PayloadValidationRules(Collections.<PayloadValidationRule>emptyList()) :
               new PayloadValidationRules(Collections.<PayloadValidationRule>singleton(new MandatoryPayloadAttributesRule(validation.required)));
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
        public Collection<String> required = emptyList();
    }

    public Optional<Authentication> getAuthentication()
    {
        return authentication.isPresent() ? Optional.of(authentication) : Optional.<Authentication>absent();
    }

    public static class Authentication
    {
        @JsonProperty
        private String username;

        @JsonProperty
        private String password;

        public String getUsername()
        {
            return username;
        }

        public String getPassword()
        {
            return password;
        }

        public boolean isPresent()
        {
            return StringUtils.isNotBlank(username);
        }
    }
}
