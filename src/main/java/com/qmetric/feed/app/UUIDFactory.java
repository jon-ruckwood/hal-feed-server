package com.qmetric.feed.app;

import com.qmetric.feed.domain.Id;
import com.qmetric.feed.domain.IdFactory;

import static java.util.UUID.randomUUID;

public class UUIDFactory implements IdFactory
{
    @Override public Id create()
    {
        return Id.of(randomUUID().toString());
    }
}
