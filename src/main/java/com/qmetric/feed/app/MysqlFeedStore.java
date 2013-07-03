package com.qmetric.feed.app;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import com.octo.java.sql.query.InsertQuery;
import com.octo.java.sql.query.SelectQuery;
import com.qmetric.feed.app.support.FeedStorePayloadRepresentation;
import com.qmetric.feed.domain.FeedEntries;
import com.qmetric.feed.domain.FeedEntry;
import com.qmetric.feed.domain.FeedRestrictionCriteria;
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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Comparator;
import java.util.List;

import static com.google.common.collect.FluentIterable.from;
import static com.octo.java.sql.query.Query.c;
import static com.octo.java.sql.query.Query.insertInto;
import static com.octo.java.sql.query.Query.select;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static org.apache.commons.lang3.math.NumberUtils.isNumber;

public class MysqlFeedStore implements FeedStore
{
    private static final String ALL = "*";

    private static final String FEED = "feed";

    private static final String ID = "id";

    private static final String PUBLISHED_DATE = "published_date";

    private static final String PAYLOAD = "payload";

    private static final int EXTENDED_LIMIT_TO_INCLUDE_FIRST_ENTRY_FROM_NEXT_AND_PREVIOUS_PAGES = 2;

    private static final List<FeedEntriesPageFactory> FEED_ENTRIES_FACTORIES = ImmutableList.of(new EarlierThanCriteriaFeedEntriesPageFactory(), //
                                                                                                new LaterThanCriteriaFeedEntriesPageFactory(), //
                                                                                                new LatestFeedEntriesPageFactory());

    private final DBI dbi;

    private final FeedStorePayloadRepresentation payloadRepresentation;

    private final ResultSetMapper<FeedEntry> feedSqlMapper = new FeedSqlMapper();

    public MysqlFeedStore(final DBI dbi, final FeedStorePayloadRepresentation payloadSerializer)
    {
        this.payloadRepresentation = payloadSerializer;
        this.dbi = dbi;
    }

    @Override public FeedEntry store(final FeedEntry feedEntry)
    {
        final long autoIncrementKey = dbi.withHandle(new HandleCallback<Long>()
        {
            public Long withHandle(final Handle handle) throws Exception
            {
                final InsertQuery query =
                        insertInto(FEED).set(PUBLISHED_DATE, new Timestamp(feedEntry.publishedDate.getMillis())).set(PAYLOAD, payloadRepresentation.serialize(feedEntry.payload));

                return handle.createStatement(query.toSql()) //
                        .bindFromMap(query.getParams()) //
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
                return handle.createQuery(select(ALL).from(FEED).orderBy(ID).desc().toSql()) //
                        .map(feedSqlMapper) //
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
                final SelectQuery query = select(ALL).from(FEED).where(c(ID)).eq(id.toString());

                return Optional.fromNullable(handle.createQuery(query.toSql()) //
                                                     .bindFromMap(query.getParams()) //
                                                     .map(feedSqlMapper) //
                                                     .first() //
                );
            }
        }) : Optional.<FeedEntry>absent();
    }

    @Override public FeedEntries retrieveBy(final FeedRestrictionCriteria criteria)
    {
        final List<FeedEntry> currentPageOfEntriesIncludingFirstEntryFromNextAndPreviousPages = dbi.withHandle(new HandleCallback<List<FeedEntry>>()
        {
            public List<FeedEntry> withHandle(final Handle handle) throws Exception
            {
                final SelectQuery query = select(ALL).from(FEED);

                if (criteria.filter.earlierThan.isPresent())
                {
                    query.where(c(ID)).leq(Long.valueOf(criteria.filter.earlierThan.get().toString()));
                    query.orderBy(ID).desc();
                }
                else if (criteria.filter.laterThan.isPresent())
                {
                    query.where(c(ID)).geq(Long.valueOf(criteria.filter.laterThan.get().toString()));
                    query.orderBy(ID).asc();
                }
                else
                {
                    query.orderBy(ID).desc();
                }

                query.limit((long) criteria.limit + EXTENDED_LIMIT_TO_INCLUDE_FIRST_ENTRY_FROM_NEXT_AND_PREVIOUS_PAGES);

                return sortInDescendingInsertionOrder(handle.createQuery(query.toSql()) //
                                                              .bindFromMap(query.getParams()) //
                                                              .map(feedSqlMapper) //
                                                              .list()); //
            }
        });

        return feedEntriesPageFrom(currentPageOfEntriesIncludingFirstEntryFromNextAndPreviousPages, criteria);
    }

    private static List<FeedEntry> sortInDescendingInsertionOrder(final List<FeedEntry> feedEntries)
    {
        return Ordering.from(new Comparator<FeedEntry>()
        {
            @Override public int compare(final FeedEntry left, final FeedEntry right)
            {
                return Integer.valueOf(right.id.toString()).compareTo(Integer.valueOf(left.id.toString()));
            }
        }).immutableSortedCopy(feedEntries);
    }

