package com.qmetric.feed.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.qmetric.feed.domain.FeedEntries;
import com.qmetric.feed.domain.FeedEntry;
import com.qmetric.feed.domain.FeedStore;
import com.qmetric.feed.domain.Id;
import com.qmetric.feed.domain.Payload;
import org.joda.time.DateTime;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.HandleCallback;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import javax.sql.DataSource;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class MysqlFeedStore implements FeedStore
{
    private static final ResultSetMapper<FeedEntry> FEED_SQL_MAPPER = new FeedSqlMapper();

    private static final ObjectMapper PAYLOAD_MAPPER = new ObjectMapper();

    private final DBI dbi;

    public MysqlFeedStore(final DataSource dataSource)
    {
        dbi = new DBI(dataSource);
    }

    @Override public void store(final FeedEntry feedEntry)
    {
        dbi.withHandle(new HandleCallback<Integer>()
        {
            public Integer withHandle(final Handle handle) throws Exception
            {
                return handle.createStatement("INSERT INTO feed (id, published_date, payload) VALUES (:id, :publishedDate, :payload)") //
                        .bind("id", feedEntry.id.toString()) //
                        .bind("publishedDate", feedEntry.publishedDate.toDate()) //
                        .bind("payload", PAYLOAD_MAPPER.writeValueAsString(feedEntry.payload.attributes)) //
                        .execute();
            }
        });
    }

    @Override public FeedEntries retrieveAll()
    {
        return new FeedEntries(dbi.withHandle(new HandleCallback<List<FeedEntry>>()
        {
            public List<FeedEntry> withHandle(final Handle handle) throws Exception
            {
                return handle.createQuery("SELECT * FROM feed ORDER BY published_date DESC") //
                        .map(FEED_SQL_MAPPER) //
                        .list(); //
            }
        }));
    }

    @Override public Optional<FeedEntry> retrieveBy(final Id id)
    {
        return dbi.withHandle(new HandleCallback<Optional<FeedEntry>>()
        {
            public Optional<FeedEntry> withHandle(final Handle handle) throws Exception
            {
                return Optional.fromNullable(handle.createQuery("SELECT * FROM feed WHERE id = :id") //
                                                     .bind("id", id.toString()) //
                                                     .map(FEED_SQL_MAPPER) //
                                                     .first() //
                );
            }
        });
    }

    private static Payload deserializePayload(final String payload)
    {
        try
        {
            //noinspection unchecked
            return new Payload(PAYLOAD_MAPPER.readValue(payload, Map.class));
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private static class FeedSqlMapper implements ResultSetMapper<FeedEntry>
    {
        @Override public FeedEntry map(final int row, final ResultSet resultSet, final StatementContext statementContext) throws SQLException
        {
            final Id id = Id.of(resultSet.getString("id"));

            final DateTime publishedDate = new DateTime(resultSet.getTimestamp("published_date"));

            final Payload payload = deserializePayload(resultSet.getString("payload"));

            return new FeedEntry(id, publishedDate, payload);
        }
    }
}
