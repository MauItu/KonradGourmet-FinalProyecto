#!/bin/bash
# Compila todos los servicios Spring Boot dentro de un contenedor de Maven (no hace falta
# instalar Maven ni Java 21). Las dependencias quedan en el volumen konrad-gourmet-m2.
set -e
cd "$(dirname "$0")/../servicios"
docker run --rm \
  -v konrad-gourmet-m2:/root/.m2 \
  -v "$PWD":/w -w /w \
  maven:3.9.9-eclipse-temurin-21 \
  sh -c "mvn -q -B -DskipTests package && chown -R $(id -u):$(id -g) /w"
echo "Servicios compilados:"
ls -1 */target/app.jar
