package nl.tudelft.st01.util.exceptions;

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
