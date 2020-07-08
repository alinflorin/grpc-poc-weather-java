FROM maven:3.6.3-openjdk-14-slim as builder
WORKDIR /app
COPY . .
RUN mvn clean install
RUN mvn package

FROM openjdk:14-slim
COPY --from=builder /app/target/grpc-poc-weather-java-1.0-SNAPSHOT.jar /grpc-poc-weather-java-1.0-SNAPSHOT.jar
EXPOSE 9090
CMD ["java", "-jar", "/grpc-poc-weather-java-1.0-SNAPSHOT.jar"]