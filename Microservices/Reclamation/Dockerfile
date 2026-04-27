FROM eclipse-temurin:17-jre
EXPOSE 8086
ADD target/Reclamation-0.0.1-SNAPSHOT.jar reclamation-service.jar
ENTRYPOINT ["java", "-jar", "reclamation-service.jar"]