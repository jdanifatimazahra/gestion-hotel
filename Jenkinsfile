pipeline {
    agent any
    stages {

        stage('Build') {
            steps {
                bat 'mvn compile'
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

stage('Deploy Nexus') {
    steps {
        bat 'mvn deploy -s "C:\\Users\\TON_NOM\\.m2\\settings.xml" -DskipTests'
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
        }

        stage('Deploy Nexus') {
            steps {
                bat 'mvn deploy -DskipTests'
            }
        }
    }
}