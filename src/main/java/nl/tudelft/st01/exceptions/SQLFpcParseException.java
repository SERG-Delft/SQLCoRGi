package nl.tudelft.st01.exceptions;

/**
 *  Exception that can be used to alert users that SQLFpc could not parse their query.
 */
public class SQLFpcParseException extends SQLFpcException {

    /**
     * Parameterless Constructor for the SQLFpcParseException class..
     */
    public SQLFpcParseException() {
        // Parameterless Constructor for the SQLFpcParseException class.
    }

    /**
     * Constructor that accepts a message.
     *
     * @param message - Message to pass along to the Exception
     */
    public SQLFpcParseException(String message) {
        super(message);
    }
}
