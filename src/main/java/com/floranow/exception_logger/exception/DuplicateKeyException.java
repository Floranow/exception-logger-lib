package com.floranow.exception_logger.exception;

/**
 * Exception for duplicate key errors (409)
 */
public class DuplicateKeyException extends RuntimeException {
	public DuplicateKeyException(String message) {
		super(message);
	}

	public DuplicateKeyException(String message, Throwable cause) {
		super(message, cause);
	}
}



