package nl.tudelft.st01.util.exceptions;

/**
 *  Exception that can be used to alert programmers that a certain input is not supported.
 */
public class UnsupportedInputException extends RuntimeException {

    /**
     * Constructor that accepts a message.
     *
     * @param message - Message to pass along to the Exception
     */
    public UnsupportedInputException(String message) {
        super(message);
    }
}
