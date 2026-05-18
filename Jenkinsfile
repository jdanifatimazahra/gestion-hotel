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

stage('Couverture de Code') {
    steps {
        bat 'mvn org.jacoco:jacoco-maven-plugin:0.8.11:prepare-agent test org.jacoco:jacoco-maven-plugin:0.8.11:report'
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