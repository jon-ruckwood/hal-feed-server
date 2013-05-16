package com.qmetric.feed;

import com.google.common.base.Optional;
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

    private Links links;

    private Optional<Iterable<String>> restrictedResourceAttributeNamesForSummary;

    public HalFeedRepresentationFactory(final URI feedUri, final Links links)
    {
        this(feedUri, links, null);
    }

    public HalFeedRepresentationFactory(final URI feedSelf, final Links links, final Iterable<String> restrictedResourceAttributesForSummary)
    {
        this.feedUri = feedSelf;
        this.links = links;
        this.restrictedResourceAttributeNamesForSummary = Optional.fromNullable(restrictedResourceAttributesForSummary);
    }

    @Override public Representation format(final FeedEntries entries)
    {
        final Representation hal = representationFactory.newRepresentation(feedUri);

        for (final FeedEntry entry : entries.all())
        {
            final Representation embedded = representationFactory.newRepresentation(uriForEntry(entry));

            addLinksToRepresentation(entry, embedded, links.allForSummary());

            embedded.withProperty(PUBLISHED_DATE_KEY, DATE_FORMATTER.print(entry.publishedDate));

            for (final String attributeName : restrictedResourceAttributeNamesForSummary.or(entry.resource.attributes.keySet()))
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

        addLinksToRepresentation(entry, hal, links.all());

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

    private void addLinksToRepresentation(final FeedEntry feedEntry, final Representation representation, final Collection<Link> links)
    {
        for (final Link link : links)
        {
            representation.withLink(link.rel, replace(link.href, feedEntry.resource.attributes, "{", "}"));
        }
    }
}
