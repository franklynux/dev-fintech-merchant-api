FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .
# Download dependencies first to leverage Docker cache
RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn clean package -DskipTests -B

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN addgroup -S merchant && adduser -S merchant -G merchant

COPY --from=build /app/target/*.jar app.jar

RUN chown -R merchant:merchant /app

USER merchant

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:7000/actuator/health || exit 1

EXPOSE 7000

ENV SPRING_PROFILES_ACTIVE=docker
ENV SERVER_PORT=7000

ENTRYPOINT ["java", "-jar", "app.jar"]
