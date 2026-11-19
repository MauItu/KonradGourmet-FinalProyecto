#!/usr/bin/env python3
# Revisa lo que dejó recorrido_corte6a.py (corre contra los contenedores con docker exec y contra el gateway en
# http://localhost:8080, sin instalar nada):
#   la auditoría de todos los servicios en ms_administracion: cada auditoria.evento de los cortes anteriores, una
#     vez, con su usuario y la correlación de su recorrido (DEC-64), y las acciones propias de ms-administracion
#     (n_bitacora) a nombre de la administradora, con la correlación del navegador y sin pasar por Kafka; un evento
#     repetido no se guarda dos veces;
#   la API de auditoría: rango obligatorio y ordenado (PD-07), y sus roles;
#   los parámetros y el tema: lo que guardó el navegador, la lectura con cualquier rol y sin token (DEC-86), que la
#     SPA no se redesplegó, el cambio que se ve enseguida y la caché de cada réplica que se refresca de la base
#     (n_parametros);
#   Elasticsearch (DEC-84): el índice logs-errores, el error real del recorrido con su correlación, la consulta por
#     servicio, y que con Elasticsearch detenido la ventana avisa (503) y ese error también llega después;
#   el alta de Sofía en ms-identidad (hash BCrypt, auditoría) y su registro en la libreta de destinatarios.
# Uso: python verificar_corte6a.py <carpeta-de-capturas-del-recorrido>
#      (COMPOSE_PROJECT_NAME elige otra instancia; por omisión, konrad-gourmet)
import datetime, json, os, subprocess, sys, time, urllib.request, uuid

PROYECTO = os.environ.get('COMPOSE_PROJECT_NAME', 'konrad-gourmet')
BASE = os.environ.get('BASE', 'http://localhost:8080')
CARPETA = sys.argv[1]
ADMIN, MESERO, JEFE = 'paula.herrera@konradgourmet.com', 'isabella.torres@konradgourmet.com', 'carlos.mendoza@konradgourmet.com'
DIRECTORA, SOFIA = 'laura.gomez@konradgourmet.com', 'sofia.ramirez@konradgourmet.com'
fallas = []
registro = json.load(open(f'{CARPETA}/corte6a.json'))
corte5 = json.load(open(f'{CARPETA}/corte5.json')) if os.path.exists(f'{CARPETA}/corte5.json') else {}

def ok(cond, texto):
    print(('  ✓ ' if cond else '  ✗ ') + texto)
    if not cond:
        fallas.append(texto)

def docker(contenedor, *orden, entrada=None):
    r = subprocess.run(['docker', 'exec', *(['-i'] if entrada else []), f'{PROYECTO}-{contenedor}-1', *orden],
                       capture_output=True, input=entrada.encode() if entrada else None)
    return r.stdout.decode('utf-8', errors='replace')

def sql(base, consulta):
    salida = docker('postgres', 'psql', '-U', 'postgres', '-d', base, '-tAF', '|', '-c', consulta)
    return [l.split('|') for l in salida.strip().splitlines() if l]

def topico(nombre):
    salida = docker('kafka', '/opt/kafka/bin/kafka-console-consumer.sh', '--bootstrap-server', 'localhost:9092', '--topic', nombre,
                    '--from-beginning', '--timeout-ms', '6000', '--property', 'print.key=true', '--property', 'key.separator=§')
    eventos = []
    for l in salida.splitlines():
        if '§{' in l:
            clave, cuerpo = l.split('§', 1)
            e = json.loads(cuerpo); e['_clave'] = clave; e['_crudo'] = cuerpo; eventos.append(e)
    return eventos

def elasticsearch(metodo, ruta, cuerpo=None):
    orden = ['curl', '-s', '-X', metodo, f'http://127.0.0.1:9200{ruta}', '-H', 'Content-Type: application/json']
    if cuerpo is not None:
        orden += ['-d', json.dumps(cuerpo)]
    salida = docker('elasticsearch', *orden)
    return json.loads(salida) if salida.strip().startswith('{') else {}

