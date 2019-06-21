package nl.tudelft.st01.exceptions;

/**
 *  Exception that can be used to alert users that a list cannot be empty.
 */
public class ListCannotBeEmptyException extends RuntimeException {

    /**
     * Parameterless Constructor for the ListCannotBeEmptyException class.
     */
    public ListCannotBeEmptyException() {
        // Parameterless Constructor for the ListCannotBeEmptyException class.
    }

    /**
     * Constructor that accepts a message.
     *
     * @param message - Message to pass along to the Exception
     */
    public ListCannotBeEmptyException(String message) {
        super(message);
    }
}
