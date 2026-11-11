FROM maven:3.9.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copiar POM padre y del módulo
COPY back/pom.xml ./back/pom.xml
COPY back/notificaciones-service/pom.xml ./back/notificaciones-service/pom.xml
# Copiar código fuente
COPY back/notificaciones-service/src ./back/notificaciones-service/src

RUN mvn clean package -DskipTests -f ./back/notificaciones-service/pom.xml

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/back/notificaciones-service/target/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
