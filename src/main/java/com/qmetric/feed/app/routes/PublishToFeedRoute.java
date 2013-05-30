package com.qmetric.feed.app.routes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qmetric.feed.domain.Feed;
import com.qmetric.feed.domain.FeedEntry;
import com.qmetric.feed.domain.FeedRepresentationFactory;
import com.qmetric.feed.domain.Resource;
import com.theoryinpractise.halbuilder.api.Representation;
import spark.Request;
import spark.Response;
import spark.Route;

import java.io.IOException;
import java.util.Map;

import static com.google.common.net.HttpHeaders.LOCATION;
import static com.theoryinpractise.halbuilder.api.RepresentationFactory.HAL_JSON;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_CREATED;

public class PublishToFeedRoute extends Route
{
    private final Feed feed;

    private final FeedRepresentationFactory<Representation> feedRepresentationFactory;

    private final ObjectMapper jsonObjectMapper;

    public PublishToFeedRoute(final String path, final Feed feed, final FeedRepresentationFactory<Representation> feedRepresentationFactory, final ObjectMapper jsonObjectMapper)
    {
        super(path);
        this.feed = feed;
        this.feedRepresentationFactory = feedRepresentationFactory;
        this.jsonObjectMapper = jsonObjectMapper;
    }

    @Override public Object handle(final Request request, final Response response)
    {
        try
        {
            //noinspection unchecked
            final Map<String, Object> resourceAttributes = jsonObjectMapper.readValue(request.body(), Map.class);

            final FeedEntry newEntry = feed.publish(new Resource(resourceAttributes));

            final Representation hal = feedRepresentationFactory.format(newEntry);

            response.header(LOCATION, hal.getResourceLink().getHref());

            response.status(HTTP_CREATED);

            return hal.toString(HAL_JSON);
        }
        catch (IOException e)
        {
            response.status(HTTP_BAD_REQUEST);

            return e.toString();
        }
    }
}
