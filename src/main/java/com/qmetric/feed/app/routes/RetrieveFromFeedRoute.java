package com.qmetric.feed.app.routes;

import com.google.common.base.Optional;
import com.qmetric.feed.domain.Feed;
import com.qmetric.feed.domain.FeedRepresentationFactory;
import com.qmetric.feed.domain.FeedRestrictionCriteria;
import com.qmetric.feed.domain.Id;
import com.theoryinpractise.halbuilder.api.Representation;
import spark.Request;
import spark.Response;
import spark.Route;

import static com.qmetric.feed.domain.FeedRestrictionCriteria.Filter;
import static com.theoryinpractise.halbuilder.api.RepresentationFactory.HAL_JSON;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static org.apache.commons.lang.math.NumberUtils.toInt;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class RetrieveFromFeedRoute extends Route
{
    private static final String EARLIER_THAN_ENTRY_WITH_ID_PARAM = "earlierThan";

    private static final String LATER_THAN_ENTRY_WITH_ID_PARAM = "laterThan";

    private static final String MAX_RESULTS_LIMIT_PARAM = "limit";

    private static final int DEFAULT_MAX_RESULTS_LIMIT = 10;

    private final Feed feed;

    private final FeedRepresentationFactory<Representation> feedRepresentationFactory;

    public RetrieveFromFeedRoute(final String path, final Feed feed, final FeedRepresentationFactory<Representation> feedRepresentationFactory)
    {
        super(path);
        this.feed = feed;
        this.feedRepresentationFactory = feedRepresentationFactory;
    }

    @Override public Object handle(final Request request, final Response response)
    {
        final String earlierThanEntryWithId = request.queryParams(EARLIER_THAN_ENTRY_WITH_ID_PARAM);

        final String laterThanEntryWithId = request.queryParams(LATER_THAN_ENTRY_WITH_ID_PARAM);

        final Integer maxResultsLimit = toInt(request.queryParams(MAX_RESULTS_LIMIT_PARAM), DEFAULT_MAX_RESULTS_LIMIT);

        try
        {
            final FeedRestrictionCriteria criteria = new FeedRestrictionCriteria(new Filter(optionalId(earlierThanEntryWithId), optionalId(laterThanEntryWithId)), maxResultsLimit);

            return feedRepresentationFactory.format(feed.retrieveBy(criteria)).toString(HAL_JSON);
        }
        catch (IllegalArgumentException e)
        {
            return badRequest(response, e);
        }
        catch (IllegalStateException e)
        {
            return badRequest(response, e);
        }
    }

    private Optional<Id> optionalId(final String id)
    {
        return isNotBlank(id) ? Optional.of(Id.of(id)) : Optional.<Id>absent();
    }

    private Object badRequest(final Response response, final Exception e)
    {
        response.status(HTTP_BAD_REQUEST);

        return e.getMessage();
    }
}
