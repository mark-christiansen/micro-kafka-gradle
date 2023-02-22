package com.mycompany.kafka.streams.topology;

import org.apache.kafka.streams.Topology;

import java.util.Properties;

public interface TopologyBuilder {
    Topology build(Properties streamProps);
}
