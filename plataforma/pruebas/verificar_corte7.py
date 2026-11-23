#!/usr/bin/env python3
# Revisa lo que dejó recorrido_corte7.py en cada uno de los 11 servicios (corre contra los contenedores con docker
# exec, contra el gateway en http://localhost:8080 y contra Mailpit en http://localhost:8025, sin instalar nada; usa
# pdftotext de poppler si está):
#   Eureka: los 11 servicios registrados y activos;
#   ms-identidad: el inicio de sesión de los 6 roles, cada uno con la correlación de su petición;
#   ms-menu: el plato, su receta y su imagen en el almacén S3; menu.actualizado y sus copias en ms-pedidos,
#     ms-inventario y ms-analitica;
#   ms-pedidos: el pedido del mesero y pedido.registrado;
#   ms-inventario: el consumo del cierre, inventario.stock-bajo del arroz y la alerta atendida por la solicitud;
#   ms-solicitudes: la solicitud del jefe de PENDIENTE a EN_TRAMITE y a COMPLETADA, con sus eventos;
#   ms-integracion: el precio del arroz, del Ministerio una sola vez (sin la caché) y de Redis la segunda;
#   ms-cotizaciones: las dos cotizaciones, sus PDF y sus eventos; la escogida CON_ORDEN;
#   ms-ordenes: la orden FINALIZADA, su PDF (almacén, gateway, navegador y correo) y orden.generada;
#   ms-notificaciones: la alarma (Redis) y los correos certificados de la historia con su traza de envíos;
#   ms-analitica: los hechos de cada evento de la historia y el tablero de hoy contra SQL;
#   ms-administracion: cada auditoría de la historia una vez en la bitácora, en el orden en que pasó;
#   y que cada servicio y cada rol dejaron su rastro.
# Uso: python verificar_corte7.py <carpeta-de-capturas-del-recorrido>
#      (COMPOSE_PROJECT_NAME elige otra instancia; por omisión, konrad-gourmet)
import datetime, json, os, re, shutil, subprocess, sys, tempfile, time, urllib.parse, urllib.request
from decimal import Decimal

PROYECTO = os.environ.get('COMPOSE_PROJECT_NAME', 'konrad-gourmet')
BASE = os.environ.get('BASE', 'http://localhost:8080')
MAILPIT = os.environ.get('MAILPIT', 'http://localhost:8025')
MESERO, JEFE = 'isabella.torres@konradgourmet.com', 'carlos.mendoza@konradgourmet.com'
AUXILIAR, DIRECTORA = 'isabella.castro@konradgourmet.com', 'laura.gomez@konradgourmet.com'
DIRECTOR, ADMIN = 'andres.rojas@konradgourmet.com', 'paula.herrera@konradgourmet.com'
ROLES = {MESERO: 'MESERO', JEFE: 'JEFE_COCINA', AUXILIAR: 'AUXILIAR_COMPRAS', DIRECTORA: 'DIRECTOR_COMPRAS',
         DIRECTOR: 'DIRECTOR_COMERCIAL', ADMIN: 'ADMINISTRADOR'}
CORREO_PROVEEDOR = {'Proveedor ABC': 'ventas@proveedorabc.com', 'Proveedor C': 'ventas@proveedorc.com'}
SERVICIOS = ['ms-identidad', 'ms-menu', 'ms-pedidos', 'ms-inventario', 'ms-solicitudes', 'ms-cotizaciones', 'ms-integracion',
             'ms-ordenes', 'ms-notificaciones', 'ms-analitica', 'ms-administracion']
HOY = datetime.date.today()
fallas = []
rastro = {s: [] for s in SERVICIOS}   # servicio → comprobaciones de la historia que pasaron

def ok(cond, texto, servicio=None):
    print(('  ✓ ' if cond else '  ✗ ') + texto)
    if not cond:
        fallas.append(texto)
    elif servicio:
        rastro[servicio].append(texto)
    return cond

def docker(contenedor, *orden, binario=False):
    r = subprocess.run(['docker', 'exec', f'{PROYECTO}-{contenedor}-1', *orden], capture_output=True)
    return r.stdout if binario else r.stdout.decode('utf-8', errors='replace')

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
            e = json.loads(cuerpo); e['_clave'] = clave; eventos.append(e)
    return eventos

