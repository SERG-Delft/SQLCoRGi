package nl.tudelft.st01.util.exceptions;

/**
 *  Exception that can be used to alert users that an item cannot be null.
 */
public class IllegalNumberOfArgumentsException extends RuntimeException {

    /**
     * Parameterless Constructor.
     */

    public IllegalNumberOfArgumentsException() {

    }

    /**
     * Constructor that accepts a message.
     *
     * @param message - Message to pass along to the Exception
     */
    public IllegalNumberOfArgumentsException(String message) {
        super(message);
    }
}
