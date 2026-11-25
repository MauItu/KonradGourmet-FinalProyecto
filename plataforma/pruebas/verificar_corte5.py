#!/usr/bin/env python3
# Revisa lo que dejó recorrido_corte5.py (corre contra los contenedores con docker exec, contra el gateway en
# http://localhost:8080 y contra la API de Mailpit en http://localhost:8025, sin instalar nada; usa pdftotext y
# pdfinfo de poppler si están):
#   ms-ordenes: OC-0012 de COT-018 en PostgreSQL (proveedor, productos, precios, solicitud, PDF), FINALIZADA desde
#     V23 con su auditoría (DEC-80);
#     COT-018 CON_ORDEN en ms-cotizaciones y SOL-031 COMPLETADA en ms-solicitudes;
#   el PDF de la orden: en el bucket, idéntico al que sirve el gateway (DEC-75) y al que bajó el navegador, legible;
#   Kafka: orden.generada con la directora y la correlación del navegador, y la ida y vuelta en el mismo recorrido
#     (ms-cotizaciones, ms-solicitudes y ms-notificaciones la recibieron y lo dejaron en la auditoría);
#   Mailpit: el correo certificado de cada cotización enviada y de la orden, con su PDF adjunto idéntico al del
#     almacén y las cabeceras de los decoradores; los avisos al jefe (solicitudes y stock bajo); ninguno para la
#     cotización PENDIENTE_PRECIO; la traza de envíos y la consulta de V14 (DEC-76);
#   la alarma: el canal de Redis, el WebSocket (token y rol) y los eventos de stock bajo;
#   la libreta de destinatarios; las reglas de HU-14 (segunda orden, no opcionada) y los roles;
#   el correo caído: el envío falla, queda en la traza y va a la cola de errores; al volver el correo, la cola se
#     reprocesa sola y lo entrega una vez, en el recorrido original (DEC-81); el reenvío de HU-12 entrega otro.
# Uso: python verificar_corte5.py <carpeta-de-capturas-del-recorrido>
#      (COMPOSE_PROJECT_NAME elige otra instancia; por omisión, konrad-gourmet)
import datetime, json, os, re, shutil, subprocess, sys, tempfile, time, urllib.request, uuid

PROYECTO = os.environ.get('COMPOSE_PROJECT_NAME', 'konrad-gourmet')
BASE = os.environ.get('BASE', 'http://localhost:8080')
MAILPIT = os.environ.get('MAILPIT', 'http://localhost:8025')
JEFE, AUXILIAR, DIRECTORA = 'carlos.mendoza@konradgourmet.com', 'isabella.castro@konradgourmet.com', 'laura.gomez@konradgourmet.com'
ADMIN = 'paula.herrera@konradgourmet.com'
fallas = []

def ok(cond, texto):
    print(('  ✓ ' if cond else '  ✗ ') + texto)
    if not cond:
        fallas.append(texto)

def docker(contenedor, *orden, binario=False):
    r = subprocess.run(['docker', 'exec', f'{PROYECTO}-{contenedor}-1', *orden], capture_output=True, text=not binario)
    return r.stdout

def sql(base, consulta):
    salida = docker('postgres', 'psql', '-U', 'postgres', '-d', base, '-tAF', '|', '-c', consulta)
    return [l.split('|') for l in salida.strip().splitlines() if l]

def leer_topico(nombre, *extra):
    # en bytes: las cabeceras de la cola de errores traen la partición y el offset en binario
    salida = docker('kafka', '/opt/kafka/bin/kafka-console-consumer.sh', '--bootstrap-server', 'localhost:9092', '--topic', nombre,
                    '--from-beginning', '--timeout-ms', '5000', '--property', 'print.key=true', '--property', 'key.separator=§', *extra, binario=True)
    return salida.decode('utf-8', errors='replace')

def topico(nombre):
    eventos = []
    for l in leer_topico(nombre).splitlines():
        if '§{' in l:
            clave, cuerpo = l.split('§', 1)
            e = json.loads(cuerpo); e['_clave'] = clave; eventos.append(e)
    return eventos

