#!/bin/bash
# Crea el bucket de los PDF (cotizaciones/ y ordenes/) por la API S3, con las mismas credenciales
# que usan ms-cotizaciones, ms-ordenes y ms-notificaciones (este último descarga el PDF para
# adjuntarlo al correo: claim check).
S3="aws --endpoint-url http://s3:8333"
until $S3 s3 ls >/dev/null 2>&1; do sleep 2; done
$S3 s3 ls s3://documentos-pdf >/dev/null 2>&1 || $S3 s3 mb s3://documentos-pdf
$S3 s3 ls
