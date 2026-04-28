pipeline {
    agent any

    environment {
        REGISTRY = "mouhareb"
        TAG = "${BUILD_NUMBER}"
        SONARQUBE_ENV = "sonar-scanner" // Jenkins Sonar config name
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/azizbenmo/Esprit-PIDEV-4SAE10-2026-PediaNephro.git'
            }
        }

        stage('Build Java Services') {
            steps {
                sh '''
                echo "Building Config Server"
                cd config && mvn clean install -DskipTests && cd ..

                echo "Building Eureka Server"
                cd eurekaserver && mvn clean install -DskipTests && cd ..

                echo "Building Gateway"
                cd gateway && mvn clean install -DskipTests && cd ..

                echo "Building Dossier Medical Service"
                cd Microservices/dossieMedicale && mvn clean install -DskipTests && cd ../..
                '''
            }
        }

        stage('Run Tests + JaCoCo') {
            steps {
                sh '''
                cd config && mvn test && cd ..
                cd eurekaserver && mvn test && cd ..
                cd gateway && mvn test && cd ..
                cd Microservices/dossieMedicale && mvn test && cd ../..
                '''
            }
        }

       

        stage('SonarQube Analysis') {
    steps {
        withSonarQubeEnv('sonar-scanner') {
            withCredentials([string(credentialsId: 'sonar-secret', variable: 'SONAR_TOKEN')]) {
                sh '''
                set -e

                echo "Sonar analysis - Config"
                (cd config && mvn sonar:sonar -Dsonar.token=$SONAR_TOKEN)

                echo "Sonar analysis - Eureka"
                (cd eurekaserver && mvn sonar:sonar -Dsonar.token=$SONAR_TOKEN)

                echo "Sonar analysis - Gateway"
                (cd gateway && mvn sonar:sonar -Dsonar.token=$SONAR_TOKEN)

                echo "Sonar analysis - Dossier Medical"
                (cd Microservices/dossieMedicale && mvn sonar:sonar -Dsonar.token=$SONAR_TOKEN)
                '''
            }
        }
    }
}

        stage('Build Docker Images') {
            steps {
                sh '''
                docker build -t $REGISTRY/config:${TAG} ./config
                docker build -t $REGISTRY/eureka:${TAG} ./eurekaserver
                docker build -t $REGISTRY/gateway:${TAG} ./gateway
                docker build -t $REGISTRY/dossier-medical:${TAG} ./Microservices/dossieMedicale
                docker build -t $REGISTRY/medical-ai:${TAG} ./Microservices/medical-ai
                '''
            }
        }

        stage('Push Docker Images') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub', usernameVariable: 'USER', passwordVariable: 'PASS')]) {
                    sh '''
                    echo $PASS | docker login -u $USER --password-stdin

                    docker push $REGISTRY/config:${TAG}
                    docker push $REGISTRY/eureka:${TAG}
                    docker push $REGISTRY/gateway:${TAG}
                    docker push $REGISTRY/dossier-medical:${TAG}
                    docker push $REGISTRY/medical-ai:${TAG}
                    '''
                }
            }
        }
    }
}
