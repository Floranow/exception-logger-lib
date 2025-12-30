# Exception Logger Library

A shared library for centralized exception logging and notification across all Floranow projects.

## Features

- **Slack Notifications**: Automatically sends error-level logs to Slack via webhook
- **Global Exception Handler**: Handles all REST API exceptions with consistent error responses
- **Thread Exception Catcher**: Catches uncaught exceptions in threads
- **Configurable**: Easy to configure via Spring properties
- **Reusable**: Can be used across all Spring Boot projects

## Installation

### From GitHub Packages (Public Repository)

Since this is a **public repository**, you can use it directly without authentication.

#### 1. Add Repository and Dependency

Add to your project's `pom.xml`:

```xml
<repositories>
  <repository>
    <id>github-exception-logger-lib</id>
    <url>https://maven.pkg.github.com/Floranow/exception-logger-lib</url>
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

> **Note:** As a public package, no authentication is required. However, for better rate limits in production environments, you can optionally configure authentication (see below).

#### Optional: Configure Authentication (Recommended for Production)

For better rate limits and reliability, you can optionally add authentication to your `~/.m2/settings.xml`:

```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
          http://maven.apache.org/xsd/settings-1.0.0.xsd">
  <servers>
    <server>
      <id>github-exception-logger-lib</id>
      <username>machine-user-gitlab</username>
      <password>YOUR_GITHUB_PERSONAL_ACCESS_TOKEN</password>
    </server>
  </servers>
</settings>
```

> **Create a Personal Access Token (Optional):** Go to GitHub Settings → Developer settings → Personal access tokens → Generate new token (classic). Select `read:packages` permission. This is optional for public packages but recommended for production use.

### Local Installation (Development)

For local development, you can install to your local Maven repository:

```bash
mvn clean install
```

Then add to your `pom.xml`:

```xml
<dependency>
    <groupId>com.floranow</groupId>
    <artifactId>exception-logger-lib</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Configuration

Add to your `application.properties` or `application.yml`:

```properties
# Slack Configuration
slack.webhook.url=https://hooks.slack.com/services/YOUR/WEBHOOK/URL
application.environment=production
application.name=your-service-name
```

### Logback Configuration

Copy the logback configuration from `src/main/resources/logback-spring-template.xml` to your project's `logback-spring.xml`, or include it:

```xml
<include resource="com/floranow/exception_logger/logback-spring-template.xml"/>
```

## Usage

### 1. Global Exception Handler

The `GlobalExceptionHandler` is automatically enabled via `@RestControllerAdvice`. It handles:

- `BadRequestException` (400)
- `NotFoundException` (404)
- `DuplicateKeyException` (409)
- `IllegalArgumentException` (400)
- `MethodArgumentNotValidException` (400)
- `RuntimeException` (500)
- Generic `Exception` (500)

### 2. Custom Exceptions

Use the provided exceptions:

```java
import com.floranow.exception_logger.exception.NotFoundException;
import com.floranow.exception_logger.exception.BadRequestException;

throw new NotFoundException("User not found");
throw new BadRequestException("Invalid input");
```

### 3. Error Response Format

All exceptions return a consistent `ErrorResponse`:

```json
{
  "message": "Error message",
  "errorCode": "400",
  "errors": {},
  "success": false
}
```

### 4. Slack Notifications

Slack notifications are automatically sent for:
- All ERROR level logs
- All exceptions caught by GlobalExceptionHandler
- Uncaught thread exceptions

## Components

- **SlackAppender**: Logback appender for Slack notifications
- **SlackTurboFilter**: More flexible filter for Slack notifications
- **GlobalExceptionHandler**: REST exception handler
- **GlobalThreadExceptionCatcher**: Catches uncaught thread exceptions
- **ErrorResponse**: Standard error response DTO

## Building

```bash
mvn clean install
```

## Troubleshooting

### Unresolved Dependency: spring-boot-starter

If you get an error like `Unresolved dependency: 'org.springframework.boot:spring-boot-starter:jar:2.7.12'`:

1. **Ensure Maven Central is configured** (should be default, but verify in your `pom.xml`):
   ```xml
   <repositories>
     <repository>
       <id>central</id>
       <url>https://repo1.maven.org/maven2</url>
     </repository>
     <repository>
       <id>github-exception-logger-lib</id>
       <url>https://maven.pkg.github.com/Floranow/exception-logger-lib</url>
     </repository>
   </repositories>
   ```

2. **The library uses Spring Boot BOM** - it will work with any Spring Boot 2.x version your service uses. The library doesn't force a specific Spring Boot version.

3. **If using a Spring Boot parent POM**, ensure it's properly configured in your service's `pom.xml`.

## Publishing

This library is automatically published to GitHub Packages via GitHub Actions when:
- Code is pushed to `main` or `master` branch
- A version tag is created (e.g., `v1.0.0`)

### Manual Publishing

To manually publish to GitHub Packages (requires authentication):

1. **Create a GitHub Personal Access Token** with `write:packages` permission
2. **Configure Maven settings** (`~/.m2/settings.xml`):
   ```xml
   <servers>
     <server>
       <id>github</id>
       <username>YOUR_GITHUB_USERNAME</username>
       <password>YOUR_GITHUB_PAT</password>
     </server>
   </servers>
   ```
3. **Publish**:
   ```bash
   mvn clean deploy -Dgithub.repository=Floranow/exception-logger-lib
   ```

### Version Management

- Use semantic versioning (e.g., `1.0.0`, `1.0.1`, `1.1.0`)
- Update version in `pom.xml` before publishing
- Create a git tag for releases: `git tag v1.0.0 && git push origin v1.0.0`



