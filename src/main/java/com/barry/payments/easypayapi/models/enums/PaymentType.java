package com.barry.payments.easypayapi.models.enums;

public enum PaymentType {

    CREDIT_CARD("CREDIT CARD"),
    GIFT_CARD("GIFT CARD"),
    PAYPAL("PAYPAL");

    public String getLabel() {
        return label;
    }

    private final String label;

    PaymentType(String label) {
        this.label = label;
    }
}
