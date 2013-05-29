package com.qmetric.feed.app;

import com.google.common.base.Optional;
import com.qmetric.feed.domain.FeedEntries;
import com.qmetric.feed.domain.FeedEntry;
import com.qmetric.feed.domain.FeedEntryLink;
import com.qmetric.feed.domain.FeedRepresentationFactory;
import com.qmetric.feed.domain.Links;
import com.qmetric.feed.domain.Resource;
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

    private static final String FEED_ENTRY_ID = "id";

    private static final String PUBLISHED_DATE_KEY = "published";

    private static final String ENTRIES_KEY = "entries";

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

        for (final FeedEntry entry : entries.all())
        {
            hal.withRepresentation(ENTRIES_KEY, formatExcludingResourceAttributes(entry));
        }

        return hal;
    }

    @Override public Representation format(final FeedEntry entry)
    {
        final Representation hal = formatExcludingResourceAttributes(entry);

        for (final Map.Entry<String, Object> resourceAttribute : entry.resource.attributes.entrySet())
        {
            hal.withProperty(resourceAttribute.getKey(), resourceAttribute.getValue());
        }

        return hal;
    }

    private Representation formatExcludingResourceAttributes(final FeedEntry entry)
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
            return replaceNamedParametersInLink(customizedSelfLink.get(), feedEntry.resource);
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
            representation.withLink(link.rel, replaceNamedParametersInLink(link, feedEntry.resource));
        }
    }

    private String replaceNamedParametersInLink(final FeedEntryLink link, final Resource resource)
    {
        return replace(link.href, resource.attributes, "{", "}");
    }
}
