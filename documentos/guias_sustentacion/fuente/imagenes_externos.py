#!/usr/bin/env python3
"""Saca de la instancia kg-demo las imágenes y las salidas reales de la guía de lo que está fuera de la plataforma
(DEC-113).

1. Repite el ensayo de la guía 3 (imagenes_plataforma.py, con SALIDA en una carpeta temporal para no pisar sus
   imágenes) mientras escucha el canal de alarmas de Redis, y comprueba que la historia sea la misma de la guía 3
   (PED-0252, SOL-034, COT-023 y COT-024, OC-0013 y los siete correos).
2. Corre, tal como las muestra la guía, las consultas a lo que no se publica en el equipo (los dos WireMock, Redis,
   el almacén de objetos, Kafka, PostgreSQL, Elasticsearch y nuestro servicio SOAP) y guarda su salida.
3. Saca las imágenes de Mailpit (las cabeceras del correo certificado y el correo de promociones).
4. El paso opcional del error real: pausa Mailpit, el jefe de cocina registra una solicitud, el error llega a
   Elasticsearch y a «Logs de errores», y al quitar la pausa la cola de errores reenvía el correo.

Deja en img/externos/ los PNG y datos.json, que lee generar_guia_externos.py. Cambia los datos de kg-demo: al
terminar hay que restablecerla con restablecer_demo.sh.

Uso, desde FinalProyecto, con kg-demo recién restablecida y encendida (y ~75 s de espera por Eureka):
    COMPOSE_PROJECT_NAME=kg-demo mockups_nuevos/.venv/bin/python documentos/guias_sustentacion/fuente/imagenes_externos.py
"""
import hashlib, json, os, re, subprocess, sys, tempfile, time, urllib.error, urllib.request
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont
from playwright.sync_api import sync_playwright

if os.environ.get('COMPOSE_PROJECT_NAME', '') != 'kg-demo':
    sys.exit('Solo corre sobre la instancia kg-demo (COMPOSE_PROJECT_NAME=kg-demo): cambia sus datos.')
AQUI = Path(__file__).resolve().parent
RAIZ = AQUI.parents[2]                                   # FinalProyecto
OUT = AQUI.parent / 'img' / 'externos'
GUIA3 = AQUI.parent / 'img' / 'plataforma' / 'datos.json'
COMPOSE = 'KonradGourmet/plataforma/docker-compose.yml'
BASE, MAILPIT = 'http://localhost:8080', 'http://localhost:8025'
CLAVE = 'Konrad2026'
JEFE, ADMIN = 'carlos.mendoza@konradgourmet.com', 'paula.herrera@konradgourmet.com'
W, H, R = 1440, 900, 17
FUENTE = ImageFont.truetype('/usr/share/fonts/liberation/LiberationSans-Bold.ttf', 19)
# Los atajos que la guía pide definir en la terminal antes de entrar (sección «Antes de entrar»).
ATAJOS = (f"kg() {{ COMPOSE_PROJECT_NAME=kg-demo docker compose -f {COMPOSE} \"$@\"; }}\n"
          "kgs3() { kg run --rm --no-deps --entrypoint aws s3-bucket --endpoint-url http://s3:8333 \"$@\"; }\n")
datos = {'imagenes': {}, 'salidas': {}, 'hechos': {}}
fallas = []


def ok(cond, texto):
    print(('  ✓ ' if cond else '  ✗ ') + texto)
    if not cond:
        fallas.append(texto)


def terminal(clave, comando, esperar_error=False):
    """Corre el comando tal como lo muestra la guía (con los atajos kg y kgs3) y guarda lo que imprime."""
    real = re.sub(r'\bkg exec (?!-T)', 'kg exec -T ', comando)      # sin terminal interactiva
    r = subprocess.run(['bash', '-c', ATAJOS + real], cwd=RAIZ, capture_output=True, text=True, timeout=120)
    salida = r.stdout if not esperar_error else (r.stdout + r.stderr)
    salida = '\n'.join(l for l in salida.rstrip().split('\n') if not re.match(r'\s*Container kg-demo-', l))
    ok((r.returncode != 0) if esperar_error else (r.returncode == 0), f'{clave}: {"falla como se espera" if esperar_error else "responde"}')
    datos['salidas'][clave] = {'comando': comando, 'salida': salida}
    return salida


