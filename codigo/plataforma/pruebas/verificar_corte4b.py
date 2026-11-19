#!/usr/bin/env python3
# Revisa lo que dejó recorrido_corte4b.py (corre contra los contenedores con docker exec y contra el gateway en
# http://localhost:8080, sin instalar nada; usa pdftotext y pdfinfo de poppler si están):
#   ms-cotizaciones: COT-018 OPCIONADA, COT-019 RECHAZADA y COT-020 SOSPECHOSA (de dos solicitudes) en PostgreSQL,
#     con el precio del Ministerio y el del proveedor de cada producto; el proveedor de HU-15 inactivo;
#   el PDF: en el bucket documentos-pdf, idéntico al que sirve el gateway y al que bajó el navegador, y legible;
#   el precio de referencia: ms-cotizaciones lo pidió a ms-integracion con el JWT de la auxiliar y la correlación
#     del navegador, el Ministerio respondió una vez por producto y las demás veces Redis;
#   Kafka: cotizacion.enviada (con la clave, la auxiliar, la correlación, las solicitudes, los precios y el pdfUrl),
#     cotizacion.validada (la directora y el estado), y ms-solicitudes la recibió: solicitud.en-tramite y su
#     auditoría en el mismo recorrido; SOL-031 y SOL-032 EN_TRAMITE con su valor sugerido;
#   el Ministerio caído (un producto que no conoce): la cotización queda PENDIENTE_PRECIO sin PDF ni evento;
#   los roles en el gateway.
# Uso: python verificar_corte4b.py <carpeta-de-capturas-del-recorrido>
#      (COMPOSE_PROJECT_NAME elige otra instancia; por omisión, konrad-gourmet)
import datetime, json, os, re, shutil, subprocess, sys, tempfile, time, urllib.error, urllib.request, uuid

PROYECTO = os.environ.get('COMPOSE_PROJECT_NAME', 'konrad-gourmet')
BASE = os.environ.get('BASE', 'http://localhost:8080')
JEFE, AUXILIAR, DIRECTORA = 'carlos.mendoza@konradgourmet.com', 'isabella.castro@konradgourmet.com', 'laura.gomez@konradgourmet.com'
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

def topico(nombre):
    salida = docker('kafka', '/opt/kafka/bin/kafka-console-consumer.sh', '--bootstrap-server', 'localhost:9092', '--topic', nombre,
                    '--from-beginning', '--timeout-ms', '5000', '--property', 'print.key=true', '--property', 'key.separator=§')
    eventos = []
    for l in salida.splitlines():
        if '§{' in l:
            clave, cuerpo = l.split('§', 1)
            e = json.loads(cuerpo); e['_clave'] = clave; eventos.append(e)
    return eventos

def http(metodo, ruta, cuerpo=None, cabeceras=None, binario=False):
    datos = json.dumps(cuerpo).encode() if cuerpo is not None else None
    cab = dict(cabeceras or {})
    if cuerpo is not None: cab['Content-Type'] = 'application/json'
    peticion = urllib.request.Request(BASE + ruta, data=datos, method=metodo, headers=cab)
    try:
        with urllib.request.urlopen(peticion, timeout=20) as r:
            leido = r.read()
            return r.status, leido if binario else leido.decode()
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode()

def token(correo):
    _, cuerpo = http('POST', '/api/auth/token', {'correo': correo, 'clave': 'Konrad2026'})
    return json.loads(cuerpo)['jwt']

def llamadas_ministerio(producto):
    patron = json.dumps({'method': 'POST', 'url': '/precios', 'bodyPatterns': [{'contains': f'<codigoProducto>{producto}</codigoProducto>'}]})
    salida = docker('ministerio', 'curl', '-s', '-X', 'POST', '-d', patron, 'http://127.0.0.1:8080/__admin/requests/count')
    return json.loads(salida)['count']

def texto_pdf(contenido):
    if not shutil.which('pdftotext'):
        return None
    with tempfile.NamedTemporaryFile(suffix='.pdf') as f:
        f.write(contenido); f.flush()
        paginas = subprocess.run(['pdfinfo', f.name], capture_output=True, text=True).stdout
        texto = subprocess.run(['pdftotext', '-layout', f.name, '-'], capture_output=True, text=True).stdout
    return paginas, texto

