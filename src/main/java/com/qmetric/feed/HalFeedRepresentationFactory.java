package com.qmetric.feed;

import com.google.common.base.Optional;
import com.theoryinpractise.halbuilder.DefaultRepresentationFactory;
import com.theoryinpractise.halbuilder.api.Representation;
import com.theoryinpractise.halbuilder.api.RepresentationFactory;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Map;

public class HalFeedRepresentationFactory implements FeedRepresentationFactory<Representation>
{
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");

    private static final String PUBLISHED_DATE_KEY = "published";

    private static final String ENTRIES_KEY = "entries";

    private final RepresentationFactory representationFactory = new DefaultRepresentationFactory();

    private final FeedUriFactory uriFactory;

    private final Optional<ResourceAttributesSummaryProvider> resourceAttributesToUseInSummary;

    public HalFeedRepresentationFactory(final FeedUriFactory uriFactory)
    {
        this(uriFactory, null);
    }

    public HalFeedRepresentationFactory(final FeedUriFactory uriFactory, final ResourceAttributesSummaryProvider resourceAttributesToUseForSummary)
    {
        this.uriFactory = uriFactory;
        this.resourceAttributesToUseInSummary = Optional.fromNullable(resourceAttributesToUseForSummary);
    }

    @Override public Representation format(final FeedEntries entries)
    {
        final Representation hal = representationFactory.newRepresentation(uriFactory.createForFeed());

        for (final FeedEntry entry : entries.all())
        {
            final Representation embedded = representationFactory.newRepresentation(uriFactory.createForFeedEntry(entry.id));

            embedded.withProperty(PUBLISHED_DATE_KEY, DATE_FORMATTER.print(entry.publishedDate));

            final Map<String, String> resourceAttributesToUseInSummary =
                    this.resourceAttributesToUseInSummary.isPresent() ? this.resourceAttributesToUseInSummary.get().filterAttributesForSummary(entry.resource) : entry.resource.attributes;

            for (final Map.Entry<String, String> property : resourceAttributesToUseInSummary.entrySet())
            {
                embedded.withProperty(property.getKey(), property.getValue());
            }

            hal.withRepresentation(ENTRIES_KEY, embedded);
        }

        return hal;
    }

    @Override public Representation format(final FeedEntry entry)
    {
        final Representation hal = representationFactory.newRepresentation(uriFactory.createForFeedEntry(entry.id));

        hal.withProperty(PUBLISHED_DATE_KEY, DATE_FORMATTER.print(entry.publishedDate));

        for (final Map.Entry<String, String> resourceAttribute : entry.resource.attributes.entrySet())
        {
            hal.withProperty(resourceAttribute.getKey(), resourceAttribute.getValue());
        }

        return hal;
    }
}
