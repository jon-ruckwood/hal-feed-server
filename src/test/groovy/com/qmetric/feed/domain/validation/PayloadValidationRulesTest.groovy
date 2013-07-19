package com.qmetric.feed.domain.validation

import com.qmetric.feed.domain.Payload
import spock.lang.Specification

class PayloadValidationRulesTest extends Specification {

    def "should check validation rules"()
    {
        given:
        final payload = Mock(Payload)
        final rule1 = Mock(PayloadValidationRule)
        final rule2 = Mock(PayloadValidationRule)
        final rules = new PayloadValidationRules([rule1, rule2])

        when:
        rules.checkValid(payload)

        then:
        1 * rule1.checkValid(payload)
        1 * rule2.checkValid(payload)
    }
}