def http(metodo, ruta, cuerpo=None, cabeceras=None):
    datos = json.dumps(cuerpo).encode() if cuerpo is not None else None
    cab = dict(cabeceras or {})
    if cuerpo is not None: cab['Content-Type'] = 'application/json'
    peticion = urllib.request.Request(BASE + ruta, data=datos, method=metodo, headers=cab)
    try:
        with urllib.request.urlopen(peticion, timeout=40) as r:
            return r.status, json.loads(r.read() or b'null'), dict(r.headers)
    except urllib.error.HTTPError as e:
        cuerpo = e.read()
        try: cuerpo = json.loads(cuerpo or b'null')
        except ValueError: cuerpo = None
        return e.code, cuerpo, dict(e.headers)

def token(correo, clave='Konrad2026'):
    return http('POST', '/api/auth/token', {'correo': correo, 'clave': clave})[1]['jwt']

def con(tok):
    return {'Authorization': f'Bearer {tok}'}

def directo(ruta, tok=None):
    """Directo a ms-administracion, sin el gateway (desde el contenedor del Ministerio, que tiene curl)."""
    orden = ['curl', '-s', '-o', '/dev/null', '-w', '%{http_code}', '--max-time', '5', f'http://ms-administracion:8080{ruta}']
    if tok: orden[1:1] = ['-H', f'Authorization: Bearer {tok}']
    return docker('ministerio', *orden)

adm, mes, jefe = token(ADMIN), token(MESERO), token(JEFE)
hoy = datetime.date.today().isoformat()

print('== Auditoría de todos los servicios (AuditoriaListener, DEC-64)')
def unicos(evs):
    return {(e['accion'], e['usuario'], e['correlacionId'], e['ocurridoEn'][:23]) for e in evs}

# la bitácora y el tópico se leen hasta que coinciden: pueden estar llegando eventos (por ejemplo, el reproceso de la cola)
for _ in range(10):
    eventos = topico('auditoria.evento')
    filas = sql('ms_administracion', "SELECT accion, usuario, fecha, hora, coalesce(correlacion_id::text, '') FROM auditoria")
    propias = [f for f in filas if f[0].startswith('Parámetro ')]
    de_kafka = [f for f in filas if not f[0].startswith('Parámetro ')]
    if len(de_kafka) == len(unicos(eventos)): break
    time.sleep(2)
ok(len(eventos) > 50, f'auditoria.evento tiene la auditoría de los cortes 3 a 6a ({len(eventos)} eventos)')
indice = {(f[0], f[1], f[4]): f for f in de_kafka}
faltan = [e for e in eventos if (e['accion'], e['usuario'], e['correlacionId']) not in indice]
ok(not faltan, f'cada evento está en la bitácora con su acción, su usuario y su correlación ({len(eventos) - len(faltan)} de {len(eventos)}; '
               f'faltan {[(e["accion"], e["usuario"]) for e in faltan[:3]]})')
ok(len(de_kafka) == len(unicos(eventos)),
   f'y una sola vez cada uno ({len(de_kafka)} filas)')
pedido = [f for f in de_kafka if f[0].startswith('Pedido PED-0251 registrado')]
ok(pedido and pedido[0][1] == MESERO, f'el pedido PED-0251 del corte 3 está a nombre del mesero ({pedido[0][1] if pedido else "—"})')
ev = [e for e in eventos if e['accion'].startswith('Pedido PED-0251 registrado')]
ok(pedido and ev and pedido[0][4] == ev[0]['correlacionId'], 'con la correlación de su recorrido')
fin = [f for f in de_kafka if f[0] == 'Orden OC-0012 finalizada']
correlacion_fin = corte5.get('finalizada', {}).get('correlacion')
ok(fin and fin[0][1] == DIRECTORA and (not correlacion_fin or fin[0][4] == correlacion_fin),
   f'«Orden OC-0012 finalizada» (corte 5) a nombre de la directora y con la correlación del navegador ({fin[0][4][:8] if fin else "—"}…)')
usuarios_distintos = sorted({f[1] for f in de_kafka})
ok({MESERO, JEFE, DIRECTORA, 'isabella.castro@konradgourmet.com', ADMIN} <= set(usuarios_distintos),
   f'con los usuarios de todos los roles ({len(usuarios_distintos)} distintos)')

