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

public class HalFeedRepresentationFactory implements FeedRepresentationFactory<Representation>
{
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");

    private static final String PUBLISHED_DATE_KEY = "published";

    private static final String ENTRIES_KEY = "entries";

    private final RepresentationFactory representationFactory = new DefaultRepresentationFactory();

    private URI feedUri;

    private Optional<Collection<String>> restrictedResourceAttributeNamesForSummary;

    public HalFeedRepresentationFactory(final URI feedUri)
    {
        this(feedUri, null);
    }

    public HalFeedRepresentationFactory(final URI feedUri, final Collection<String> restrictedResourceAttributesForSummary)
    {
        this.feedUri = feedUri;

        this.restrictedResourceAttributeNamesForSummary = Optional.fromNullable(restrictedResourceAttributesForSummary);
    }

    @Override public Representation format(final FeedEntries entries)
    {
        final Representation hal = representationFactory.newRepresentation(feedUri);

        for (final FeedEntry entry : entries.all())
        {
            final Representation embedded = representationFactory.newRepresentation(uriForEntry(entry));

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
}
