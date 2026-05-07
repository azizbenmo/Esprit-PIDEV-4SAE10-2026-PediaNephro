pipeline {
    agent any

    environment {
        DOCKERHUB_USER = "mouhareb"
        TAG = "${BUILD_NUMBER}"
        SONARQUBE_ENV = "sonar-scanner"
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: 'main',
                    credentialsId: 'github-token',
                    url: 'https://github.com/azizbenmo/Esprit-PIDEV-4SAE10-2026-PediaNephro.git'
            }
        }

        stage('Build Java Services') {
            steps {
                sh '''
                set -e

                echo "========== Build Config Server =========="
                (cd config && mvn clean install -DskipTests)

                echo "========== Build Eureka Server =========="
                (cd eurekaserver && mvn clean install -DskipTests)

                echo "========== Build Gateway =========="
                (cd gateway && mvn clean install -DskipTests)

                echo "========== Build Dossier Medical Service =========="
                (cd Microservices/dossieMedicale && mvn clean install -DskipTests)
                '''
            }
        }

        stage('Run Unit Tests') {
            steps {
                sh '''
                set -e

                echo "========== Tests Config Server =========="
                (cd config && mvn test)

                echo "========== Tests Eureka Server =========="
                (cd eurekaserver && mvn test)

                echo "========== Tests Gateway =========="
                (cd gateway && mvn test \
                  -Dspring.cloud.config.enabled=false \
                  -Dspring.cloud.discovery.enabled=false \
                  -Deureka.client.enabled=false)

                echo "========== Tests Dossier Medical Service =========="
                (cd Microservices/dossieMedicale && mvn test \
                  -Dspring.cloud.config.enabled=false \
                  -Dspring.cloud.discovery.enabled=false \
                  -Deureka.client.enabled=false)
                '''
            }
        }

        stage('Publish Unit Test Reports') {
            steps {
                junit allowEmptyResults: true, testResults: '''
                    config/target/surefire-reports/*.xml,
                    eurekaserver/target/surefire-reports/*.xml,
                    gateway/target/surefire-reports/*.xml,
                    Microservices/dossieMedicale/target/surefire-reports/*.xml
                '''
            }
        }

        stage('Check SonarQube Access') {
            steps {
                withSonarQubeEnv("${SONARQUBE_ENV}") {
                    sh '''
                    set -e
                    echo "========== Checking SonarQube access =========="
                    curl -I $SONAR_HOST_URL
                    '''
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv("${SONARQUBE_ENV}") {
                    sh '''
                    set -e

                    echo "========== SonarQube Config Server =========="
                    (cd config && mvn sonar:sonar \
                      -Dsonar.host.url=$SONAR_HOST_URL \
                      -Dsonar.token=$SONAR_AUTH_TOKEN)

                    echo "========== SonarQube Eureka Server =========="
                    (cd eurekaserver && mvn sonar:sonar \
                      -Dsonar.host.url=$SONAR_HOST_URL \
                      -Dsonar.token=$SONAR_AUTH_TOKEN)

                    echo "========== SonarQube Gateway =========="
                    (cd gateway && mvn sonar:sonar \
                      -Dsonar.host.url=$SONAR_HOST_URL \
                      -Dsonar.token=$SONAR_AUTH_TOKEN \
                      -Dspring.cloud.config.enabled=false \
                      -Dspring.cloud.discovery.enabled=false \
                      -Deureka.client.enabled=false)

                    echo "========== SonarQube Dossier Medical Service =========="
                    (cd Microservices/dossieMedicale && mvn sonar:sonar \
                      -Dsonar.host.url=$SONAR_HOST_URL \
                      -Dsonar.token=$SONAR_AUTH_TOKEN \
                      -Dspring.cloud.config.enabled=false \
                      -Dspring.cloud.discovery.enabled=false \
                      -Deureka.client.enabled=false)
                    '''
                }
            }
        }

        stage('Check Docker Access') {
            steps {
                sh '''
                set -e
                echo "========== Checking Docker access from Jenkins =========="
                docker --version
                docker ps
                '''
            }
        }

        stage('Docker Hub Login') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-credentials',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    sh '''
                    set -e
                    echo "========== Docker Hub Login =========="
                    echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                    '''
                }
            }
        }

        stage('Build Docker Images') {
            steps {
                sh '''
                set -e

                echo "========== Build Docker image: config =========="
                docker build -t $DOCKERHUB_USER/config:$TAG ./config
                docker tag $DOCKERHUB_USER/config:$TAG $DOCKERHUB_USER/config:latest

                echo "========== Build Docker image: eureka =========="
                docker build -t $DOCKERHUB_USER/eureka:$TAG ./eurekaserver
                docker tag $DOCKERHUB_USER/eureka:$TAG $DOCKERHUB_USER/eureka:latest

                echo "========== Build Docker image: gateway =========="
                docker build -t $DOCKERHUB_USER/gateway:$TAG ./gateway
                docker tag $DOCKERHUB_USER/gateway:$TAG $DOCKERHUB_USER/gateway:latest

                echo "========== Build Docker image: dossier-medical =========="
                docker build -t $DOCKERHUB_USER/dossier-medical:$TAG ./Microservices/dossieMedicale
                docker tag $DOCKERHUB_USER/dossier-medical:$TAG $DOCKERHUB_USER/dossier-medical:latest
                '''
            }
        }

        stage('Push Docker Images to Docker Hub') {
            steps {
                sh '''
                set -e

                echo "========== Push config =========="
                docker push $DOCKERHUB_USER/config:$TAG
                docker push $DOCKERHUB_USER/config:latest

                echo "========== Push eureka =========="
                docker push $DOCKERHUB_USER/eureka:$TAG
                docker push $DOCKERHUB_USER/eureka:latest

                echo "========== Push gateway =========="
                docker push $DOCKERHUB_USER/gateway:$TAG
                docker push $DOCKERHUB_USER/gateway:latest

                echo "========== Push dossier-medical =========="
                docker push $DOCKERHUB_USER/dossier-medical:$TAG
                docker push $DOCKERHUB_USER/dossier-medical:latest
                '''
            }
        }

        stage('Trigger CD Pipeline') {
            steps {
                build job: 'docier-medical-CD',
                    parameters: [
                        string(name: 'IMAGE_TAG', value: "${BUILD_NUMBER}")
                    ]
            }
        }
    }

    post {
        always {
            echo "========== Publishing Unit Test Reports =========="

            junit allowEmptyResults: true, testResults: '''
                config/target/surefire-reports/*.xml,
                eurekaserver/target/surefire-reports/*.xml,
                gateway/target/surefire-reports/*.xml,
                Microservices/dossieMedicale/target/surefire-reports/*.xml
            '''

            sh '''
            docker logout || true
            '''
        }

        success {
            echo "CI Pipeline completed successfully. Build, tests, SonarQube analysis, Docker push and CD trigger are OK."
        }

        failure {
            echo "CI Pipeline failed. Check logs, tests, SonarQube, Docker access or CD deployment."
        }
    }
}
