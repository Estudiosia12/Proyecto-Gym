# ----------------------------------------
## 1. ⚙️ ETAPA DE CONSTRUCCIÓN (BUILD STAGE)
# Esta etapa usa una imagen con Maven para compilar tu código.
# ----------------------------------------
FROM maven:3.9.5-amazoncorretto-21 AS builder

WORKDIR /app

# Copia los archivos de configuración de Maven
COPY pom.xml .

# Descarga las dependencias para un caché más rápido
RUN mvn dependency:go-offline

# Copia el código fuente y compila el proyecto
COPY src ./src
RUN mvn clean package -DskipTests

# ----------------------------------------
## 2. 🚀 ETAPA DE EJECUCIÓN (FINAL STAGE)
# Esta etapa usa una imagen ligera para ejecutar solo el JAR compilado.
# ----------------------------------------
# Usamos una imagen ligera de Corretto, similar a la que tenías
FROM amazoncorretto:23-alpine

WORKDIR /app

# COPIA el archivo JAR desde la etapa de 'builder' (¡Aquí se resuelve el error!)
# El JAR compilado está en /app/target/ en la etapa 'builder'.
COPY --from=builder /app/target/ProyectoGym-0.0.1-SNAPSHOT.jar app.jar

# Define el punto de entrada para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "/app/app.jar"]