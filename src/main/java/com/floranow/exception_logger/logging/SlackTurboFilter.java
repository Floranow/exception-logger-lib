package com.floranow.exception_logger.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;
import lombok.Setter;
import org.slf4j.Marker;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Logback TurboFilter that intercepts log events and sends them to Slack
 * More flexible than appender as it can filter based on logger name, level, etc.
 */
public class SlackTurboFilter extends TurboFilter {

	private final HttpClient client = HttpClient.newHttpClient();

	@Setter
	private String webhookUrl;

	@Setter
	private String serviceName;

	@Setter
	private String environment;

	private String minLevel; // store as string

	@Setter
	private boolean includeStackTrace = true; // default true

	// Optional: limit the message length to avoid Slack truncation
	private static final int MAX_MESSAGE_LENGTH = 3000;

	private Level minLevelLevel = Level.ERROR;

	// List of logger names to exclude from Slack notifications (noisy loggers)
	private static final String[] EXCLUDED_LOGGERS = {
			"io.netty.util.ResourceLeakDetector",
			"io.netty.util.ResourceLeakDetectorFactory"
	};

	public void setMinLevel(String minLevel) {
		this.minLevel = minLevel;
		this.minLevelLevel = Level.toLevel(minLevel, Level.ERROR);
	}

	@Override
	public FilterReply decide(Marker marker, Logger logger, Level level, String format, Object[] params, Throwable t) {

		// Skip excluded loggers to avoid noise in Slack
		String loggerName = logger.getName();
		for (String excludedLogger : EXCLUDED_LOGGERS) {
			if (loggerName.startsWith(excludedLogger)) {
				return FilterReply.NEUTRAL; // Allow normal logging but skip Slack notification
			}
		}

		if (level.isGreaterOrEqual(minLevelLevel)) {
			try {
				// Base message
				StringBuilder message = new StringBuilder();
				message.append("🚨 *Exception Alert!*\n").append("*Service:* ").append(serviceName).append("\n")
						.append("*Environment:* ").append(environment).append("\n").append("*Logger:* ")
						.append(logger.getName()).append("\n*Level:* ").append(level.toString()).append("\n")
						.append("*Message:* ").append(format);

				// Include full stack trace if present
				if (t != null && includeStackTrace) {
					StringWriter sw = new StringWriter();
					t.printStackTrace(new PrintWriter(sw));
					String stackTrace = sw.toString();

					// Optional: truncate if too long
					if (stackTrace.length() > MAX_MESSAGE_LENGTH) {
						stackTrace = stackTrace.substring(0, MAX_MESSAGE_LENGTH) + "\n... (truncated)";
					}

					message.append("\n*Throwable:* ```").append(stackTrace).append("```");
				}

				// Prepare JSON payload
				String json = "{\"text\":\"" + message.toString().replace("\"", "\\\"") + "\"}";

				HttpRequest request = HttpRequest.newBuilder().uri(URI.create(webhookUrl))
						.header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(json))
						.build();

				client.send(request, HttpResponse.BodyHandlers.discarding());

			} catch (Exception e) {
				// avoid recursive logging
				System.err.println("Failed to send log to Slack: " + e.getMessage());
			}
		}

		return FilterReply.NEUTRAL; // allow normal logging to continue
	}
}

