package nl.tudelft.st01.util.exceptions;

public class CannotParseInputSQLFileException extends SQLFpcException {
    /**
     * Parameterless Constructor for the CannotParseInputSQLFileException class..
     */
    public CannotParseInputSQLFileException() {
        // Parameterless Constructor for the CannotParseInputSQLFileException class.
    }

    /**
     * Constructor that accepts a message.
     *
     * @param message - Message to pass along to the Exception
     */
    public CannotParseInputSQLFileException(String message) {
        super(message);
    }
}