def http(metodo, ruta, cuerpo=None, token=None, base=BASE):
    pet = urllib.request.Request(base + ruta, method=metodo, data=json.dumps(cuerpo).encode() if cuerpo is not None else None,
                                 headers={'Content-Type': 'application/json', **({'Authorization': f'Bearer {token}'} if token else {})})
    with urllib.request.urlopen(pet, timeout=30) as r:
        return json.loads(r.read() or b'null')


def token(correo):
    return http('POST', '/api/auth/token', {'correo': correo, 'clave': CLAVE})['jwt']


def correos():
    return http('GET', '/api/v1/messages?limit=50', base=MAILPIT)['messages']


def esperar(cond, segundos):
    fin = time.time() + segundos
    while time.time() < fin:
        if cond():
            return True
        time.sleep(1)
    return False


def foto(page, nombre, pines=()):
    page.wait_for_timeout(500)
    ruta = OUT / f'{nombre}.png'
    page.screenshot(path=str(ruta))
    img = Image.open(ruta).convert('RGB')
    marcas, dib = [], ImageDraw.Draw(img)
    for n, loc in pines:
        caja = loc.first.bounding_box()
        if caja is None:
            ok(False, f'{nombre}: el círculo {n} no encontró su elemento')
            continue
        x, y = min(max(caja['x'] + 2, R + 2), img.width - R - 2), min(max(caja['y'] + 2, R + 2), img.height - R - 2)
        dib.ellipse((x - R, y - R, x + R, y + R), fill=(216, 27, 96), outline=(255, 255, 255), width=3)
        dib.text((x, y), str(n), font=FUENTE, fill='white', anchor='mm')
        marcas.append([n, round(x), round(y)])
    img.save(ruta)
    datos['imagenes'][nombre] = {'ancho': img.width, 'alto': img.height, 'pines': marcas, 'diametro': 2 * R}
    print(f'     imagen {nombre}.png ({img.width}×{img.height}, {len(marcas)} círculos)')


OUT.mkdir(parents=True, exist_ok=True)
for viejo in OUT.glob('*.png'):
    viejo.unlink()
hechos = datos['hechos']

# ════════════ 0. La instancia tiene que estar recién restablecida ════════════
print('-- 0. Estado inicial')
cuenta = lambda: json.loads(terminal('_cuenta', "kg exec ministerio curl -s -X POST localhost:8080/__admin/requests/count "
                                     "-d '{\"method\":\"POST\",\"url\":\"/precios\"}'"))['count']
ok(cuenta() == 0, 'el Ministerio no ha recibido llamadas')
ok(terminal('_cache', "kg exec redis redis-cli --scan --pattern 'precio-referencia:*'") == '',
   'la caché de precios está vacía (restablecer_demo.sh)')
s = terminal('_kafka', 'kg exec kafka /opt/kafka/bin/kafka-get-offsets.sh --bootstrap-server localhost:9092 --topic orden.generada')
ok(sum(int(l.split(':')[-1]) for l in s.split('\n') if l.count(':') == 2) == 0, 'el tópico orden.generada está vacío (restablecer_demo.sh)')
datos['salidas'].pop('_kafka', None)
if fallas:
    sys.exit('kg-demo no está recién restablecida: correr restablecer_demo.sh y levantar.sh --sin-compilar.')

# ════════════ 1. El ensayo de la guía 3, escuchando el canal de alarmas ════════════
print('-- 1. Ensayo de la guía 3 (imagenes_plataforma.py) con SUBSCRIBE alarmas:stock-bajo')
temporal = tempfile.mkdtemp(prefix='ensayo-guia3-')
canal = open(Path(temporal) / 'canal.txt', 'w')
escucha = subprocess.Popen(['bash', '-c', ATAJOS + 'kg exec -T redis redis-cli SUBSCRIBE alarmas:stock-bajo'],
                           cwd=RAIZ, stdout=canal, stderr=subprocess.DEVNULL, start_new_session=True)
r = subprocess.run([sys.executable, str(AQUI / 'imagenes_plataforma.py')], cwd=RAIZ,
                   env={**os.environ, 'SALIDA': temporal}, capture_output=True, text=True)
