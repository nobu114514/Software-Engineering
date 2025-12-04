package com.shop.exception;

public class BuyerServiceException extends RuntimeException {
    public BuyerServiceException(String message) {
        super(message);
    }
    
    public BuyerServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}