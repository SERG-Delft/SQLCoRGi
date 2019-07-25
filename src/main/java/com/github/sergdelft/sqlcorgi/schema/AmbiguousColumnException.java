package com.github.sergdelft.sqlcorgi.schema;

/**
 * This exception should be thrown if a query contains an ambiguous reference to a column.
 */
public class AmbiguousColumnException extends RuntimeException {

    /**
     * Creates a new instance of this exception with a message.
     *
     * @param message a message describing why this exception is thrown.
     */
    public AmbiguousColumnException(String message) {
        super(message);
    }

}
