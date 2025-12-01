# 1) Etapa de build
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# 2) Etapa final
FROM eclipse-temurin:17-jdk
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 9090

# JWT desde Render (opcional)
ARG JWT_SECRET
ENV JWT_SECRET=${JWT_SECRET}

ENTRYPOINT ["java", "-jar", "app.jar"]