print('\n'.join(r.stdout.strip().split('\n')[-3:]))
ok(r.returncode == 0, 'el ensayo de la guía 3 terminó sin fallas')
os.killpg(escucha.pid, 15)
escucha.wait(timeout=10)
canal.close()
subprocess.run(['bash', '-c', ATAJOS + 'kg exec -T redis pkill -f "redis-cli SUBSCRIBE"'], cwd=RAIZ, capture_output=True)
alarmas = (Path(temporal) / 'canal.txt').read_text().strip().split('\n')
mensajes = [l for l in alarmas if l.startswith('{')]
ok(len(mensajes) == 1, f'el canal recibió una alarma ({len(mensajes)})')
datos['salidas']['r_alarma'] = {'comando': 'kg exec redis redis-cli SUBSCRIBE alarmas:stock-bajo', 'salida': '\n'.join(alarmas)}
historia = json.load(open(Path(temporal) / 'datos.json', encoding='utf-8'))['historia']
guia3 = json.load(open(GUIA3, encoding='utf-8'))['historia']
for k in guia3:
    if k not in ('auditoria', 'logs'):
        ok(historia.get(k) == guia3[k], f'la historia coincide con la guía 3: {k}')
hechos['historia'] = {k: historia[k] for k in ('pedido', 'solicitud', 'cotizaciones', 'orden', 'orden_total', 'alarma', 'correos')}
alarma = json.loads(mensajes[0]) if mensajes else {}
ok(alarma.get('contenido') == historia['alarma'] and alarma.get('destino') == JEFE, 'la alarma del canal es la de la historia')

# ════════════ 2. Ministerio de Comercio (WireMock) ════════════
print('-- 2. Ministerio de Comercio')
hechos['ministerio_llamadas'] = cuenta()
ok(hechos['ministerio_llamadas'] == 1, f'dos cotizaciones de res y una sola llamada al Ministerio ({hechos["ministerio_llamadas"]})')
datos['salidas']['m_cuenta'] = datos['salidas'].pop('_cuenta')
terminal('m_wsdl', "kg exec ministerio curl -s 'localhost:8080/precios?wsdl'")
SOBRE = ('<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/" '
         'xmlns:min="http://precios.ministerio-comercio.simulado/"><soap:Body><min:consultarPrecioEstandar>'
         '<codigoProducto>{}</codigoProducto></min:consultarPrecioEstandar></soap:Body></soap:Envelope>')
s = terminal('m_precio', "kg exec ministerio curl -s -w '\\n%{http_code} en %{time_total} s\\n' -H 'Content-Type: text/xml' "
             f"localhost:8080/precios -d '{SOBRE.format(1)}'")
ok('<precio>20000.0</precio>' in s and '\n200 en 0.3' in s, 'la res cuesta 20000.0 y responde en unos 300 ms')
s = terminal('m_fault', "kg exec ministerio curl -s -w '\\n%{http_code} en %{time_total} s\\n' -H 'Content-Type: text/xml' "
             f"localhost:8080/precios -d '{SOBRE.format(99)}'")
ok('soap:Fault' in s and 'producto 99' in s and '\n500 en' in s, 'un producto sin precio recibe un SOAP Fault (500)')

# ════════════ 3. Redis ════════════
print('-- 3. Redis')
terminal('r_llaves', "kg exec redis redis-cli --scan --pattern 'precio-referencia:*'")
s = terminal('r_get', 'kg exec redis redis-cli GET precio-referencia:1')
ok('"valor":"20000.0"' in s, 'la caché guarda el precio de la res')
s = terminal('r_ttl', 'kg exec redis redis-cli TTL precio-referencia:1')
ok(80000 < int(s or 0) <= 86400, f'la llave vence en menos de un día ({s} s)')
try:
    http('GET', '/api/solicitudes/mias', token=token(JEFE))   # cualquier petición con sesión crea el balde del jefe
except urllib.error.HTTPError:
    pass
s = terminal('r_limite', "kg exec redis redis-cli --scan --pattern 'request_rate_limiter*'")
ok('usuario:' in s, 'los baldes del límite de tasa están en Redis')

# ════════════ 4. Redes sociales (WireMock) ════════════
print('-- 4. Redes sociales')
CUENTA_REDES = ("kg exec redes curl -s -X POST localhost:8080/__admin/requests/count "
                "-d '{\"method\":\"GET\",\"urlPath\":\"/v1/tendencias\"}'")
