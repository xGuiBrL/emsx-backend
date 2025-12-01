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

ENV JWT_SECRET="TU_SECRETO_AQUI"

ENTRYPOINT ["java", "-jar", "app.jar"]
