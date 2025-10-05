package com.barry.payments.easypayapi.exceptions;

public class CapturedNotAllowException extends RuntimeException{

    public CapturedNotAllowException(String message) {
        super(message);
    }
}
