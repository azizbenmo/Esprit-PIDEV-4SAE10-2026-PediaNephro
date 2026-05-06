pipeline {
    agent any

    environment {
        DOCKERHUB_USER = "mouhareb"
        TAG = "${BUILD_NUMBER}"
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/azizbenmo/Esprit-PIDEV-4SAE10-2026-PediaNephro.git'
            }
        }

        stage('Build Java Services') {
            steps {
                sh '''
                set -e

                echo "Building Config Server..."
                cd config
                mvn clean install -DskipTests
                cd ..

                echo "Building Eureka Server..."
                cd eurekaserver
                mvn clean install -DskipTests
                cd ..

                echo "Building Gateway..."
                cd gateway
                mvn clean install -DskipTests
                cd ..

                echo "Building Dossier Medical Service..."
                cd Microservices/dossieMedicale
                mvn clean install -DskipTests
                cd ../../..
                '''
            }
        }

        stage('Run Tests') {
            steps {
                sh '''
                set -e

                echo "Running tests for Config Server..."
                cd config
                mvn test
                cd ..

                echo "Running tests for Eureka Server..."
                cd eurekaserver
                mvn test
                cd ..

                echo "Running tests for Gateway..."
                cd gateway
                mvn test \
                  -Dspring.cloud.config.enabled=false \
                  -Dspring.cloud.discovery.enabled=false \
                  -Deureka.client.enabled=false
                cd ..

                echo "Running tests for Dossier Medical Service..."
                cd Microservices/dossieMedicale
                mvn test \
                  -Dspring.cloud.config.enabled=false \
                  -Dspring.cloud.discovery.enabled=false \
                  -Deureka.client.enabled=false
                cd ../../..
                '''
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('sonar-scanner') {
                    withCredentials([string(credentialsId: 'sonar-secret', variable: 'SONAR_TOKEN')]) {
                        sh '''
                        set -e

                        echo "SonarQube analysis for Config Server..."
                        cd config
                        mvn sonar:sonar -Dsonar.token=$SONAR_TOKEN
                        cd ..

                        echo "SonarQube analysis for Eureka Server..."
                        cd eurekaserver
                        mvn sonar:sonar -Dsonar.token=$SONAR_TOKEN
                        cd ..

                        echo "SonarQube analysis for Gateway..."
                        cd gateway
                        mvn sonar:sonar \
                          -Dsonar.token=$SONAR_TOKEN \
                          -Dspring.cloud.config.enabled=false \
                          -Dspring.cloud.discovery.enabled=false \
                          -Deureka.client.enabled=false
                        cd ..

                        echo "SonarQube analysis for Dossier Medical Service..."
                        cd Microservices/dossieMedicale
                        mvn sonar:sonar \
                          -Dsonar.token=$SONAR_TOKEN \
                          -Dspring.cloud.config.enabled=false \
                          -Dspring.cloud.discovery.enabled=false \
                          -Deureka.client.enabled=false
                        cd ../../..
                        '''
                    }
                }
            }
        }

        stage('Check Docker Access') {
            steps {
                sh '''
                set -e
                echo "Checking Docker access from Jenkins..."
                docker --version
                docker ps
                '''
            }
        }

        stage('Docker Hub Login') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sh '''
                    set -e
                    echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                    '''
                }
            }
        }

        stage('Build Docker Images') {
            steps {
                sh '''
                set -e

                echo "Building Docker image: config"
                docker build -t $DOCKERHUB_USER/config:$TAG ./config
                docker tag $DOCKERHUB_USER/config:$TAG $DOCKERHUB_USER/config:latest

                echo "Building Docker image: eureka"
                docker build -t $DOCKERHUB_USER/eureka:$TAG ./eurekaserver
                docker tag $DOCKERHUB_USER/eureka:$TAG $DOCKERHUB_USER/eureka:latest

                echo "Building Docker image: gateway"
                docker build -t $DOCKERHUB_USER/gateway:$TAG ./gateway
                docker tag $DOCKERHUB_USER/gateway:$TAG $DOCKERHUB_USER/gateway:latest

                echo "Building Docker image: dossier-medical"
                docker build -t $DOCKERHUB_USER/dossier-medical:$TAG ./Microservices/dossieMedicale
                docker tag $DOCKERHUB_USER/dossier-medical:$TAG $DOCKERHUB_USER/dossier-medical:latest
                '''
            }
        }

        stage('Push Docker Images to Docker Hub') {
            steps {
                sh '''
                set -e

                docker push $DOCKERHUB_USER/config:$TAG
                docker push $DOCKERHUB_USER/config:latest

                docker push $DOCKERHUB_USER/eureka:$TAG
                docker push $DOCKERHUB_USER/eureka:latest

                docker push $DOCKERHUB_USER/gateway:$TAG
                docker push $DOCKERHUB_USER/gateway:latest

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
        success {
            echo "CI Pipeline completed successfully. Docker images pushed and CD pipeline triggered."
        }

        failure {
            echo "CI Pipeline failed. Check the logs above."
        }

        always {
            sh '''
            docker logout || true
            '''
        }
    }
}
