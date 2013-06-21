package com.qmetric.feed.app.routes;

import com.qmetric.feed.app.PayloadSerializationMapper;
import com.qmetric.feed.domain.Feed;
import com.qmetric.feed.domain.FeedEntry;
import com.qmetric.feed.domain.FeedRepresentationFactory;
import com.qmetric.feed.domain.Payload;
import com.theoryinpractise.halbuilder.api.Representation;
import spark.Request;
import spark.Response;
import spark.Route;

import static com.google.common.net.HttpHeaders.LOCATION;
import static com.theoryinpractise.halbuilder.api.RepresentationFactory.HAL_JSON;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_CREATED;

public class PublishToFeedRoute extends Route
{
    private final Feed feed;

    private final FeedRepresentationFactory<Representation> feedRepresentationFactory;

    private final PayloadSerializationMapper payloadSerializationMapper;

    public PublishToFeedRoute(final String path, final Feed feed, final FeedRepresentationFactory<Representation> feedRepresentationFactory,
                              final PayloadSerializationMapper payloadSerializationMapper)
    {
        super(path);
        this.feed = feed;
        this.feedRepresentationFactory = feedRepresentationFactory;
        this.payloadSerializationMapper = payloadSerializationMapper;
    }

    @Override public Object handle(final Request request, final Response response)
    {
        try
        {
            final Payload payload = payloadSerializationMapper.deserializePayload(request.body());

            final FeedEntry newEntry = feed.publish(payload);

            final Representation hal = feedRepresentationFactory.format(newEntry);

            response.header(LOCATION, hal.getResourceLink().getHref());

            response.status(HTTP_CREATED);

            return hal.toString(HAL_JSON);
        }
        catch (IllegalArgumentException e)
        {
            response.status(HTTP_BAD_REQUEST);

            return e.getMessage();
        }
    }
}
