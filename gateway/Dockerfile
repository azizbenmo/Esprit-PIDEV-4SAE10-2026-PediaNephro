FROM eclipse-temurin:17-jre
EXPOSE 8900
ADD target/gateway-0.0.1-SNAPSHOT.jar gateway.jar
ENTRYPOINT ["java", "-jar", "gateway.jar"]