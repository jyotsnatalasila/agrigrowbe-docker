# Backend multi-stage Dockerfile
# Builds the Spring Boot app and produces a slim runtime image

FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml ./
COPY src ./src
RUN mvn -DskipTests package

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
# copy jar produced by maven build
COPY --from=build /workspace/target/agrigrowbe-0.0.1-SNAPSHOT.jar /app/app.jar

# Expose the backend port (matches application.properties)
EXPOSE 1010

# Allow overriding JVM options and Spring properties via env
ENV JAVA_OPTS="-Xms256m -Xmx512m"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
