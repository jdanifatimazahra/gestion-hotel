pipeline {
    agent any

    tools {
        maven 'Maven'
        jdk 'JDK17'
    }

    stages {

        // ─────────────────────────────────────
        // STAGE 1 : BUILD (Compilation)
        // ─────────────────────────────────────
        stage('Build') {
            steps {
                echo '=== Compilation du projet ==='
                bat 'mvn clean compile -DskipTests'
            }
            post {
                failure {
                    echo 'ECHEC - Compilation'
                    mail to: 'admin@hotel.com',
                         subject: "[Jenkins] ECHEC Build - ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                         body: "La compilation a echoue.\nURL: ${env.BUILD_URL}"
                }
            }
        }

        // ─────────────────────────────────────
        // STAGE 2 : TESTS (en parallèle)
        // ─────────────────────────────────────
        stage('Tests') {
            parallel {

                // Nœud 1 : Tests Unitaires
                stage('Tests Unitaires') {
                    steps {
                        echo '=== Tests Unitaires ==='
                        bat 'mvn test'
                    }
                    post {
                        always {
                            junit allowEmptyResults: true,
                                  testResults: 'target/surefire-reports/**/*.xml'
                        }
                    }
                }

                // Nœud 2 : Couverture de code JaCoCo
                stage('Couverture JaCoCo') {
                    steps {
                        echo '=== Couverture de code ==='
                        bat 'mvn org.jacoco:jacoco-maven-plugin:prepare-agent test org.jacoco:jacoco-maven-plugin:report'
                    }
                    post {
                        always {
                            jacoco(
                                execPattern: 'target/jacoco.exec',
                                classPattern: 'target/classes',
                                sourcePattern: 'src',
                                exclusionPattern: ''
                            )
                        }
                    }
                }

                // Nœud 3 : Documentation Javadoc
                stage('Documentation') {
                    steps {
                        echo '=== Generation Javadoc ==='
                        bat 'mvn org.apache.maven.plugins:maven-javadoc-plugin:javadoc'
                    }
                    post {
                        success {
                            publishHTML(target: [
                                allowMissing         : true,
                                alwaysLinkToLastBuild: true,
                                keepAll              : true,
                                reportDir            : 'target/site/apidocs',
                                reportFiles          : 'index.html',
                                reportName           : 'Javadoc'
                            ])
                        }
                    }
                }

            } // fin parallel

            post {
                failure {
                    mail to: 'admin@hotel.com',
                         subject: "[Jenkins] ECHEC Tests - ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                         body: "Des tests ont echoue.\nURL: ${env.BUILD_URL}"
                }
            }
        }

        // ─────────────────────────────────────
        // STAGE 3 : PACKAGING
        // ─────────────────────────────────────
        stage('Packaging') {
            steps {
                echo '=== Packaging JAR ==='
                bat 'mvn package -DskipTests'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
                failure {
                    mail to: 'admin@hotel.com',
                         subject: "[Jenkins] ECHEC Package - ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                         body: "Le packaging a echoue.\nURL: ${env.BUILD_URL}"
                }
            }
        }

        // ─────────────────────────────────────
        // STAGE 4 : DEPLOY NEXUS
        // ─────────────────────────────────────
        stage('Deploy Nexus') {
            steps {
                echo '=== Deploiement vers Nexus ==='
                bat 'mvn deploy -DskipTests'
            }
            post {
                success {
                    echo 'Deploiement Nexus reussi!'
                }
                failure {
                    mail to: 'admin@hotel.com',
                         subject: "[Jenkins] ECHEC Deploy - ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                         body: "Le deploiement Nexus a echoue.\nURL: ${env.BUILD_URL}"
                }
            }
        }

    } // fin stages

    // ─────────────────────────────────────
    // NOTIFICATIONS GLOBALES
    // ─────────────────────────────────────
    post {
        success {
            echo '=== PIPELINE REUSSI ==='
            mail to: 'admin@hotel.com',
                 subject: "[Jenkins] SUCCES - ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                 body: "Pipeline termine avec succes!\nURL: ${env.BUILD_URL}"
        }
        failure {
            echo '=== PIPELINE ECHOUE ==='
            mail to: 'admin@hotel.com',
                 subject: "[Jenkins] ECHEC GLOBAL - ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                 body: "Le pipeline a echoue.\nURL: ${env.BUILD_URL}"
        }
        always {
            cleanWs()
        }
    }
}
