pipeline {
    agent any

    parameters {
        choice(name: 'ENVIRONMENT', choices: ['qa', 'staging', 'local'], description: 'Environment to run tests against')
        choice(name: 'SUITE', choices: ['smoke', 'regression', 'negative', 'contract', 'all'], description: 'Test suite to execute')
        string(name: 'THREAD_COUNT', defaultValue: '10', description: 'Number of parallel threads')
    }

    tools {
        maven 'Maven-3.9'
        jdk 'JDK-17'
    }

    environment {
        ENV = "${params.ENVIRONMENT}"
        SUITE = "${params.SUITE}"
        THREAD_COUNT = "${params.THREAD_COUNT}"
    }

    stages {
        stage('Checkout') {
            steps {
                echo "Checking out code..."
                checkout scm
            }
        }

        stage('Build') {
            steps {
                echo "Building project..."
                sh 'mvn clean compile -DskipTests'
            }
        }

        stage('Run Tests') {
            steps {
                script {
                    echo "Running ${SUITE} tests on ${ENV} environment with ${THREAD_COUNT} threads"

                    if (params.SUITE == 'all') {
                        sh "mvn clean test -Denv=${ENV} -DthreadCount=${THREAD_COUNT}"
                    } else {
                        sh "mvn clean test -Denv=${ENV} -P${SUITE} -DthreadCount=${THREAD_COUNT}"
                    }
                }
            }
        }

        stage('Generate Allure Report') {
            steps {
                echo "Generating Allure report..."
                sh 'mvn allure:report'
            }
        }
    }

    post {
        always {
            echo "Archiving test results and reports..."

            // Publish TestNG results
            junit allowEmptyResults: true, testResults: '**/target/surefire-reports/testng-results.xml'

            // Publish Allure report
            allure includeProperties: false, jdk: '', results: [[path: 'target/allure-results']]

            // Archive logs
            archiveArtifacts artifacts: 'target/logs/*.log', allowEmptyArchive: true

            // Archive Allure report
            archiveArtifacts artifacts: 'target/allure-report/**/*', allowEmptyArchive: true
        }

        success {
            echo "✅ Tests passed successfully!"
            emailext(
                subject: "✅ API Tests Passed - ${ENV} - Build #${BUILD_NUMBER}",
                body: """
                    <h2>API Test Execution Successful</h2>
                    <p><strong>Environment:</strong> ${ENV}</p>
                    <p><strong>Suite:</strong> ${SUITE}</p>
                    <p><strong>Build Number:</strong> ${BUILD_NUMBER}</p>
                    <p><strong>Duration:</strong> ${currentBuild.durationString}</p>
                    <p><a href="${BUILD_URL}allure">View Allure Report</a></p>
                """,
                to: '${DEFAULT_RECIPIENTS}',
                mimeType: 'text/html'
            )
        }

        failure {
            echo "❌ Tests failed!"
            emailext(
                subject: "❌ API Tests Failed - ${ENV} - Build #${BUILD_NUMBER}",
                body: """
                    <h2>API Test Execution Failed</h2>
                    <p><strong>Environment:</strong> ${ENV}</p>
                    <p><strong>Suite:</strong> ${SUITE}</p>
                    <p><strong>Build Number:</strong> ${BUILD_NUMBER}</p>
                    <p><strong>Duration:</strong> ${currentBuild.durationString}</p>
                    <p><a href="${BUILD_URL}allure">View Allure Report</a></p>
                    <p><a href="${BUILD_URL}console">View Console Output</a></p>
                """,
                to: '${DEFAULT_RECIPIENTS}',
                mimeType: 'text/html'
            )
        }

        unstable {
            echo "⚠️ Tests completed with some failures"
        }
    }
}
