package com.qmetric.feed.app.configuration;

import com.qmetric.feed.app.Configuration;
import com.qmetric.feed.app.DataSourceFactory;
import com.qmetric.spark.metrics.DBHealthCheck;

import java.sql.SQLException;

public class DBHealthCheckBuilder
{

    private final Configuration configuration;

    public DBHealthCheckBuilder(final Configuration configuration)
    {
        this.configuration = configuration;
    }

    public DBHealthCheck build() throws SQLException
    {
        return new DBHealthCheck(DataSourceFactory.create(configuration.dataSourceConfiguration));
    }
}
