#!/usr/bin/env python3
# Revisa lo que dejó recorrido_corte6b.py (corre contra los contenedores con docker exec, contra el gateway en
# http://localhost:8080 y contra Mailpit en http://localhost:8025, sin instalar nada):
#   el almacén de hechos de ms-analitica contra Kafka: cada evento de los 5 tópicos de los cortes 3 a 6b dejó sus
#     hechos, con los nombres de la copia local (DEC-90), su fecha y su correlación (DEC-64); solo las cotizaciones
#     OPCIONADAS cuentan (PD-03) y cotizacion.validada lleva tipos simples (PD-05, DEC-89); un evento publicado otra
#     vez no se cuenta dos veces;
#   el tablero BAM contra SQL: los tres indicadores sin rango (30 y 365 días), con la sucursal y con un rango, su
#     ranking y su periodo (DEC-88); los mensajes de PD-10 y el tiempo de respuesta (HU-18);
#   las promociones: lo que sugirió TendenciasJob contra las redes simuladas y los hechos de los 7 días anteriores
#     (PD-04); tendencia.detectada en Kafka, una vez, con su correlación, y el correo «Promociones sugeridas» en
#     Mailpit, uno por jefe, con la misma correlación en la traza de envíos; con una tendencia nueva el job sugiere
#     otra vez sin mandar otro correo (máximo uno al día); con la API de redes caída, el error llega a «Logs de
#     errores» y lo demás sigue;
#   aceptar y descartar: los estados del recorrido, «Esta sugerencia ya fue aceptada o descartada.» (409), 404 y su
#     auditoría a nombre del jefe y con la correlación del navegador;
#   los roles, por el gateway y directo al servicio.
# Uso: python verificar_corte6b.py <carpeta-de-capturas-del-recorrido>
#      (COMPOSE_PROJECT_NAME elige otra instancia; por omisión, konrad-gourmet)
import collections, datetime, json, os, subprocess, sys, time, urllib.parse, urllib.request
from decimal import Decimal

PROYECTO = os.environ.get('COMPOSE_PROJECT_NAME', 'konrad-gourmet')
BASE = os.environ.get('BASE', 'http://localhost:8080')
CORREO = os.environ.get('MAILPIT', 'http://localhost:8025')
CARPETA = sys.argv[1]
DIRECTOR, JEFE, SOFIA, MESERO, ADMIN = ('andres.rojas@konradgourmet.com', 'carlos.mendoza@konradgourmet.com', 'sofia.ramirez@konradgourmet.com',
                                        'isabella.torres@konradgourmet.com', 'paula.herrera@konradgourmet.com')
HOY = datetime.date.today()
fallas = []
registro = json.load(open(f'{CARPETA}/corte6b.json'))

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

def publicar_otra_vez(topico_, clase, evento):
    docker('kafka', '/opt/kafka/bin/kafka-console-producer.sh', '--bootstrap-server', 'localhost:9092', '--topic', topico_,
           '--property', 'parse.key=true', '--property', 'key.separator=#K#', '--property', 'parse.headers=true',
           '--property', 'headers.delimiter=#H#', '--property', 'headers.key.separator=:',
           entrada=f"kg-evento:{clase}#H#{evento['_clave']}#K#{evento['_crudo']}\n")

def http(metodo, ruta, cuerpo=None, tok=None, base=BASE):
    datos = json.dumps(cuerpo).encode() if cuerpo is not None else None
    cab = {'Content-Type': 'application/json'} if cuerpo is not None else {}
    if tok: cab['Authorization'] = f'Bearer {tok}'
    peticion = urllib.request.Request(base + ruta, data=datos, method=metodo, headers=cab)
    try:
        with urllib.request.urlopen(peticion, timeout=40) as r:
            return r.status, json.loads(r.read() or b'null')
    except urllib.error.HTTPError as e:
        cuerpo = e.read()
        try: cuerpo = json.loads(cuerpo or b'null')
        except ValueError: cuerpo = None
        return e.code, cuerpo

