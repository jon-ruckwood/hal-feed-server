package com.qmetric.feed.domain
import com.google.common.base.Optional
import spock.lang.Specification

import static FeedRestrictionCriteria.Filter

class FeedRestrictionCriteriaTest extends Specification {

    def "should allow criteria for entries occurring earlier than entry with given id"()
    {
        expect:
        new FeedRestrictionCriteria(new Filter(Optional.of(Id.of("5")), Optional.absent()), 10).filter.earlierThan.get() == Id.of("5")
    }

    def "should allow criteria for entries occurring later than entry with given id"()
    {
        expect:
        new FeedRestrictionCriteria(new Filter(Optional.absent(), Optional.of(Id.of("5"))), 10).filter.laterThan.get() == Id.of("5")
    }

    def "should allow criteria for latest given number of entries"()
    {
        expect:
        final criteria = new FeedRestrictionCriteria(new Filter(Optional.absent(), Optional.absent()), 10)
        criteria.limit == 10
    }

    def "should throw exception if limit < 1"()
    {
        when:
        new FeedRestrictionCriteria(new Filter(Optional.absent(), Optional.absent()), 0)

        then:
        thrown(IllegalArgumentException)
    }

    def "should throw exception with both earlier and later than conditions"()
    {
        when:
        new FeedRestrictionCriteria(new Filter(Optional.of(Id.of("5")), Optional.of(Id.of("1"))), 10)

        then:
        thrown(IllegalArgumentException)
    }
}
