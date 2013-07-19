package com.qmetric.feed.domain.validation

import com.qmetric.feed.domain.Payload
import spock.lang.Specification

class MandatoryPayloadAttributesRuleTest extends Specification {

    def "should not throw Exception when valid"()
    {
        when:
        new MandatoryPayloadAttributesRule(["requiredAttr"]).checkValid(new Payload(["requiredAttr": "abc", "somethingElse": "123"]))

        then:
        notThrown(IllegalArgumentException)
    }

    def "should throw IllegalArgumentException when invalid"()
    {
        when:
        new MandatoryPayloadAttributesRule(["requiredAttr"]).checkValid(new Payload(["somethingElse": "123"]))

        then:
        thrown(IllegalArgumentException)
    }
}
