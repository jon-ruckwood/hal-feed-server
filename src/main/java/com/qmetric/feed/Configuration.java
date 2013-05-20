package com.qmetric.feed;

import com.google.common.base.Function;
import com.qmetric.feed.domain.FeedEntryLink;
import com.qmetric.feed.domain.Links;
import org.yaml.snakeyaml.Yaml;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Map;

import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.Lists.newArrayList;
import static com.qmetric.feed.domain.Links.NO_LINKS;
import static java.lang.String.format;

public class Configuration
{
    private static final String PUBLIC_BASE_URL = "publicBaseUrl";

    private static final String LOCAL_PORT = "localPort";

    private static final String FEED_NAME = "feedName";

    private static final String FEED_ENTRY_LINKS = "feedEntryLinks";

    private static final String LINK = "link";

    private static final String LINK_REL = "rel";

    private static final String LINK_HREF = "href";

    public final String publicBaseUrl;

    public final int localPort;

    public final String feedName;

    public final URI feedSelfLink;

    public final Links feedEntryLinks;

    public static Configuration load(final InputStream inputStream) throws FileNotFoundException, URISyntaxException
    {
        return new Configuration(inputStream);
    }

    private Configuration(final InputStream inputStream) throws FileNotFoundException, URISyntaxException
    {
        //noinspection unchecked
        final Map<String, Object> properties = (Map<String, Object>) new Yaml().load(inputStream);

        publicBaseUrl = (String) properties.get(PUBLIC_BASE_URL);

        localPort = (Integer) properties.get(LOCAL_PORT);

        feedName = (String) properties.get(FEED_NAME);

        feedSelfLink = new URI(format("%s/%s", publicBaseUrl, feedName));

        feedEntryLinks = parseLinks(properties);
    }

    private Links parseLinks(final Map<String, Object> properties)
    {
        if (properties.containsKey(FEED_ENTRY_LINKS))
        {
            //noinspection unchecked
            final Collection<Map<String, Object>> linksConfiguration = (Collection<Map<String, Object>>) properties.get(FEED_ENTRY_LINKS);

            return new Links(newArrayList(transform(linksConfiguration, new Function<Map<String, Object>, FeedEntryLink>()
            {
                @Override public FeedEntryLink apply(final Map<String, Object> input)
                {
                    //noinspection unchecked
                    final Map<String, Object> linkDetails = (Map<String, Object>) input.get(LINK);

                    return new FeedEntryLink((String) linkDetails.get(LINK_REL), (String) linkDetails.get(LINK_HREF));
                }
            })));
        }
        else
        {
            return NO_LINKS;
        }
    }
}