def cola_de_errores(numero):
    """Los registros de notificaciones.dlq de una cotización y las cabeceras que puso Spring Kafka (la traza del
    error trae saltos de línea, así que se lee la salida completa)."""
    salida = leer_topico('notificaciones.dlq', '--property', 'print.headers=true')
    registros = [l.rsplit('§', 2) for l in salida.splitlines() if '§{' in l]
    propios = [json.loads(r[-1]) for r in registros if len(r) == 3 and r[-2] == numero]
    return propios, salida

def http(metodo, ruta, cuerpo=None, cabeceras=None, binario=False, base=BASE):
    datos = json.dumps(cuerpo).encode() if cuerpo is not None else None
    cab = dict(cabeceras or {})
    if cuerpo is not None: cab['Content-Type'] = 'application/json'
    peticion = urllib.request.Request(base + ruta, data=datos, method=metodo, headers=cab)
    try:
        with urllib.request.urlopen(peticion, timeout=30) as r:
            leido = r.read()
            return r.status, leido if binario else leido.decode()
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode()

def token(correo):
    _, cuerpo = http('POST', '/api/auth/token', {'correo': correo, 'clave': 'Konrad2026'})
    return json.loads(cuerpo)['jwt']

def con(tok):
    return {'Authorization': f'Bearer {tok}'}

def texto_pdf(contenido):
    if not shutil.which('pdftotext'):
        return None
    with tempfile.NamedTemporaryFile(suffix='.pdf') as f:
        f.write(contenido); f.flush()
        paginas = subprocess.run(['pdfinfo', f.name], capture_output=True, text=True).stdout
        texto = subprocess.run(['pdftotext', '-layout', f.name, '-'], capture_output=True, text=True).stdout
    return paginas, texto

def filer(llave):
    return docker('s3', 'wget', '-qO-', f'http://127.0.0.1:8888/buckets/documentos-pdf/{llave}', binario=True)

def correos():
    _, cuerpo = http('GET', '/api/v1/messages?limit=1000', base=MAILPIT)
    return json.loads(cuerpo)['messages']

def correo_de(para, asunto):
    return [m for m in correos() if m['Subject'] == asunto and any(t['Address'] == para for t in m['To'])]

def adjunto(id_correo):
    _, cuerpo = http('GET', f'/api/v1/message/{id_correo}', base=MAILPIT)
    m = json.loads(cuerpo)
    if not m.get('Attachments'):
        return None, None
    a = m['Attachments'][0]
    _, datos = http('GET', f'/api/v1/message/{id_correo}/part/{a["PartID"]}', base=MAILPIT, binario=True)
    return a['FileName'], datos

def cabeceras_correo(id_correo):
    _, cuerpo = http('GET', f'/api/v1/message/{id_correo}/headers', base=MAILPIT)
    return json.loads(cuerpo)

def handshake(url, contenedor=None):
    """El código de la respuesta a la apertura de un WebSocket (101 si se abre)."""
    orden = ['curl', '-s', '-o', '/dev/null', '-w', '%{http_code}', '--http1.1', '--max-time', '3', '-H', 'Connection: Upgrade',
             '-H', 'Upgrade: websocket', '-H', 'Sec-WebSocket-Version: 13', '-H', 'Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ==', url]
    if contenedor:
        return docker(contenedor, *orden).strip()
    return subprocess.run(orden, capture_output=True, text=True).stdout.strip()

if len(sys.argv) < 2 or not os.path.exists(f'{sys.argv[1]}/corte5.json'):
    sys.exit('Falta la carpeta del recorrido con corte5.json (corre antes recorrido_corte5.py)')
recorrido = json.load(open(f'{sys.argv[1]}/corte5.json'))
hoy = datetime.date.today().isoformat()
aux, dire, jefe, admin = token(AUXILIAR), token(DIRECTORA), token(JEFE), token(ADMIN)
cot22 = next(iter(recorrido['generadas']))
c_orden = recorrido['ordenes']['OC-0012']['correlacion']
c_final = recorrido['finalizada']['correlacion']

