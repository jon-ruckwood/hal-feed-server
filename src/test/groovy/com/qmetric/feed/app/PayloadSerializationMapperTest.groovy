package com.qmetric.feed.app

import com.qmetric.feed.domain.Payload
import spock.lang.Specification

class PayloadSerializationMapperTest extends Specification {

    final expectedPayload = new Payload(["a": "1", "b": true, "inner": ["c": 1], arry: ["d", "e"]])

    final expectedDeserializedPayload = """{"a":"1","b":true,"inner":{"c":1},"arry":["d","e"]}"""

    final payloadMapper = new PayloadSerializationMapper()

    def "should serialize payload"()
    {
        expect:
        expectedDeserializedPayload == payloadMapper.serializePayloadOf(expectedPayload)
    }

    def "should deserialize payload"()
    {
        expect:
        expectedPayload == payloadMapper.deserializePayload(expectedDeserializedPayload)
    }
}
