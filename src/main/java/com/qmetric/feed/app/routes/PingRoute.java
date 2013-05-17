package com.qmetric.feed.app.routes;

import spark.Request;
import spark.Response;
import spark.Route;

public class PingRoute extends Route
{
    public PingRoute(final String path)
    {
        super(path);
    }

    @Override public Object handle(final Request request, final Response response)
    {
        return "pong";
    }
}
