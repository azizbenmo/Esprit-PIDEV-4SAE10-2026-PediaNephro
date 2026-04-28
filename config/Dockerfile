FROM eclipse-temurin:17-jre
EXPOSE 8070
ADD target/config-0.0.1-SNAPSHOT.jar config.jar
ENTRYPOINT ["java", "-jar", "config.jar"]
