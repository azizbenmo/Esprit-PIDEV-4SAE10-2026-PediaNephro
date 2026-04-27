FROM eclipse-temurin:17-jre
EXPOSE 8085
ADD target/Fraud-0.0.1-SNAPSHOT.jar fraud-service.jar
ENTRYPOINT ["java", "-jar", "fraud-service.jar"]