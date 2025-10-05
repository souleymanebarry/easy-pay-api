package com.barry.payments.easypayapi.exceptions;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class InvalidPaginationParameterException extends RuntimeException{

    public InvalidPaginationParameterException(int page, int size) {
        super("Invalid pagination parameters: page= "+page+", size= "+size+
                " : { Page must be >= 0 and/Or size must be > 0 }");
        log.error("Invalid pagination parameters: page= {}, size= {}", page, size);
    }
}