def http(metodo, ruta, cuerpo=None, tok=None, binario=False, base=BASE):
    datos = json.dumps(cuerpo).encode() if cuerpo is not None else None
    cab = {'Content-Type': 'application/json'} if cuerpo is not None else {}
    if tok: cab['Authorization'] = f'Bearer {tok}'
    try:
        with urllib.request.urlopen(urllib.request.Request(base + ruta, data=datos, method=metodo, headers=cab), timeout=30) as r:
            leido = r.read()
            return r.status, leido if binario else json.loads(leido or b'null')
    except urllib.error.HTTPError as e:
        return e.code, None

def token(correo):
    return http('POST', '/api/auth/token', {'correo': correo, 'clave': 'Konrad2026'})[1]['jwt']

def objeto(bucket, llave):
    return docker('s3', 'wget', '-qO-', f'http://127.0.0.1:8888/buckets/{bucket}/{llave}', binario=True)

def llamadas_ministerio(producto):
    patron = json.dumps({'method': 'POST', 'url': '/precios', 'bodyPatterns': [{'contains': f'<codigoProducto>{producto}</codigoProducto>'}]})
    return json.loads(docker('ministerio', 'curl', '-s', '-X', 'POST', '-d', patron, 'http://127.0.0.1:8080/__admin/requests/count'))['count']

def correos(para, asunto):
    consulta = urllib.parse.quote(f'to:"{para}" subject:"{asunto}"')
    lista = http('GET', f'/api/v1/search?query={consulta}&limit=50', base=MAILPIT)[1]['messages']
    return [m for m in lista if m['Subject'] == asunto]

def adjunto(id_correo):
    m = http('GET', f'/api/v1/message/{id_correo}', base=MAILPIT)[1]
    if not m.get('Attachments'):
        return None, None
    a = m['Attachments'][0]
    return a['FileName'], http('GET', f'/api/v1/message/{id_correo}/part/{a["PartID"]}', base=MAILPIT, binario=True)[1]

def certificado(id_correo):
    cab = http('GET', f'/api/v1/message/{id_correo}/headers', base=MAILPIT)[1]
    return cab.get('X-Certificado', [''])[0].startswith('SHA-256:') and bool(cab.get('X-Sello-Tiempo', [''])[0])

def texto_pdf(contenido):
    if not shutil.which('pdftotext'):
        return None
    with tempfile.NamedTemporaryFile(suffix='.pdf') as f:
        f.write(contenido); f.flush()
        return subprocess.run(['pdftotext', '-layout', f.name, '-'], capture_output=True, text=True).stdout

def dec(v):
    return Decimal(str(v)).normalize()

if len(sys.argv) < 2 or not os.path.exists(f'{sys.argv[1]}/corte7.json'):
    sys.exit('Falta la carpeta del recorrido con corte7.json (corre antes recorrido_corte7.py)')
CARPETA = sys.argv[1]
h = json.load(open(f'{CARPETA}/corte7.json'))
plato, pedido, sol, orden = h['plato'], h['pedido'], h['solicitud'], h['orden']
cots = h['cotizaciones']
barata = orden['cotizacion']
otra = next(c for c in cots if c != barata)
oc, numero_sol, numero_ped = orden['numero'], sol['numero'], pedido['numero']
pid = plato['id'] or int(sql('ms_menu', f"SELECT id FROM plato WHERE nombre = '{plato['nombre']}'")[0][0])
director, adm, aux, dire, jefe = token(DIRECTOR), token(ADMIN), token(AUXILIAR), token(DIRECTORA), token(JEFE)
auditoria = topico('auditoria.evento')
def auditado(correlacion, accion, usuario=None):
    return [a for a in auditoria if a['correlacionId'] == correlacion and a['accion'] == accion and (usuario is None or a['usuario'] == usuario)]

print('== Eureka: los 11 servicios')
_, registro = http('GET', '/plataforma/registro')
vivos = {a['name'].lower() for a in (registro or {}).get('applications', {}).get('application', []) if any(i['status'] == 'UP' for i in a['instance'])}
ok(set(SERVICIOS) <= vivos, f'los 11 servicios están registrados y activos en Eureka ({len(set(SERVICIOS) & vivos)} de 11)')

