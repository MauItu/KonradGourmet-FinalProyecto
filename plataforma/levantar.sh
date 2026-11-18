#!/bin/bash
# Compila los servicios y levanta toda la plataforma. Al terminar, abre http://localhost:8080
#   ./plataforma/levantar.sh             compila y levanta
#   ./plataforma/levantar.sh --sin-compilar
set -e
cd "$(dirname "$0")"
[ "$1" = "--sin-compilar" ] || ./compilar.sh
# Los que crean los tópicos y los buckets se vuelven a correr siempre (no repiten lo que ya existe):
# así una instalación anterior recibe los que se agregaron después.
docker compose rm -fs kafka-topicos s3-bucket >/dev/null 2>&1 || true
docker compose up -d --build
# Espera a que el servidor web esté sano: depende del gateway y de los microservicios (sanos) y de
# que los tópicos y los buckets se hayan creado, así que cuando él está arriba lo está todo.
web=$(docker compose ps -q web)
for i in $(seq 1 120); do
  estado=$(docker inspect -f '{{.State.Health.Status}}' "$web" 2>/dev/null || true)
  [ "$estado" = "healthy" ] && break
  sleep 2
done
if [ "$estado" != "healthy" ]; then
  echo "La plataforma no quedó sana. Revisa: docker compose -f plataforma/docker-compose.yml ps -a" >&2
  exit 1
fi
# Y a que el gateway ya encuentre cada servicio en Eureka (si no, responde 503). Primero el login:
# sin datos responde 401.
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
# Después los demás, con el token de la administradora: cada servicio responde 403 (su rol no entra),
# y eso ya prueba que la petición llegó al servicio. El WSDL de ms-integracion lo puede leer cualquier
# usuario con sesión (DEC-72); la libreta de destinatarios de ms-notificaciones y los parámetros de
# ms-administracion son de la administradora: las tres responden 200.
token=$(curl -s -H 'Content-Type: application/json' \
        -d '{"correo":"paula.herrera@konradgourmet.com","clave":"Konrad2026"}' http://localhost:8080/api/auth/token \
        | sed -n 's/.*"jwt":"\([^"]*\)".*/\1/p')
for par in /api/pedidos/mesas=403 /api/platos=403 /api/inventario/insumos=403 /api/solicitudes=403 \
           '/api/integracion/abastecimiento?wsdl=200' /api/cotizaciones=403 /api/proveedores=403 /api/ordenes-compra=403 \
           '/api/notificaciones/destinatarios?sucursalId=1=200' /api/admin/parametros=200; do
  ruta=${par%=*}; esperado=${par##*=}
  for i in $(seq 1 60); do
    codigo=$(curl -s -o /dev/null -w '%{http_code}' -H "Authorization: Bearer $token" "http://localhost:8080$ruta" || true)
    [ "$codigo" = "$esperado" ] && break
    sleep 2
  done
  if [ "$codigo" != "$esperado" ]; then
    echo "El gateway todavía no enruta $ruta (respuesta $codigo)." >&2
    exit 1
  fi
done
echo
echo "Konrad Gourmet está arriba:"
echo "  Aplicación  http://localhost:8080"
echo "  Eureka      http://localhost:8761"
echo "  Correos     http://localhost:8025"
