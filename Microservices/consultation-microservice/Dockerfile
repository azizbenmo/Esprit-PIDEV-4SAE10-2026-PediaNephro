FROM eclipse-temurin:17-jre
EXPOSE 8089
ADD target/consultation-microservice-0.0.1-SNAPSHOT.jar consultation-microservice.jar
ENTRYPOINT ["java", "-jar", "consultation-microservice.jar"]