print('== ms-identidad: los 6 roles')
for correo, rol in ROLES.items():
    correlaciones = h['logins'].get(correo, [])
    vistos = [c for c in correlaciones if auditado(c, f'Inicio de sesión ({rol})', correo)]
    ok(correlaciones and len(vistos) == len(correlaciones), f'{rol}: {len(vistos)} inicios de sesión de la historia, cada uno con la correlación de su petición', 'ms-identidad')

print('== ms-menu: el plato nuevo y sus copias (menu.actualizado)')
fila = sql('ms_menu', f'SELECT nombre, categoria, precio_venta, activo, imagen_url FROM plato WHERE id = {pid}')
ok(fila == [[plato['nombre'], 'Fuertes', '22000.00', 't', f's3://imagenes-platos/platos/{pid}.png']], f'ms_menu: {plato["nombre"]}, Fuertes, $22.000, activo y con imagen ({fila})', 'ms-menu')
ok(sql('ms_menu', f'SELECT producto_id, cantidad, unidad FROM ingrediente WHERE plato_id = {pid}') == [['2', '250.0000', 'GRAMO']], 'su receta: 250 g de arroz', 'ms-menu')
ok(objeto('imagenes-platos', f'platos/{pid}.png') == open(f'{CARPETA}/arroz-con-verduras.png', 'rb').read(), 'la imagen en el almacén S3 es la que subió el jefe', 'ms-menu')
menu = [e for e in topico('menu.actualizado') if e['platoId'] == pid]
ok(menu and menu[0]['usuario'] == JEFE and menu[0]['correlacionId'] == plato['correlacion'] and menu[-1]['activo'],
   f'menu.actualizado a nombre del jefe y con la correlación de V06 ({len(menu)} eventos)', 'ms-menu')
ok(auditado(plato['correlacion'], f'Plato {plato["nombre"]} creado', JEFE), 'su auditoría: «Plato … creado», en el mismo recorrido', 'ms-menu')
ok(sql('ms_pedidos', f'SELECT categoria, precio, activo FROM plato_catalogo WHERE plato_id = {pid}') == [['Fuertes', '22000.00', 't']], 'ms_pedidos tiene la copia del catálogo', 'ms-pedidos')
ok(sql('ms_inventario', f'SELECT producto_id, cantidad FROM receta WHERE plato_id = {pid}') == [['2', '0.2500']], 'ms_inventario tiene la receta', 'ms-inventario')
ok(sql('ms_analitica', f'SELECT nombre FROM plato_nombre WHERE plato_id = {pid}') == [[plato['nombre']]], 'ms_analitica tiene el nombre del plato (DEC-90)', 'ms-analitica')

print('== ms-pedidos: el pedido del evento')
id_mesero = sql('ms_identidad', f"SELECT id FROM usuario WHERE correo = '{MESERO}'")[0][0]
fila = sql('ms_pedidos', f"SELECT p.mesero_id, p.mesa_numero, d.plato_id, d.cantidad, d.precio_unitario FROM pedido p JOIN detalle_pedido d ON d.pedido_numero = p.numero WHERE p.numero = '{numero_ped}'")
ok(fila == [[id_mesero, str(pedido['mesa']), str(pid), str(pedido['unidades']), '22000.00']], f'{numero_ped}: del mesero (id {id_mesero}), mesa {pedido["mesa"]}, {pedido["unidades"]} × {plato["nombre"]} a $22.000 ({fila})', 'ms-pedidos')
ev = [e for e in topico('pedido.registrado') if e['numero'] == numero_ped]
ok(len(ev) == 1 and ev[0]['usuario'] == MESERO and ev[0]['correlacionId'] == pedido['correlacion'] and ev[0]['lineas'] == {str(pid): pedido['unidades']},
   f'pedido.registrado: a nombre del mesero, con la correlación de V03 y sus {pedido["unidades"]} platos', 'ms-pedidos')