def token(correo, clave='Konrad2026'):
    return http('POST', '/api/auth/token', {'correo': correo, 'clave': clave})[1]['jwt']

def directo(metodo, ruta, tok=None):
    """Directo a ms-analitica, sin el gateway (desde el contenedor del Ministerio, que tiene curl)."""
    orden = ['curl', '-s', '-o', '/dev/null', '-w', '%{http_code}', '--max-time', '5', '-X', metodo, f'http://ms-analitica:8080{ruta}']
    if tok: orden[1:1] = ['-H', f'Authorization: Bearer {tok}']
    return docker('ministerio', *orden)

def redes(metodo, ruta, cuerpo=None):
    orden = ['curl', '-s', '-X', metodo, f'http://127.0.0.1:8080{ruta}']
    if cuerpo is not None: orden += ['-H', 'Content-Type: application/json', '-d', json.dumps(cuerpo)]
    salida = docker('redes', *orden)
    return json.loads(salida) if salida.strip().startswith('{') else {}

def dec(v):
    return Decimal(str(v)).normalize()

def fecha_de(e):
    return e['ocurridoEn'][:10]

directora, jefe, mesero, adm, director = token('laura.gomez@konradgourmet.com'), token(JEFE), token(MESERO), token(ADMIN), token(DIRECTOR)

print('== El almacén de hechos contra los 5 tópicos (NegocioEventsListener)')
platos = {int(i): n for i, n in sql('ms_menu', 'SELECT id, nombre FROM plato')}
sucursales = {int(i): n for i, n in sql('ms_analitica', 'SELECT id, nombre FROM sucursal')}
copia = {int(i): n for i, n in sql('ms_analitica', 'SELECT plato_id, nombre FROM plato_nombre')}
ok(copia and all(copia.get(i) == n for i, n in platos.items()), f'la copia local tiene el nombre de los {len(platos)} platos de ms-menu (menu.actualizado, DEC-90)')
def hechos_de(correlacion):
    return sql('ms_analitica', f"SELECT tipo, coalesce(sucursal_id::text, ''), clave, valor, fecha FROM hecho WHERE correlacion_id = '{correlacion}' ORDER BY id")
pedidos = topico('pedido.registrado')
for _ in range(15):
    if len(sql('ms_analitica', "SELECT DISTINCT correlacion_id FROM hecho WHERE tipo = 'DEMANDA_SUCURSAL' AND correlacion_id IS NOT NULL")) >= len({p['correlacionId'] for p in pedidos}): break
    time.sleep(2)
ok(len(pedidos) >= 4, f'pedido.registrado tiene los pedidos de los cortes 3 a 6b ({len(pedidos)})')
malos = []
for p in pedidos:
    h = [f for f in hechos_de(p['correlacionId']) if f[0].startswith('DEMANDA_')]
    esperado = [('DEMANDA_PRODUCTO', str(p['sucursalId']), platos[int(i)], dec(c), fecha_de(p)) for i, c in p['lineas'].items()]
    esperado.append(('DEMANDA_SUCURSAL', str(p['sucursalId']), sucursales[p['sucursalId']], dec(sum(p['lineas'].values())), fecha_de(p)))
    if sorted(esperado) != sorted((f[0], f[1], f[2], dec(f[3]), f[4]) for f in h):
        malos.append((p['numero'], h, esperado))
ok(not malos, f'cada pedido dejó un hecho por plato, con su nombre y sus unidades, y uno de la sucursal, con su fecha y su correlación ({len(pedidos) - len(malos)} de {len(pedidos)}; {malos[:1]})')
nuestro = registro.get('pedido') or {}
suyo = [p for p in pedidos if p['correlacionId'] == nuestro.get('correlacion')]
ok(suyo and [f[2:4] for f in hechos_de(nuestro['correlacion']) if f[0] == 'DEMANDA_PRODUCTO'] == [['Punta de anca', '3.0000']],
   f'el pedido del recorrido ({nuestro.get("numero")}) dejó 3 Punta de anca con la correlación del mesero')

