package com.qmetric.feed.domain.validation;

import com.qmetric.feed.domain.Payload;

public class PayloadValidationRules
{
    private final Iterable<PayloadValidationRule> validationRules;

    public PayloadValidationRules(final Iterable<PayloadValidationRule> validationRules)
    {
        this.validationRules = validationRules;
    }

    public void checkValid(final Payload payload) throws IllegalArgumentException
    {
        for (final PayloadValidationRule validationRule : validationRules)
        {
            validationRule.checkValid(payload);
        }
    }

    public Iterable<PayloadValidationRule> all()
    {
        return validationRules;
    }
}
