package com.qmetric.feed.domain.validation;

import com.qmetric.feed.domain.Payload;

import java.util.Collection;

import static com.google.common.base.Preconditions.checkArgument;

public class MandatoryPayloadAttributesRule implements PayloadValidationRule
{
    private final Collection<String> requiredAttributeNames;

    public MandatoryPayloadAttributesRule(final Collection<String> requiredAttributeNames)
    {
        this.requiredAttributeNames = requiredAttributeNames;
    }

    @Override public void checkValid(final Payload payload) throws IllegalArgumentException
    {
        checkArgument(payload.attributes.keySet().containsAll(requiredAttributeNames), "Required payload attributes: %s", requiredAttributeNames);
    }
}
