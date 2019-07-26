package com.github.sergdelft.sqlcorgi.schema;

/**
 * This exception should be thrown if a column is referenced from within a query whilst the column is not in scope.
 */
public class UnknownColumnException extends RuntimeException {

    /**
     * Creates a new instance of this exception with a message.
     *
     * @param message a message describing why this exception is thrown.
     */
    public UnknownColumnException(String message) {
        super(message);
    }

}
