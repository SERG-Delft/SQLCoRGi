package com.github.sergdelft.sqlcorgi.schema;

/**
 * This exception should be thrown if a query contains an ambiguous reference to a column.
 */
public class AmbiguousColumnException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "The following column reference is ambiguous: ";

    /**
     * Creates a new instance of this exception with a message.
     *
     * @param column the name of the column that is ambiguously referenced.
     */
    public AmbiguousColumnException(String column) {
        super(DEFAULT_MESSAGE + column);
    }

}
