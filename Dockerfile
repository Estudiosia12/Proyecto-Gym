FROM amazoncorretto:23-alpine
WORKDIR /app
COPY target/ProyectoGym-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]