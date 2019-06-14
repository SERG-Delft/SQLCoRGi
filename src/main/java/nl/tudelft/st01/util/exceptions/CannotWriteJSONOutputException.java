package nl.tudelft.st01.util.exceptions;

/**
 *  Exception that can be used to alert users that the output JSON file could not be saved succesfully.
 */
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
