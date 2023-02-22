package com.mycompany.kafka.streams.topology;

import java.time.Instant;
import java.time.LocalDate;

public abstract class AbstractTopologyBuilder implements TopologyBuilder {

    protected String asString(Object value) {
        if (value != null) {
            return value.toString();
        }
        return null;
    }

    protected Instant asInstant(Object value) {
        if (value != null) {
            return  Instant.ofEpochMilli((Long) value);
        }
        return null;
    }

    protected LocalDate asLocalDate(Object value) {
        if (value != null) {
            return  LocalDate.ofEpochDay((Integer) value);
        }
        return null;
    }
}
