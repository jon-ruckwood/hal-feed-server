package com.qmetric.feed.app;

import com.google.common.base.Optional;
import com.qmetric.feed.domain.FeedEntries;
import com.qmetric.feed.domain.FeedEntry;
import com.qmetric.feed.domain.FeedEntryLink;
import com.qmetric.feed.domain.FeedRepresentationFactory;
import com.qmetric.feed.domain.Links;
import com.qmetric.feed.domain.Payload;
import com.theoryinpractise.halbuilder.DefaultRepresentationFactory;
import com.theoryinpractise.halbuilder.api.Representation;
import com.theoryinpractise.halbuilder.api.RepresentationFactory;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.net.URI;
import java.util.Collection;
import java.util.Map;

import static org.apache.commons.lang3.text.StrSubstitutor.replace;

public class HalFeedRepresentationFactory implements FeedRepresentationFactory<Representation>
{
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");

    private static final String FEED_ENTRY_ID = "_id";

    private static final String PUBLISHED_DATE_KEY = "_published";

    private static final String ENTRIES_KEY = "entries";

    private static final String PREVIOUS_LINK_RELATION = "previous";

    private static final String NEXT_LINK_RELATION = "next";

    private final RepresentationFactory representationFactory = new DefaultRepresentationFactory();

    private final URI feedUri;

    private final Links links;

    public HalFeedRepresentationFactory(final URI feedSelf, final Links links)
    {
        this.feedUri = feedSelf;
        this.links = links;
    }

    @Override public Representation format(final FeedEntries entries)
    {
        final Representation hal = representationFactory.newRepresentation(feedUri);

        includeNavigationalLinks(entries, hal);

        for (final FeedEntry entry : entries.all())
        {
            hal.withRepresentation(ENTRIES_KEY, formatExcludingPayloadAttributes(entry));
        }

        return hal;
    }

    @Override public Representation format(final FeedEntry entry)
    {
        final Representation hal = formatExcludingPayloadAttributes(entry);

        for (final Map.Entry<String, Object> payloadAttribute : entry.payload.attributes.entrySet())
        {
            hal.withProperty(payloadAttribute.getKey(), payloadAttribute.getValue());
        }

        return hal;
    }

    private void includeNavigationalLinks(final FeedEntries entries, final Representation hal)
    {
        if (entries.laterExists)
        {
            hal.withLink(PREVIOUS_LINK_RELATION, String.format("%s/experimental?laterThan=%s", feedUri, entries.first().get().id));
        }

        if (entries.earlierExists)
        {
            hal.withLink(NEXT_LINK_RELATION, String.format("%s/experimental?earlierThan=%s", feedUri, entries.last().get().id));
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
        final Optional<FeedEntryLink> customizedSelfLink = links.customizedSelfLinkForFeedEntry();

        if (customizedSelfLink.isPresent())
        {
            return replaceNamedParametersInLink(customizedSelfLink.get(), feedEntry.payload);
        }
        else
        {
            return String.format("%s/%s", feedUri, feedEntry.id);
        }
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
        return replace(link.href, payload.attributes, "{", "}");
    }
}
