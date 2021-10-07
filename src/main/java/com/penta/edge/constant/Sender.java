package com.penta.edge.constant;

public enum Sender {
    VEHICLE("0x01"),
    NODE("0x02"),
    CENTRAL("0x03"),
    USER("0x04");

    private final String value;

    Sender(String value) {
        this.value = value;
    }

    public String getValue() {return value;}
}
