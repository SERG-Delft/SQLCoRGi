package nl.tudelft.st01.util.exceptions;

class CanNotBeNullException extends Exception {

    /**
     * Parameterless Constructor.
     */

    CanNotBeNullException() {

    }

    /**
     * Constructor that accepts a message.
     *
     * @param message - Message to pass along to the Exception
     */
    CanNotBeNullException(String message) {
        super(message);
    }
}
