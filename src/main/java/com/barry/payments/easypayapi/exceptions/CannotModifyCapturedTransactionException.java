package com.barry.payments.easypayapi.exceptions;

public class CannotModifyCapturedTransactionException extends RuntimeException{

    public CannotModifyCapturedTransactionException(String message) {
        super(message);
    }
}
