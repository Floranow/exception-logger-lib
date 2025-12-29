package com.floranow.exception_logger.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Standard error response format for all Floranow services
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
	private String message;
	private String errorCode;
	private Map<String, String> errors;
	private Boolean success = false;

	public ErrorResponse(String message, String errorCode) {
		this.message = message;
		this.errorCode = errorCode;
		this.success = false;
	}

	public ErrorResponse(String message, String errorCode, Map<String, String> errors) {
		this.message = message;
		this.errorCode = errorCode;
		this.errors = errors;
		this.success = false;
	}
}



