package com.github.sergdelft.sqlcorgi.schema;

/**
 * This exception should be thrown if a table is referenced from within a query whilst the table is not in scope.
 */
public class UnknownTableException extends RuntimeException {

    /**
     * Creates a new instance of this exception with a message.
     *
     * @param message a message describing why this exception is thrown.
     */
    public UnknownTableException(String message) {
        super(message);
    }

}
