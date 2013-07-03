package com.qmetric.feed.app.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.ok;

@Path("/ping")
public class PingResource
{
    @GET
    public Response ping()
    {
        return ok("pong").build();
    }
}
