package nl.tudelft.st01.util.exceptions;

/**
 *  Exception that can be used to alert users that a list cannot be empty.
 */
public class ListCanNotBeEmptyException extends RuntimeException {

    /**
     * Constructor that accepts a message.
     *
     * @param message - Message to pass along to the Exception
     */
    public ListCanNotBeEmptyException(String message) {
        super(message);
    }
}
