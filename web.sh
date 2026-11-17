#!/bin/bash
# Interfaz local conectada a los servicios Java en memoria.
set -e
cd "$(dirname "$0")"
mkdir -p out/clases
javac --release 21 -d out/clases $(find src -name '*.java')
java -cp out/clases co.edu.konradlorenz.demo.ServidorWeb "${1:-8000}"
