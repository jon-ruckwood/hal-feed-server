package com.qmetric.feed.domain.validation;

import com.qmetric.feed.domain.Payload;

public interface PayloadValidationRule
{
    void checkValid(final Payload payload) throws IllegalArgumentException;
}