print('== ms-ordenes, ms-cotizaciones y ms-solicitudes (PostgreSQL)')
fila = sql('ms_ordenes', "SELECT numero, fecha, cotizacion_numero, proveedor, correo_proveedor, pdf_url, estado FROM orden_compra")
ok(fila == [['OC-0012', hoy, 'COT-018', 'Proveedor ABC', 'ventas@proveedorabc.com', 's3://documentos-pdf/ordenes/OC-0012.pdf', 'FINALIZADA']],
   f'una sola orden: OC-0012 de hoy sobre COT-018, Proveedor ABC, con su PDF, FINALIZADA desde V23 (DEC-80) ({fila})')
items = sql('ms_ordenes', "SELECT producto_id, cantidad, unidad, precio FROM item_orden WHERE orden_numero = 'OC-0012' ORDER BY posicion")
ok(items == [['1', '20.000', 'kg', '21250.00'], ['2', '15.000', 'kg', '8000.00']], f'hereda de COT-018 productos, cantidades, unidades y precios (HU-14) ({items})')
sols = sql('ms_ordenes', "SELECT solicitud_numero FROM orden_solicitud WHERE orden_numero = 'OC-0012'")
ok(sols == [['SOL-031']], f'y su solicitud ({sols})')
ok(sql('ms_ordenes', 'SELECT producto_id, nombre FROM producto_nombre ORDER BY 1') == [['1', 'Res'], ['2', 'Arroz']], 'la copia local de los nombres (Res, Arroz)')
estados = dict((r[0], r[1]) for r in sql('ms_cotizaciones', f"SELECT numero, estado FROM cotizacion WHERE numero IN ('COT-018', '{cot22}')"))
ok(estados == {'COT-018': 'CON_ORDEN', cot22: 'OPCIONADA'}, f'ms-cotizaciones: COT-018 CON_ORDEN (recibió orden.generada) y {cot22} OPCIONADA ({estados})')
prov22 = sql('ms_cotizaciones', f"SELECT p.razon_social, i.producto_id, i.precio_proveedor FROM cotizacion c JOIN proveedor p ON p.id = c.proveedor_id "
                                f"JOIN item_cotizacion i ON i.cotizacion_numero = c.numero WHERE c.numero = '{cot22}' ORDER BY i.orden")
ok(prov22 == [['Proveedor B', '1', '22000.00'], ['Proveedor B', '2', '8000.00']], f'{cot22}: Proveedor B, res $22.000 y arroz $8.000 ({prov22})')
sol = dict((r[0], r[1]) for r in sql('ms_solicitudes', "SELECT numero, estado FROM solicitud WHERE numero IN ('SOL-031', 'SOL-032')"))
ok(sol == {'SOL-031': 'COMPLETADA', 'SOL-032': 'EN_TRAMITE'}, f'ms-solicitudes: SOL-031 COMPLETADA y SOL-032 sigue EN_TRAMITE ({sol})')

print('== El PDF de la orden en el almacén de objetos')
guardado = filer('ordenes/OC-0012.pdf')
estado, servido = http('GET', '/api/ordenes-compra/OC-0012/pdf', cabeceras=con(dire), binario=True)
ok(guardado[:5] == b'%PDF-' and guardado.rstrip().endswith(b'%%EOF'), f'OC-0012.pdf está en documentos-pdf/ordenes/ ({len(guardado)} bytes)')
ok(estado == 200 and servido == guardado, 'el gateway sirve los mismos bytes («Ver PDF» de V23, DEC-75)')
bajado = open(recorrido['pdf']['archivo'], 'rb').read()
ok(bajado == guardado, 'y el navegador bajó ese mismo archivo («Descargar»)')
leido = texto_pdf(guardado)
if leido:
    info, texto = leido
    ok(re.search(r'^Pages:\s+1$', info, re.M) is not None, 'pdfinfo lo abre sin errores (1 página)')
    ok(all(t in texto for t in ('ORDEN DE COMPRA', 'OC-0012', 'Proveedor ABC', 'COT-018', 'SOL-031', 'Res', 'Arroz', '$425.000', '$120.000', '$545.000')),
       'dice el número, el proveedor, la cotización, la solicitud, los productos y el total')

