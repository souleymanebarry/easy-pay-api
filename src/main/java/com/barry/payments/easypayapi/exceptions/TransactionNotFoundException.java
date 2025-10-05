package com.barry.payments.easypayapi.exceptions;

public class TransactionNotFoundException extends RuntimeException{

    public TransactionNotFoundException(String message) {
        super("Transaction not found with ID: "+message);
    }
}