antes = json.loads(terminal('n_cuenta', CUENTA_REDES))['count']
time.sleep(31)
despues = json.loads(terminal('n_cuenta2', CUENTA_REDES))['count']
ok(despues > antes, f'TendenciasJob consulta la API cada 30 s ({antes} → {despues})')
hechos['redes_llamadas'] = [antes, despues]
URL_REDES = "'localhost:8080/v1/tendencias?pais=CO&categoria=gastronomia&periodo=semana'"
s = terminal('n_tendencias', "kg exec redes curl -s -w '\\n%{http_code} en %{time_total} s\\n' "
             f"-H 'Authorization: Bearer konrad-redes-local' {URL_REDES}")
ok('"Ajiaco"' in s and '\n200 en 0.2' in s, 'las tendencias responden con el token, en unos 200 ms')
s = terminal('n_401', f"kg exec redes curl -s -w '\\n%{{http_code}}\\n' {URL_REDES}")
ok(s.endswith('401'), 'sin token responde 401')
promo = [m for m in correos() if m['Subject'] == 'Promociones sugeridas']
ok(len(promo) == 1, 'llegó un correo «Promociones sugeridas»')
hechos['promociones'] = http('GET', f'/api/v1/message/{promo[0]["ID"]}', base=MAILPIT)['Text'] if promo else ''

# ════════════ 5. Nuestro servicio SOAP (lo consumen otros sistemas) ════════════
print('-- 5. Servicio SOAP de ms-integracion')
s = terminal('s_401', "curl -s -o /dev/null -w '%{http_code}\\n' 'localhost:8080/api/integracion/abastecimiento?wsdl'")
ok(s == '401', 'sin token, el gateway responde 401')
TOKEN = ("T=$(curl -s localhost:8080/api/auth/token -H 'Content-Type: application/json' "
         "-d '{\"correo\":\"isabella.castro@konradgourmet.com\",\"clave\":\"Konrad2026\"}' "
         "| python3 -c 'import sys,json; print(json.load(sys.stdin)[\"jwt\"])')\n")
s = terminal('s_wsdl', TOKEN + "curl -s -H \"Authorization: Bearer $T\" 'localhost:8080/api/integracion/abastecimiento?wsdl'")
ok('consultarPrecioSugerido' in s and 'AbastecimientoService' in s, 'el WSDL publica consultarPrecioSugerido')
llamadas = cuenta()
s = terminal('s_precio', TOKEN + "curl -s -H \"Authorization: Bearer $T\" -H 'Content-Type: text/xml' "
             "localhost:8080/api/integracion/abastecimiento -d '<soap:Envelope "
             "xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body><a:consultarPrecioSugerido "
             "xmlns:a=\"http://abastecimiento.konradgourmet.com/\"><productoId>1</productoId></a:consultarPrecioSugerido>"
             "</soap:Body></soap:Envelope>'")
ok('<precioSugerido>20000.0</precioSugerido>' in s, 'nuestro SOAP responde el precio de la res')
ok(cuenta() == llamadas, 'y lo toma de la caché: el Ministerio no recibe otra llamada')
datos['salidas'].pop('_cuenta', None)
datos['salidas'].pop('_cache', None)

# ════════════ 6. Almacén de objetos (SeaweedFS) ════════════
print('-- 6. Almacén de objetos')
s = terminal('o_buckets', 'kgs3 s3 ls')
ok('documentos-pdf' in s and 'imagenes-platos' in s, 'los dos buckets')
s = terminal('o_pdf', 'kgs3 s3 ls --recursive s3://documentos-pdf')
for f in ('cotizaciones/COT-023.pdf', 'cotizaciones/COT-024.pdf', 'ordenes/OC-0013.pdf'):
    ok(f in s, f'el PDF {f} está en documentos-pdf')
terminal('o_img', 'kgs3 s3 ls --recursive s3://imagenes-platos')

# ════════════ 7. Kafka ════════════
print('-- 7. Kafka')
s = terminal('k_topicos', 'kg exec kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --list')
topicos = [t for t in s.split('\n') if not t.startswith('__')]
ok(len(topicos) == 11 and 'notificaciones.dlq' in topicos, f'10 tópicos y la cola de errores ({len(topicos)})')
s = terminal('k_atraso', 'kg exec kafka /opt/kafka/bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 '
             "--describe --group ms-notificaciones | awk 'NF {print $2, $3, $6}'")
atrasos = [l.split()[-1] for l in s.split('\n')[1:] if l.strip()]
ok(atrasos and set(atrasos) <= {'0', '-'} and '0' in atrasos,
   'ms-notificaciones no tiene eventos pendientes (LAG 0; «-» es una partición que nunca recibió eventos)')
