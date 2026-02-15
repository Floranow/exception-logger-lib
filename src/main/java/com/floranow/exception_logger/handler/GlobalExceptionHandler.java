package com.floranow.exception_logger.handler;

import com.floranow.exception_logger.exception.BadRequestException;
import com.floranow.exception_logger.exception.ErrorResponse;
import com.floranow.exception_logger.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler for all REST controllers
 * Handles ALL possible exceptions and automatically logs them with Slack notifications
 * Order matters: more specific exceptions should be handled before generic ones
 * 
 * This bean will only be registered if no other GlobalExceptionHandler bean exists.
 * This allows services to provide their own implementation without conflicts.
 * 
 * Uses a unique bean name "exceptionLoggerGlobalExceptionHandler" to avoid conflicts,
 * and conditionally registers only if "globalExceptionHandler" doesn't exist.
 */
@Component("exceptionLoggerGlobalExceptionHandler")
@RestControllerAdvice
@ConditionalOnMissingBean(name = "globalExceptionHandler")
public class GlobalExceptionHandler {
	private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	// ========== Custom Business Exceptions ==========
	
	@ExceptionHandler(BadRequestException.class)
	public ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException ex) {
		logger.error("BadRequestException occurred: {}", ex.getMessage(), ex);
		ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(),
				String.valueOf(HttpStatus.BAD_REQUEST.value()));
		return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(NotFoundException.class)
	public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException ex) {
		logger.error("NotFoundException occurred: {}", ex.getMessage(), ex);
		ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), 
				String.valueOf(HttpStatus.NOT_FOUND.value()));
		return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(com.floranow.exception_logger.exception.DuplicateKeyException.class)
	public ResponseEntity<ErrorResponse> handleCustomDuplicateKeyException(
			com.floranow.exception_logger.exception.DuplicateKeyException ex) {
		logger.error("Custom DuplicateKeyException occurred: {}", ex.getMessage(), ex);
		ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), 
				String.valueOf(HttpStatus.CONFLICT.value()));
		return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
	}

	// ========== Spring Validation Exceptions ==========
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
		logger.error("Validation exception occurred", ex);
		Map<String, String> errors = new HashMap<>();
		ex.getBindingResult().getFieldErrors()
				.forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
		String firstErrorMessage = errors.isEmpty() ? "Validation failed" : errors.values().iterator().next();
		ErrorResponse errorResponse = new ErrorResponse(firstErrorMessage,
				String.valueOf(HttpStatus.BAD_REQUEST.value()), errors);
		return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(BindException.class)
	public ResponseEntity<ErrorResponse> handleBindException(BindException ex) {
		logger.error("BindException occurred", ex);
		Map<String, String> errors = new HashMap<>();
		ex.getBindingResult().getFieldErrors()
				.forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
		String firstErrorMessage = errors.isEmpty() ? "Binding validation failed" : errors.values().iterator().next();
		ErrorResponse errorResponse = new ErrorResponse(firstErrorMessage,
				String.valueOf(HttpStatus.BAD_REQUEST.value()), errors);
		return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
		logger.error("ConstraintViolationException occurred", ex);
		Map<String, String> errors = ex.getConstraintViolations().stream()
				.collect(Collectors.toMap(
						violation -> violation.getPropertyPath().toString(),
						violation -> violation.getMessage()
				));
		String firstErrorMessage = errors.isEmpty() ? "Constraint validation failed" : errors.values().iterator().next();
		ErrorResponse errorResponse = new ErrorResponse(firstErrorMessage,
				String.valueOf(HttpStatus.BAD_REQUEST.value()), errors);
		return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
	}

	// ========== Spring HTTP Exceptions ==========
	
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ErrorResponse> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
		logger.error("HttpRequestMethodNotSupportedException occurred", ex);
		String message = String.format("Method %s is not supported for this endpoint. Supported methods: %s",
				ex.getMethod(), ex.getSupportedHttpMethods());
		ErrorResponse errorResponse = new ErrorResponse(message,
				String.valueOf(HttpStatus.METHOD_NOT_ALLOWED.value()));
		return new ResponseEntity<>(errorResponse, HttpStatus.METHOD_NOT_ALLOWED);
	}

	@ExceptionHandler(HttpMediaTypeNotSupportedException.class)
	public ResponseEntity<ErrorResponse> handleMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException ex) {
		logger.error("HttpMediaTypeNotSupportedException occurred", ex);
		String message = "Media type not supported";
		ErrorResponse errorResponse = new ErrorResponse(message,
				String.valueOf(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value()));
		return new ResponseEntity<>(errorResponse, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
		logger.error("HttpMessageNotReadableException occurred: {}", ex.getMessage(), ex);
		ErrorResponse errorResponse = new ErrorResponse("Malformed JSON request: " + ex.getMessage(),
				String.valueOf(HttpStatus.BAD_REQUEST.value()));
		return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(
			MissingServletRequestParameterException ex) {
		logger.error("MissingServletRequestParameterException occurred: {}", ex.getMessage(), ex);
		String message = String.format("Required parameter '%s' is missing", ex.getParameterName());
		ErrorResponse errorResponse = new ErrorResponse(message,
				String.valueOf(HttpStatus.BAD_REQUEST.value()));
		return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
			MethodArgumentTypeMismatchException ex) {
		logger.error("MethodArgumentTypeMismatchException occurred: {}", ex.getMessage(), ex);
		String message = String.format("Parameter '%s' should be of type %s", ex.getName(),
				ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
		ErrorResponse errorResponse = new ErrorResponse(message,
				String.valueOf(HttpStatus.BAD_REQUEST.value()));
		return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(NoHandlerFoundException.class)
	public ResponseEntity<ErrorResponse> handleNoHandlerFoundException(NoHandlerFoundException ex) {
		logger.error("NoHandlerFoundException occurred", ex);
		String message = String.format("No handler found for %s %s", ex.getHttpMethod(), ex.getRequestURL());
		ErrorResponse errorResponse = new ErrorResponse(message,
				String.valueOf(HttpStatus.NOT_FOUND.value()));
		return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
	}

	// ========== Security Exceptions (Optional - only if Spring Security is on classpath) ==========
	// Note: These handlers will only work if Spring Security is available at runtime
	// They are commented out to avoid compilation errors when Spring Security is not present
	// Uncomment and add Spring Security dependency if needed:
	/*
	@ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
	public ResponseEntity<ErrorResponse> handleAuthenticationException(
			org.springframework.security.core.AuthenticationException ex) {
		logger.error("AuthenticationException occurred: {}", ex.getMessage(), ex);
		ErrorResponse errorResponse = new ErrorResponse("Authentication failed: " + ex.getMessage(),
				String.valueOf(HttpStatus.UNAUTHORIZED.value()));
		return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
	public ResponseEntity<ErrorResponse> handleAccessDeniedException(
			org.springframework.security.access.AccessDeniedException ex) {
		logger.error("AccessDeniedException occurred: {}", ex.getMessage(), ex);
		ErrorResponse errorResponse = new ErrorResponse("Access denied: " + ex.getMessage(),
				String.valueOf(HttpStatus.FORBIDDEN.value()));
		return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
	}
	*/

	// ========== Database Exceptions ==========
	
	@ExceptionHandler(org.springframework.dao.DuplicateKeyException.class)
	public ResponseEntity<ErrorResponse> handleSpringDuplicateKeyException(
			org.springframework.dao.DuplicateKeyException ex) {
		logger.error("Spring DuplicateKeyException occurred: {}", ex.getMessage(), ex);
		ErrorResponse errorResponse = new ErrorResponse("Duplicate key violation: " + ex.getMessage(),
				String.valueOf(HttpStatus.CONFLICT.value()));
		return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
	}

	@ExceptionHandler(DataAccessException.class)
	public ResponseEntity<ErrorResponse> handleDataAccessException(DataAccessException ex) {
		logger.error("DataAccessException occurred: {}", ex.getMessage(), ex);
		ErrorResponse errorResponse = new ErrorResponse("Database access error occurred",
				String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
		return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	// ========== Common Java Exceptions ==========
	
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
		logger.error("IllegalArgumentException occurred: {}", ex.getMessage(), ex);
		ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(),
				String.valueOf(HttpStatus.BAD_REQUEST.value()));
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}

	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException ex) {
		logger.error("IllegalStateException occurred: {}", ex.getMessage(), ex);
		ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(),
				String.valueOf(HttpStatus.BAD_REQUEST.value()));
		return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(NullPointerException.class)
	public ResponseEntity<ErrorResponse> handleNullPointerException(NullPointerException ex) {
		logger.error("NullPointerException occurred", ex);
		ErrorResponse errorResponse = new ErrorResponse("A null pointer exception occurred",
				String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
		return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(UnsupportedOperationException.class)
	public ResponseEntity<ErrorResponse> handleUnsupportedOperationException(UnsupportedOperationException ex) {
		logger.error("UnsupportedOperationException occurred: {}", ex.getMessage(), ex);
		ErrorResponse errorResponse = new ErrorResponse("Operation not supported: " + ex.getMessage(),
				String.valueOf(HttpStatus.NOT_IMPLEMENTED.value()));
		return new ResponseEntity<>(errorResponse, HttpStatus.NOT_IMPLEMENTED);
	}

	// ========== Generic Runtime Exceptions ==========
	
	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
		logger.error("RuntimeException occurred", ex);
		String message = ex.getLocalizedMessage() != null ? ex.getLocalizedMessage() : "An unexpected runtime error occurred";
		ErrorResponse errorResponse = new ErrorResponse(message,
				String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
		return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	// ========== Catch-All Exception Handler (MUST BE LAST) ==========
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
		logger.error("Unhandled exception occurred!", ex);
		String message = ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred";
		ErrorResponse errorResponse = new ErrorResponse(message,
				String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
		return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}

