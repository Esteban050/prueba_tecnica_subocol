FROM maven:3.9.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copiar POM padre y del módulo
COPY back/pom.xml ./back/pom.xml
COPY back/ordenes-service/pom.xml ./back/ordenes-service/pom.xml
# Copiar código fuente
COPY back/ordenes-service/src ./back/ordenes-service/src

RUN mvn clean package -DskipTests -f ./back/ordenes-service/pom.xml

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/back/ordenes-service/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
