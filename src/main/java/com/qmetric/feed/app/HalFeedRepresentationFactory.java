package com.qmetric.feed.app;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.qmetric.feed.domain.FeedEntries;
import com.qmetric.feed.domain.FeedEntry;
import com.qmetric.feed.domain.FeedEntryLink;
import com.qmetric.feed.domain.FeedRepresentationFactory;
import com.qmetric.feed.domain.HiddenPayloadAttributes;
import com.qmetric.feed.domain.Links;
import com.qmetric.feed.domain.Payload;
import com.theoryinpractise.halbuilder.DefaultRepresentationFactory;
import com.theoryinpractise.halbuilder.api.Representation;
import com.theoryinpractise.halbuilder.api.RepresentationFactory;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.annotation.Nullable;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Map;

import static com.google.common.collect.Maps.filterKeys;
import static com.google.common.collect.Maps.transformValues;
import static com.googlecode.flyway.core.util.StringUtils.replaceAll;
import static org.apache.commons.lang3.text.StrSubstitutor.replace;

public class HalFeedRepresentationFactory implements FeedRepresentationFactory<Representation>
{
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");

    private static final String FEED_NAME_KEY = "_name";

    private static final String FEED_ENTRY_ID = "_id";

    private static final String PUBLISHED_DATE_KEY = "_published";

    private static final String ENTRIES_KEY = "entries";

    private static final String PREVIOUS_LINK_RELATION = "previous";

    private static final String NEXT_LINK_RELATION = "next";

    private final RepresentationFactory representationFactory = new DefaultRepresentationFactory();

    private final String feedName;

    private final URI feedUri;

    private final Links links;

    private final HiddenPayloadAttributes hiddenPayloadAttributes;

    public HalFeedRepresentationFactory(final String feedName, final URI feedSelf, final Links links, final HiddenPayloadAttributes hiddenPayloadAttributes)
    {
        this.feedName = feedName;
        this.feedUri = feedSelf;
        this.links = links;
        this.hiddenPayloadAttributes = hiddenPayloadAttributes;
    }

    @Override public Representation format(final FeedEntries entries)
    {
        final Representation hal = representationFactory.newRepresentation(feedUri);

        hal.withProperty(FEED_NAME_KEY, feedName);

        includeNavigationalLinks(entries, hal);

        for (final FeedEntry entry : entries.all())
        {
            final Representation entryHal = formatExcludingPayloadAttributes(entry);

            includePayloadAttributes(entryHal, filterKeys(entry.payload.attributes, new Predicate<String>()
            {
                @Override public boolean apply(@Nullable final String input)
                {
                    return hiddenPayloadAttributes.isNotHidden(input);
                }
            }));

            hal.withRepresentation(ENTRIES_KEY, entryHal);
        }

        return hal;
    }

    @Override public Representation format(final FeedEntry entry)
    {
        final Representation hal = formatExcludingPayloadAttributes(entry);

        includePayloadAttributes(hal, entry.payload.attributes);

        return hal;
    }

    private void includePayloadAttributes(final Representation hal, final Map<String, Object> attributes)
    {
        for (final Map.Entry<String, Object> payloadAttribute : attributes.entrySet())
        {
            hal.withProperty(payloadAttribute.getKey(), payloadAttribute.getValue());
        }
    }

    private void includeNavigationalLinks(final FeedEntries entries, final Representation hal)
    {
        if (entries.laterExists)
        {
            hal.withLink(PREVIOUS_LINK_RELATION, String.format("%s?laterThan=%s", feedUri, entries.first().get().id));
        }

        if (entries.earlierExists)
        {
            hal.withLink(NEXT_LINK_RELATION, String.format("%s?earlierThan=%s", feedUri, entries.last().get().id));
        }
    }

    private Representation formatExcludingPayloadAttributes(final FeedEntry entry)
    {
        final Representation hal = representationFactory.newRepresentation(selfLinkForEntry(entry));

        includeAdditionalLinks(entry, hal, links.additionalLinksForFeedEntry());

        hal.withProperty(FEED_ENTRY_ID, entry.id.toString());

        hal.withProperty(PUBLISHED_DATE_KEY, DATE_FORMATTER.print(entry.publishedDate));

        return hal;
    }

    private String selfLinkForEntry(final FeedEntry feedEntry)
    {
        return String.format("%s/%s", feedUri, feedEntry.id);
    }

    private void includeAdditionalLinks(final FeedEntry feedEntry, final Representation representation, final Collection<FeedEntryLink> links)
    {
        for (final FeedEntryLink link : links)
        {
            representation.withLink(link.rel, replaceNamedParametersInLink(link, feedEntry.payload));
        }
    }

    private String replaceNamedParametersInLink(final FeedEntryLink link, final Payload payload)
    {
        final Map<String, Object> encodedPayloadAttributes = transformValues(payload.attributes, new Function<Object, Object>()
        {
            @Override public Object apply(final Object input)
            {
                return input instanceof String ? encodeParameterForUrl((String) input) : input;
            }
        });

        return replace(link.href, encodedPayloadAttributes, "{", "}");
    }

    private String encodeParameterForUrl(final String param)
    {
        try
        {
            return replaceAll(URLEncoder.encode(param, "UTF-8"), "+", "%20");
        }
        catch (final UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
    }
}