print('== ms-inventario: el cierre, el stock bajo y la alerta atendida')
antes = h['arroz_antes']
actual, referencia, atendida = sql('ms_inventario', 'SELECT stock_actual, stock_referencia, alerta_atendida FROM insumo WHERE sucursal_id = 1 AND producto_id = 2')[0]
consumo = Decimal(str(pedido['unidades'])) * Decimal('0.25')
ok(Decimal(actual) <= Decimal(str(antes['stockActual'])) - consumo and Decimal(actual) / Decimal(referencia) < Decimal('0.25'),
   f'el cierre descontó el pedido: el arroz pasó de {antes["stockActual"]} a {actual} de {referencia} kg (≥ {consumo} kg menos, bajo el 25 %)', 'ms-inventario')
ok(not sql('ms_inventario', 'SELECT 1 FROM consumo_pendiente WHERE producto_id = 2 AND cantidad > 0'), 'y no quedó consumo de arroz pendiente', 'ms-inventario')
bajos = [e for e in topico('inventario.stock-bajo') if e['productoId'] == 2 and e['ocurridoEn'][:10] == HOY.isoformat()]
# el cierre es el job de las 23:59 (V07 solo lo adelanta): publica a nombre de «sistema» y con su propia correlación,
# la misma de su auditoría «Consumo diario descontado…» (como en el corte 3)
c_cierre = bajos[0]['correlacionId'] if bajos else None
ok(len(bajos) == 1 and bajos[0]['nombreProducto'] == 'Arroz' and dec(bajos[0]['porcentaje']) < 25 and bajos[0]['usuario'] == 'sistema'
   and auditado(c_cierre, 'Consumo diario descontado en la sucursal 1', 'sistema'),
   f'inventario.stock-bajo del arroz, una vez hoy, a nombre de «sistema» y en el recorrido del cierre ({[(e["porcentaje"], e["usuario"]) for e in bajos]})', 'ms-inventario')
ok(bajos and f'{dec(bajos[0]["porcentaje"]):f}'.replace('.', ',').rstrip('0').rstrip(',') in h['alarma'].replace('.', ','),
   f'el porcentaje del evento es el de la alarma en vivo («{h["alarma"]}»)', 'ms-notificaciones')
ok(atendida == 't' and auditado(sol['correlacion'], 'Alertas atendidas en la sucursal 1'), 'la alerta quedó atendida al llegar solicitud.registrada, en el recorrido del jefe', 'ms-inventario')

print('== ms-solicitudes: la solicitud del jefe')
id_jefe = sql('ms_identidad', f"SELECT id FROM usuario WHERE correo = '{JEFE}'")[0][0]
fila = sql('ms_solicitudes', f"SELECT solicitante_id, correo_solicitante, sucursal_id, estado, valor_sugerido_total FROM solicitud WHERE numero = '{numero_sol}'")
ok(fila == [[id_jefe, JEFE, '1', 'COMPLETADA', '320000.00']], f'{numero_sol}: del jefe, Sucursal Centro, COMPLETADA, valor sugerido $320.000 ({fila})', 'ms-solicitudes')
ok(sql('ms_solicitudes', f"SELECT producto_id, cantidad, unidad FROM item_solicitud WHERE solicitud_numero = '{numero_sol}'") == [['2', '40.000', 'kg']], 'con 40 kg de arroz', 'ms-solicitudes')
reg = [e for e in topico('solicitud.registrada') if e['numero'] == numero_sol]
ok(len(reg) == 1 and reg[0]['usuario'] == JEFE and reg[0]['correlacionId'] == sol['correlacion'], 'solicitud.registrada a nombre del jefe, con la correlación de V08', 'ms-solicitudes')
tramite = [e for e in topico('solicitud.en-tramite') if e['numero'] == numero_sol]
primera = next(iter(cots))
ok(len(tramite) == 1 and tramite[0]['correlacionId'] == cots[primera]['correlacion'] and tramite[0]['correoSolicitante'] == JEFE,
   f'solicitud.en-tramite una vez, en el recorrido de {primera} (la primera cotización)', 'ms-solicitudes')