print('== Kafka: orden.generada y la ida y vuelta')
ordenes = [e for e in topico('orden.generada') if e.get('numero') == 'OC-0012']
ok(len(ordenes) == 1, f'una sola orden.generada de OC-0012 ({len(ordenes)})')
if ordenes:
    e = ordenes[0]
    ok(e['_clave'] == 'OC-0012' and e['cotizacionNumero'] == 'COT-018' and e['solicitudNumeros'] == ['SOL-031'], 'con la clave OC-0012, COT-018 y SOL-031')
    ok(e['usuario'] == DIRECTORA and e['correlacionId'] == c_orden, 'a nombre de la directora y con la correlación de la petición del navegador')
    ok(e['correoProveedor'] == 'ventas@proveedorabc.com' and e['pdfUrl'] == 's3://documentos-pdf/ordenes/OC-0012.pdf' and float(e['total']) == 545000,
       'con el correo del proveedor, el pdfUrl (claim check) y el total $545.000')
auditoria = [a for a in topico('auditoria.evento') if a.get('correlacionId') == c_orden]
acciones = [a['accion'] for a in auditoria]
for accion, quien in (('Orden OC-0012 generada sobre COT-018', 'ms-ordenes'), ('Cotización COT-018 pasó a CON_ORDEN', 'ms-cotizaciones'),
                      ('Solicitud SOL-031 completada', 'ms-solicitudes'), ('Notificación orden.generada enviada a ventas@proveedorabc.com', 'ms-notificaciones')):
    ok(accion in acciones, f'{quien} lo dejó en la auditoría, en el mismo recorrido: «{accion}»')
ok(any(a['accion'] == 'Orden OC-0012 generada sobre COT-018' and a['usuario'] == DIRECTORA for a in auditoria), 'la generación queda a nombre de la directora (HU-14)')
final = [a for a in topico('auditoria.evento') if a.get('accion') == 'Orden OC-0012 finalizada']
ok(len(final) == 1 and final[0]['usuario'] == DIRECTORA and final[0]['correlacionId'] == c_final,
   f'«Marcar como recibida» (DEC-80) deja una auditoría, a nombre de la directora y en el recorrido del navegador ({len(final)})')

print('== Mailpit: los correos certificados con el PDF')
cotizaciones = {'COT-018': 'ventas@proveedorabc.com', 'COT-019': 'ventas@proveedorc.com', 'COT-020': 'ventas@proveedord.com', cot22: 'ventas@proveedorb.com'}
for numero, para in cotizaciones.items():
    m = correo_de(para, f'Solicitud de cotización {numero}')
    ok(len(m) >= 1, f'{numero}: el proveedor ({para}) recibió la solicitud de cotización')
    if m:
        nombre, datos = adjunto(m[0]['ID'])
        ok(nombre == f'{numero}.pdf' and datos == filer(f'cotizaciones/{numero}.pdf'), f'{numero}: con {nombre} adjunto, idéntico al del almacén')
ok(not [m for m in correos() if 'COT-021' in m['Subject']], 'COT-021 (PENDIENTE_PRECIO) no generó ningún correo')
m = correo_de('ventas@proveedorabc.com', 'Orden de compra OC-0012')
ok(len(m) == 1, f'el Proveedor ABC recibió la orden OC-0012 ({len(m)} correo)')
if m:
    nombre, datos = adjunto(m[0]['ID'])
    ok(nombre == 'OC-0012.pdf' and datos == guardado, 'con OC-0012.pdf adjunto, idéntico al del almacén, al del gateway y al que bajó el navegador')
    cab = cabeceras_correo(m[0]['ID'])
    ok(cab.get('X-Certificado', [''])[0].startswith('SHA-256:'), f'correo certificado: X-Certificado {cab.get("X-Certificado")}')
    ok(re.match(r'^\d{4}-\d\d-\d\dT\d\d:\d\d:\d\d-05:00$', cab.get('X-Sello-Tiempo', [''])[0] or '') is not None,
       f'con estampa cronológica en hora de Bogotá: X-Sello-Tiempo {cab.get("X-Sello-Tiempo")}')
    _, cuerpo = http('GET', f'/api/v1/message/{m[0]["ID"]}', base=MAILPIT)
    texto = json.loads(cuerpo).get('Text', '')
    ok('Orden OC-0012 sobre la cotización COT-018, total $545.000.' in texto,
       f'el cuerpo dice el total en pesos, como la aplicación (DEC-115): {texto.strip()[:120]!r}')
