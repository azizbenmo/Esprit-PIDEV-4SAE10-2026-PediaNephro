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
                (cd config && mvn clean install -DskipTests)
                (cd eurekaserver && mvn clean install -DskipTests)
                (cd gateway && mvn clean install -DskipTests)
                (cd Microservices/dossieMedicale && mvn clean install -DskipTests)
                '''
            }
        }

        stage('Run Tests') {
            steps {
                sh '''
                set -e
                (cd config && mvn test)
                (cd eurekaserver && mvn test)
                (cd gateway && mvn test)
                (cd Microservices/dossieMedicale && mvn test)
                '''
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('sonar-scanner') {
                    withCredentials([string(credentialsId: 'sonar-secret', variable: 'SONAR_TOKEN')]) {
                        sh '''
                        set -e
                        (cd config && mvn sonar:sonar -Dsonar.token=$SONAR_TOKEN)
                        (cd eurekaserver && mvn sonar:sonar -Dsonar.token=$SONAR_TOKEN)
                        (cd gateway && mvn sonar:sonar -Dsonar.token=$SONAR_TOKEN)
                        (cd Microservices/dossieMedicale && mvn sonar:sonar -Dsonar.token=$SONAR_TOKEN)
                        '''
                    }
                }
            }
        }

        stage('Docker Hub Login') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sh '''
                    echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                    '''
                }
            }
        }

        stage('Build Docker Images') {
            steps {
                sh '''
                set -e
                docker build -t $DOCKERHUB_USER/pedianephro-config:$TAG ./config
                docker build -t $DOCKERHUB_USER/pedianephro-eureka:$TAG ./eurekaserver
                docker build -t $DOCKERHUB_USER/pedianephro-gateway:$TAG ./gateway
                docker build -t $DOCKERHUB_USER/pedianephro-dossier-medical:$TAG ./Microservices/dossieMedicale
                '''
            }
        }

        stage('Push Docker Images to Docker Hub') {
            steps {
                sh '''
                set -e
                docker push $DOCKERHUB_USER/pedianephro-config:$TAG
                docker push $DOCKERHUB_USER/pedianephro-eureka:$TAG
                docker push $DOCKERHUB_USER/pedianephro-gateway:$TAG
                docker push $DOCKERHUB_USER/pedianephro-dossier-medical:$TAG
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
}
