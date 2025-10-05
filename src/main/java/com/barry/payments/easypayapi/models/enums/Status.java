package com.barry.payments.easypayapi.models.enums;

public enum Status {

    NEW("New"),
    AUTHORIZED("Authorized"),
    CAPTURED("Captured"),
    ;

    private final String label;

    Status(String label) {
        this.label = label;
    }
}
