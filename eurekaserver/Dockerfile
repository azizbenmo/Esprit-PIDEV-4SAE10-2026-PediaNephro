FROM eclipse-temurin:17-jre
EXPOSE 8761
ADD target/eurekaserver-0.0.1-SNAPSHOT.jar eurekaServer.jar
ENTRYPOINT ["java", "-jar", "eurekaServer.jar"]
