#!/bin/sh
# Una base y un usuario por microservicio (database per service). Cada usuario solo puede
# conectarse a su propia base: ningún servicio lee las tablas de otro (sección 5 del reporte).
set -e
for s in identidad pedidos menu inventario solicitudes cotizaciones ordenes notificaciones integracion administracion analitica; do
  psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" <<SQL
CREATE ROLE ms_$s LOGIN PASSWORD 'ms_$s';
CREATE DATABASE ms_$s OWNER ms_$s;
REVOKE CONNECT ON DATABASE ms_$s FROM PUBLIC;
SQL
done