    private static List<FeedEntry> remove(final List<FeedEntry> entries, final Id excludeId)
    {
        return from(entries).filter(new Predicate<FeedEntry>()
        {
            @Override public boolean apply(final FeedEntry input)
            {
                return !input.id.equals(excludeId);
            }
        }).toList();
    }

    private class FeedSqlMapper implements ResultSetMapper<FeedEntry>
    {
        @Override public FeedEntry map(final int row, final ResultSet resultSet, final StatementContext statementContext) throws SQLException
        {
            final Id id = Id.of(resultSet.getString(ID));

            final DateTime publishedDate = new DateTime(resultSet.getTimestamp(PUBLISHED_DATE));

            final Payload payload = payloadRepresentation.deserialize(resultSet.getString(PAYLOAD));

            return new FeedEntry(id, publishedDate, payload);
        }
    }

    private static FeedEntries feedEntriesPageFrom(final List<FeedEntry> entries, final FeedRestrictionCriteria criteria)
    {
        for (final FeedEntriesPageFactory factory : FEED_ENTRIES_FACTORIES)
        {
            if (factory.isApplicable(criteria))
            {
                return factory.create(entries, criteria);
            }
        }

        throw new RuntimeException("No matching FeedEntries factory found");
    }

    private interface FeedEntriesPageFactory
    {
        boolean isApplicable(FeedRestrictionCriteria criteria);

        FeedEntries create(List<FeedEntry> feedEntries, FeedRestrictionCriteria criteria);
    }

    private static class EarlierThanCriteriaFeedEntriesPageFactory implements FeedEntriesPageFactory
    {
        @Override public boolean isApplicable(final FeedRestrictionCriteria criteria)
        {
            return criteria.filter.earlierThan.isPresent();
        }

        @Override public FeedEntries create(final List<FeedEntry> pageWithSurplusEntryFromNextAndPreviousPage, final FeedRestrictionCriteria criteria)
        {
            final List<FeedEntry> pageWithoutSurplusEntryFromNextPage = remove(pageWithSurplusEntryFromNextAndPreviousPage, criteria.filter.earlierThan.get());

            final int indexOfLatestEntryForPage = 0;
            final int indexOfEarliestEntryForPage = min(pageWithoutSurplusEntryFromNextPage.size(), criteria.limit);

            final List<FeedEntry> page = pageWithoutSurplusEntryFromNextPage.subList(indexOfLatestEntryForPage, indexOfEarliestEntryForPage);

            final boolean entryInNextPageExists = pageWithoutSurplusEntryFromNextPage.size() > criteria.limit;
            final boolean entryInPreviousPageExists = pageWithoutSurplusEntryFromNextPage.size() != pageWithSurplusEntryFromNextAndPreviousPage.size();

            return new FeedEntries(page, entryInNextPageExists, entryInPreviousPageExists);
        }
    }

    private static class LaterThanCriteriaFeedEntriesPageFactory implements FeedEntriesPageFactory
    {
        @Override public boolean isApplicable(final FeedRestrictionCriteria criteria)
        {
            return criteria.filter.laterThan.isPresent();
        }

        @Override public FeedEntries create(final List<FeedEntry> pageWithSurplusEntryFromNextAndPreviousPage, final FeedRestrictionCriteria criteria)
        {
            final List<FeedEntry> pageWithoutSurplusEntryFromPreviousPage = remove(pageWithSurplusEntryFromNextAndPreviousPage, criteria.filter.laterThan.get());

            final int indexOfLatestEntryForPage = max(0, pageWithoutSurplusEntryFromPreviousPage.size() - criteria.limit);
            final int indexOfEarliestEntryForPage = min(pageWithoutSurplusEntryFromPreviousPage.size(), indexOfLatestEntryForPage + criteria.limit);

            final List<FeedEntry> page = pageWithoutSurplusEntryFromPreviousPage.subList(indexOfLatestEntryForPage, indexOfEarliestEntryForPage);

            final boolean entryInNextPageExists = pageWithoutSurplusEntryFromPreviousPage.size() != pageWithSurplusEntryFromNextAndPreviousPage.size();
            final boolean entryInPreviousPageExists = pageWithoutSurplusEntryFromPreviousPage.size() > criteria.limit;

            return new FeedEntries(page, entryInNextPageExists, entryInPreviousPageExists);
        }
    }

    private static class LatestFeedEntriesPageFactory implements FeedEntriesPageFactory
    {
        @Override public boolean isApplicable(final FeedRestrictionCriteria criteria)
        {
            return !criteria.filter.laterThan.isPresent() && !criteria.filter.laterThan.isPresent();
        }

        @Override public FeedEntries create(final List<FeedEntry> pageWithSurplusEntryFromNextAndPreviousPage, final FeedRestrictionCriteria criteria)
        {
            return new FeedEntries(pageWithSurplusEntryFromNextAndPreviousPage.subList(0, min(pageWithSurplusEntryFromNextAndPreviousPage.size(), criteria.limit)),
                                   pageWithSurplusEntryFromNextAndPreviousPage.size() > criteria.limit, false);
        }
    }
}
