package nl.tudelft.st01.util.exceptions;

public class CannotWriteJSONOutputException extends SQLFpcException {

    /**
     * Parameterless Constructor for the CannotWriteJSONOutputException class..
     */
    public CannotWriteJSONOutputException() {
        // Parameterless Constructor for the CannotWriteJSONOutputException class.
    }

    /**
     * Constructor that accepts a message.
     *
     * @param message - Message to pass along to the Exception
     */
    public CannotWriteJSONOutputException(String message) {
        super(message);
    }
}
