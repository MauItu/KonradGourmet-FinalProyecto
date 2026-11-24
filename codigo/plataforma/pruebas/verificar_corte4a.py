#!/usr/bin/env python3
# Revisa lo que dejó recorrido_corte4a.py y prueba ms-integracion (corre contra los contenedores con
# docker exec y contra el gateway en http://localhost:8080, sin instalar nada):
#   ms-solicitudes: las solicitudes a nombre del jefe, con sus productos, en PostgreSQL;
#   Kafka: solicitud.registrada con el correo del jefe y la correlación de la petición del navegador, su
#     auditoría y la de ms-inventario (que la recibió) en el mismo recorrido, y la alerta atendida en su base;
#   ms-integracion: el precio sale del Ministerio la primera vez y de Redis la segunda (WireMock cuenta las
#     llamadas), el SOAP responde a través del gateway (con token; sin token, 401) y el REST interno no se publica.
# Uso: python verificar_corte4a.py <carpeta-de-capturas-del-recorrido>
#      (COMPOSE_PROJECT_NAME elige otra instancia; por omisión, konrad-gourmet)
import json, os, re, subprocess, sys, time, urllib.error, urllib.request, uuid

PROYECTO = os.environ.get('COMPOSE_PROJECT_NAME', 'konrad-gourmet')
BASE = os.environ.get('BASE', 'http://localhost:8080')
JEFE, AUXILIAR = 'carlos.mendoza@konradgourmet.com', 'isabella.castro@konradgourmet.com'
fallas = []

def ok(cond, texto):
    print(('  ✓ ' if cond else '  ✗ ') + texto)
    if not cond:
        fallas.append(texto)

def docker(contenedor, *orden):
    r = subprocess.run(['docker', 'exec', f'{PROYECTO}-{contenedor}-1', *orden], capture_output=True, text=True)
    return r.stdout

def sql(base, consulta):
    salida = docker('postgres', 'psql', '-U', 'postgres', '-d', base, '-tAF', '|', '-c', consulta)
    return [l.split('|') for l in salida.strip().splitlines() if l]

def topico(nombre):
    """Los eventos del tópico, cada uno con su clave de Kafka en «_clave»."""
    salida = docker('kafka', '/opt/kafka/bin/kafka-console-consumer.sh', '--bootstrap-server', 'localhost:9092', '--topic', nombre,
                    '--from-beginning', '--timeout-ms', '5000', '--property', 'print.key=true', '--property', 'key.separator=§')
    eventos = []
    for l in salida.splitlines():
        if '§{' in l:
            clave, cuerpo = l.split('§', 1)
            e = json.loads(cuerpo); e['_clave'] = clave; eventos.append(e)
    return eventos

def http(metodo, ruta, cuerpo=None, cabeceras=None):
    datos = cuerpo.encode() if isinstance(cuerpo, str) else (json.dumps(cuerpo).encode() if cuerpo is not None else None)
    peticion = urllib.request.Request(BASE + ruta, data=datos, method=metodo, headers=cabeceras or {})
    inicio = time.perf_counter()
    try:
        with urllib.request.urlopen(peticion, timeout=20) as r:
            return r.status, r.read().decode(), time.perf_counter() - inicio
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode(), time.perf_counter() - inicio

def token(correo):
    _, cuerpo, _ = http('POST', '/api/auth/token', {'correo': correo, 'clave': 'Konrad2026'}, {'Content-Type': 'application/json'})
    return json.loads(cuerpo)['jwt']

def soap(tok, producto, correlacion=None):
    sobre = ('<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ab="http://abastecimiento.konradgourmet.com/">'
             f'<soap:Body><ab:consultarPrecioSugerido><productoId>{producto}</productoId></ab:consultarPrecioSugerido></soap:Body></soap:Envelope>')
    cab = {'Content-Type': 'text/xml; charset=utf-8', 'SOAPAction': '""'}
    if tok: cab['Authorization'] = f'Bearer {tok}'
    if correlacion: cab['X-Correlation-Id'] = correlacion
    return http('POST', '/api/integracion/abastecimiento', sobre, cab)

