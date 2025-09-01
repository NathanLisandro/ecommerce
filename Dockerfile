FROM openjdk:21-jdk-slim

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN apt-get update && \
    apt-get install -y maven && \
    mvn clean package -DskipTests && \
    mv target/*.jar app.jar && \
    rm -rf target src pom.xml ~/.m2

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]