print('== Las acciones propias de ms-administracion (n_bitacora)')
ok(not any(e['accion'].startswith('Parámetro ') for e in eventos), 'los cambios de parámetros no pasan por Kafka: se escriben directo en la bitácora')
cambios = [p for p in registro['parametros'] if p['estado'] == 200]
ok(len(cambios) >= 7, f'el navegador cambió {len(cambios)} parámetros (propina, nombre, color y logo, y el tema de siempre)')
por_correlacion = {f[4]: f for f in propias}
sin_fila = [p for p in cambios if p['correlacion'] not in por_correlacion]
ok(not sin_fila, f'cada cambio está en la bitácora con la correlación de su petición ({len(cambios) - len(sin_fila)} de {len(cambios)})')
ok(all(por_correlacion[p['correlacion']][1] == ADMIN and por_correlacion[p['correlacion']][0] == f'Parámetro {p["clave"]} actualizado'
       for p in cambios if p['correlacion'] in por_correlacion), 'a nombre de la administradora (el núcleo pone «sistema» y el adaptador, su correo)')
ok(not [f for f in propias if f[1] in ('sistema', 'administrador')], 'ninguna acción propia quedó como «sistema» o «administrador»')

print('== Un evento repetido no se guarda dos veces')
original = next(e for e in eventos if e['accion'].startswith('Pedido PED-0251 registrado'))
antes = sql('ms_administracion', f"SELECT count(*) FROM auditoria WHERE correlacion_id = '{original['correlacionId']}' AND accion = '{original['accion']}'")[0][0]
docker('kafka', '/opt/kafka/bin/kafka-console-producer.sh', '--bootstrap-server', 'localhost:9092', '--topic', 'auditoria.evento',
       '--property', 'parse.key=true', '--property', 'key.separator=#K#', '--property', 'parse.headers=true',
       '--property', 'headers.delimiter=#H#', '--property', 'headers.key.separator=:',
       entrada=f"kg-evento:co.edu.konradlorenz.compartido.eventos.AuditoriaEvent#H#{original['_clave']}#K#{original['_crudo']}\n")
time.sleep(6)
despues = sql('ms_administracion', f"SELECT count(*) FROM auditoria WHERE correlacion_id = '{original['correlacionId']}' AND accion = '{original['accion']}'")[0][0]
ok(len(topico('auditoria.evento')) >= len(eventos) + 1 and antes == despues == '1',
   f'se publicó otra vez el evento del pedido y la bitácora sigue con una fila ({antes} → {despues})')

print('== La API de auditoría (HU-21, PD-07)')
estado, lista, _ = http('GET', f'/api/admin/auditoria?desde={(datetime.date.today() - datetime.timedelta(days=7)).isoformat()}&hasta={hoy}', cabeceras=con(adm))
ok(estado == 200 and len(lista) == len(sql('ms_administracion', f"SELECT 1 FROM auditoria WHERE fecha BETWEEN current_date - 7 AND current_date")),
   f'los últimos 7 días: {estado}, {len(lista or [])} movimientos')
ok(lista == sorted(lista, key=lambda m: (m['fecha'], m['hora']), reverse=True), 'lo más reciente primero')
ok(set(lista[0]) == {'accion', 'usuario', 'fecha', 'hora'} and len(lista[0]['hora']) == 8, f'con acción, usuario, fecha y hora: {lista[0]}')
estado, cuerpo, _ = http('GET', '/api/admin/auditoria', cabeceras=con(adm))
ok(estado == 400 and cuerpo['mensaje'] == 'Seleccione la fecha inicial y la fecha final.', f'sin rango: {estado} «{cuerpo and cuerpo["mensaje"]}»')
estado, cuerpo, _ = http('GET', f'/api/admin/auditoria?desde={hoy}&hasta=2020-01-01', cabeceras=con(adm))
ok(estado == 400 and cuerpo['mensaje'] == 'La fecha inicial no puede ser posterior a la fecha final.', f'rango invertido: {estado}')
estado, cuerpo, _ = http('GET', '/api/admin/auditoria?desde=2026-13-01&hasta=2026-11-01', cabeceras=con(adm))
ok(estado == 400 and cuerpo['mensaje'] == 'La fecha no es válida.', f'fecha mal escrita: {estado}')
for quien, tok in (('el mesero', mes), ('el jefe', jefe)):
    estado, _, _ = http('GET', f'/api/admin/auditoria?desde={hoy}&hasta={hoy}', cabeceras=con(tok))
    ok(estado == 403, f'{quien} no ve la auditoría ({estado})')
