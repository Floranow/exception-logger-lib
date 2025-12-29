package com.floranow.exception_logger.exception;

/**
 * Exception for bad request errors (400)
 */
public class BadRequestException extends RuntimeException {
	public BadRequestException(String message) {
		super(message);
	}

	public BadRequestException(String message, Throwable cause) {
		super(message, cause);
	}
}



