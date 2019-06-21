package nl.tudelft.st01.exceptions;

/**
 *  Exception that can be used to alert users that an error related to the SQLFpc webservice and/or processing its
 *  results has occurred.
 */
public class SQLFpcException extends RuntimeException {

    /**
     * Parameterless Constructor for the SQLFpcException class..
     */
    public SQLFpcException() {
        // Parameterless Constructor for the SQLFpcException class.
    }

    /**
     * Constructor that accepts a message.
     *
     * @param message - Message to pass along to the Exception
     */
    public SQLFpcException(String message) {
        super(message);
    }
}
