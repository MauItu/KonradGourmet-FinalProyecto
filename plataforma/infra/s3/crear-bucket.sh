#!/bin/bash
# Crea por la API S3 el bucket de los PDF (cotizaciones/ y ordenes/), con las mismas credenciales
# que usan ms-cotizaciones, ms-ordenes y ms-notificaciones (este último descarga el PDF para
# adjuntarlo al correo: claim check), y el de las imágenes de los platos, que usa ms-menu (DEC-67).
S3="aws --endpoint-url http://s3:8333"
until $S3 s3 ls >/dev/null 2>&1; do sleep 2; done
for b in documentos-pdf imagenes-platos; do
  $S3 s3 ls s3://$b >/dev/null 2>&1 || $S3 s3 mb s3://$b
done
$S3 s3 ls
