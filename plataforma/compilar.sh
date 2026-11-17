#!/bin/bash
# Compila y prueba todos los servicios Spring Boot dentro de un contenedor de Maven (no hace falta
# instalar Maven ni Java 21). Monta la raíz del repositorio porque cada servicio copia su núcleo de
# src/ (DEC-61). Las dependencias quedan en el volumen konrad-gourmet-m2.
set -e
cd "$(dirname "$0")/.."
estado=0
docker run --rm \
  -v konrad-gourmet-m2:/root/.m2 \
  -v "$PWD":/w -w /w/servicios \
  maven:3.9.9-eclipse-temurin-21 \
  sh -c "mvn -q -B clean package; e=\$?; chown -R $(id -u):$(id -g) /w/servicios; exit \$e" || estado=$?
if [ "$estado" -ne 0 ]; then
  echo "La compilación o las pruebas de los servicios fallaron." >&2
  exit "$estado"
fi
echo "Servicios compilados y probados:"
ls -1 servicios/*/target/app.jar
