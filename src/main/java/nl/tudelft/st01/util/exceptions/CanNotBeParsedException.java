package nl.tudelft.st01.util.exceptions;

/**
 *  Exception that can be used to alert users that a certain input cannot be parsed by our parser.
 */
public class CanNotBeParsedException extends RuntimeException {

    /**
     * Constructor that accepts a message.
     *
     * @param message - Message to pass along to the Exception
     */
    public CanNotBeParsedException(String message) {
        super(message);
    }
}
