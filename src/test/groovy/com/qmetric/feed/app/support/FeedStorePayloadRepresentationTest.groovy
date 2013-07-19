package com.qmetric.feed.app.support

import com.fasterxml.jackson.databind.ObjectMapper
import com.qmetric.feed.domain.Payload
import spock.lang.Specification

class FeedStorePayloadRepresentationTest extends Specification {

    final expectedPayload = new Payload(["a": "1", "b": true, "inner": ["c": 1], arry: ["d", "e"]])

    final expectedDeserializedPayload = """{"a":"1","b":true,"inner":{"c":1},"arry":["d","e"]}"""

    final representation = new FeedStorePayloadRepresentation(new ObjectMapper())

    def "should serialize payload for writing to datastore"()
    {
        expect:
        expectedDeserializedPayload == representation.serialize(expectedPayload)
    }

    def "should deserialize payload for reading from datastore"()
    {
        expect:
        expectedPayload == representation.deserialize(expectedDeserializedPayload)
    }
}