ok(directo(f'/api/admin/auditoria?desde={hoy}&hasta={hoy}', mes) == '403' and directo(f'/api/admin/auditoria?desde={hoy}&hasta={hoy}') == '401',
   'directo al servicio: el mesero 403 y sin token 401')

print('== Parámetros y tema (HU-20, DEC-85, DEC-86)')
param = dict((f[0], f[1]) for f in sql('ms_administracion', 'SELECT clave, valor FROM parametro'))
ok(param.get('general.propina-sugerida') == '10 %', f'general.propina-sugerida = {param.get("general.propina-sugerida")!r}')
ok(param.get('tema.color-primario') == '#F28705' and param.get('tema.nombre') == 'Konrad Gourmet' and param.get('tema.logo') == '/assets/logo/logo.png',
   'el recorrido dejó el tema de siempre')
logo = [p for p in cambios if p['clave'] == 'tema.logo' and p['correlacion'] in por_correlacion]
ok(len(logo) == 2, f'el logo se cambió y se restauró ({len(logo)} cambios del recorrido en la bitácora)')
estado, todos, _ = http('GET', '/api/admin/parametros', cabeceras=con(adm))
ok(estado == 200 and todos == param, f'GET /api/admin/parametros devuelve los {len(todos or {})} parámetros de la base')
for quien, tok in (('el mesero', mes), ('la directora', token(DIRECTORA)), ('el director comercial', token('andres.rojas@konradgourmet.com'))):
    estado, tema, _ = http('GET', '/api/admin/parametros/tema', cabeceras=con(tok))
    ok(estado == 200 and set(tema) == {'tema.nombre', 'tema.color-primario', 'tema.logo'}, f'{quien} lee el tema, solo las claves tema.* ({estado})')
estado, _, _ = http('GET', '/api/admin/parametros/tema')
ok(estado == 401 and directo('/api/admin/parametros/tema') == '401', 'sin token no se lee: 401 en el gateway y en el servicio (DEC-86)')
estado, _, _ = http('GET', '/api/admin/parametros', cabeceras=con(mes))
ok(estado == 403, f'el mesero no lista los parámetros ({estado})')
estado, _, _ = http('PUT', '/api/admin/parametros/tema.nombre', {'valor': 'Otro'}, con(jefe))
ok(estado == 403, f'ni el jefe los cambia ({estado})')
web = subprocess.run(['docker', 'inspect', '-f', '{{.State.StartedAt}}', f'{PROYECTO}-web-1'], capture_output=True, text=True).stdout.strip()
color = [p['correlacion'] for p in cambios if p['clave'] == 'tema.color-primario']
primer_cambio = sql('ms_administracion', f"SELECT fecha + hora FROM auditoria WHERE correlacion_id = '{color[0] if color else uuid.uuid4()}'")[0][0]
arranque_web = datetime.datetime.fromisoformat(web[:19]).replace(tzinfo=datetime.timezone.utc)
cambio = datetime.datetime.fromisoformat(primer_cambio[:19]).replace(tzinfo=datetime.timezone(datetime.timedelta(hours=-5)))
ok(arranque_web < cambio, f'la SPA no se redesplegó: el contenedor web arrancó antes del cambio de color ({arranque_web:%H:%M:%S} UTC < {cambio:%H:%M:%S} Bogotá)')
c = str(uuid.uuid4())
estado, cuerpo, cab = http('PUT', '/api/admin/parametros/tema.color-primario', {'valor': '#2F6FD6'}, {**con(adm), 'X-Correlation-Id': c})
_, tema, _ = http('GET', '/api/admin/parametros/tema', cabeceras=con(mes))
ok(estado == 200 and cuerpo['mensaje'] == 'Parámetro actualizado.' and tema['tema.color-primario'] == '#2F6FD6',
   'un cambio de la administradora lo lee el mesero enseguida (la réplica que lo guarda actualiza su caché)')
