package com.qmetric.feed.app.auth;

import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;
import com.yammer.dropwizard.auth.Auth;

public class AnonymousAuthenticationProvider implements InjectableProvider<Auth, Parameter>
{
    @Override public ComponentScope getScope()
    {
        return ComponentScope.PerRequest;
    }

    @Override public Injectable getInjectable(final ComponentContext componentContext, final Auth auth, final Parameter parameter)
    {
        return new AnonymousInjectable();
    }

    private static class AnonymousInjectable extends AbstractHttpContextInjectable<Principle>
    {
        @Override
        public Principle getValue(HttpContext c)
        {
            return new Principle("anonymous");
        }
    }
}