for asunto in ('Solicitud SOL-031 registrada', 'Solicitud SOL-032 registrada', 'Solicitud SOL-031 en trámite', 'Solicitud SOL-032 en trámite',
               'Stock bajo: Res', 'Stock bajo: Pollo', 'Stock bajo: Papa'):
    ok(len(correo_de(JEFE, asunto)) >= 1, f'el jefe recibió «{asunto}»')
envios = sql('ms_notificaciones', "SELECT tipo, count(*) FROM envio WHERE exito GROUP BY tipo ORDER BY tipo")
ok(dict((t, int(n)) for t, n in envios).get('orden.generada') == 1, f'la traza de envíos (ms_notificaciones.envio): {envios}')
c22 = recorrido['generadas'][cot22]['correlacion']
estado, cuerpo = http('GET', f'/api/notificaciones/envios?correlacion={c22}', cabeceras=con(aux))
lista = json.loads(cuerpo) if estado == 200 else []
ok(any(e['tipo'] == 'cotizacion.enviada' and e['exito'] and e['destino'] == 'ventas@proveedorb.com' for e in lista),
   f'V14 (DEC-76): los envíos del recorrido de {cot22} dicen que el correo salió ({estado})')

print('== La alarma de stock bajo (PD-01)')
eventos = [e for e in topico('inventario.stock-bajo')]
ok({'Res', 'Pollo', 'Papa'} <= {e['nombreProducto'] for e in eventos}, f'inventario.stock-bajo de la res, el pollo y la papa ({[e["nombreProducto"] for e in eventos]})')
ok(docker('redis', 'redis-cli', 'PUBSUB', 'NUMSUB', 'alarmas:stock-bajo').split() == ['alarmas:stock-bajo', '1'], 'ms-notificaciones escucha el canal de Redis alarmas:stock-bajo')
interna = 'http://ms-notificaciones:8080/api/notificaciones/alarmas'
ok(handshake(f'{interna}?access_token={jefe}', 'ministerio') == '101', 'WebSocket directo al servicio: el jefe de cocina lo abre (101)')
ok(handshake(f'{interna}?access_token={aux}', 'ministerio') == '403', 'la auxiliar no (403: solo el jefe recibe alarmas)')
ok(handshake(interna, 'ministerio') == '401', 'y sin token, 401')
ok(handshake(f'{BASE}/api/notificaciones/alarmas') == '401', 'por Nginx y el gateway, sin token: 401')
ok(handshake(f'{BASE}/api/notificaciones/alarmas?access_token={jefe}') == '101', 'por Nginx y el gateway, con el token del jefe en la URL: 101')
estado, _ = http('GET', f'/api/inventario/alertas?access_token={jefe}')
ok(estado == 401, f'el token en la URL solo vale para el WebSocket: GET /api/inventario/alertas?access_token=… → {estado}')

print('== Destinatarios (libreta de jefes por sucursal)')
estado, cuerpo = http('GET', '/api/notificaciones/destinatarios?sucursalId=1', cabeceras=con(admin))
ok(estado == 200 and json.loads(cuerpo) == [JEFE], f'Sucursal Centro: {cuerpo}')
estado, _ = http('POST', '/api/notificaciones/destinatarios', {'sucursalId': 2, 'correo': 'jefe.norte@konradgourmet.com'}, con(admin))
_, cuerpo = http('GET', '/api/notificaciones/destinatarios?sucursalId=2', cabeceras=con(admin))
ok(estado == 201 and json.loads(cuerpo) == ['jefe.norte@konradgourmet.com'], f'la administradora registra el jefe de la Sucursal Norte ({estado}, {cuerpo})')
estado, cuerpo = http('POST', '/api/notificaciones/destinatarios', {'sucursalId': 2, 'correo': 'sin-arroba'}, con(admin))
ok(estado == 400 and 'no es válido' in cuerpo, f'un correo mal escrito: {estado} {cuerpo}')

