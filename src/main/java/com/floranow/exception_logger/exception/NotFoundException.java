package com.floranow.exception_logger.exception;

/**
 * Exception for not found errors (404)
 */
public class NotFoundException extends RuntimeException {
	public NotFoundException(String message) {
		super(message);
	}

	public NotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}

