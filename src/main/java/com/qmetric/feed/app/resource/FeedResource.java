package com.qmetric.feed.app.resource;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.qmetric.feed.app.auth.Principle;
import com.qmetric.feed.domain.Feed;
import com.qmetric.feed.domain.FeedEntry;
import com.qmetric.feed.domain.FeedRepresentationFactory;
import com.qmetric.feed.domain.FeedRestrictionCriteria;
import com.qmetric.feed.domain.Id;
import com.qmetric.feed.domain.Payload;
import com.theoryinpractise.halbuilder.api.Representation;
import com.yammer.dropwizard.auth.Auth;
import com.yammer.metrics.annotation.Timed;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import java.net.URI;
import java.net.URISyntaxException;

import static com.qmetric.feed.app.resource.FeedResource.CONTEXT;
import static com.qmetric.feed.domain.FeedRestrictionCriteria.Filter;
import static com.theoryinpractise.halbuilder.api.RepresentationFactory.HAL_JSON;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static javax.ws.rs.core.Response.created;
import static javax.ws.rs.core.Response.ok;
import static javax.ws.rs.core.Response.status;

@Path(CONTEXT) @Produces(HAL_JSON)
public class FeedResource
{
    private static final String NOT_FOUND_RESPONSE_BODY = "Feed entry not found";

    public static final String CONTEXT = "/feed";

    private final Feed feed;

    private final FeedRepresentationFactory<Representation> feedRepresentationFactory;

    private final int defaultEntriesPerPage;

    public FeedResource(final Feed feed, final FeedRepresentationFactory<Representation> feedRepresentationFactory, final int defaultEntriesPerPage)
    {
        this.feed = feed;
        this.feedRepresentationFactory = feedRepresentationFactory;
        this.defaultEntriesPerPage = defaultEntriesPerPage;
    }

    @GET @Timed
    public Response getPage(@Auth final Principle principle, @QueryParam("earlierThan") final Optional<String> earlierThan, @QueryParam("laterThan") final Optional<String> laterThan,
                            @QueryParam("limit") final Optional<Integer> limit)
    {
        try
        {
            return ok(feedRepresentationFactory
                              .format(feed.retrieveBy(new FeedRestrictionCriteria(new Filter(toOptionalId(earlierThan), toOptionalId(laterThan)), limit.or(defaultEntriesPerPage))))
                              .toString(HAL_JSON)).build();
        }
        catch (IllegalArgumentException e)
        {
            return status(HTTP_BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @GET @Path("{id}") @Timed
    public Response getEntry(@Auth final Principle principle, @PathParam("id") final String id)
    {
        final Optional<FeedEntry> feedEntry = feed.retrieveBy(Id.of(id));

        if (feedEntry.isPresent())
        {
            return ok(feedRepresentationFactory.format(feedEntry.get()).toString(HAL_JSON)).build();
        }
        else
        {
            return status(HTTP_NOT_FOUND).entity(NOT_FOUND_RESPONSE_BODY).build();
        }
    }

    @POST @Timed
    public Response postEntry(@Auth final Principle principle, final Payload payload) throws URISyntaxException
    {
        try
        {
            final Representation hal = feedRepresentationFactory.format(feed.publish(payload));

            return created(new URI(hal.getResourceLink().getHref())).entity(hal.toString(HAL_JSON)).build();
        }
        catch (IllegalArgumentException e)
        {
            return status(HTTP_BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    private Optional<Id> toOptionalId(final Optional<String> idString)
    {
        return idString.transform(new Function<String, Id>()
        {
            @Override public Id apply(final String input)
            {
                return Id.of(input);
            }
        });
    }
}