print('== HU-14: reglas de la orden de compra')
estado, cuerpo = http('POST', '/api/ordenes-compra', {'cotizacionNumero': 'COT-018'}, con(dire))
ok(estado == 409 and 'Ya existe una orden de compra para la cotización COT-018' in cuerpo, f'una segunda orden sobre COT-018: {estado} {cuerpo}')
estado, cuerpo = http('POST', '/api/ordenes-compra', {'cotizacionNumero': 'COT-019'}, con(dire))
ok(estado == 409 and 'Solo se pueden generar órdenes de compra sobre cotizaciones opcionadas' in cuerpo, f'sobre COT-019 (RECHAZADA): {estado} {cuerpo}')
estado, cuerpo = http('POST', '/api/ordenes-compra', {'cotizacionNumero': 'COT-999'}, con(dire))
ok(estado == 400 and 'No existe la cotización COT-999' in cuerpo, f'sobre una cotización que no existe: {estado} {cuerpo}')
estado, _ = http('GET', '/api/ordenes-compra/OC-9999/pdf', cabeceras=con(dire))
ok(estado == 404, f'el PDF de una orden que no existe: {estado}')
estado, cuerpo = http('PATCH', '/api/ordenes-compra/OC-0012/finalizar', cabeceras=con(dire))
ok(estado == 409 and 'La orden OC-0012 ya está finalizada' in cuerpo, f'finalizar OC-0012 otra vez (DEC-80): {estado} {cuerpo}')
estado, cuerpo = http('PATCH', '/api/ordenes-compra/OC-9999/finalizar', cabeceras=con(dire))
ok(estado == 404 and 'No existe la orden de compra OC-9999' in cuerpo, f'finalizar una orden que no existe: {estado} {cuerpo}')
ok(len(sql('ms_ordenes', 'SELECT numero FROM orden_compra')) == 1, 'sigue habiendo una sola orden')

print('== Roles en el gateway')
for quien, tok, metodo, ruta, esperado in (('la auxiliar', aux, 'POST', '/api/ordenes-compra', 403), ('el jefe', jefe, 'GET', '/api/ordenes-compra', 403),
                                          ('la auxiliar', aux, 'GET', '/api/ordenes-compra/OC-0012/pdf', 403), ('sin token', None, 'GET', '/api/ordenes-compra', 401),
                                          ('la auxiliar', aux, 'PATCH', '/api/ordenes-compra/OC-0012/finalizar', 403),
                                          ('el jefe', jefe, 'PATCH', '/api/ordenes-compra/OC-0012/finalizar', 403),
                                          ('la directora', dire, 'GET', '/api/notificaciones/destinatarios?sucursalId=1', 403),
                                          ('el jefe', jefe, 'GET', f'/api/notificaciones/envios?correlacion={c22}', 403)):
    estado, _ = http(metodo, ruta, {'cotizacionNumero': 'COT-022'} if metodo == 'POST' else None, con(tok) if tok else {})
    ok(estado == esperado, f'{metodo} {ruta.split("?")[0]} con {quien}: {estado}')

print('== Correo caído: reintento, cola de errores, su reproceso (DEC-81) y el reenvío (HU-12)')
recorrido_fallo, recorrido_reenvio = str(uuid.uuid4()), str(uuid.uuid4())
subprocess.run(['docker', 'pause', f'{PROYECTO}-correo-1'], capture_output=True)
try:
    estado, cuerpo = http('POST', '/api/cotizaciones', {'solicitudes': ['SOL-032'], 'proveedorId': 3, 'fechaLimite': (datetime.date.today() + datetime.timedelta(days=7)).isoformat(),
                                                        'items': [{'productoId': 3, 'producto': 'Papa', 'cantidad': 10, 'unidad': 'kg'}]},
                          {**con(aux), 'X-Correlation-Id': recorrido_fallo})
    nueva = json.loads(cuerpo)['numero'] if estado == 201 else None
    ok(estado == 201 and json.loads(cuerpo)['estado'] == 'ENVIADA', f'con el servidor de correo detenido, la auxiliar envía {nueva} al Proveedor C ({estado})')
    dlq, crudo = [], ''
    for _ in range(40):
        dlq, crudo = cola_de_errores(nueva)
        if dlq: break
        time.sleep(1)
    fallos = sql('ms_notificaciones', f"SELECT count(*) FROM envio WHERE NOT exito AND correlacion_id = '{recorrido_fallo}'")[0][0]
    ok(int(fallos) == 3, f'cada intento fallido queda en la traza (registrarFallo): {fallos} (1 + 2 reintentos)')
    ok(len(dlq) == 1 and dlq[0]['numero'] == nueva and 'kafka_dlt-original-topic:cotizacion.enviada' in crudo,
       f'después de los reintentos, el evento pasó a la cola de errores notificaciones.dlq, con su tópico de origen ({len(dlq)})')
    estado, cuerpo = http('GET', f'/api/notificaciones/envios?correlacion={recorrido_fallo}', cabeceras=con(aux))
    ok(estado == 200 and json.loads(cuerpo) and not any(e['exito'] for e in json.loads(cuerpo)), 'V14 vería «No se pudo enviar» (DEC-76)')
