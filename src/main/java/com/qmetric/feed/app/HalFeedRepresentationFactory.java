package com.qmetric.feed.app;

import com.google.common.base.Optional;
import com.qmetric.feed.domain.FeedEntries;
import com.qmetric.feed.domain.FeedEntry;
import com.qmetric.feed.domain.FeedEntryLink;
import com.qmetric.feed.domain.FeedRepresentationFactory;
import com.qmetric.feed.domain.Links;
import com.theoryinpractise.halbuilder.DefaultRepresentationFactory;
import com.theoryinpractise.halbuilder.api.Representation;
import com.theoryinpractise.halbuilder.api.RepresentationFactory;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Map;

import static org.apache.commons.lang3.text.StrSubstitutor.replace;

public class HalFeedRepresentationFactory implements FeedRepresentationFactory<Representation>
{
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");

    private static final String PUBLISHED_DATE_KEY = "published";

    private static final String ENTRIES_KEY = "entries";

    private final RepresentationFactory representationFactory = new DefaultRepresentationFactory();

    private URI feedUri;

    private Links otherLinks;

    private Optional<Collection<String>> resourceAttributesForSummarisedFeedEntry;

    public HalFeedRepresentationFactory(final URI feedUri, final Links otherLinks)
    {
        this(feedUri, otherLinks, null);
    }

    public HalFeedRepresentationFactory(final URI feedSelf, final Links otherLinks, final Collection<String> resourceAttributesForSummarisedFeedEntry)
    {
        this.feedUri = feedSelf;
        this.otherLinks = otherLinks;

        if (resourceAttributesForSummarisedFeedEntry != null && !resourceAttributesForSummarisedFeedEntry.isEmpty())
        {
            this.resourceAttributesForSummarisedFeedEntry = Optional.of(resourceAttributesForSummarisedFeedEntry);
        }
        else
        {
            this.resourceAttributesForSummarisedFeedEntry = Optional.absent();
        }
    }

    @Override public Representation format(final FeedEntries entries)
    {
        final Representation hal = representationFactory.newRepresentation(feedUri);

        for (final FeedEntry entry : entries.all())
        {
            final Representation embedded = representationFactory.newRepresentation(uriForEntry(entry));

            addLinksToRepresentation(entry, embedded, otherLinks.forSummarisedFeedEntry());

            embedded.withProperty(PUBLISHED_DATE_KEY, DATE_FORMATTER.print(entry.publishedDate));

            for (final String attributeName : resourceAttributesForSummarisedFeedEntry.or(entry.resource.attributes.keySet()))
            {
                embedded.withProperty(attributeName, entry.resource.attributes.get(attributeName));
            }

            hal.withRepresentation(ENTRIES_KEY, embedded);
        }

        return hal;
    }

    @Override public Representation format(final FeedEntry entry)
    {
        final Representation hal = representationFactory.newRepresentation(uriForEntry(entry));

        addLinksToRepresentation(entry, hal, otherLinks.forFeedEntry());

        hal.withProperty(PUBLISHED_DATE_KEY, DATE_FORMATTER.print(entry.publishedDate));

        for (final Map.Entry<String, Object> resourceAttribute : entry.resource.attributes.entrySet())
        {
            hal.withProperty(resourceAttribute.getKey(), resourceAttribute.getValue());
        }

        return hal;
    }

    private URI uriForEntry(final FeedEntry feedEntry)
    {
        try
        {
            return new URI(String.format("%s/%s", feedUri, feedEntry.id));
        }
        catch (URISyntaxException e)
        {
            throw new RuntimeException("Unable to create URI for feed entry", e);
        }
    }

    private void addLinksToRepresentation(final FeedEntry feedEntry, final Representation representation, final Collection<FeedEntryLink> links)
    {
        for (final FeedEntryLink link : links)
        {
            representation.withLink(link.rel, replace(link.href, feedEntry.resource.attributes, "{", "}"));
        }
    }
}
