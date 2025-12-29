package com.floranow.exception_logger.logging;

import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.classic.spi.ILoggingEvent;
import lombok.Setter;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Logback appender that sends error-level logs to Slack via webhook
 */
public class SlackAppender extends AppenderBase<ILoggingEvent> {

	@Setter
	private String webhookUrl;
	@Setter
	private String environment;
	@Setter
	private String serviceName;
	private final HttpClient client = HttpClient.newHttpClient();

	@Override
	protected void append(ILoggingEvent event) {
		// Only handle errors
		if (event.getLevel().isGreaterOrEqual(ch.qos.logback.classic.Level.ERROR)) {
			sendSlack(event);
		}
	}

	private void sendSlack(ILoggingEvent event) {
		try {
			String message = "🚨 *Exception Alert!*" + "\n*Service:* `" + serviceName + "`" + "\n*Environment:* `"
					+ environment + "`" + "\n*Message:* " + event.getFormattedMessage() + "\n*Logger:* "
					+ event.getLoggerName();

			if (event.getThrowableProxy() != null) {
				String fullStackTrace = ThrowableProxyUtil.asString(event.getThrowableProxy());
				if (fullStackTrace.length() > 3500) {
					fullStackTrace = fullStackTrace.substring(0, 3500) + "... [truncated]";
				}
				message += "\n*Stacktrace:* ```" + fullStackTrace + "```";
			}

			String json = "{\"text\":\"" + message.replace("\"", "\\\"") + "\"}";

			HttpRequest request = HttpRequest.newBuilder().uri(URI.create(webhookUrl))
					.header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(json)).build();

			client.send(request, HttpResponse.BodyHandlers.discarding());
		} catch (Exception e) {
			addError("Failed to send log to Slack", e);
		}
	}
}



