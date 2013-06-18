package com.qmetric.feed.app;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import javax.sql.DataSource;

import java.beans.PropertyVetoException;

public class DataSourceFactory
{
    public static DataSource create(final DataSourceConfiguration configuration)
    {
        final ComboPooledDataSource dataSource = new ComboPooledDataSource(true);

        try
        {
            dataSource.setDriverClass(configuration.driver);
        }
        catch (PropertyVetoException e)
        {
            throw new RuntimeException(e);
        }

        dataSource.setJdbcUrl(configuration.url);
        dataSource.setUser(configuration.username);
        dataSource.setPassword(configuration.password);

        return dataSource;
    }
}
