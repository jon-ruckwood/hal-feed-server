package com.qmetric.feed.app;

import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;

public class DataSourceConfiguration
{
    public final String driver;

    public final String url;

    public final String username;

    public final String password;

    public DataSourceConfiguration(final String driver, final String url, final String username, final String password)
    {
        this.driver = driver;
        this.url = url;
        this.username = username;
        this.password = password;
    }

    @Override
    public int hashCode()
    {
        return reflectionHashCode(this);
    }

    @Override
    public boolean equals(final Object obj)
    {
        return reflectionEquals(this, obj);
    }
}
