package nl.tudelft.st01.util.exceptions;

/**
 *  Exception that can be used to alert programmers that a class should not be instantiated.
 */
public class ShouldNotBeInstantiatedException extends RuntimeException {

    /**
     * Parameterless Constructor.
     */

    public ShouldNotBeInstantiatedException() {

    }

    /**
     * Constructor that accepts a message.
     *
     * @param message - Message to pass along to the Exception
     */
    public ShouldNotBeInstantiatedException(String message) {
        super(message);
    }
}
