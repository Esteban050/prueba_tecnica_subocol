FROM maven:3.9.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copiar POM padre y del módulo
COPY back/pom.xml ./back/pom.xml
COPY back/agenda-service/pom.xml ./back/agenda-service/pom.xml
# Copiar código fuente
COPY back/agenda-service/src ./back/agenda-service/src

RUN mvn clean package -DskipTests -f ./back/agenda-service/pom.xml

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/back/agenda-service/target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
