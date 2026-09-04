# Enterprise REST API Automation Framework

A production-grade REST API automation framework built with Java 17, REST Assured, TestNG, and Allure reporting. Designed for enterprise-scale testing with thousands of test cases running in parallel across CI/CD pipelines.

## 🎯 Key Features

- ✅ **Clean Architecture** - Layered design with clear separation of concerns
- ✅ **Thread-Safe Parallel Execution** - Execute tests concurrently without race conditions
- ✅ **Complete Body Validation** - JSONAssert with customizable comparison modes
- ✅ **JSON Schema Validation** - Contract testing with JSON Schema Draft v7
- ✅ **Automatic JIRA Integration** - Create defects with duplicate detection
- ✅ **Failure Classification** - Automatic categorization of failures
- ✅ **Request/Response Logging** - With sensitive data masking
- ✅ **Allure Reports** - Rich, interactive test reports
- ✅ **Retry Logic** - Intelligent retry for transient failures only
- ✅ **Token Caching** - Efficient authentication management
- ✅ **CI/CD Ready** - GitHub Actions and Jenkins pipelines included

## 📋 Table of Contents

- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [Project Structure](#project-structure)
- [Writing Tests](#writing-tests)
- [Running Tests](#running-tests)
- [Validation Strategies](#validation-strategies)
- [JIRA Integration](#jira-integration)
- [Reporting](#reporting)
- [Parallel Execution](#parallel-execution)
- [CI/CD Integration](#cicd-integration)
- [Troubleshooting](#troubleshooting)

## 🔧 Prerequisites

- Java 17 or higher
- Maven 3.6+
- Git
- IDE (IntelliJ IDEA / Eclipse)
- Access to the API under test
- (Optional) JIRA account for defect management

## 📦 Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd RESTASSURED_API_AUTOMATION
   ```

2. **Build the project**
   ```bash
   mvn clean install -DskipTests
   ```

3. **Verify installation**
   ```bash
   mvn clean compile
   ```

## ⚙️ Configuration

### Environment Configuration

The framework supports multiple environments: `local`, `qa`, `staging`.

Configuration files are located in `src/test/resources/config/`:

- `application.properties` - Base configuration
- `local.properties` - Local environment
- `qa.properties` - QA environment
- `staging.properties` - Staging environment

### Environment Selection

```bash
# Via system property
mvn clean test -Denv=qa

# Via environment variable
export ENV=qa
mvn clean test
```

### Sensitive Data Configuration

**Never commit secrets to Git.** Use environment variables:

```bash
# Authentication
export API_USERNAME=your_username
export API_PASSWORD=your_password
export API_KEY=your_api_key
export API_SECRET=your_api_secret

# JIRA Integration
export JIRA_BASE_URL=https://your-jira.atlassian.net
export JIRA_USERNAME=your_jira_email
export JIRA_API_TOKEN=your_jira_api_token
```

## 📁 Project Structure

```
src/
├── main/java/com/api/automation/
│   ├── config/              # Configuration management
│   ├── constants/           # Constants and enums
│   ├── clients/             # API client layer
│   ├── models/              # Request/Response POJOs
│   ├── validators/          # Response validators
│   ├── assertions/          # Assertion framework
│   ├── auth/                # Authentication management
│   ├── retry/               # Retry logic
│   ├── jira/                # JIRA integration
│   ├── listeners/           # TestNG listeners
│   ├── reporting/           # Reporting helpers
│   ├── logging/             # Logging and masking
│   └── utilities/           # Common utilities
│
└── test/
    ├── java/com/api/automation/tests/
    │   ├── smoke/           # Smoke tests
    │   ├── regression/      # Regression tests
    │   ├── negative/        # Negative tests
    │   ├── contract/        # Contract tests
    │   └── security/        # Security tests
    │
    └── resources/
        ├── config/          # Environment configs
        ├── schemas/         # JSON schemas
        ├── testdata/        # Test data
        └── suites/          # TestNG suite XMLs
```

## ✍️ Writing Tests

### Basic Test Structure

```java
@Test(groups = {"smoke"}, retryAnalyzer = RetryAnalyzer.class)
@ApiTest(requirement = "USER-001", endpoint = "POST /users", category = "SMOKE")
@Description("Verify that a new user can be created successfully")
public void shouldCreateUserSuccessfully() {
    // Arrange
    CreateUserRequest request = RandomDataGenerator.createValidUser();
    
    // Act
    Response response = userApi.createUser(request);
    
    // Assert
    ApiAssertions.assertThat(response)
        .validateStatusCode(201)
        .validateSchema("user-response-schema.json")
        .validateField("email", request.getEmail())
        .validateResponseTime(2000);
}
```

### Key Components

1. **API Clients** - Clean abstraction over REST Assured
   ```java
   UserApiClient userApi = new UserApiClient();
   Response response = userApi.getUserById("123");
   ```

2. **Test Data Generation**
   ```java
   CreateUserRequest request = RandomDataGenerator.createValidUser();
   ```

3. **Fluent Assertions**
   ```java
   ApiAssertions.assertThat(response)
       .validateStatusCode(200)
       .validateSchema("schema.json")
       .validateCompleteBody(expectedJson)
       .validateField("name", "John");
   ```

## 🚀 Running Tests

### Maven Commands

```bash
# Run all tests
mvn clean test

# Run specific environment
mvn clean test -Denv=qa

# Run smoke tests only
mvn clean test -Psmoke

# Run regression tests
mvn clean test -Pregression

# Run with custom thread count
mvn clean test -DthreadCount=15

# Sequential execution
mvn clean test -Psequential

# Generate Allure report
mvn allure:serve
```

### Available Profiles

- `smoke` - Smoke test suite
- `regression` - Full regression suite
- `negative` - Negative test scenarios
- `contract` - JSON Schema validation tests
- `sequential` - Disable parallel execution

### Test Groups

Tests are organized using TestNG groups:

```xml
<groups>
    <run>
        <include name="smoke"/>
        <include name="regression"/>
    </run>
</groups>
```

## ✅ Validation Strategies

### 1. Status Code Validation

```java
ApiAssertions.assertThat(response)
    .validateStatusCode(200)
    .validateStatusCodeInRange(200, 299);
```

### 2. JSON Schema Validation

```java
ApiAssertions.assertThat(response)
    .validateSchema("user-response-schema.json");
```

### 3. Complete Body Validation

```java
// Lenient mode (ignores field order, extra fields allowed)
ApiAssertions.assertThat(response)
    .validateCompleteBody(expectedJson);

// Strict mode (exact match)
ApiAssertions.assertThat(response)
    .validateCompleteBodyStrict(expectedJson);

// Ignore specific fields (timestamps, IDs)
ApiAssertions.assertThat(response)
    .validateBodyIgnoringFields(expectedJson, "id", "createdAt", "updatedAt");
```

### 4. Field-Level Validation

```java
ApiAssertions.assertThat(response)
    .validateField("email", "test@example.com")
    .validateFieldNotNull("id")
    .validateHeader("Content-Type", "application/json");
```

### 5. Response Time Validation

```java
ApiAssertions.assertThat(response)
    .validateResponseTime(2000); // milliseconds
```

## 🐛 JIRA Integration

### Setup

1. Set environment variables:
   ```bash
   export JIRA_BASE_URL=https://your-jira.atlassian.net
   export JIRA_USERNAME=your_email@example.com
   export JIRA_API_TOKEN=your_api_token
   ```

2. Configure in properties:
   ```properties
   jira.auto.create=true
   jira.duplicate.check=true
   jira.update.existing=true
   jira.project.key=API
   ```

### Features

- **Automatic Defect Creation** - Failed tests create JIRA defects
- **Duplicate Detection** - SHA-256 signature prevents duplicate tickets
- **Update Existing** - Adds comments to existing tickets on re-occurrence
- **Failure Classification** - Categorizes failures automatically
- **Non-Blocking** - JIRA failures don't affect test results

### Generated Defect Content

```
Summary: [API Automation][QA] Create User API - Response Body Validation Failed

Environment: QA
Test Name: shouldCreateUserSuccessfully
Endpoint: POST /users
Failure Category: RESPONSE_BODY_FAILURE
Build Number: 123
Timestamp: 2026-09-04T13:25:20

Error Message:
Expected: {"status": "active"}
Actual: {"status": "inactive"}

Request Details: [attached]
Response Details: [attached]
```

## 📊 Reporting

### Allure Reports

Generate and view Allure report:

```bash
# Generate and open report
mvn allure:serve

# Generate report only
mvn allure:report

# Report location
target/allure-report/index.html
```

### Report Features

- ✅ Test execution timeline
- ✅ Test duration graphs
- ✅ Failure categorization
- ✅ Request/Response attachments
- ✅ Environment information
- ✅ Historical trends
- ✅ Retry information

## ⚡ Parallel Execution

### Configuration

In `testng.xml`:
```xml
<suite name="API Tests" parallel="methods" thread-count="10">
```

Or via command line:
```bash
mvn clean test -DthreadCount=15
```

### Thread Safety

The framework is designed for safe parallel execution:

- **ThreadLocal Context** - Isolated per-thread state
- **Stateless API Clients** - No shared mutable state
- **Independent Tests** - No test dependencies
- **Connection Pooling** - Efficient HTTP connection reuse

### Performance Tips

- Start with 5-10 threads and scale based on API capacity
- Monitor API rate limits
- Use connection pooling (configured by default)
- Cache authentication tokens (implemented)

## 🔄 CI/CD Integration

### GitHub Actions

Workflow is configured in `.github/workflows/api-tests.yml`

Triggered on:
- Push to main/develop
- Pull requests
- Manual workflow dispatch

### Jenkins

Pipeline is defined in `Jenkinsfile`

Parameters:
- Environment (qa, staging)
- Suite (smoke, regression, all)
- Thread count

### Artifacts

Both CI/CD pipelines archive:
- Allure report
- TestNG results
- Test logs
- Allure results (raw)

## 🔍 Troubleshooting

### Common Issues

**Issue: Tests failing with connection timeout**
```
Solution: Increase timeouts in config:
api.connection.timeout=60000
api.socket.timeout=60000
```

**Issue: JIRA integration failing**
```
Solution: Verify environment variables are set:
echo $JIRA_BASE_URL
echo $JIRA_USERNAME
echo $JIRA_API_TOKEN
```

**Issue: Parallel execution causing failures**
```
Solution: Reduce thread count or check for test dependencies:
mvn clean test -DthreadCount=5
```

**Issue: Schema validation failing**
```
Solution: Verify schema file exists and path is correct:
src/test/resources/schemas/your-schema.json
```

### Debug Mode

Enable verbose logging:

1. Update `logback-test.xml`:
   ```xml
   <logger name="com.api.automation" level="DEBUG"/>
   ```

2. Or set in properties:
   ```properties
   logging.level=DEBUG
   logging.request.enabled=true
   logging.response.enabled=true
   ```

## 📝 Best Practices

1. **Test Independence** - Each test should be independently executable
2. **Test Data** - Generate unique data per test execution
3. **Cleanup** - Delete test data created during execution
4. **Assertions** - Validate complete response, not just status code
5. **Retry** - Only retry transient failures, never assertion failures
6. **Secrets** - Never commit credentials to Git
7. **Naming** - Use descriptive test method names
8. **Groups** - Tag tests appropriately for selective execution

## 🤝 Contributing

1. Create a feature branch
2. Follow existing code style
3. Add tests for new features
4. Update documentation
5. Submit pull request

## 📄 License

Copyright © 2026. All rights reserved.

## 📧 Support

For issues or questions:
- Create a GitHub issue
- Contact the QA team

---

**Framework Version:** 1.0.0  
**Last Updated:** 2026-09-04