def llamadas_ministerio(producto):
    patron = json.dumps({'method': 'POST', 'url': '/precios', 'bodyPatterns': [{'contains': f'<codigoProducto>{producto}</codigoProducto>'}]})
    salida = docker('ministerio', 'curl', '-s', '-X', 'POST', '-d', patron, 'http://127.0.0.1:8080/__admin/requests/count')
    return json.loads(salida)['count']

recorrido = {}
if len(sys.argv) > 1 and os.path.exists(f'{sys.argv[1]}/corte4a.json'):
    recorrido = json.load(open(f'{sys.argv[1]}/corte4a.json'))
else:
    print('(sin corte4a.json del recorrido: no se compara la correlación con la del navegador)')

print('== ms-solicitudes (PostgreSQL)')
id_jefe = sql('ms_identidad', f"SELECT id FROM usuario WHERE correo = '{JEFE}'")[0][0]
for tag, esperados in (('escritorio', [['1', '20.000', 'Marca X', 'kg'], ['2', '15.000', '', 'kg']]), ('movil', [['3', '10.000', '', 'kg']])):
    numero = recorrido.get(tag, {}).get('numero')
    if not numero:
        continue
    fila = sql('ms_solicitudes', f"SELECT solicitante_id, correo_solicitante, sucursal_id, estado FROM solicitud WHERE numero = '{numero}'")
    ok(fila == [[id_jefe, JEFE, '1', 'PENDIENTE']], f'{numero}: a nombre del jefe (id {id_jefe}), Sucursal Centro, PENDIENTE ({fila})')
    items = sql('ms_solicitudes', f"SELECT producto_id, cantidad, coalesce(marca, ''), unidad FROM item_solicitud WHERE solicitud_numero = '{numero}' ORDER BY orden")
    ok(items == esperados, f'{numero}: sus productos ({items})')
primera = sql('ms_solicitudes', 'SELECT min(numero) FROM solicitud')[0][0]
ok(primera == 'SOL-031', f'la numeración sigue a la de la demo: la primera es {primera}')

print('== Kafka: solicitud.registrada, su auditoría y ms-inventario')
auditoria = topico('auditoria.evento')
registradas = {e['numero']: e for e in topico('solicitud.registrada')}
for tag, datos in recorrido.items():
    numero, correlacion = datos['numero'], datos['correlacion']
    e = registradas.get(numero)
    ok(e is not None, f'solicitud.registrada de {numero} está en Kafka')
    if not e:
        continue
    ok(e['_clave'] == numero, f'con la solicitud como clave de Kafka ({e["_clave"]})')
    ok(e['usuario'] == JEFE and e['correoSolicitante'] == JEFE, f'lleva el correo del jefe ({e["usuario"]})')
    ok(e['correlacionId'] == correlacion, f'y la correlación de la petición del navegador ({correlacion})')
    a = [x for x in auditoria if x['accion'] == f'Solicitud {numero} registrada']
    ok(bool(a) and a[0]['correlacionId'] == correlacion and a[0]['usuario'] == JEFE, 'su auditoría: mismo recorrido, a nombre del jefe')
    inv = [x for x in auditoria if x['correlacionId'] == correlacion and x['accion'].startswith('Alertas atendidas')]
    ok(bool(inv), f'ms-inventario la recibió: «{inv[0]["accion"] if inv else "?"}» en el mismo recorrido')
atendidas = sql('ms_inventario', 'SELECT producto_id, alerta_atendida FROM insumo WHERE sucursal_id = 1 ORDER BY producto_id')
ok(['1', 't'] in atendidas and ['2', 't'] in atendidas and ['3', 't'] in atendidas,
   f'ms_inventario: las alertas de la res, el arroz y la papa quedaron atendidas ({atendidas})')

