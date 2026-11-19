#!/bin/bash
# Apaga la plataforma. Con --borrar también borra los datos (bases, tópicos y PDF).
set -e
cd "$(dirname "$0")"
if [ "$1" = "--borrar" ]; then docker compose down -v; else docker compose down; fi