aplicadas = sql('ms_solicitudes', f"SELECT cotizacion_numero FROM cotizacion_aplicada WHERE solicitud_numero = '{numero_sol}' ORDER BY 1")
ok(aplicadas == [[c] for c in sorted(cots)], f'las dos cotizaciones aplicadas a la solicitud ({aplicadas})', 'ms-solicitudes')
ok(auditado(orden['correlacion'], f'Solicitud {numero_sol} completada'), 'completada al llegar orden.generada, en el recorrido de la directora', 'ms-solicitudes')

print('== ms-integracion: el precio del Ministerio')
ok(llamadas_ministerio(2) == h['ministerio_antes'] + 1, f'sin la caché, el Ministerio respondió el arroz una sola vez para las dos cotizaciones ({h["ministerio_antes"]} → {llamadas_ministerio(2)})', 'ms-integracion')
guardado = docker('redis', 'redis-cli', 'GET', 'precio-referencia:2').strip()
ok('"valor":"8000.0"' in guardado and 0 < int(docker('redis', 'redis-cli', 'TTL', 'precio-referencia:2').strip() or -2) <= 86400, f'Redis volvió a guardar el precio por un día ({guardado})', 'ms-integracion')
for cot, datos in cots.items():
    ok(auditado(datos['correlacion'], 'Precio sugerido consultado: producto 2', AUXILIAR), f'{cot}: la consulta del precio a nombre de la auxiliar y con la correlación de V13', 'ms-integracion')

print('== ms-cotizaciones: las dos cotizaciones')
enviadas = {e['numero']: e for e in topico('cotizacion.enviada')}
validadas = {e['numero']: e for e in topico('cotizacion.validada')}
for cot, datos in cots.items():
    estado = 'CON_ORDEN' if cot == barata else 'OPCIONADA'
    fila = sql('ms_cotizaciones', f"SELECT p.razon_social, c.estado, c.pdf_url FROM cotizacion c JOIN proveedor p ON p.id = c.proveedor_id WHERE c.numero = '{cot}'")
    ok(fila == [[datos['proveedor'], estado, f's3://documentos-pdf/cotizaciones/{cot}.pdf']], f'{cot}: {datos["proveedor"]}, {estado}, con su PDF ({fila})', 'ms-cotizaciones')
    items = sql('ms_cotizaciones', f"SELECT producto_id, cantidad, precio_estandar, precio_proveedor FROM item_cotizacion WHERE cotizacion_numero = '{cot}'")
    ok(items == [['2', '40.000', '8000.00', f'{datos["precio"]}.00']], f'{cot}: 40 kg, estándar $8.000 y proveedor ' + f'${datos["precio"]:,}'.replace(',', '.'), 'ms-cotizaciones')
    pdf = objeto('documentos-pdf', f'cotizaciones/{cot}.pdf')
    estado_http, servido = http('GET', f'/api/cotizaciones/{cot}/pdf', tok=aux, binario=True)
    ok(pdf[:5] == b'%PDF-' and estado_http == 200 and servido == pdf, f'{cot}.pdf está en el almacén y el gateway sirve los mismos bytes', 'ms-cotizaciones')
    e = enviadas.get(cot, {})
    ok(e.get('usuario') == AUXILIAR and e.get('correlacionId') == datos['correlacion'] and e.get('solicitudNumeros') == [numero_sol] and float(e['precios']['2']) == 8000,
       f'cotizacion.enviada: a nombre de la auxiliar, de {numero_sol}, con el precio de referencia', 'ms-cotizaciones')
    v = validadas.get(cot, {})
    diferencia = (Decimal(datos['precio']) - 8000) / 80
    ok(v.get('estado') == 'OPCIONADA' and v.get('usuario') == DIRECTORA and v.get('correlacionId') == h['validaciones'][cot]['correlacion']
       and dec(v.get('diferenciaPorcentual')) == dec(diferencia) and v.get('proveedor') == datos['proveedor'],
       f'cotizacion.validada: OPCIONADA, {str(diferencia.normalize()).replace('.', ',') if diferencia < 0 else '+' + str(diferencia.normalize()).replace('.', ',')} %, a nombre de la directora y con la correlación de V18', 'ms-cotizaciones')
