package com.barry.payments.easypayapi.controllers;


import com.barry.payments.easypayapi.exceptions.CannotModifyCapturedTransactionException;
import com.barry.payments.easypayapi.exceptions.CapturedNotAllowException;
import com.barry.payments.easypayapi.exceptions.InvalidPaginationParameterException;
import com.barry.payments.easypayapi.exceptions.TransactionNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(TransactionNotFoundException.class)
    public ResponseEntity<String> notFoundHandler(TransactionNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
    }


    @ExceptionHandler({CannotModifyCapturedTransactionException.class, CapturedNotAllowException.class})
    public ResponseEntity<String> handlerBusinessConflict(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }


    //InvalidPaginationParameterException

    @ExceptionHandler(InvalidPaginationParameterException.class)
    public ResponseEntity<String> invalidPaginationParameterHandler(InvalidPaginationParameterException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED.value()).body(ex.getMessage());
    }
}
