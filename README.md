# 🏋️‍♂️ ProyectoGym

Aplicación web desarrollada con **Spring Boot** y **Maven** para la gestión integral de un gimnasio.  
Permite administrar miembros, rutinas, clases, instructores y el progreso de los usuarios.

---

## 🚀 Características principales
- Registro y autenticación de miembros.  
- Gestión de clases grupales y rutinas personalizadas.  
- Control de asistencia y seguimiento del progreso físico.  
- Panel de administración con gestión de usuarios, planes y estadísticas.  
- Persistencia de datos mediante **Spring Data JPA** y conexión a base de datos relacional.  
- Plantillas web en **HTML** integradas con **Thymeleaf**.

---

## 💾 Base de Datos
El archivo **`db/ScriptV2.txt`** contiene el script SQL necesario para crear y poblar la base de datos de pruebas. PostgreSQL

---

## 🛠️ Instalación y Ejecución Local

Clonar este repositorio:
```bash
git clone https://github.com/Estudiosia12/Proyecto-Gym.git
```

Abrir el proyecto en IntelliJ IDEA o Eclipse.

Verificar la conexión a la base de datos en `application.properties`.

Ejecutar la clase principal:
```
ProyectoGymApplication.java
```

Acceder a la aplicación:
👉 http://localhost:8080

---

## 🌐 Despliegue en Producción

Para desplegar esta aplicación en **Render**, consulta la guía completa:

📖 **[Guía de Despliegue en Render](GUIA_DESPLIEGUE_RENDER.md)**

La guía incluye:
- Configuración de PostgreSQL en Render
- Variables de entorno necesarias
- Comandos de build y start
- Solución de problemas comunes

---

## 📚 Tecnologías Utilizadas

- **Java 17**
- **Spring Boot 3.5.6**
- **Spring Data JPA**
- **Thymeleaf**
- **PostgreSQL**
- **Maven**

---

Proyecto desarrollado para fines académicos