s = terminal('k_evento', 'kg exec kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 '
             '--topic orden.generada --from-beginning --timeout-ms 8000')
ok('s3://documentos-pdf/ordenes/OC-0013.pdf' in s, 'orden.generada lleva solo la dirección del PDF (claim check)')
ok(len([l for l in s.split('\n') if l.startswith('{')]) == 1, 'el tópico tiene un solo evento: el de OC-0013')
datos['salidas']['k_evento']['salida'] = '\n'.join(l for l in s.split('\n') if not l.startswith(('[', 'org.apache.kafka.common.errors.TimeoutException')))
s = terminal('k_dlq', 'kg exec kafka /opt/kafka/bin/kafka-get-offsets.sh --bootstrap-server localhost:9092 --topic notificaciones.dlq')
hechos['dlq_antes'] = sum(int(l.split(':')[-1]) for l in s.split('\n') if l.count(':') == 2)

# ════════════ 8. PostgreSQL ════════════
print('-- 8. PostgreSQL')
s = terminal('p_bases', "kg exec postgres psql -U postgres -Atc \"select datname from pg_database where datname like 'ms\\_%' order by 1\"")
ok(len(s.split('\n')) == 11, f'once bases, una por servicio ({len(s.split())})')
s = terminal('p_orden', 'kg exec postgres psql -U postgres -d ms_ordenes -c '
             '"select numero, cotizacion_numero, proveedor, pdf_url, estado from orden_compra order by numero"')
ok('OC-0013' in s and 'COT-023' in s and 'FINALIZADA' in s, 'OC-0013 sobre COT-023, FINALIZADA')
s = terminal('p_outbox', 'kg exec postgres psql -U postgres -d ms_ordenes -Atc "select count(*) from outbox"')
ok(s == '0', 'la outbox de ms-ordenes está vacía: todo se publicó')
s = terminal('p_ajena', "kg exec postgres psql -U ms_pedidos -d ms_ordenes -c 'select 1'", esperar_error=True)
ok('permission denied for database "ms_ordenes"' in s, 'ms_pedidos no puede entrar a la base de ms-ordenes')

# ════════════ 9. Elasticsearch antes del error ════════════
print('-- 9. Elasticsearch')
s = terminal('e_indices0', "kg exec elasticsearch curl -s 'localhost:9200/_cat/indices?v'")
ok('logs-errores' not in s, 'todavía no hay índice logs-errores: ningún servicio ha fallado')

# ════════════ 10. Mailpit ════════════
print('-- 10. Mailpit')
lista = correos()
ok([m['Subject'] for m in lista] == [c['asunto'] for c in historia['correos']], 'los siete correos de la historia')
cot = next(m for m in lista if m['Subject'] == 'Solicitud de cotización COT-023')
cab = http('GET', f'/api/v1/message/{cot["ID"]}/headers', base=MAILPIT)
txt = http('GET', f'/api/v1/message/{cot["ID"]}', base=MAILPIT)
para = cot['To'][0]['Address']
huella = hashlib.sha256((para + '\n' + txt['Text']).encode()).hexdigest()[:16]
ok(cab['X-Certificado'] == [f'SHA-256:{huella}'], f'la huella del destinatario y el cuerpo es la de la cabecera ({huella})')
ok(re.fullmatch(r'\d{4}-\d\d-\d\dT\d\d:\d\d:\d\d-05:00', cab['X-Sello-Tiempo'][0]) is not None, 'el sello de tiempo es de Bogotá')
hechos['cotizacion_correo'] = {'para': para, 'cuerpo': txt['Text'], 'certificado': cab['X-Certificado'][0],
                               'sello': cab['X-Sello-Tiempo'][0], 'adjuntos': [a['FileName'] for a in txt['Attachments']]}
terminal('c_huella', f"printf '%s\\n%s' '{para}' '{txt['Text']}' | sha256sum | cut -c1-16")
ok(datos['salidas']['c_huella']['salida'] == huella, 'sha256sum da la misma huella')
hechos['correos'] = []
for m in lista:
    det = http('GET', f'/api/v1/message/{m["ID"]}', base=MAILPIT)
    hechos['correos'].append({'asunto': m['Subject'], 'para': m['To'][0]['Address'],
                              'adjuntos': [a['FileName'] for a in det['Attachments']], 'cuerpo': det['Text']})

