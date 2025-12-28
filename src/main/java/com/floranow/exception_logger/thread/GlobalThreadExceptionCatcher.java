package com.floranow.exception_logger.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Catches uncaught exceptions in threads and logs them
 * This ensures all exceptions are logged even if not caught
 */
@Component
public class GlobalThreadExceptionCatcher {

	private static final Logger logger = LoggerFactory.getLogger(GlobalThreadExceptionCatcher.class);

	@PostConstruct
	public void init() {
		Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
			logger.error("Uncaught exception in thread: {}", thread.getName(), throwable);
		});
	}
}

