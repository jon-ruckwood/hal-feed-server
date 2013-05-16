package com.qmetric.feed;

import com.theoryinpractise.halbuilder.DefaultRepresentationFactory;
import com.theoryinpractise.halbuilder.api.Representation;
import com.theoryinpractise.halbuilder.api.RepresentationFactory;

import java.util.Map;

public class HalFeedRepresentationFactory implements FeedRepresentationFactory<Representation>
{
    private static final String ENTRIES_KEY = "entries";

    private final RepresentationFactory representationFactory = new DefaultRepresentationFactory();

    private final FeedUriFactory uriFactory;

    private final FeedEntryPropertiesProvider propertiesSummaryProvider;

    public HalFeedRepresentationFactory(final FeedUriFactory uriFactory, final FeedEntryPropertiesProvider propertiesSummaryProvider)
    {
        this.uriFactory = uriFactory;
        this.propertiesSummaryProvider = propertiesSummaryProvider;
    }

    @Override public Representation format(final FeedEntries entries)
    {
        final Representation hal = representationFactory.newRepresentation(uriFactory.createForFeed());

        for (final FeedEntry entry : entries.all())
        {
            final Representation embedded = representationFactory.newRepresentation(uriFactory.createForFeedEntry(entry.id));

            for (final Map.Entry<String, String> property : propertiesSummaryProvider.getSummarisedProperties(entry).entrySet())
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

        for (final Map.Entry<String, String> property : propertiesSummaryProvider.getProperties(entry).entrySet())
        {
            hal.withProperty(property.getKey(), property.getValue());
        }

        return hal;
    }
}