ok(auditado(orden['correlacion'], f'Cotización {barata} pasó a CON_ORDEN'), f'{barata} pasó a CON_ORDEN al llegar orden.generada', 'ms-cotizaciones')

print('== ms-ordenes: la orden de compra')
fila = sql('ms_ordenes', f"SELECT fecha, cotizacion_numero, proveedor, correo_proveedor, pdf_url, estado FROM orden_compra WHERE numero = '{oc}'")
ok(fila == [[HOY.isoformat(), barata, 'Proveedor C', 'ventas@proveedorc.com', f's3://documentos-pdf/ordenes/{oc}.pdf', 'FINALIZADA']], f'{oc}: de hoy, sobre {barata}, Proveedor C, FINALIZADA ({fila})', 'ms-ordenes')
ok(sql('ms_ordenes', f"SELECT producto_id, cantidad, unidad, precio FROM item_orden WHERE orden_numero = '{oc}'") == [['2', '40.000', 'kg', '7900.00']], 'hereda 40 kg de arroz a $7.900', 'ms-ordenes')
ok(sql('ms_ordenes', f"SELECT solicitud_numero FROM orden_solicitud WHERE orden_numero = '{oc}'") == [[numero_sol]], f'y la solicitud {numero_sol}', 'ms-ordenes')
pdf_oc = objeto('documentos-pdf', f'ordenes/{oc}.pdf')
estado_http, servido = http('GET', f'/api/ordenes-compra/{oc}/pdf', tok=dire, binario=True)
ok(pdf_oc[:5] == b'%PDF-' and estado_http == 200 and servido == pdf_oc and open(orden['pdf'], 'rb').read() == pdf_oc,
   f'{oc}.pdf: el del almacén, el que sirve el gateway y el que bajó el navegador son el mismo', 'ms-ordenes')
texto = texto_pdf(pdf_oc)
if texto is not None:
    ok(all(t in texto for t in (oc, 'Proveedor C', barata, numero_sol, 'Arroz', '$316.000')), 'el PDF dice la orden, el proveedor, la cotización, la solicitud, el arroz y $316.000', 'ms-ordenes')
og = [e for e in topico('orden.generada') if e['numero'] == oc]
ok(len(og) == 1 and og[0]['usuario'] == DIRECTORA and og[0]['correlacionId'] == orden['correlacion'] and float(og[0]['total']) == 316000
   and og[0]['solicitudNumeros'] == [numero_sol], 'orden.generada: a nombre de la directora, con la correlación de V21 y $316.000', 'ms-ordenes')
ok(auditado(h['finalizada']['correlacion'], f'Orden {oc} finalizada', DIRECTORA), 'la recepción queda en la auditoría a nombre de la directora', 'ms-ordenes')

print('== ms-notificaciones: la alarma y los correos certificados')
ok(docker('redis', 'redis-cli', 'PUBSUB', 'NUMSUB', 'alarmas:stock-bajo').split() == ['alarmas:stock-bajo', '1'], 'escucha el canal de Redis de las alarmas (PD-01)', 'ms-notificaciones')
for para, asunto in ((JEFE, 'Stock bajo: Arroz'), (JEFE, f'Solicitud {numero_sol} registrada'), (JEFE, f'Solicitud {numero_sol} en trámite')):
    m = []
    for _ in range(10):
        m = correos(para, asunto)
        if m: break
        time.sleep(1)
    ok(len(m) == 1 and certificado(m[0]['ID']), f'el jefe recibió «{asunto}», certificado', 'ms-notificaciones')
m = correos(JEFE, 'Stock bajo: Arroz')
cuerpo = http('GET', f'/api/v1/message/{m[0]["ID"]}', base=MAILPIT)[1].get('Text', '') if m else ''
ok(re.search(r'Quedan \d+(,\d+)? kg de Arroz \(\d+(,\d+)? % del stock de referencia\) en la Sucursal Centro\.', cuerpo) is not None,
   'el correo de stock bajo dice la cantidad, la unidad y la sucursal (HU-16, 4; DEC-102)', 'ms-notificaciones')
