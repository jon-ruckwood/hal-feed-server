package com.qmetric.feed.app.auth;

import com.google.common.base.Optional;
import com.yammer.dropwizard.auth.AuthenticationException;
import com.yammer.dropwizard.auth.Authenticator;
import com.yammer.dropwizard.auth.basic.BasicCredentials;

public class BasicAuthenticator implements Authenticator<BasicCredentials, Principle>
{
    private final String username;

    private final char[] password;

    public BasicAuthenticator(final String username, final char[] password)
    {
        this.username = username;
        this.password = password;
    }

    @Override
    public Optional<Principle> authenticate(final BasicCredentials credentials) throws AuthenticationException
    {
        return isValid(credentials) ? Optional.of(new Principle(credentials.getUsername())) : Optional.<Principle>absent();
    }

    private boolean isValid(final BasicCredentials credentials)
    {
        return username.equals(credentials.getUsername()) && String.valueOf(password).equals(credentials.getPassword());
    }
}
