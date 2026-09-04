# Use Maven with JDK 17 for building and running tests
FROM maven:3.9-eclipse-temurin-17

# Set working directory
WORKDIR /automation

# Copy project files
COPY pom.xml .
COPY src ./src
COPY testng.xml .

# Download dependencies
RUN mvn dependency:go-offline

# Set environment variables
ENV ENV=qa
ENV SUITE=smoke

# Create directories for reports
RUN mkdir -p /automation/target/allure-results
RUN mkdir -p /automation/target/logs

# Default command - run smoke tests
CMD mvn clean test -Denv=${ENV} -P${SUITE}

# To run with custom parameters:
# docker build -t api-automation .
# docker run -e ENV=qa -e SUITE=regression api-automation
#
# To run with environment variables for secrets:
# docker run -e ENV=qa \
#   -e API_USERNAME=user \
#   -e API_PASSWORD=pass \
#   -e JIRA_BASE_URL=url \
#   -e JIRA_USERNAME=user \
#   -e JIRA_API_TOKEN=token \
#   api-automation
