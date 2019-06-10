package nl.tudelft.st01.util.exceptions;

/**
 *  Exception that can be used to alert users that an item cannot be null.
 */
public class CanNotBeNullException extends RuntimeException {

    /**
     * Parameterless Constructor.
     */

    public CanNotBeNullException() {

    }

    /**
     * Constructor that accepts a message.
     *
     * @param message - Message to pass along to the Exception
     */
    public CanNotBeNullException(String message) {
        super(message);
    }
}
