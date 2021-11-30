package com.penta.edge.constant;

public enum EdgeNode {

    EDGE_NODE_1("20.194.98.12","582acd9a-d6ef-4870-80b7-93991cdd89e7"),
    EDGE_NODE_2("52.141.2.70","b00d53c0-317e-4ca7-a35d-fc62f6d4b8dc"),
    EDGE_NODE_3("52.141.1.55","6791a59e-065a-4e4a-b744-e0576ca36d71"),
    EDGE_NODE_4("52.141.20.211","9dc00904-2945-45a8-8aa3-9fe968894292");

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
