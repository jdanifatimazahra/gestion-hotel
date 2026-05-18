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
                bat 'mvn cobertura:cobertura'
            }
        }

        stage('Documentation') {
            steps {
                bat 'mvn site'
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

    post {
        failure {
            mail to: 'admin@hotel.com',
                 subject: "ECHEC Pipeline - ${env.JOB_NAME}",
                 body: "Erreur : ${env.BUILD_URL}"
        }
    }
}