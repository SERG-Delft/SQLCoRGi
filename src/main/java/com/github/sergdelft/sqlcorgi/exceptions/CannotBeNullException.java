package com.github.sergdelft.sqlcorgi.exceptions;

/**
 *  Exception that can be used to alert users that an item cannot be null.
 */
public class CannotBeNullException extends RuntimeException {

    /**
     * Constructor that accepts a message.
     *
     * @param message - Message to pass along to the Exception
     */
    public CannotBeNullException(String message) {
        super(message);
    }
}
