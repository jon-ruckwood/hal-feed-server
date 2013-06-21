package com.qmetric.feed.app.routes;

import com.google.common.base.Optional;
import com.qmetric.feed.domain.Feed;
import com.qmetric.feed.domain.FeedEntry;
import com.qmetric.feed.domain.FeedRepresentationFactory;
import com.qmetric.feed.domain.Id;
import com.theoryinpractise.halbuilder.api.Representation;
import spark.Request;
import spark.Response;
import spark.Route;

import static com.theoryinpractise.halbuilder.api.RepresentationFactory.HAL_JSON;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;

public class RetrieveEntryFromFeedRoute extends Route
{
    private static final Object EMPTY_BODY = null;

    private final Feed feed;

    private final FeedRepresentationFactory<Representation> feedRepresentationFactory;

    public RetrieveEntryFromFeedRoute(final String path, final Feed feed, final FeedRepresentationFactory<Representation> feedRepresentationFactory)
    {
        super(path);
        this.feed = feed;
        this.feedRepresentationFactory = feedRepresentationFactory;
    }

    @Override public Object handle(final Request request, final Response response)
    {
        final Optional<FeedEntry> feedEntry = feed.retrieveBy(Id.of(request.params("id")));

        if (feedEntry.isPresent())
        {
            return feedRepresentationFactory.format(feedEntry.get()).toString(HAL_JSON);
        }
        else
        {
            response.status(HTTP_NOT_FOUND);

            return EMPTY_BODY;
        }
    }
}