with sync_playwright() as pw:
    nav = pw.chromium.launch(executable_path='/usr/bin/chromium')
    ctx = lambda: nav.new_context(viewport={'width': W, 'height': H}, locale='es-CO', timezone_id='America/Bogota').new_page()
    p = ctx()
    p.goto(f'{MAILPIT}/view/{cot["ID"]}'); p.wait_for_timeout(1500)
    p.get_by_role('tab', name='Headers').click(); p.wait_for_timeout(1000)
    foto(p, 'e01_cabeceras', [(1, p.get_by_text('X-Certificado', exact=True)), (2, p.get_by_text('X-Sello-Tiempo', exact=True)),
                              (3, p.get_by_text('Attachment (1)'))])
    p.goto(f'{MAILPIT}/view/{promo[0]["ID"]}'); p.wait_for_timeout(1500)
    foto(p, 'e02_promociones', [(1, p.locator('#nav-plain-text'))])

    # ════════════ 11. El error real (paso opcional) ════════════
    print('-- 11. Error real: Mailpit en pausa')
    terminal('x_pausa', 'kg pause correo')
    inicio = time.time()
    sol = http('POST', '/api/solicitudes', {'items': [{'productoId': 2, 'cantidad': 5, 'marca': 'Diana', 'unidad': 'kg'}]},
               token=token(JEFE))
    hechos['error_solicitud'] = sol['numero']
    cuenta_es = lambda: json.loads(subprocess.run(['bash', '-c', ATAJOS + "kg exec -T elasticsearch curl -s "
                                    "'localhost:9200/logs-errores/_count'"], cwd=RAIZ, capture_output=True, text=True).stdout or '{}').get('count', 0)
    ok(esperar(lambda: cuenta_es() > 0, 90), 'el error llegó a Elasticsearch')
    hechos['error_segundos'] = round(time.time() - inicio)
    s = terminal('e_error', "kg exec elasticsearch curl -s 'localhost:9200/logs-errores/_search?pretty&size=1&sort=fechaHora:desc"
                 "&filter_path=hits.hits._source&_source_excludes=traza,hilo,logger'")
    ok('solicitud.registrada' in s and 'notificaciones.dlq' in s and 'correlacionId' in s,
       'el error dice qué evento falló, que pasó a la cola de errores y su correlación')
    terminal('e_indices1', "kg exec elasticsearch curl -s 'localhost:9200/_cat/indices?v'")
    a = ctx()
    a.goto(BASE + '/login'); a.fill('#email', ADMIN); a.fill('#password', CLAVE); a.click('button.btn-login')
    a.wait_for_url(re.compile('/inicio')); a.goto(BASE + '/admin/logs'); a.wait_for_selector('table tbody tr td'); a.wait_for_timeout(1000)
    fila = a.locator('table tbody tr', has_text='ms-notificaciones')
    ok(fila.count() >= 1, '«Logs de errores» muestra el error de ms-notificaciones')
    foto(a, 'e03_logs_error', [(1, fila.locator('td').nth(2)), (2, fila.locator('td').nth(3))])
    s = terminal('k_dlq2', 'kg exec kafka /opt/kafka/bin/kafka-get-offsets.sh --bootstrap-server localhost:9092 --topic notificaciones.dlq')
    hechos['dlq_despues'] = sum(int(l.split(':')[-1]) for l in s.split('\n') if l.count(':') == 2)
    ok(hechos['dlq_despues'] == hechos['dlq_antes'] + 1, 'el evento pasó a la cola de errores')
    terminal('x_reanuda', 'kg unpause correo')
    inicio = time.time()
    asunto = f'Solicitud {sol["numero"]} registrada'
    ok(esperar(lambda: any(m['Subject'] == asunto for m in correos()), 120), f'al volver Mailpit llega «{asunto}»')
    hechos['reenvio_segundos'] = round(time.time() - inicio)
    hechos['correos_al_final'] = len(correos())
    ok(hechos['correos_al_final'] == len(historia['correos']) + 1, 'sin correos repetidos')
    p.goto(MAILPIT + '/'); p.wait_for_timeout(1500)
    foto(p, 'e04_mailpit_reenvio', [(1, p.locator('.message', has_text=asunto).first)])
    nav.close()

json.dump(datos, open(OUT / 'datos.json', 'w', encoding='utf-8'), ensure_ascii=False, indent=1)
print('\nFALLAS:' if fallas else '\nTodo bien.', *fallas, sep='\n  ')
sys.exit(1 if fallas else 0)
