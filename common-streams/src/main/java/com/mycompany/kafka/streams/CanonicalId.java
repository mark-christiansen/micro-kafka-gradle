package com.mycompany.kafka.streams;

import java.util.UUID;

public class CanonicalId {

    public static UUID getId(String source, String sourceType, long id) {
        // identify uniquely identifies source and ID
        return UUID.nameUUIDFromBytes((source + "-" + sourceType + "-" + id).getBytes());
    }
}
