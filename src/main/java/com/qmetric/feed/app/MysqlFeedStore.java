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
import org.skife.jdbi.v2.util.LongMapper;

import javax.sql.DataSource;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.math.NumberUtils.isNumber;

public class MysqlFeedStore implements FeedStore
{
    private static final ResultSetMapper<FeedEntry> FEED_SQL_MAPPER = new FeedSqlMapper();

    private static final ObjectMapper PAYLOAD_MAPPER = new ObjectMapper();

    private final DBI dbi;

    public MysqlFeedStore(final DataSource dataSource)
    {
        dbi = new DBI(dataSource);
    }

    @Override public FeedEntry store(final FeedEntry feedEntry)
    {
        final long autoIncrementKey = dbi.withHandle(new HandleCallback<Long>()
        {
            public Long withHandle(final Handle handle) throws Exception
            {
                return handle.createStatement("INSERT INTO feed (published_date, payload) VALUES (:publishedDate, :payload)") //
                        .bind("publishedDate", feedEntry.publishedDate.toDate()) //
                        .bind("payload", PAYLOAD_MAPPER.writeValueAsString(feedEntry.payload.attributes)) //
                        .executeAndReturnGeneratedKeys(LongMapper.FIRST).first();
            }
        });

        return new FeedEntry(Id.of(String.valueOf(autoIncrementKey)), feedEntry.publishedDate, feedEntry.payload);
    }

    @Override public FeedEntries retrieveAll()
    {
        return new FeedEntries(dbi.withHandle(new HandleCallback<List<FeedEntry>>()
        {
            public List<FeedEntry> withHandle(final Handle handle) throws Exception
            {
                return handle.createQuery("SELECT * FROM feed ORDER BY published_date DESC, id DESC") //
                        .map(FEED_SQL_MAPPER) //
                        .list(); //
            }
        }));
    }

    @Override public Optional<FeedEntry> retrieveBy(final Id id)
    {
        return isNumber(id.toString()) ? dbi.withHandle(new HandleCallback<Optional<FeedEntry>>()
        {
            public Optional<FeedEntry> withHandle(final Handle handle) throws Exception
            {
                return Optional.fromNullable(handle.createQuery("SELECT * FROM feed WHERE id = :id") //
                                                     .bind("id", id.toString()) //
                                                     .map(FEED_SQL_MAPPER) //
                                                     .first() //
                );
            }
        }) : Optional.<FeedEntry>absent();
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