if len(sys.argv) < 2 or not os.path.exists(f'{sys.argv[1]}/corte4b.json'):
    sys.exit('Falta la carpeta del recorrido con corte4b.json (corre antes recorrido_corte4b.py)')
recorrido = json.load(open(f'{sys.argv[1]}/corte4b.json'))
generadas, validadas = recorrido['generadas'], recorrido['validadas']
ESPERADO = {  # cotización: proveedor, estado, solicitudes, {producto: (cantidad, estándar, proveedor)}
    'COT-018': ('1', 'OPCIONADA', ['SOL-031'], {'1': ('20.000', '20000.00', '21250.00'), '2': ('15.000', '8000.00', '8000.00')}),
    'COT-019': ('3', 'RECHAZADA', ['SOL-031'], {'1': ('20.000', '20000.00', '29000.00'), '2': ('15.000', '8000.00', '8000.00')}),
    'COT-020': ('4', 'SOSPECHOSA', ['SOL-031', 'SOL-032'],
                {'1': ('20.000', '20000.00', '4000.00'), '2': ('15.000', '8000.00', '8000.00'), '3': ('10.000', '3500.00', '1000.00')}),
}
ok(sorted(generadas) == sorted(ESPERADO), f'el recorrido generó {sorted(generadas)}')

print('== ms-cotizaciones (PostgreSQL)')
for numero, (prov, estado, sols, items) in ESPERADO.items():
    fila = sql('ms_cotizaciones', f"SELECT proveedor_id, estado, pdf_url, fecha_envio FROM cotizacion WHERE numero = '{numero}'")
    ok(bool(fila) and fila[0][:3] == [prov, estado, f's3://documentos-pdf/cotizaciones/{numero}.pdf'] and fila[0][3] == datetime.date.today().isoformat(),
       f'{numero}: proveedor {prov}, {estado}, con su PDF y enviada hoy ({fila})')
    s = [r[0] for r in sql('ms_cotizaciones', f"SELECT solicitud_numero FROM cotizacion_solicitud WHERE cotizacion_numero = '{numero}' ORDER BY orden")]
    ok(s == sols, f'{numero}: de {", ".join(sols)} ({s})')
    filas = sql('ms_cotizaciones', f"SELECT producto_id, cantidad, precio_estandar, precio_proveedor FROM item_cotizacion WHERE cotizacion_numero = '{numero}' ORDER BY orden")
    ok({f[0]: tuple(f[1:]) for f in filas} == items, f'{numero}: cantidad, precio del Ministerio y del proveedor por producto ({filas})')
primera = sql('ms_cotizaciones', 'SELECT min(numero) FROM cotizacion')[0][0]
ok(primera == 'COT-018', f'la numeración sigue a la de la demo: la primera es {primera}')
nombres = sql('ms_cotizaciones', 'SELECT producto_id, nombre FROM producto_nombre WHERE producto_id < 99 ORDER BY producto_id')
ok(nombres == [['1', 'Res'], ['2', 'Arroz'], ['3', 'Papa']], f'la copia local de los nombres de productos (DEC-74): {nombres}')
prov = sql('ms_cotizaciones', "SELECT nit, telefono, activo FROM proveedor WHERE razon_social = 'Frutas del Valle'")
ok(prov == [['901555666-7', '6024445566', 'f']], f'HU-15: «Frutas del Valle» registrado, editado e inactivo, no borrado ({prov})')
cats = sql('ms_cotizaciones', "SELECT categoria FROM proveedor_categoria c JOIN proveedor p ON p.id = c.proveedor_id WHERE p.razon_social = 'Frutas del Valle' ORDER BY orden")
ok(cats == [['Frutas'], ['Verduras']], f'con sus categorías ({cats})')

