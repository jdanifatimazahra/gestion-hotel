pipeline {
    agent any

    tools {
        maven 'Maven'
        jdk 'JDK25'
    }

    stages {

        stage('Build') {
            steps {
                bat 'mvn compile -DskipTests'
            }
        }

        stage('Tests Unitaires') {
            steps {
                bat 'mvn test'
            }
            post {
                always {
                    junit 'target/surefire-reports/**/*.xml'
                }
            }
        }

        stage('Couverture JaCoCo') {
            steps {
                bat 'mvn jacoco:report'
            }
        }

        stage('Documentation') {
            steps {
                bat 'mvn site -DskipTests'
            }
        }

        stage('Packaging') {
            steps {
                bat 'mvn package -DskipTests'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }

        stage('Deploy Nexus') {
            steps {
                bat 'mvn deploy -DskipTests'
            }
        }

    }

    post {
        failure {
            echo '❌ Pipeline échoué !'
        }
        success {
            echo '✅ Pipeline réussi !'
        }
    }
}
