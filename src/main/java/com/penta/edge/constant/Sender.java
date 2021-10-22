package com.penta.edge.constant;

public enum Sender {
    VEHICLE("01"),
    NODE("02"),
    CENTRAL("03"),
    USER("04");

    private final String value;

    Sender(String value) {
        this.value = value;
    }

    public String getValue() {return value;}
}