print('== El PDF en el almacén de objetos')
aux, dire, jefe = token(AUXILIAR), token(DIRECTORA), token(JEFE)
for numero in ESPERADO:
    guardado = docker('s3', 'wget', '-qO-', f'http://127.0.0.1:8888/buckets/documentos-pdf/cotizaciones/{numero}.pdf', binario=True)
    estado, servido = http('GET', f'/api/cotizaciones/{numero}/pdf', cabeceras={'Authorization': f'Bearer {dire}'}, binario=True)
    ok(guardado[:5] == b'%PDF-' and guardado.rstrip().endswith(b'%%EOF'), f'{numero}.pdf está en el bucket documentos-pdf ({len(guardado)} bytes, de %PDF- a %%EOF)')
    ok(estado == 200 and servido == guardado, f'{numero}: el gateway sirve los mismos bytes que guardó el almacén')
    leido = texto_pdf(guardado)
    if leido is None:
        print('  (sin pdftotext: no se lee el texto del PDF)')
        continue
    info, texto = leido
    ok(re.search(r'^Pages:\s+1$', info, re.M) is not None, f'{numero}: pdfinfo lo abre sin errores (1 página)')
    nombre_prov = {'1': 'Proveedor ABC', '3': 'Proveedor C', '4': 'Proveedor D'}[ESPERADO[numero][0]]
    ok(all(t in texto for t in (numero, nombre_prov, 'Res', 'Arroz', 'SOLICITUD DE COTIZACIÓN')), f'{numero}: dice el número, {nombre_prov} y los productos')
    ok('20.000' not in texto and '20000' not in texto, f'{numero}: no muestra el precio de referencia del Ministerio')
bajado = recorrido.get('pdf')
if bajado:
    guardado = docker('s3', 'wget', '-qO-', f'http://127.0.0.1:8888/buckets/documentos-pdf/cotizaciones/{bajado["numero"]}.pdf', binario=True)
    ok(open(bajado['archivo'], 'rb').read() == guardado, f'el PDF que descargó el navegador en V15 ({bajado["numero"]}) es idéntico al del almacén')

print('== Precio de referencia: ms-cotizaciones → ms-integracion (Proxy con Redis) → Ministerio')
for producto, precio in (('1', '20000.0'), ('2', '8000.0'), ('3', '3500.0')):
    guardado = docker('redis', 'redis-cli', 'GET', f'precio-referencia:{producto}').strip()
    ok(f'"valor":"{precio}"' in guardado, f'Redis tiene el precio del producto {producto} ({guardado})')
    ok(llamadas_ministerio(producto) == 1, f'el Ministerio respondió una sola vez por el producto {producto}; las demás cotizaciones lo leyeron de Redis')
auditoria = topico('auditoria.evento')
c18 = generadas['COT-018']['correlacion']
pedidos = [a for a in auditoria if a['correlacionId'] == c18 and a['accion'].startswith('Precio sugerido consultado')]
ok(len(pedidos) == 2 and all(a['usuario'] == AUXILIAR for a in pedidos),
   f'ms-integracion registró las 2 consultas de COT-018 a nombre de la auxiliar y con la correlación del navegador: PrecioSugeridoRestAdapter pasa el JWT y X-Correlation-Id ({[a["accion"] for a in pedidos]})')

print('== Kafka: cotizacion.enviada y ms-solicitudes')
enviadas = {e['numero']: e for e in topico('cotizacion.enviada')}
for numero, datos in generadas.items():
    e = enviadas.get(numero)
    ok(e is not None, f'cotizacion.enviada de {numero} está en Kafka')
    if not e:
        continue
    ok(e['_clave'] == numero and e['usuario'] == AUXILIAR and e['correlacionId'] == datos['correlacion'],
       f'{numero}: clave {e["_clave"]}, a nombre de la auxiliar y con la correlación de la petición del navegador')
    ok(e['solicitudNumeros'] == datos['solicitudes'] and e['pdfUrl'] == f's3://documentos-pdf/cotizaciones/{numero}.pdf',
       f'{numero}: con sus solicitudes y solo la dirección del PDF (claim check)')
    ok(float(e['precios']['1']) == 20000 and float(e['precios']['2']) == 8000, f'{numero}: con el precio sugerido de cada producto ({e["precios"]})')
    ok(e['correoProveedor'].startswith('ventas@proveedor'), f'{numero}: con el correo del proveedor para ms-notificaciones ({e["correoProveedor"]})')
    aplicadas = [a for a in auditoria if a['accion'].startswith(f'Cotización {numero} aplicada a la solicitud')]
    ok(len(aplicadas) == len(datos['solicitudes']) and all(a['correlacionId'] == datos['correlacion'] for a in aplicadas),
       f'ms-solicitudes la aplicó a {len(aplicadas)} solicitud(es) en el mismo recorrido')
