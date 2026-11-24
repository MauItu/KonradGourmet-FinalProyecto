#!/bin/bash
# Deja la instancia kg-demo con los datos de la demostración: copia los cuatro volúmenes de kg-mockups
# (bases, Kafka, PDF e imágenes, logs) sobre los de kg-demo (DEC-111). kg-mockups no cambia.
# Uso, desde FinalProyecto:  documentos/guias_sustentacion/fuente/restablecer_demo.sh
# Después:  COMPOSE_PROJECT_NAME=kg-demo KonradGourmet/plataforma/levantar.sh --sin-compilar
#           y esperar ~75 s a que Eureka propague antes de entrar.
set -e
ORIGEN=kg-mockups
DESTINO=kg-demo
COMPOSE=$(cd "$(dirname "$0")/../../../KonradGourmet/plataforma" && pwd)/docker-compose.yml
IMAGEN=postgres:17-alpine   # cualquier imagen con sh y cp; esta ya está en el equipo

# Las dos instancias tienen que estar detenidas para que la copia sea coherente.
for p in $ORIGEN $DESTINO; do
  if [ -n "$(docker ps -q --filter label=com.docker.compose.project=$p)" ]; then
    echo "Deteniendo $p…"
    COMPOSE_PROJECT_NAME=$p docker compose -f "$COMPOSE" stop >/dev/null 2>&1
  fi
done
for v in datos-postgres datos-kafka datos-s3 datos-elasticsearch; do
  docker volume inspect "${ORIGEN}_$v" >/dev/null
  docker volume inspect "${DESTINO}_$v" >/dev/null 2>&1 || docker volume create \
    --label com.docker.compose.project=$DESTINO --label com.docker.compose.volume=$v "${DESTINO}_$v" >/dev/null
  docker run --rm -v "${ORIGEN}_$v":/de:ro -v "${DESTINO}_$v":/a $IMAGEN \
    sh -c 'find /a -mindepth 1 -delete && cp -a /de/. /a/'
  echo "  ${DESTINO}_$v ← ${ORIGEN}_$v"
done
echo "kg-demo restablecida. Para encenderla:"
echo "  COMPOSE_PROJECT_NAME=$DESTINO KonradGourmet/plataforma/levantar.sh --sin-compilar"