validadas = topico('cotizacion.validada')
ok(validadas and all(isinstance(v['estado'], str) and 'diferenciaPorcentual' in v and v.get('proveedor') for v in validadas),
   f'cotizacion.validada lleva el estado como texto, la diferencia y el nombre del proveedor (PD-05, DEC-89; {len(validadas)} eventos)')
malos = []
for v in validadas:
    h = [f for f in hechos_de(v['correlacionId']) if f[0] == 'PRECIO_PROVEEDOR']
    if v['estado'] == 'OPCIONADA':
        if [(f[1], f[2], dec(f[3]), f[4]) for f in h] != [('', v['proveedor'], dec(v['diferenciaPorcentual']), fecha_de(v))]: malos.append(v['numero'])
    elif h:
        malos.append(v['numero'])
opcionadas = [f"{v['numero']} {v['proveedor']} {v['diferenciaPorcentual']}" for v in validadas if v['estado'] == 'OPCIONADA']
ok(not malos, f'solo las OPCIONADAS dejan su diferencia, sin sucursal ({opcionadas}; las demás no cuentan, PD-03; mal: {malos})')
for nombre, tipo, valor in (('inventario.stock-bajo', 'STOCK_BAJO', lambda e: (str(e['sucursalId']), e['nombreProducto'], dec(e['porcentaje']))),
                            ('solicitud.registrada', 'SOLICITUD_SUCURSAL', lambda e: (str(e['sucursalId']), sucursales[e['sucursalId']], dec(1))),
                            ('orden.generada', 'ORDEN_PROVEEDOR', lambda e: ('', e['proveedor'], dec(e['total'])))):
    evs = topico(nombre)
    bien = [e for e in evs if [(f[1], f[2], dec(f[3])) for f in hechos_de(e['correlacionId']) if f[0] == tipo].count(valor(e)) >= 1]
    ok(evs and len(bien) == len(evs), f'{nombre} → {tipo} ({len(bien)} de {len(evs)})')
total_eventos = len({e['id'] for t in ('pedido.registrado', 'cotizacion.validada', 'inventario.stock-bajo', 'solicitud.registrada', 'orden.generada') for e in topico(t)})
procesados = int(sql('ms_analitica', 'SELECT count(*) FROM evento_procesado')[0][0])
ok(procesados == total_eventos, f'evento_procesado tiene cada evento de los 5 tópicos una vez ({procesados} de {total_eventos})')
original = next(p for p in pedidos if p['numero'] == 'PED-0251') if any(p['numero'] == 'PED-0251' for p in pedidos) else pedidos[0]
antes = len(hechos_de(original['correlacionId']))
publicar_otra_vez('pedido.registrado', 'co.edu.konradlorenz.pedidos.eventos.PedidoRegistradoEvent', original)
time.sleep(6)
ok(len(topico('pedido.registrado')) == len(pedidos) + 1 and len(hechos_de(original['correlacionId'])) == antes,
   f'se publicó otra vez {original["numero"]} y sus hechos siguen siendo {antes}: no se cuenta dos veces')

print('== El tablero BAM contra SQL (HU-18, PD-03, DEC-88)')
def esperado_demanda(tipo, desde, hasta, sucursal=None):
    filtro = f" AND sucursal_id = {sucursal}" if sucursal else ''
    filas = sql('ms_analitica', f"SELECT clave, sum(valor) FROM hecho WHERE tipo = '{tipo}' AND fecha BETWEEN '{desde}' AND '{hasta}'{filtro} "
                                f"GROUP BY clave ORDER BY sum(valor) DESC, min(id) LIMIT 5")
    return [(c, dec(v)) for c, v in filas]
def esperado_proveedor(desde, hasta):
    filas = sql('ms_analitica', f"SELECT clave, round(sum(valor) / count(*), 2) FROM hecho WHERE tipo = 'PRECIO_PROVEEDOR' "
                                f"AND fecha BETWEEN '{desde}' AND '{hasta}' GROUP BY clave ORDER BY 2, min(id) LIMIT 5")
    return [(c, dec(v)) for c, v in filas]