for cot, datos in cots.items():
    m = correos(CORREO_PROVEEDOR[datos['proveedor']], f'Solicitud de cotización {cot}')
    nombre, datos_pdf = adjunto(m[0]['ID']) if m else (None, None)
    ok(len(m) == 1 and nombre == f'{cot}.pdf' and datos_pdf == objeto('documentos-pdf', f'cotizaciones/{cot}.pdf') and certificado(m[0]['ID']),
       f'el {datos["proveedor"]} recibió {cot} con su PDF, idéntico al del almacén, certificado', 'ms-notificaciones')
m = correos('ventas@proveedorc.com', f'Orden de compra {oc}')
nombre, datos_pdf = adjunto(m[0]['ID']) if m else (None, None)
ok(len(m) == 1 and nombre == f'{oc}.pdf' and datos_pdf == pdf_oc and certificado(m[0]['ID']), f'el Proveedor C recibió {oc} con su PDF, el mismo que bajó la directora', 'ms-notificaciones')
traza = sql('ms_notificaciones', "SELECT tipo, destino, correlacion_id FROM envio WHERE exito AND correlacion_id IN ('"
            + "', '".join([d['correlacion'] for d in cots.values()] + [orden['correlacion']]) + "') ORDER BY id")
esperada = [['cotizacion.enviada', CORREO_PROVEEDOR[d['proveedor']], d['correlacion']] for d in cots.values()] + [['orden.generada', 'ventas@proveedorc.com', orden['correlacion']]]
ok(all(e in traza for e in esperada), f'la traza de envíos tiene cada correo a un proveedor con la correlación de su paso ({len(traza)} envíos)', 'ms-notificaciones')

print('== ms-analitica: los hechos de la historia y el tablero de hoy')
def hechos(correlacion, tipo):
    return [(c, dec(v)) for c, v in sql('ms_analitica', f"SELECT clave, valor FROM hecho WHERE correlacion_id = '{correlacion}' AND tipo = '{tipo}' ORDER BY id")]
for _ in range(10):
    if hechos(orden['correlacion'], 'ORDEN_PROVEEDOR'): break
    time.sleep(1)
for correlacion, tipo, esperado, texto in (
        (pedido['correlacion'], 'DEMANDA_PRODUCTO', [(plato['nombre'], dec(pedido['unidades']))], f'el pedido: {pedido["unidades"]} {plato["nombre"]}'),
        (pedido['correlacion'], 'DEMANDA_SUCURSAL', [('Sucursal Centro', dec(pedido['unidades']))], 'y la demanda de la Sucursal Centro'),
        (c_cierre, 'STOCK_BAJO', [('Arroz', dec(bajos[0]['porcentaje']) if bajos else None)], 'el stock bajo del arroz'),
        (sol['correlacion'], 'SOLICITUD_SUCURSAL', [('Sucursal Centro', dec(1))], 'la solicitud de la Sucursal Centro'),
        (h['validaciones'][barata]['correlacion'], 'PRECIO_PROVEEDOR', [('Proveedor C', dec('-1.25'))], 'la cotización opcionada del Proveedor C (−1,25 %)'),
        (h['validaciones'][otra]['correlacion'], 'PRECIO_PROVEEDOR', [('Proveedor ABC', dec('3.75'))], 'y la del Proveedor ABC (+3,75 %)'),
        (orden['correlacion'], 'ORDEN_PROVEEDOR', [('Proveedor C', dec(316000))], 'la orden al Proveedor C por $316.000')):
    encontrado = hechos(correlacion, tipo)
    ok(encontrado == esperado, f'{tipo}: {texto} ({encontrado})', 'ms-analitica')
_, kpis = http('GET', f'/api/bam/kpis?desde={HOY}&hasta={HOY}', tok=director)
demanda = dict((p['clave'], dec(p['valor'])) for p in kpis[0]['ranking'])
sql_plato = sql('ms_analitica', f"SELECT sum(valor) FROM hecho WHERE tipo = 'DEMANDA_PRODUCTO' AND clave = '{plato['nombre']}' AND fecha = '{HOY}'")[0][0]
ok(demanda.get(plato['nombre']) == dec(sql_plato), f'el tablero de hoy cuenta {demanda.get(plato["nombre"])} {plato["nombre"]}, como SQL', 'ms-analitica')
proveedores = sql('ms_analitica', f"SELECT clave, round(sum(valor) / count(*), 2) FROM hecho WHERE tipo = 'PRECIO_PROVEEDOR' AND fecha = '{HOY}' GROUP BY clave ORDER BY 2, min(id) LIMIT 1")
ok(kpis[2]['ganador'] == proveedores[0][0] == 'Proveedor C', f'el proveedor con mejores precios de hoy: {kpis[2]["ganador"]} ({kpis[2]["valor"]} %), como SQL', 'ms-analitica')

