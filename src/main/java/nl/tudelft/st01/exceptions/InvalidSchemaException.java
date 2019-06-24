package nl.tudelft.st01.exceptions;

/**
 *  Exception that can be used to alert users that the provided schema is not syntactically valid.
 */
public class InvalidSchemaException extends SQLFpcException {

    /**
     * Parameterless Constructor for the InvalidSchemaException class..
     */
    public InvalidSchemaException() {
        // Parameterless Constructor for the InvalidSchemaException class.
    }

    /**
     * Constructor that accepts a message.
     *
     * @param message - Message to pass along to the Exception
     */
    public InvalidSchemaException(String message) {
        super(message);
    }
}