def ranking(k):
    return [(p['clave'], dec(p['valor'])) for p in k['ranking']]
d30, d365 = (HOY - datetime.timedelta(days=29)).isoformat(), (HOY - datetime.timedelta(days=364)).isoformat()
inicio = time.time()
estado, kpis = http('GET', '/api/bam/kpis', tok=director)
ms = int((time.time() - inicio) * 1000)
ok(estado == 200 and len(kpis) == 3 and ms < 5000, f'el director comercial recibe los tres indicadores en {ms} ms (< 5 s)')
ok(ranking(kpis[0]) == esperado_demanda('DEMANDA_PRODUCTO', d30, HOY), f'producto: {kpis[0]["ganador"]} ({kpis[0]["valor"]}) y su ranking, como en SQL, de los últimos 30 días')
ok(ranking(kpis[1]) == esperado_demanda('DEMANDA_SUCURSAL', d30, HOY), f'sucursal: {kpis[1]["ganador"]} ({kpis[1]["valor"]})')
ok(ranking(kpis[2]) == esperado_proveedor(d365, HOY), f'proveedor: {kpis[2]["ganador"]} ({kpis[2]["valor"]} %), de los últimos 365 días y solo OPCIONADAS')
ok([(k['desde'], k['hasta']) for k in kpis] == [(d30, HOY.isoformat())] * 2 + [(d365, HOY.isoformat())], 'cada indicador dice su periodo')
_, norte = http('GET', '/api/bam/kpis?sucursalId=2', tok=director)
ok(ranking(norte[0]) == esperado_demanda('DEMANDA_PRODUCTO', d30, HOY, 2) and norte[0]['ganador'] == 'Ajiaco', f'Sucursal Norte: {norte[0]["ganador"]}, como en SQL')
ok(ranking(norte[2]) == ranking(kpis[2]), 'y el proveedor no cambia con la sucursal')
d, h = (HOY - datetime.timedelta(days=120)).isoformat(), (HOY - datetime.timedelta(days=70)).isoformat()
_, rango = http('GET', f'/api/bam/kpis?desde={d}&hasta={h}', tok=director)
ok(ranking(rango[0]) == esperado_demanda('DEMANDA_PRODUCTO', d, h) and ranking(rango[2]) == esperado_proveedor(d, h)
   and all((k['desde'], k['hasta']) == (d, h) for k in rango), f'con rango, los tres usan el rango: {rango[0]["ganador"]} y {rango[2]["ganador"]}')
_, viejo = http('GET', f'/api/bam/kpis?desde={(HOY - datetime.timedelta(days=400)).isoformat()}&hasta={(HOY - datetime.timedelta(days=366)).isoformat()}', tok=director)
ok(viejo[2]['ganador'] == 'Proveedor D', 'la cotización de hace más de un año solo cuenta si el rango la incluye (Proveedor D)')
for ruta, mensaje in ((f'/api/bam/kpis?desde={HOY}&hasta={HOY - datetime.timedelta(days=1)}', 'La fecha inicial no puede ser posterior a la fecha final.'),
                      (f'/api/bam/kpis?desde={HOY}', 'Seleccione la fecha inicial y la fecha final.'),
                      ('/api/bam/kpis?desde=18-11-2026&hasta=2026-11-18', 'La fecha no es válida.')):
    estado, cuerpo = http('GET', ruta, tok=director)
    ok(estado == 400 and cuerpo and cuerpo.get('mensaje') == mensaje, f'«{mensaje}» ({estado})')
_, vacio = http('GET', '/api/bam/kpis?desde=2020-01-01&hasta=2020-01-31', tok=director)
ok(all(k['ganador'] is None and k['valor'] is None and k['ranking'] == [] for k in vacio), 'un periodo sin hechos: sin ganador ni ranking (la SPA dice «No hay información suficiente…»)')

