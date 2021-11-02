package com.penta.edge.constant;

public enum EdgeNode {

    EDGE_NODE_1("20.194.98.12"),
    EDGE_NODE_2("52.141.2.70"),
    EDGE_NODE_3("52.141.1.55"),
    EDGE_NODE_4("52.141.20.211");

    private final String IP;
    EdgeNode(String value) {
        this.IP = value;
    }
    public String getIP() {return IP;}

}
