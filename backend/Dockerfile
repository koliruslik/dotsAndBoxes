FROM maven:3.9.2-eclipse-temurin-17

WORKDIR /app

COPY pom.xml mvnw ./
COPY .mvn .mvn

RUN mvn dependency:go-offline

COPY src ./src

# Expose port
EXPOSE 8080

CMD ["./mvnw", "spring-boot:run"]