estado, cuerpo, _ = http('PUT', '/api/admin/parametros/tema.color-primario', {'valor': 'azul'}, con(adm))
ok(estado == 400 and cuerpo['mensaje'].startswith('El color debe tener el formato #RRGGBB'), f'un color mal escrito: {estado}')
http('PUT', '/api/admin/parametros/tema.color-primario', {'valor': '#F28705'}, con(adm))
# Un cambio hecho directo en la base (como el que guarda otra réplica) no se ve enseguida: se lee la caché. Si el
# refresco de cada 10 s cae justo entre el UPDATE y la lectura sí se ve, así que se intenta hasta tres veces.
anterior = 'Konrad Gourmet'
for intento in range(1, 4):
    nuevo = f'Konrad Gourmet Norte {intento}'
    sql('ms_administracion', f"UPDATE parametro SET valor = '{nuevo}' WHERE clave = 'tema.nombre'")
    _, tema, _ = http('GET', '/api/admin/parametros/tema', cabeceras=con(mes))
    if tema['tema.nombre'] == anterior: break
    anterior = nuevo
ok(tema['tema.nombre'] == anterior, f'un cambio hecho directo en la base (como el de otra réplica) no se ve enseguida: se lee la caché (intento {intento})')
inicio = time.time()
for _ in range(30):
    _, tema, _ = http('GET', '/api/admin/parametros/tema', cabeceras=con(mes))
    if tema['tema.nombre'] == nuevo: break
    time.sleep(1)
ok(tema['tema.nombre'] == nuevo, f'y llega cuando la réplica refresca su caché de la base (a los {time.time() - inicio:.0f} s; 10 s en local, 1 min en el diseño)')
sql('ms_administracion', "UPDATE parametro SET valor = 'Konrad Gourmet' WHERE clave = 'tema.nombre'")
time.sleep(12)
_, tema, _ = http('GET', '/api/admin/parametros/tema', cabeceras=con(mes))
ok(tema['tema.nombre'] == 'Konrad Gourmet', 'queda el nombre de siempre')

print('== Elasticsearch y los logs de errores (HU-22, DEC-84)')
mapeo = elasticsearch('GET', '/logs-errores/_mapping')
campos = mapeo.get('logs-errores', {}).get('mappings', {}).get('properties', {})
ok(campos.get('servicio', {}).get('type') == 'keyword' and campos.get('fechaHora', {}).get('type') == 'date',
   f'el índice logs-errores existe con su mapeo (servicio keyword, fechaHora date)')
ok(subprocess.run(['curl', '-s', '--max-time', '2', 'http://localhost:9200'], capture_output=True).returncode != 0,
   'Elasticsearch no se publica en el equipo: solo lo ven los servicios')
error = registro.get('error') or {}
docs = elasticsearch('POST', '/logs-errores/_search', {'size': 50, 'query': {'term': {'correlacionId': error.get('correlacion', '-')}}}).get('hits', {}).get('hits', [])
ok(docs and all(d['_source']['servicio'] == 'ms-notificaciones' and d['_source']['nivel'] == 'ERROR' for d in docs),
   f'el error real del recorrido (el correo caído al reenviar {error.get("cotizacion")}) está en Elasticsearch con la correlación del reenvío ({len(docs)} documentos)')
if docs:
    d = docs[0]['_source']
    print('     ', d['mensaje'][:160])
    ok(d['fechaHora'].endswith('-05:00') and d.get('traza') and d.get('logger'), 'con la fecha y hora de Bogotá, el logger y la traza')
estado, lista, _ = http('GET', '/api/admin/logs?servicio=ms-notificaciones', cabeceras=con(adm))
ok(estado == 200 and docs and any(m['mensaje'] == docs[0]['_source']['mensaje'] for m in lista), f'y GET /api/admin/logs?servicio=ms-notificaciones lo devuelve ({len(lista or [])} errores)')
ok(lista and all(m['servicio'] == 'ms-notificaciones' for m in lista) and lista == sorted(lista, key=lambda m: m['fechaHora'], reverse=True),
   'solo de ese servicio y lo más reciente primero')
