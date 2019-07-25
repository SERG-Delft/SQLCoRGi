package com.github.sergdelft.sqlcorgi.exceptions;

/**
 * The type checker exception is used for the {@link com.github.sergdelft.sqlcorgi.schema.TypeChecker}.
 */
public class TypeCheckerException extends RuntimeException {
    public TypeCheckerException() {
    }

    public TypeCheckerException(String message) {
        super(message);
    }
}
