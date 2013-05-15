package com.qmetric.feed;

import org.joda.time.DateTime;

import static org.joda.time.DateTime.*;

public class PublishedDateProvider
{
    public DateTime getPublishedDate()
    {
        return now();
    }
}
