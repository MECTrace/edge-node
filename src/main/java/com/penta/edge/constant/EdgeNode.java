package com.penta.edge.constant;

public enum EdgeNode {

    EDGE_NODE_1("20.194.98.12","582acd9a-d6ef-4870-80b7-93991cdd89e7"),
    EDGE_NODE_2("52.141.2.70","b00d53c0-317e-4ca7-a35d-fc62f6d4b8dc"),
    EDGE_NODE_3("52.141.1.55","6791a59e-065a-4e4a-b744-e0576ca36d71"),
    EDGE_NODE_4("20.41.96.101","9dc00904-2945-45a8-8aa3-9fe968894292"),
    EDGE_NODE_5("20.41.102.11","cc920d32-5ed3-11ec-ab1a-0022480edc91"),
    EDGE_NODE_6("52.231.98.206","cfbf0384-5ed3-11ec-ab25-000d3ad75607"),
    EDGE_NODE_7("20.194.20.219","e11ff354-5ed3-11ec-8efa-0022480e81a4"),
    EDGE_NODE_8("52.141.61.111","e9b61106-5ed3-11ec-833a-000d3a88217a"),
    EDGE_NODE_9("52.141.61.52","f43ae318-5ed3-11ec-9422-000d3a882d9c"),
    EDGE_NODE_10("52.141.61.54","00af111e-5ed4-11ec-89ba-000d3a8808fb");

    private final String IP;
    private final String UUID;

    EdgeNode(String value, String UUID) {
        this.IP = value;
        this.UUID = UUID;
    }

    public String getIP() {
        return IP;
    }
    public String getUUID() {
        return UUID;
    }

}
