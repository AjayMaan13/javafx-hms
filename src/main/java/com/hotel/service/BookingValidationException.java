package com.hotel.service;

public class BookingValidationException extends RuntimeException {

    public BookingValidationException(String message) {
        super(message);
    }
}
