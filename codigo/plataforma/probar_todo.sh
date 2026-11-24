#!/bin/bash
# Prueba todo el proyecto de punta a punta y cuenta las comprobaciones:
#   1. las verificaciones del núcleo (pruebas/, ./ejecutar.sh pruebas; solo necesita un JDK 21 o más nuevo);
#   2. las pruebas JUnit de los servicios (compilar.sh, dentro de un contenedor de Maven);
#   3. una instancia de la plataforma recién creada, sin datos, y sobre ella los recorridos de navegador y los
#      verificadores de los cortes 3 a 7, en orden (cada corte sigue con los datos que dejó el anterior).
# Al terminar detiene la instancia (docker compose stop: los datos quedan para revisarlos).
#
#   ./plataforma/probar_todo.sh [carpeta]                 todo; las capturas y los registros quedan en la carpeta
#   ./plataforma/probar_todo.sh --sin-compilar [carpeta]  sin el paso 2 (usa lo último compilado)
#   ./plataforma/probar_todo.sh --conservar [carpeta]     no detiene la instancia al final
#
# COMPOSE_PROJECT_NAME elige la instancia; por omisión, kg-limpio. Como la instancia se borra al empezar
# (docker compose down -v), no acepta konrad-gourmet, la de todos los días.
# PYTHON elige el intérprete con Playwright (por omisión, python3); los recorridos usan /usr/bin/chromium.
# CORTES elige los cortes (por omisión, "3 4a 4b 5 6a 6b 7"; siempre en ese orden y desde el 3).
set -u
cd "$(dirname "$0")/.."
compilar=1; conservar=0; carpeta=""
for a in "$@"; do
  case "$a" in
    --sin-compilar) compilar=0 ;;
    --conservar) conservar=1 ;;
    *) carpeta="$a" ;;
  esac
done
export COMPOSE_PROJECT_NAME="${COMPOSE_PROJECT_NAME:-kg-limpio}"
if [ "$COMPOSE_PROJECT_NAME" = "konrad-gourmet" ]; then
  echo "probar_todo.sh borra los datos de la instancia que usa; escoge otra con COMPOSE_PROJECT_NAME (por ejemplo, kg-limpio)." >&2
  exit 2
fi
PYTHON="${PYTHON:-python3}"
if ! "$PYTHON" -c 'import playwright' 2>/dev/null; then
  echo "$PYTHON no tiene Playwright; indica otro intérprete con PYTHON=… (pip install playwright)." >&2
  exit 2
fi
CORTES="${CORTES:-3 4a 4b 5 6a 6b 7}"
carpeta="${carpeta:-${TMPDIR:-/tmp}/konrad-gourmet-pruebas-$(date +%Y%m%d-%H%M%S)}"
mkdir -p "$carpeta"
carpeta="$(cd "$carpeta" && pwd)"
resumen="$carpeta/resumen.txt"
: > "$resumen"
fallo=0

anotar() {  # nombre, bien, mal
  printf '%-34s %5s bien %4s mal\n' "$1" "$2" "$3" | tee -a "$resumen"
  [ "$3" -eq 0 ] || fallo=1
}

echo "== 1. Verificaciones del núcleo (pruebas/)"
./ejecutar.sh pruebas > "$carpeta/nucleo.log" 2>&1
linea=$(grep -E '^[0-9]+ verificaciones: ' "$carpeta/nucleo.log" | tail -1)
nucleo_bien=$(echo "$linea" | sed -E 's/.*: ([0-9]+) bien.*/\1/'); nucleo_mal=$(echo "$linea" | sed -E 's/.* ([0-9]+) mal.*/\1/')
[ -n "$linea" ] || { nucleo_bien=0; nucleo_mal=1; tail -20 "$carpeta/nucleo.log"; }
anotar "pruebas/ (núcleo)" "$nucleo_bien" "$nucleo_mal"

junit_bien=0
if [ "$compilar" = 1 ]; then
  echo "== 2. Pruebas JUnit de los servicios (compilar.sh)"
  ./plataforma/compilar.sh > "$carpeta/junit.log" 2>&1
  estado=$?
  total=0; malas=0
  for f in servicios/*/target/surefire-reports/*.txt; do
    [ -f "$f" ] || continue
    l=$(grep -E '^Tests run:' "$f" | head -1)
    t=$(echo "$l" | sed -E 's/Tests run: ([0-9]+).*/\1/'); fa=$(echo "$l" | sed -E 's/.*Failures: ([0-9]+).*/\1/'); er=$(echo "$l" | sed -E 's/.*Errors: ([0-9]+).*/\1/')
    total=$((total + t)); malas=$((malas + fa + er))
  done
  [ "$estado" -eq 0 ] || [ "$malas" -gt 0 ] || malas=1
  junit_bien=$((total - malas))
  anotar "JUnit (servicios)" "$junit_bien" "$malas"
  [ "$estado" -eq 0 ] || { tail -30 "$carpeta/junit.log"; echo "La compilación falló; no se levanta la plataforma." >&2; exit 1; }
fi

echo "== 3. Plataforma limpia ($COMPOSE_PROJECT_NAME)"
docker compose -f plataforma/docker-compose.yml down -v --remove-orphans > "$carpeta/plataforma.log" 2>&1
if ! ./plataforma/levantar.sh --sin-compilar >> "$carpeta/plataforma.log" 2>&1; then
  tail -20 "$carpeta/plataforma.log"; exit 1
fi
# levantar.sh ya espera a que el gateway encuentre cada servicio; entre los servicios (ms-ordenes → ms-cotizaciones,
# por ejemplo) Eureka tarda un poco más en propagar las instancias: sin esta espera, ms-ordenes responde 503.
sleep 75
navegador_bien=0
for corte in $CORTES; do
  for paso in recorrido verificar; do
    script="plataforma/pruebas/${paso}_corte${corte}.py"
    echo "== corte $corte · $paso"
    "$PYTHON" "$script" "$carpeta/capturas" > "$carpeta/${paso}_corte${corte}.log" 2>&1
    salida=$?
    bien=$(grep -c '^  ✓ ' "$carpeta/${paso}_corte${corte}.log"); mal=$(grep -c '^  ✗ ' "$carpeta/${paso}_corte${corte}.log")
    if [ "$salida" -ne 0 ] && [ "$mal" -eq 0 ]; then
      mal=1  # terminó sin llegar al final (una excepción): cuenta como falla
      tail -15 "$carpeta/${paso}_corte${corte}.log"
    fi
    grep '^  ✗ ' "$carpeta/${paso}_corte${corte}.log" | head -5
    anotar "${paso}_corte${corte}.py" "$bien" "$mal"
    navegador_bien=$((navegador_bien + bien))
  done
done

[ "$conservar" = 1 ] || docker compose -f plataforma/docker-compose.yml stop >> "$carpeta/plataforma.log" 2>&1
echo | tee -a "$resumen"
printf '%-34s %5s\n' "Total de comprobaciones que pasan" "$((nucleo_bien + junit_bien + navegador_bien))" | tee -a "$resumen"
printf '%-34s %5s\n' "  núcleo (pruebas/)" "$nucleo_bien" | tee -a "$resumen"
printf '%-34s %5s\n' "  JUnit" "$junit_bien" | tee -a "$resumen"
printf '%-34s %5s\n' "  recorridos y verificadores" "$navegador_bien" | tee -a "$resumen"
if [ "$fallo" = 0 ]; then echo "Todo bien. Registros y capturas en $carpeta" | tee -a "$resumen"
else echo "HAY FALLAS. Registros y capturas en $carpeta" | tee -a "$resumen"; fi
exit "$fallo"
