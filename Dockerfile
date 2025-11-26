FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY target/ProyectoGym-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-Dserver.port=${PORT}","-Dspring.profiles.active=prod","-jar","/app.jar"]
