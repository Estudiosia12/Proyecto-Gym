#!/bin/bash
# Script de inicio para Render
# Ejecuta la aplicación Spring Boot con el perfil de producción

java -Dserver.port=$PORT \
     -Dspring.profiles.active=prod \
     -Xmx512m \
     -jar target/ProyectoGym-0.0.1-SNAPSHOT.jar
