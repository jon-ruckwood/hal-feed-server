package com.qmetric.feed.app;

import com.qmetric.feed.domain.Id;
import com.qmetric.feed.domain.IdFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class SequencedIdFactory implements IdFactory
{
    private final AtomicInteger atomicInt = new AtomicInteger();

    @Override public Id create()
    {
        return Id.of(String.valueOf(atomicInt.getAndIncrement()));
    }
}