print('== Promociones sugeridas (TendenciasJob, PD-04)')
hoy = HOY.isoformat()
sugeridas = sql('ms_analitica', f"SELECT id, producto, motivo, vigencia_desde, vigencia_hasta, estado FROM sugerencia WHERE vigencia_desde = '{hoy}' ORDER BY id")
top = [c for c, _ in esperado_demanda('DEMANDA_PRODUCTO', (HOY - datetime.timedelta(days=7)).isoformat(), (HOY - datetime.timedelta(days=1)).isoformat())][:3]
en_redes = ['Ajiaco', 'Limonada natural']
motivos = collections.OrderedDict((p, 'Tendencia en redes') for p in en_redes)
for p in top:
    motivos[p] = 'Tendencia en redes y entre los 3 más pedidos de la semana' if p in motivos else 'Entre los 3 más pedidos de la semana'
primeras = [(s[1], s[2]) for s in sugeridas[:len(motivos)]]
ok(primeras == list(motivos.items()), f'el job sugirió las tendencias de las redes y los 3 más pedidos de los 7 días anteriores ({[p for p, _ in primeras]})')
ok(all(s[3] == hoy and s[4] == (HOY + datetime.timedelta(days=6)).isoformat() for s in sugeridas), 'cada una vale 7 días desde hoy')
peticiones = redes('GET', '/__admin/requests').get('requests', [])
ok(peticiones and all({k.lower(): v for k, v in r['request']['headers'].items()}.get('authorization') == 'Bearer konrad-redes-local'
                      and 'pais=CO&categoria=gastronomia&periodo=semana' in r['request']['url'] for r in peticiones),
   f'RedesSocialesAdapter llamó a la API de redes con su token y su consulta ({len(peticiones)} llamadas)')
ok(sql('ms_analitica', "SELECT name FROM shedlock") == [['TendenciasJob']], 'TendenciasJob corre con ShedLock (DEC-36)')
def tendencias_de_hoy():
    return [e for e in topico('tendencia.detectada') if fecha_de(e) == hoy]
tendencias = tendencias_de_hoy()
ok(len(tendencias) == 1, f'tendencia.detectada salió una vez hoy ({len(tendencias)})')
t = tendencias[0] if tendencias else {}
lineas = [f"{p} · {m} · del {HOY:%d/%m/%Y} al {HOY + datetime.timedelta(days=6):%d/%m/%Y}" for p, m in motivos.items()]
ok(t.get('sugerencias') == lineas, f'con una línea de texto por sugerencia (PD-05): {t.get("sugerencias", [""])[0]!r}…')
ok(t.get('usuario') == 'sistema' and t.get('correlacionId'), f'a nombre de «sistema» y con su correlación ({str(t.get("correlacionId"))[:8]}…, DEC-64)')
auditoria = topico('auditoria.evento')
ok(any(a['accion'] == 'Promociones sugeridas: ' + ', '.join(motivos) and a['correlacionId'] == t.get('correlacionId') for a in auditoria),
   'la auditoría de las sugerencias comparte esa correlación')

def promociones_en_mailpit():
    salida = urllib.request.urlopen(f'{CORREO}/api/v1/search?query=' + urllib.parse.quote('subject:"Promociones sugeridas"') + '&limit=100', timeout=20)
    return [m for m in json.load(salida)['messages'] if m['Created'][:10] >= (HOY - datetime.timedelta(days=1)).isoformat()]
for _ in range(20):
    correos = promociones_en_mailpit()
    if correos: break
    time.sleep(2)
destinatarios = sorted(m['To'][0]['Address'] for m in correos)
libreta = sorted({c for (c,) in sql('ms_notificaciones', 'SELECT correo FROM destinatario')})
ok(destinatarios and len(destinatarios) == len(set(destinatarios)), f'el correo «Promociones sugeridas» llegó una vez a cada jefe ({destinatarios}; libreta: {libreta})')
if correos:
    texto = json.load(urllib.request.urlopen(f'{CORREO}/api/v1/message/{correos[0]["ID"]}', timeout=20))
    ok(all(f'• {l}' in texto['Text'] for l in lineas), 'con una línea por sugerencia')
    cabeceras = json.load(urllib.request.urlopen(f'{CORREO}/api/v1/message/{correos[0]["ID"]}/headers', timeout=20))
    ok(cabeceras.get('X-Certificado', [''])[0].startswith('SHA-256:'), 'certificado, como los demás correos')