finally:
    subprocess.run(['docker', 'unpause', f'{PROYECTO}-correo-1'], capture_output=True)
if nueva:
    # DEC-81: en el compose local la cola se reprocesa a los 30 s de que el evento llegó a ella
    asunto = f'Solicitud de cotización {nueva}'
    m, inicio = [], time.time()
    for _ in range(90):
        m = correo_de('ventas@proveedorc.com', asunto)
        if m: break
        time.sleep(1)
    ok(len(m) == 1, f'con el correo de vuelta, la cola de errores se reprocesa sola y el Proveedor C recibe {nueva} '
                    f'({len(m)} correo, a los {time.time() - inicio:.0f} s)')
    if m:
        nombre, datos = adjunto(m[0]['ID'])
        ok(nombre == f'{nueva}.pdf' and datos == filer(f'cotizaciones/{nueva}.pdf'), f'con {nombre} adjunto, idéntico al del almacén')
    exitos = sql('ms_notificaciones', f"SELECT count(*) FROM envio WHERE exito AND tipo = 'cotizacion.enviada' AND correlacion_id = '{recorrido_fallo}'")[0][0]
    ok(int(exitos) == 1, f'la traza lo registra en el recorrido original de la auxiliar ({exitos} envío con la correlación {recorrido_fallo[:8]}…)')
    estado, cuerpo = http('GET', f'/api/notificaciones/envios?correlacion={recorrido_fallo}', cabeceras=con(aux))
    ok(estado == 200 and any(e['exito'] for e in json.loads(cuerpo)), 'V14 ya vería «Correo enviado» para ese recorrido (DEC-76)')
    time.sleep(5)
    dlq, crudo = cola_de_errores(nueva)
    ok(len(correo_de('ventas@proveedorc.com', asunto)) == 1 and len(dlq) == 1 and 'kg-reproceso' not in crudo,
       f'una sola vez: no llega otro correo ni el evento vuelve a la cola ({len(dlq)} registro)')
    estado, cuerpo = http('POST', f'/api/cotizaciones/{nueva}/reenvio', cabeceras={**con(aux), 'X-Correlation-Id': recorrido_reenvio})
    ok(estado == 200, f'después, la auxiliar reenvía {nueva} (HU-12) ({estado})')
    for _ in range(30):
        m = correo_de('ventas@proveedorc.com', asunto)
        if len(m) >= 2: break
        time.sleep(1)
    ok(len(m) == 2, f'el Proveedor C recibe {nueva} por segunda vez con el reenvío ({len(m)} correos)')
    if m:
        nombre, datos = adjunto(m[0]['ID'])
        ok(nombre == f'{nueva}.pdf' and datos == filer(f'cotizaciones/{nueva}.pdf'), f'con {nombre} adjunto, idéntico al del almacén')
    _, cuerpo = http('GET', f'/api/notificaciones/envios?correlacion={recorrido_reenvio}', cabeceras=con(aux))
    ok(any(e['exito'] for e in json.loads(cuerpo)), 'y la traza del reenvío dice que salió')

print('\nFALLAS:' if fallas else '\nTodo bien.', *fallas, sep='\n  ')
sys.exit(1 if fallas else 0)
