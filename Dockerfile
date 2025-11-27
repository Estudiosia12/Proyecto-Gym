# ----------------------------------------
## 1. ⚙️ ETAPA DE CONSTRUCCIÓN (BUILDER)
# Usamos una imagen con Maven y Java para compilar el proyecto.
# ----------------------------------------
FROM maven:3.9.5-amazoncorretto-21 AS builder

# Establece el directorio de trabajo
WORKDIR /app

# Copia pom.xml para gestionar dependencias
COPY pom.xml .
RUN mvn dependency:go-offline

# Copia el código fuente y compila, generando el JAR en /app/target/
COPY src ./src
RUN mvn clean package -DskipTests

# ----------------------------------------
## 2. 🚀 ETAPA DE EJECUCIÓN (FINAL)
# Usamos una imagen base ligera para la ejecución.
# ----------------------------------------
FROM amazoncorretto:23-alpine

# Establece el directorio de trabajo final
WORKDIR /app

# COPIA el JAR compilado desde la etapa 'builder'
# Esto resuelve el error "not found"
COPY --from=builder /app/target/ProyectoGym-0.0.1-SNAPSHOT.jar app.jar

# Comando de ejecución: incluye los parámetros de start.sh (puerto, perfil, memoria)
# Usamos 'sh -c' para que la variable $PORT de Render sea interpretada correctamente.
ENTRYPOINT ["sh", "-c", "java -Dserver.port=$PORT -Dspring.profiles.active=prod -Xmx512m -jar /app/app.jar"]