package nl.tudelft.st01.util.exceptions;

/**
 *  Exception that can be used to alert users that a certain input cannot be parsed by our parser.
 */
public class CannotBeParsedException extends RuntimeException {

    /**
     * Parameterless Constructor.
     */

    public CannotBeParsedException() {

    }

    /**
     * Constructor that accepts a message.
     *
     * @param message - Message to pass along to the Exception
     */
    public CannotBeParsedException(String message) {
        super(message);
    }
}