tramite = {e['numero']: e for e in topico('solicitud.en-tramite')}
for numero, cot in (('SOL-031', 'COT-018'), ('SOL-032', 'COT-020')):
    e = tramite.get(numero)
    ok(e is not None and e['correlacionId'] == generadas[cot]['correlacion'] and e['correoSolicitante'] == JEFE,
       f'solicitud.en-tramite de {numero}: en el recorrido de {cot}, para el jefe que la pidió')
filas = sql('ms_solicitudes', "SELECT numero, estado, valor_sugerido_total FROM solicitud ORDER BY numero")
ok(filas == [['SOL-031', 'EN_TRAMITE', '520000.00'], ['SOL-032', 'EN_TRAMITE', '35000.00']], f'ms_solicitudes: las dos EN_TRAMITE con su valor sugerido ({filas})')
aplicadas = sql('ms_solicitudes', "SELECT solicitud_numero, cotizacion_numero FROM cotizacion_aplicada ORDER BY 1, 2")
ok(aplicadas == [['SOL-031', 'COT-018'], ['SOL-031', 'COT-019'], ['SOL-031', 'COT-020'], ['SOL-032', 'COT-020']],
   f'cada cotización quedó aplicada una vez (idempotencia) ({aplicadas})')

print('== Kafka: cotizacion.validada')
validas = {e['numero']: e for e in topico('cotizacion.validada')}
for numero, datos in validadas.items():
    e = validas.get(numero)
    ok(e is not None and e['estado'] == datos['estado'] and e['usuario'] == DIRECTORA and e['correlacionId'] == datos['correlacion'],
       f'cotizacion.validada de {numero}: {datos["estado"]}, a nombre de la directora y en el recorrido de su validación')
ok(float(validas.get('COT-018', {}).get('valorCotizado', 0)) == 545000, 'COT-018 validada por $545.000 (la cifra de la demo)')

print('== Si el Ministerio no conoce el producto: PENDIENTE_PRECIO')
c = str(uuid.uuid4())
limite = (datetime.date.today() + datetime.timedelta(days=7)).isoformat()
estado, cuerpo = http('POST', '/api/cotizaciones', {'solicitudes': ['SOL-032'], 'proveedorId': 2, 'fechaLimite': limite,
                      'items': [{'productoId': 99, 'producto': 'Producto sin precio', 'cantidad': 1, 'unidad': 'kg'}]},
                      {'Authorization': f'Bearer {aux}', 'X-Correlation-Id': c})
pend = json.loads(cuerpo)
ok(estado == 201 and pend['estado'] == 'PENDIENTE_PRECIO' and not pend['pdf'], f'{pend.get("numero")}: 201, PENDIENTE_PRECIO y sin PDF')
time.sleep(1)
ok(pend['numero'] not in {e['numero'] for e in topico('cotizacion.enviada')}, 'no se publicó cotizacion.enviada')
ok(any(a['correlacionId'] == c and 'PENDIENTE_PRECIO' in a['accion'] for a in topico('auditoria.evento')), 'el fallo quedó en la auditoría con su correlación')
estado, cuerpo = http('POST', f'/api/cotizaciones/{pend["numero"]}/reenvio', cabeceras={'Authorization': f'Bearer {aux}'})
ok(estado == 200 and json.loads(cuerpo)['estado'] == 'PENDIENTE_PRECIO', 'reenviarla vuelve a consultar y, sin precio, sigue pendiente')
estado, _ = http('GET', f'/api/cotizaciones/{pend["numero"]}/pdf', cabeceras={'Authorization': f'Bearer {aux}'})
ok(estado == 409, f'pedir su PDF responde 409 ({estado})')

print('== Roles en el gateway')
for quien, tok, metodo, ruta, esperado in (('el jefe', jefe, 'GET', '/api/cotizaciones', 403), ('la auxiliar', aux, 'POST', '/api/cotizaciones/COT-018/validacion', 403),
                                          ('la directora', dire, 'POST', '/api/proveedores', 403), ('sin token', None, 'GET', '/api/proveedores', 401)):
    estado, _ = http(metodo, ruta, {} if metodo == 'POST' else None, {'Authorization': f'Bearer {tok}'} if tok else {})
    ok(estado == esperado, f'{metodo} {ruta} con {quien}: {estado}')

print('\nFALLAS:' if fallas else '\nTodo bien.', *fallas, sep='\n  ')
sys.exit(1 if fallas else 0)
