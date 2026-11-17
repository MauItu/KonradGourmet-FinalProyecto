#!/bin/bash
# Compila los servicios y levanta toda la plataforma. Al terminar, abre http://localhost:8080
#   ./plataforma/levantar.sh             compila y levanta
#   ./plataforma/levantar.sh --sin-compilar
set -e
cd "$(dirname "$0")"
[ "$1" = "--sin-compilar" ] || ./compilar.sh
docker compose up -d --build
# Espera a que el servidor web esté sano: depende del gateway (sano) y de que los tópicos y el
# bucket se hayan creado, así que cuando él está arriba lo está todo.
for i in $(seq 1 90); do
  estado=$(docker inspect -f '{{.State.Health.Status}}' konrad-gourmet-web-1 2>/dev/null || true)
  [ "$estado" = "healthy" ] && break
  sleep 2
done
if [ "$estado" != "healthy" ]; then
  echo "La plataforma no quedó sana. Revisa: docker compose -f plataforma/docker-compose.yml ps -a" >&2
  exit 1
fi
# Y a que el gateway ya encuentre a ms-identidad en Eureka: el login responde 401 y no 503.
for i in $(seq 1 60); do
  codigo=$(curl -s -o /dev/null -w '%{http_code}' -H 'Content-Type: application/json' \
           -d '{"correo":"","clave":""}' http://localhost:8080/api/auth/token || true)
  [ "$codigo" = "401" ] && break
  sleep 2
done
if [ "$codigo" != "401" ]; then
  echo "El gateway todavía no enruta a ms-identidad (respuesta $codigo)." >&2
  exit 1
fi
echo
echo "Konrad Gourmet está arriba:"
echo "  Aplicación  http://localhost:8080"
echo "  Eureka      http://localhost:8761"
echo "  Correos     http://localhost:8025"
