FROM maven:3.9.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copiar POMs y compilar dependencias
COPY pom.xml ../pom.xml
COPY notificaciones-service/pom.xml ./pom.xml
# Copiar código fuente
COPY notificaciones-service/src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
