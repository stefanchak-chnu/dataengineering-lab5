FROM eclipse-temurin:17-jre-alpine
WORKDIR /tmp
COPY ./target/*.jar /tmp/
ENTRYPOINT ["java", "-jar", "kafka-consumer-1.0-SNAPSHOT.jar"]