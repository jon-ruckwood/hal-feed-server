package com.qmetric.feed.domain

import spock.lang.Specification

class HiddenPayloadAttributesTest extends Specification {

    def "should hold attribute names to hide"()
    {
        expect:
        new HiddenPayloadAttributes(["abc"]).all() == ["abc"]
    }

    def "should check whether a given attribute name is hidden"()
    {
        given:
        final hiddenAttributes = new HiddenPayloadAttributes(["abc"])

        expect:
        hiddenAttributes.isHidden("abc")
        !hiddenAttributes.isNotHidden("abc")
        !hiddenAttributes.isHidden("cba")
        hiddenAttributes.isNotHidden("cba")
    }
}
