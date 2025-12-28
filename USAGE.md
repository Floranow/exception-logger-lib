# Exception Logger Library - Usage Guide

## Quick Start

### 1. Configure GitHub Packages Repository

First, configure Maven to access GitHub Packages. Add to your `~/.m2/settings.xml`:

```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
          http://maven.apache.org/xsd/settings-1.0.0.xsd">
  <servers>
    <server>
      <id>github</id>
      <username>YOUR_GITHUB_USERNAME</username>
      <password>YOUR_GITHUB_PERSONAL_ACCESS_TOKEN</password>
    </server>
  </servers>
</settings>
```

> **Create a Personal Access Token:** Go to GitHub Settings → Developer settings → Personal access tokens → Generate new token (classic). Select `read:packages` permission.

### 2. Add Dependency

Add to your project's `pom.xml`:

```xml
<repositories>
  <repository>
    <id>github</id>
    <url>https://maven.pkg.github.com/YOUR_USERNAME/exception-logger-lib</url>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>com.floranow</groupId>
    <artifactId>exception-logger-lib</artifactId>
    <version>1.0.0</version>
  </dependency>
</dependencies>
```

> **Note:** Replace `YOUR_USERNAME` with the GitHub username/organization that owns the repository.

### 2. Configure Application Properties

Add to `application.properties`:

```properties
# Slack Configuration
slack.webhook.url=https://hooks.slack.com/services/YOUR/WEBHOOK/URL
application.environment=production
application.name=your-service-name
```

### 3. Configure Logback

Add to your `logback-spring.xml`:

```xml
<configuration>
    <!-- Include the library's logback configuration -->
    <include resource="com/floranow/exception_logger/logback-spring-template.xml"/>
    
    <!-- Your other appenders -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
    </root>
</configuration>
```

### 4. Use in Your Code

The library automatically:
- ✅ Catches all REST exceptions via `GlobalExceptionHandler`
- ✅ Sends Slack notifications for all ERROR level logs
- ✅ Catches uncaught thread exceptions
- ✅ Provides standard exception classes

#### Example Usage:

```java
import com.floranow.exception_logger.exception.NotFoundException;
import com.floranow.exception_logger.exception.BadRequestException;

// In your service
public User getUser(String id) {
    User user = userRepository.findById(id);
    if (user == null) {
        throw new NotFoundException("User not found with id: " + id);
    }
    return user;
}

// The exception will be automatically:
// 1. Logged with ERROR level
// 2. Sent to Slack
// 3. Returned as proper HTTP 404 response
```

## Features

### Automatic Exception Handling
- All exceptions are automatically caught and formatted
- Consistent error response format across all services
- Automatic Slack notifications

### Slack Notifications
- Sends notifications for ERROR level logs
- Includes stack traces
- Configurable via properties
- Filters out noisy loggers

### Thread Safety
- Catches uncaught exceptions in threads
- Prevents silent failures

## Customization

### Exclude Loggers from Slack

Edit `SlackTurboFilter.java` and add to `EXCLUDED_LOGGERS` array:

```java
private static final String[] EXCLUDED_LOGGERS = {
    "io.netty.util.ResourceLeakDetector",
    "your.noisy.logger"
};
```

### Custom Exception Handling

The `GlobalExceptionHandler` can be extended or you can create your own handlers. The library's handler will catch any exceptions not handled by your custom handlers.

## Building and Installing

### Local Development

For local development, you can build and install to your local Maven repository:

```bash
cd exception-logger-lib
mvn clean install
```

This will install the library to your local Maven repository (`~/.m2/repository`), making it available to other projects on your machine.

### Publishing to GitHub Packages

The library is automatically published to GitHub Packages via GitHub Actions when:
- Code is pushed to `main` or `master` branch
- A version tag is created (e.g., `v1.0.0`)

#### Manual Publishing

To manually publish:

1. **Update version** in `pom.xml` (e.g., `1.0.1`)
2. **Configure authentication** in `~/.m2/settings.xml` (see Quick Start section)
3. **Publish**:
   ```bash
   mvn clean deploy -Dgithub.repository=YOUR_USERNAME/exception-logger-lib
   ```

#### Version Management

- Use semantic versioning: `MAJOR.MINOR.PATCH` (e.g., `1.0.0`, `1.0.1`, `1.1.0`)
- Update version in `pom.xml` before publishing
- Create git tags for releases:
   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```

