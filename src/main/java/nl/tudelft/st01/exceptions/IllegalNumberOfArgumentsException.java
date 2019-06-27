package nl.tudelft.st01.exceptions;

/**
 *  Exception that can be used to alert users that there is something wrong with the number of arguments.
 */
public class IllegalNumberOfArgumentsException extends RuntimeException {

    /**
     * Parameterless Constructor for the IllegalNumberOfArgumentsException class.
     */
    public IllegalNumberOfArgumentsException() {
        // Parameterless Constructor for the IllegalNumberOfArgumentsException class.
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