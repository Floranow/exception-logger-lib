package com.floranow.exception_logger.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Catches uncaught exceptions in threads and logs them
 * This ensures all exceptions are logged even if not caught
 * 
 * This bean will only be registered if no other GlobalThreadExceptionCatcher bean exists.
 * This allows services to provide their own implementation without conflicts.
 */
@Component
@ConditionalOnMissingBean(name = "globalThreadExceptionCatcher")
public class GlobalThreadExceptionCatcher {

	private static final Logger logger = LoggerFactory.getLogger(GlobalThreadExceptionCatcher.class);

	@PostConstruct
	public void init() {
		Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
			logger.error("Uncaught exception in thread: {}", thread.getName(), throwable);
		});
	}
}



