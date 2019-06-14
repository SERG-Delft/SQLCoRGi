package nl.tudelft.st01.util.exceptions;

public class SQLFpcException extends RuntimeException {

    /**
     * Parameterless Constructor for the ShouldNotBeInstantiatedException class..
     */
    public SQLFpcException() {
        // Parameterless Constructor for the ShouldNotBeInstantiatedException class.
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
