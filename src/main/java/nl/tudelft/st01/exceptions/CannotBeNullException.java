package nl.tudelft.st01.exceptions;

/**
 *  Exception that can be used to alert users that an item cannot be null.
 */
public class CannotBeNullException extends RuntimeException {

    /**
     * Parameterless Constructor for the CannotBeNullException class.
     */
    public CannotBeNullException() {
       // Parameterless Constructor for the CannotBeNullException class.
    }

    /**
     * Constructor that accepts a message.
     *
     * @param message - Message to pass along to the Exception
     */
    public CannotBeNullException(String message) {
        super(message);
    }
}
