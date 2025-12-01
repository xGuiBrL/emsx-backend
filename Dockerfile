# 1) Etapa de build
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app
COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# 2) Etapa final
FROM eclipse-temurin:17-jdk
WORKDIR /app

# Copiar el jar compilado como app.jar
COPY --from=build /app/target/*.jar app.jar

EXPOSE 9090

# Declaras un argumento que viene desde afuera
ARG JWT_SECRET

# Lo conviertes en variable de entorno dentro del contenedor
ENV JWT_SECRET=${JWT_SECRET}

ENV DATABASE_URL=""
ENV DATABASE_USER=""
ENV DATABASE_PASSWORD="

ENTRYPOINT ["java", "-jar", "app.jar"]
