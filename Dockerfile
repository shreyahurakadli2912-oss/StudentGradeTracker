# Step 1: Build the application with Maven
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
COPY web ./web
RUN mvn clean package -DskipTests

# Step 2: Run the packaged Spring Boot application
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/student-grade-tracker-2.0.0.jar app.jar
COPY web ./web
ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