print('== ms-integracion: precio de referencia con la caché en Redis')
aux = token(AUXILIAR)
PRODUCTO = 4  # pollo: $14.000 por kg en el Ministerio simulado
docker('redis', 'redis-cli', 'DEL', f'precio-referencia:{PRODUCTO}')
antes = llamadas_ministerio(PRODUCTO)
c1, c2 = str(uuid.uuid4()), str(uuid.uuid4())
estado, cuerpo, t1 = soap(aux, PRODUCTO, c1)
ok(estado == 200 and '<precioSugerido>14000.0</precioSugerido>' in cuerpo, f'SOAP por el gateway: el pollo vale $14.000 ({estado}, {t1 * 1000:.0f} ms)')
ok(llamadas_ministerio(PRODUCTO) == antes + 1, 'la 1.ª consulta llamó al Ministerio (1 llamada)')
guardado = docker('redis', 'redis-cli', 'GET', f'precio-referencia:{PRODUCTO}').strip()
vence = int(docker('redis', 'redis-cli', 'TTL', f'precio-referencia:{PRODUCTO}').strip() or -2)
ok('"valor":"14000.0"' in guardado and f'"productoId":{PRODUCTO}' in guardado, f'Redis guardó el precio ({guardado})')
ok(86000 < vence <= 86400, f'y vence en un día ({vence} s)')
estado, cuerpo, t2 = soap(aux, PRODUCTO, c2)
ok(estado == 200 and '<precioSugerido>14000.0</precioSugerido>' in cuerpo, f'la 2.ª consulta responde lo mismo ({t2 * 1000:.0f} ms)')
ok(llamadas_ministerio(PRODUCTO) == antes + 1, 'sin llamar al Ministerio: la respondió Redis')
ok(t2 < t1, f'y más rápido que la primera ({t2 * 1000:.0f} ms < {t1 * 1000:.0f} ms; el Ministerio tarda 300 ms)')
time.sleep(1)
auditoria = topico('auditoria.evento')
for c in (c1, c2):
    a = [x for x in auditoria if x['correlacionId'] == c]
    ok(bool(a) and a[0]['usuario'] == AUXILIAR and a[0]['accion'] == f'Precio sugerido consultado: producto {PRODUCTO}',
       f'auditoría de la consulta a nombre de la auxiliar, con su correlación ({a[0]["accion"] if a else "?"})')

print('== ms-integracion: el SOAP a través del gateway')
estado, wsdl, _ = http('GET', '/api/integracion/abastecimiento?wsdl', cabeceras={'Authorization': f'Bearer {aux}'})
ok(estado == 200 and 'consultarPrecioSugerido' in wsdl, 'el WSDL se publica en /api/integracion/abastecimiento?wsdl')
ok('location="http://localhost:8080/api/integracion/abastecimiento"' in wsdl, 'y anuncia la dirección del gateway')
estado, _, _ = soap(None, PRODUCTO)
ok(estado == 401, f'sin token el gateway responde 401 ({estado})')
estado, cuerpo, _ = soap(aux, 99)
ok(estado == 500 and 'No hay precio estándar disponible para el producto 99.' in cuerpo, 'un producto que el Ministerio no conoce llega como SOAP Fault')
rutas = json.loads(http('GET', '/plataforma/rutas')[1])
ok(not any('/interno' in json.dumps(r) for r in rutas), 'el REST interno (/interno/precios) no está en las rutas del gateway')
dentro = docker('api-gateway', 'wget', '-qO-', '--header', f'Authorization: Bearer {aux}', 'http://ms-integracion:8080/interno/precios/2')
ok(dentro.strip() == '8000.0', f'dentro de la red, PrecioRestController responde el arroz a $8.000 ({dentro.strip()})')

print('\nFALLAS:' if fallas else '\nTodo bien.', *fallas, sep='\n  ')
sys.exit(1 if fallas else 0)
