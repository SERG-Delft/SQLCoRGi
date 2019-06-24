package nl.tudelft.st01.exceptions;

/**
 *  Exception that can be used to alert programmers that a certain input is not supported.
 */
public class UnsupportedInputException extends RuntimeException {

    /**
     * Parameterless Constructor for the UnsupportedInputException class.
     */
    public UnsupportedInputException() {
        // Parameterless Constructor for the UnsupportedInputException class.
    }

    /**
     * Constructor that accepts a message.
     *
     * @param message - Message to pass along to the Exception
     */
    public UnsupportedInputException(String message) {
        super(message);
    }
}
