package com.qmetric.feed.app.routes;

import com.qmetric.feed.domain.Feed;
import com.qmetric.feed.domain.FeedRepresentationFactory;
import com.theoryinpractise.halbuilder.api.Representation;
import spark.Request;
import spark.Response;
import spark.Route;

import static com.theoryinpractise.halbuilder.api.RepresentationFactory.HAL_JSON;

public class RetrieveAllFromFeedRoute extends Route
{
    private final String path;

    private final Feed feed;

    private final FeedRepresentationFactory<Representation> feedRepresentationFactory;

    public RetrieveAllFromFeedRoute(final String path, final Feed feed, final FeedRepresentationFactory<Representation> feedRepresentationFactory)
    {
        super(path);
        this.path = path;
        this.feed = feed;
        this.feedRepresentationFactory = feedRepresentationFactory;
    }

    @Override public Object handle(final Request request, final Response response)
    {
        return feedRepresentationFactory.format(feed.retrieveAll()).toString(HAL_JSON);
    }
}