estado, todos_logs, _ = http('GET', '/api/admin/logs', cabeceras=con(adm))
servicios = sorted({m['servicio'] for m in todos_logs or []})
ok(estado == 200 and len(todos_logs) >= len(lista), f'sin servicio salen todos ({len(todos_logs or [])} errores de {servicios})')
estado, vacio, _ = http('GET', '/api/admin/logs?servicio=ms-analitica', cabeceras=con(adm))
ok(estado == 200 and vacio == [], 'un servicio sin errores: lista vacía')
estado, _, _ = http('GET', '/api/admin/logs', cabeceras=con(mes))
ok(estado == 403, f'el mesero no ve los logs ({estado})')
subprocess.run(['docker', 'pause', f'{PROYECTO}-elasticsearch-1'], capture_output=True)
try:
    estado, cuerpo, _ = http('GET', '/api/admin/logs', cabeceras=con(adm))
    ok(estado == 503 and cuerpo['mensaje'] == 'No se pudieron consultar los logs de errores. Intente de nuevo en unos minutos.',
       f'con Elasticsearch en pausa, la ventana avisa: {estado} «{cuerpo and cuerpo.get("mensaje")}»')
    estado, _, _ = http('GET', '/api/admin/parametros', cabeceras=con(adm))
    ok(estado == 200, 'y lo demás de ms-administracion sigue respondiendo')
finally:
    subprocess.run(['docker', 'unpause', f'{PROYECTO}-elasticsearch-1'], capture_output=True)
hallado = []
for _ in range(60):
    hallado = elasticsearch('POST', '/logs-errores/_search', {'query': {'bool': {'filter': [
        {'term': {'servicio': 'ms-administracion'}}, {'match_phrase': {'mensaje': 'No se pudo consultar Elasticsearch'}}]}}}).get('hits', {}).get('hits', [])
    if hallado: break
    time.sleep(1)
ok(bool(hallado), 'al volver, también llega el error de ms-administracion (el appender reintenta el lote)')

print('== Usuarios y destinatarios (CU-19, DEC-13)')
u = sql('ms_identidad', f"SELECT u.nombre, u.rol, s.nombre, u.password_hash FROM usuario u JOIN sucursal s ON s.id = u.sucursal_id WHERE correo = '{SOFIA}'")
ok(u and u[0][:3] == ['Sofía Ramírez', 'JEFE_COCINA', 'Sucursal Norte'], f'Sofía en ms_identidad: {u[0][:3] if u else "—"}')
ok(u and u[0][3].startswith('$2') and 'Norte2026' not in u[0][3], 'solo con el hash BCrypt de su contraseña')
alta = [f for f in de_kafka if f[0] == f'Usuario registrado: {SOFIA} (JEFE_COCINA)']
ok(any(a[1] == ADMIN and a[4] == (registro.get('usuario') or {}).get('correlacion') for a in alta),
   'el alta está en la auditoría a nombre de la administradora y con la correlación del navegador')
d = sql('ms_notificaciones', f"SELECT sucursal_id FROM destinatario WHERE correo = '{SOFIA}'")
ok(d == [['2']], f'Sofía es destinataria de la Sucursal Norte ({d})')
reg = [f for f in de_kafka if f[0] == f'Destinatario registrado: {SOFIA} (sucursal 2)']
ok(any(r[1] == ADMIN and r[4] == (registro.get('destinatario') or {}).get('correlacion') for r in reg),
   'su registro llegó a la auditoría por Kafka, a nombre de la administradora y con la correlación del navegador (CU-15)')
estado, _, _ = http('POST', '/api/usuarios', {'nombre': 'X', 'correo': 'x@konradgourmet.com', 'rol': 'MESERO', 'sucursalId': 1, 'clave': 'Konrad2026'}, con(mes))
ok(estado == 403, f'el mesero no registra usuarios ({estado})')
estado, lista, _ = http('GET', '/api/notificaciones/destinatarios?sucursalId=2', cabeceras=con(adm))
ok(estado == 200 and SOFIA in lista, f'GET /api/notificaciones/destinatarios?sucursalId=2 → {lista}')

print('\nFALLAS:' if fallas else '\nTodo bien.', *fallas, sep='\n  ')
sys.exit(1 if fallas else 0)