envios = sql('ms_notificaciones', "SELECT destino, coalesce(correlacion_id::text, '') FROM envio WHERE tipo = 'tendencia.detectada' AND exito")
ok(envios and all(e[1] == t.get('correlacionId') for e in envios), f'la traza de envíos lo guarda con la correlación del evento ({len(envios)} envíos)')

print('== Una tendencia nueva el mismo día: otra sugerencia, sin otro correo')
nuevo = redes('POST', '/__admin/mappings', {
    'priority': 0, 'request': {'method': 'GET', 'urlPath': '/v1/tendencias', 'headers': {'Authorization': {'equalTo': 'Bearer konrad-redes-local'}}},
    'response': {'status': 200, 'headers': {'Content-Type': 'application/json'},
                 'jsonBody': {'tendencias': [{'producto': 'Ajiaco', 'menciones': 2100}, {'producto': 'Mazorcada', 'menciones': 1500}]}}})
try:
    mazorcada = []
    for _ in range(40):
        mazorcada = sql('ms_analitica', f"SELECT motivo, estado FROM sugerencia WHERE producto = 'Mazorcada' AND vigencia_desde = '{hoy}'")
        if mazorcada: break
        time.sleep(2)
    ok(mazorcada == [['Tendencia en redes', 'SUGERIDA']], f'las redes traen la Mazorcada y el job la sugiere en menos de un minuto ({mazorcada})')
    time.sleep(8)
    ok(len(tendencias_de_hoy()) == 1, 'pero no publica otra vez tendencia.detectada: ya se notificó hoy')
    ok(len(promociones_en_mailpit()) == len(correos), f'ni manda otro correo (máximo uno al día: siguen {len(promociones_en_mailpit())})')
    ok(int(sql('ms_analitica', f"SELECT count(*) FROM sugerencia WHERE vigencia_desde = '{hoy}' AND producto = 'Ajiaco'")[0][0]) == 1,
       'y el Ajiaco, que ya estaba sugerido hoy, no se repite')
    ok(any(a['accion'] == 'Promociones sugeridas: Mazorcada' for a in topico('auditoria.evento')), 'la sugerencia nueva queda en la auditoría')
finally:
    redes('DELETE', f"/__admin/mappings/{nuevo.get('id')}")

print('== Con la API de redes caída, el error queda en el log y lo demás sigue (HU-19, DEC-84)')
caida = redes('POST', '/__admin/mappings', {'priority': 0, 'request': {'method': 'GET', 'urlPath': '/v1/tendencias'},
                                             'response': {'status': 503, 'body': 'mantenimiento'}})
antes = int(sql('ms_analitica', 'SELECT count(*) FROM sugerencia')[0][0])
try:
    hallado = []
    for _ in range(45):
        _, logs = http('GET', '/api/admin/logs?servicio=ms-analitica', tok=adm)
        hallado = [l for l in logs or [] if 'La API de tendencias de las redes sociales respondió 503' in l['mensaje']]
        if hallado: break
        time.sleep(2)
    ok(hallado and hallado[0]['mensaje'].startswith('Falló el análisis de tendencias de promociones'),
       f'«Logs de errores» muestra el error de ms-analitica ({hallado[0]["mensaje"][:90] if hallado else "—"}…)')
    ok(int(sql('ms_analitica', 'SELECT count(*) FROM sugerencia')[0][0]) == antes, 'no se guardó ninguna sugerencia')
    ok(http('GET', '/api/bam/kpis', tok=director)[0] == 200 and http('GET', '/api/bam/promociones', tok=jefe)[0] == 200,
       'y el tablero y las promociones siguen respondiendo')
