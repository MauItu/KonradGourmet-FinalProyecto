#!/bin/bash
# Compila el código y las pruebas, ejecuta la demo y corre las pruebas. Solo necesita un JDK 21 o más nuevo.
#   ./ejecutar.sh           compila, ejecuta la demo y corre las pruebas
#   ./ejecutar.sh demo      compila y ejecuta solo la demo
#   ./ejecutar.sh pruebas   compila y corre solo las pruebas
set -e
cd "$(dirname "$0")"
que="${1:-todo}"

rm -rf out
mkdir -p out/clases out/pruebas

echo "== Compilando (javac --release 21 -Xlint:all) =="
javac --release 21 -Xlint:all -d out/clases $(find src -name '*.java')
javac --release 21 -Xlint:all -cp out/clases -d out/pruebas $(find pruebas -name '*.java')
echo "Compiló sin errores ni avisos."

if [ "$que" = "todo" ] || [ "$que" = "demo" ]; then
    echo
    echo "== Demo =="
    java -cp out/clases co.edu.konradlorenz.demo.DemoArquitectura
fi

if [ "$que" = "todo" ] || [ "$que" = "pruebas" ]; then
    echo
    echo "== Pruebas =="
    java -cp out/clases:out/pruebas co.edu.konradlorenz.pruebas.EjecutarPruebas
fi