print('== ms-administracion: la bitácora de la historia')
correlaciones = [plato['correlacion'], pedido['correlacion'], c_cierre, sol['correlacion']] + \
                [d['correlacion'] for d in cots.values()] + [v['correlacion'] for v in h['validaciones'].values()] + \
                [orden['correlacion'], h['finalizada']['correlacion']] + [c for l in h['logins'].values() for c in l]
de_la_historia = [a for a in auditoria if a['correlacionId'] in correlaciones]
bitacora = sql('ms_administracion', "SELECT id, accion, usuario, coalesce(correlacion_id::text, '') FROM auditoria WHERE correlacion_id IN ('"
               + "', '".join(correlaciones) + "') ORDER BY fecha, hora, id")
# una misma acción puede salir dos veces en un paso (la alarma y el correo del stock bajo dejan cada uno
# «Notificación inventario.stock-bajo enviada a …»): la bitácora debe tener cada una tantas veces como Kafka
from collections import Counter
en_kafka = Counter((a['accion'], a['usuario'], a['correlacionId']) for a in de_la_historia)
en_bitacora = Counter((accion, usuario, correlacion) for _, accion, usuario, correlacion in bitacora)
mal = [k[0] for k in en_kafka if en_bitacora[k] != en_kafka[k]] + [k[0] for k in en_bitacora if k not in en_kafka]
ok(de_la_historia and not mal, f'cada auditoria.evento de la historia está en la bitácora, ni más ni menos veces que en Kafka ({len(de_la_historia)} movimientos; mal: {mal[:3]})', 'ms-administracion')
acciones = [b[1] for b in bitacora]
orden_esperado = [f'Plato {plato["nombre"]} creado', f'Pedido {numero_ped} registrado por el mesero {id_mesero}', 'Consumo diario descontado en la sucursal 1',
                  f'Solicitud {numero_sol} registrada', *[f'Cotización {c} enviada a {d["proveedor"]}' for c, d in cots.items()],
                  *[f'Cotización {c} validada: OPCIONADA' for c in cots], f'Orden {oc} generada sobre {barata}', f'Orden {oc} finalizada']
posiciones = [acciones.index(a) if a in acciones else -1 for a in orden_esperado]
ok(-1 not in posiciones and posiciones == sorted(posiciones), f'la bitácora cuenta la historia en orden ({" → ".join(a.split(" ")[0] for a in orden_esperado)}; {posiciones})', 'ms-administracion')
usuarios = {b[2] for b in bitacora}
ok(set(ROLES) <= usuarios, f'y en ella están los 6 roles ({len(set(ROLES) & usuarios)} de 6)', 'ms-administracion')
_, api = http('GET', f'/api/admin/auditoria?desde={HOY}&hasta={HOY}', tok=adm)
ok(api and all(any(m['accion'] == a for m in api) for a in orden_esperado), '«Auditoría» (GET /api/admin/auditoria de hoy) devuelve todos esos pasos', 'ms-administracion')

print('== Los 11 servicios y los 6 roles')
for s in SERVICIOS:
    ok(bool(rastro[s]), f'{s}: {len(rastro[s])} comprobaciones de la historia')
ok(all(rastro.values()), f'la historia pasó por los 11 servicios ({sum(1 for v in rastro.values() if v)} de 11)')
ok(all(h['logins'].get(c) for c in ROLES), f'y por los 6 roles ({sum(1 for c in ROLES if h["logins"].get(c))} de 6)')

print('\nFALLAS:' if fallas else '\nTodo bien.', *fallas, sep='\n  ')
sys.exit(1 if fallas else 0)