finally:
    redes('DELETE', f"/__admin/mappings/{caida.get('id')}")

print('== Aceptar y descartar (HU-19)')
estados = dict((p, e) for _, p, _, _, _, e in sugeridas)
estados_db = {p: e for p, e in sql('ms_analitica', f"SELECT producto, estado FROM sugerencia WHERE vigencia_desde = '{hoy}'")}
ok(estados_db.get('Ajiaco') == 'ACEPTADA' and estados_db.get('Limonada natural') == 'DESCARTADA' and estados_db.get('Bandeja paisa') == 'ACEPTADA'
   and estados_db.get('Pechuga a la plancha') == 'ACEPTADA', f'los estados del recorrido están en la base ({estados_db})')
ajiaco = next(int(s[0]) for s in sugeridas if s[1] == 'Ajiaco')
estado, cuerpo = http('PATCH', f'/api/bam/promociones/{ajiaco}/descartar', tok=jefe)
ok(estado == 409 and cuerpo.get('mensaje') == 'Esta sugerencia ya fue aceptada o descartada.', f'descartar el Ajiaco ya aceptado: 409 «{cuerpo and cuerpo.get("mensaje")}»')
ok(sql('ms_analitica', f'SELECT estado FROM sugerencia WHERE id = {ajiaco}') == [['ACEPTADA']], 'y sigue ACEPTADA')
estado, cuerpo = http('PATCH', '/api/bam/promociones/999999/aceptar', tok=jefe)
ok(estado == 404 and cuerpo.get('mensaje') == 'No existe la sugerencia 999999', f'una sugerencia que no existe: 404 ({estado})')
auditoria = topico('auditoria.evento')
por_correlacion = {a['correlacionId']: a for a in auditoria}
decididas = [d for d in registro['decisiones'] if d['estado'] == 200]
ok(len(decididas) >= 3 and all(d['correlacion'] in por_correlacion and por_correlacion[d['correlacion']]['usuario'] == JEFE for d in decididas),
   f'cada decisión del recorrido está en la auditoría a nombre de Carlos, con la correlación del navegador ({len(decididas)})')
ok(any(a['accion'] == 'Promoción sugerida Bandeja paisa aceptada' and a['usuario'] in (SOFIA, JEFE) for a in auditoria),
   'la Bandeja paisa la aceptó la otra jefa (Sofía)')
ok(any(d['estado'] == 409 for d in registro['decisiones']), 'el intento de Carlos sobre la Bandeja paisa ya resuelta respondió 409')

print('== Roles (por el gateway y directo al servicio)')
for nombre, tok, kpi, promo in (('director comercial', director, 200, 403), ('jefe de cocina', jefe, 403, 200), ('mesero', mesero, 403, 403),
                                ('administradora', adm, 403, 403), ('directora de compras', directora, 403, 403)):
    k, p = http('GET', '/api/bam/kpis', tok=tok)[0], http('GET', '/api/bam/promociones', tok=tok)[0]
    ok((k, p) == (kpi, promo), f'{nombre}: tablero {k}, promociones {p}')
ok(http('PATCH', f'/api/bam/promociones/{ajiaco}/aceptar', tok=director)[0] == 403, 'el director comercial no acepta promociones (403)')
ok(http('GET', '/api/bam/kpis')[0] == 401, 'sin token, el gateway responde 401')
ok(directo('GET', '/api/bam/kpis') == '401' and directo('GET', '/api/bam/kpis', jefe) == '403' and directo('GET', '/api/bam/kpis', director) == '200',
   'directo a ms-analitica (sin el gateway): sin token 401, el jefe 403 y el director 200')
ok(directo('PATCH', f'/api/bam/promociones/{ajiaco}/aceptar', mesero) == '403', 'y el mesero no acepta promociones directo al servicio')

print('\nFALLAS:' if fallas else '\nTodo bien.', *fallas, sep='\n  ')
sys.exit(1 if fallas else 0)
