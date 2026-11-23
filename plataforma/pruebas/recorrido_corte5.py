#!/usr/bin/env python3
# Recorrido del corte 5 en el navegador (Playwright), en escritorio (1440 px) y a 390 px:
#   escritorio: la auxiliar de compras envía COT-022 al Proveedor B con SOL-031 y V14 marca «Correo enviado»
#   cuando ms-notificaciones lo envía (DEC-76). La directora registra sus valores (res $22.000, arroz $8.000) y
#   valida: OPCIONADA, +7,69 %; desde V19, «Ver cotizaciones opcionadas» abre V20, que compara COT-018 y
#   COT-022 de SOL-031. Escoge COT-018, la de menor diferencia, y genera OC-0012 (V21 → V22, $545.000); «VER
#   ORDEN» abre V23 con el detalle, «Ver PDF» y «Descargar» (DEC-75). COT-018 sale de V20 (CON_ORDEN), V17 la
#   muestra «Con orden», V16 cuenta 1 orden y el jefe ve SOL-031 COMPLETADA. Alarma en vivo (HU-16, PD-01): con
#   un pedido de 50 pechugas, el cierre del día deja el pollo bajo el 25 % y la alarma llega por WebSocket a V07,
#   sin recargar; V05 la lista.
#   390 px: V20 (solo COT-022), V21 abierta, V23 con el detalle; la directora marca OC-0012 como recibida y pasa
#   a FINALIZADA (DEC-80); y la alarma de la papa (70 ajiacos), que cabe a lo ancho y con «Solicitar» abre V08.
# Que nada se desborde a lo ancho y que la consola quede limpia.
# Corre después de recorrido_corte4b.py y verificar_corte4b.py (COT-018 OPCIONADA; COT-021 ya existe).
# Uso: python recorrido_corte5.py <carpeta-de-capturas>   (necesita playwright y /usr/bin/chromium;
#      BASE=http://localhost:8080 por omisión). Deja en la carpeta corte5.json con la correlación de cada
#      petición y el PDF descargado, que verificar_corte5.py revisa en PostgreSQL, Kafka, el almacén y Mailpit.
import json, os, re, sys, time, urllib.request
from playwright.sync_api import sync_playwright, expect

BASE = os.environ.get('BASE', 'http://localhost:8080')
OUT = sys.argv[1]
os.makedirs(OUT, exist_ok=True)
MESERO, JEFE = 'isabella.torres@konradgourmet.com', 'carlos.mendoza@konradgourmet.com'
AUXILIAR, DIRECTORA = 'isabella.castro@konradgourmet.com', 'laura.gomez@konradgourmet.com'
fallas, registro = [], {'generadas': {}, 'validadas': {}, 'ordenes': {}, 'alarmas': []}

def ok(cond, texto):
    print(('  ✓ ' if cond else '  ✗ ') + texto)
    if not cond: fallas.append(texto)

def entrar(page, correo):
    page.goto(BASE + '/login')
    page.fill('#email', correo)
    page.fill('#password', 'Konrad2026')
    page.click('button.btn-login')
    page.wait_for_url(re.compile(r'/inicio'))

def foto(page, nombre):
    page.wait_for_timeout(400)
    page.screenshot(path=f'{OUT}/{nombre}.png', full_page=True)

def sin_desborde(page, ancho, nombre):
    doc = page.evaluate('document.documentElement.scrollWidth')
    ok(doc <= ancho + 1, f'{nombre}: sin desplazamiento horizontal ({doc} ≤ {ancho})')

def tarjeta(page, titulo):
    return page.locator('.card', has=page.locator('h2', has_text=titulo))

def http(metodo, ruta, cuerpo=None, token=None):
    pet = urllib.request.Request(BASE + ruta, method=metodo, data=json.dumps(cuerpo).encode() if cuerpo is not None else None,
                                 headers={'Content-Type': 'application/json', **({'Authorization': f'Bearer {token}'} if token else {})})
    try:
        with urllib.request.urlopen(pet, timeout=20) as r:
            return r.status, json.loads(r.read() or b'null')
    except urllib.error.HTTPError as e:
        return e.code, None

def token(correo):
    return http('POST', '/api/auth/token', {'correo': correo, 'clave': 'Konrad2026'})[1]['jwt']

def pedido_grande(plato, cantidad, mesa):
    """El mesero registra por la API un pedido de muchos platos iguales: prepara el cruce del umbral."""
    t = token(MESERO)
    carta = {p['nombre']: p for p in http('GET', '/api/pedidos/catalogo', token=t)[1]}
    p = carta[plato]
    estado, cuerpo = http('POST', '/api/pedidos', {'mesa': mesa, 'detalles': [
        {'platoId': p['platoId'], 'cantidad': cantidad, 'nota': '', 'precioUnitario': p['precio']}]}, t)
    ok(estado == 201, f'preparación: el mesero registra {cantidad} × {plato} en la mesa {mesa} ({estado}, {cuerpo and cuerpo["numero"]})')

def alarma_en_vivo(jefe, tag, producto, ancho, solicitar=False):
    """El jefe ejecuta el cierre del día en V07 y la alarma llega por WebSocket (sin recargar la página). El
    porcentaje depende de lo que vendieron los recorridos anteriores: basta con que quede bajo el 25 %."""
    jefe.goto(BASE + '/inventario'); jefe.wait_for_selector('table')
    jefe.wait_for_timeout(1200)  # el WebSocket ya abrió
    with jefe.expect_response(lambda r: r.url.endswith('/api/inventario/cierre')) as resp:
        jefe.get_by_role('button', name=re.compile('Cierre del día')).click()
    ok(resp.value.status in (200, 204), f'{tag} V07: «Cierre del día» → {resp.value.status}')
    alarma = jefe.locator('.alarma-viva', has_text=producto)
    expect(alarma).to_be_visible(timeout=15000)
    aviso = alarma.locator('p').inner_text()
    m = re.fullmatch(rf'{producto} al (\d+(?:,\d+)?) %: realice una solicitud', aviso)
    ok(m is not None and float(m.group(1).replace(',', '.')) < 25, f'{tag}: la alarma llega en vivo a V07 por WebSocket, sin recargar: «{aviso}»')
    registro['alarmas'].append({'producto': producto, 'aviso': aviso})
    caja = alarma.bounding_box()
    ok(caja is not None and caja['x'] >= 0 and caja['x'] + caja['width'] <= ancho, f'{tag}: la alarma cabe a lo ancho ({ancho} px)')
    sin_desborde(jefe, ancho, f'{tag} V07 con la alarma')
    foto(jefe, f'{tag}-V07-alarma-en-vivo-{producto.lower()}')
    if solicitar:
        alarma.get_by_role('button', name='Solicitar').click()
        jefe.wait_for_url(re.compile(r'/solicitudes/nueva'))
        expect(jefe.locator('[role=dialog]', has=jefe.locator('h2', has_text='Nueva solicitud de alimentos'))).to_be_visible()
        ok(True, f'{tag}: «Solicitar» lleva directo al formulario de solicitud (V08, HU-16)')
        foto(jefe, f'{tag}-V08-desde-la-alarma')
    jefe.goto(BASE + '/inicio'); jefe.wait_for_selector('.alert-list')
    jefe.wait_for_function(f"() => [...document.querySelectorAll('.alert-row b')].some(b => b.innerText === '{producto}')", timeout=10000)
    ok(True, f'{tag} V05: la alerta de {producto} está en la lista (ms-inventario la guardó)')
    return alarma

with sync_playwright() as pw:
    nav = pw.chromium.launch(executable_path='/usr/bin/chromium')
    for ancho, alto, tag in ((1440, 900, 'escritorio'), (390, 844, 'movil')):
        print(f'== {tag} ({ancho} px)')
        errores = []
        ctx = {q: nav.new_context(viewport={'width': ancho, 'height': alto}, locale='es-CO', timezone_id='America/Bogota', accept_downloads=True)
               for q in ('jefe', 'aux', 'dir')}
        jefe, aux, dire = ctx['jefe'].new_page(), ctx['aux'].new_page(), ctx['dir'].new_page()
        for p in (jefe, aux, dire):
            p.on('console', lambda m: errores.append(m.text) if m.type == 'error' else None)
        entrar(aux, AUXILIAR); entrar(jefe, JEFE); entrar(dire, DIRECTORA)

        if tag == 'escritorio':
            # ── Auxiliar: COT-022 al Proveedor B, y V14 con el correo ya enviado ──
            aux.goto(BASE + '/cotizaciones'); aux.wait_for_selector('tr:has-text("COT-018")')
            aux.get_by_role('button', name='Generar cotización').first.click()
            v13 = aux.locator('[role=dialog]')
            v13.get_by_label('Incluir SOL-031').check()
            v13.get_by_label('Proveedor').select_option(label='Proveedor B')
            with aux.expect_response(lambda r: r.url.endswith('/api/cotizaciones') and r.request.method == 'POST') as resp:
                v13.get_by_role('button', name='GENERAR Y ENVIAR COTIZACIÓN').click()
            r = resp.value
            cot22 = r.json()['numero']
            ok(r.status == 201 and r.json()['estado'] == 'ENVIADA', f'V13: {cot22} ENVIADA al Proveedor B')
            registro['generadas'][cot22] = {'correlacion': r.headers.get('x-correlation-id'), 'proveedor': 'Proveedor B'}
            v14 = aux.locator('.ticket-modal')
            expect(v14).to_be_visible()
            correo = v14.locator('.ticket-checks span', has_text='Correo enviado')
            expect(correo.locator('.visto')).to_be_visible(timeout=15000)
            ok(True, f'V14: «Correo enviado» se marca cuando ms-notificaciones envía {cot22} al proveedor (DEC-76)')
            ok('enviado al proveedor' in v14.inner_text(), 'V14: «El PDF fue generado y enviado al proveedor.»')
            foto(aux, f'{tag}-V14-{cot22}-correo-enviado')
            v14.get_by_role('button', name='Cerrar').click()

            # ── Directora: valores y validación de COT-022, y V19 → V20 ──
            dire.goto(BASE + f'/cotizaciones/pendientes?sel={cot22}&valores={cot22}')
            v18 = dire.locator('[role=dialog]', has=dire.locator('h2', has_text='Registrar valores de cotización'))
            expect(v18).to_be_visible()
            v18.get_by_label(re.compile('Precio del proveedor por .* de Res')).fill('22000')
            v18.get_by_label(re.compile('Precio del proveedor por .* de Arroz')).fill('8000')
            with dire.expect_response(lambda r: r.url.endswith(f'/{cot22}/validacion')) as resp:
                v18.get_by_role('button', name='Validar cotización').click()
            ok(resp.value.json()['estado'] == 'OPCIONADA', f'V19: {cot22} queda OPCIONADA')
            v19 = dire.locator('.ticket-modal', has=dire.locator('h2', has_text='Validación completada'))
            ok('+7,69 %' in v19.locator('.diff-row').inner_text(), 'V19: +7,69 % contra el Ministerio ($560.000 frente a $520.000)')
            v19.get_by_role('button', name='Ver cotizaciones opcionadas').click()
            dire.wait_for_url(re.compile(r'/cotizaciones/opcionadas'))
            ok(True, 'V19: «Ver cotizaciones opcionadas» lleva a V20')

            # ── V20: comparación de COT-018 y COT-022 de SOL-031 ──
            dire.wait_for_selector(f'tr:has-text("{cot22}")')
            comparacion = tarjeta(dire, 'Comparación de cotizaciones')
            filas = comparacion.locator('tbody tr').all_inner_texts()
            ok(len(filas) == 2 and 'COT-018' in filas[0] and cot22 in filas[1],
               f'V20: compara COT-018 y {cot22} de SOL-031, la de menor diferencia primero ({[f.split(chr(9))[0] for f in filas]})')
            ok('Proveedor ABC' in filas[0] and '$545.000' in filas[0] and '+4,81 %' in filas[0] and '$520.000' in filas[0],
               'V20: COT-018 con el estándar $520.000, la cotización $545.000 y +4,81 %')
            ok('Proveedor B' in filas[1] and '$560.000' in filas[1] and '+7,69 %' in filas[1], f'V20: {cot22} con $560.000 y +7,69 %')
            dire.get_by_label('Ordenar por').select_option(label='Número de cotización')
            ok(comparacion.locator('tbody tr').first.inner_text().startswith('COT-018'), 'V20: se puede ordenar por número, por valor total o por diferencia (HU-17)')
            dire.get_by_label('Ordenar por').select_option(label='Menor valor total')
            comparacion.locator('tr', has=dire.locator('b', has_text='COT-018')).click()
            detalle = tarjeta(dire, 'Detalle de cotización opcionada')
            econ = detalle.locator('.econ').inner_text()
            ok('$520.000' in econ and '$545.000' in econ and '+4,81 %' in econ, 'V20: la comparación económica de COT-018 (Ministerio, proveedor y diferencia)')
            foto(dire, f'{tag}-V20-opcionadas')
            sin_desborde(dire, ancho, 'V20')

            # ── V21 → V22: OC-0012 ──
            detalle.get_by_role('button', name='Seleccionar para compra').click()
            v21 = dire.locator('[role=dialog]', has=dire.locator('h2', has_text='Generar orden de compra'))
            expect(v21).to_be_visible()
            texto = v21.inner_text()
            ok(all(t in texto for t in ('COT-018', 'SOL-031', 'Proveedor ABC', 'Res', '20 kg', '$425.000', 'Arroz', '15 kg', '$120.000', '$545.000')),
               'V21: COT-018 de SOL-031, Proveedor ABC, res 20 kg $425.000, arroz 15 kg $120.000 y total $545.000')
            foto(dire, f'{tag}-V21-generar-orden')
            with dire.expect_response(lambda r: r.url.endswith('/api/ordenes-compra') and r.request.method == 'POST') as resp:
                v21.get_by_role('button', name='Confirmar orden de compra').click()
            r = resp.value
            orden = r.json()
            ok(r.status == 201 and orden['numero'] == 'OC-0012', f'POST /api/ordenes-compra → {r.status} {orden.get("numero")} (sigue la numeración de la demo)')
            registro['ordenes'][orden['numero']] = {'correlacion': r.headers.get('x-correlation-id'), 'cotizacion': 'COT-018'}
            v22 = dire.locator('.ticket-modal', has=dire.locator('h2', has_text='Orden generada'))
            expect(v22).to_be_visible()
            texto = v22.inner_text()
            ok('OC-0012' in texto and 'Proveedor ABC' in texto and 'COT-018' in texto and '$545.000' in texto,
               'V22: OC-0012, Proveedor ABC, COT-018 y $545.000')
            foto(dire, f'{tag}-V22-orden-generada')
            v22.get_by_role('button', name='VER ORDEN').click()

            # ── V23: la orden, su detalle y su PDF ──
            dire.wait_for_url(re.compile(r'/ordenes-compra\?sel=OC-0012'))
            dire.wait_for_selector('.oc-head h3:has-text("OC-0012")')
            det = tarjeta(dire, 'Detalle de orden de compra')
            texto = det.inner_text()
            ok(all(t in texto for t in ('OC-0012', 'EN PROCESO', 'Proveedor ABC', 'SOL-031', 'COT-018', 'Siguiente paso', 'Res', 'Arroz', '$545.000')),
               'V23: el detalle de OC-0012 (EN PROCESO, proveedor, solicitud, cotización, siguiente paso, productos y total)')
            ok(det.get_by_role('button', name='Marcar como recibida').is_visible(), 'V23: la orden EN PROCESO tiene «Marcar como recibida» (DEC-80)')
            lista = tarjeta(dire, 'Listado de órdenes de compra').locator('tbody tr').all_inner_texts()
            ok(len(lista) == 1 and 'OC-0012' in lista[0] and 'Proveedor ABC' in lista[0] and '$545.000' in lista[0], f'V23: la lista trae OC-0012 ({lista})')
            with dire.expect_popup() as pestana:
                det.get_by_role('button', name='Ver PDF').click()
            pdf = pestana.value
            pdf.wait_for_url(re.compile(r'^blob:'), timeout=10000)
            ok(True, f'V23: «Ver PDF» abre el PDF de la orden en otra pestaña ({pdf.url[:30]}…)')
            pdf.close()
            with dire.expect_download() as descarga:
                det.get_by_role('button', name='Descargar').click()
            ruta = f'{OUT}/OC-0012.pdf'
            descarga.value.save_as(ruta)
            ok(open(ruta, 'rb').read(5) == b'%PDF-' and descarga.value.suggested_filename == 'OC-0012.pdf', f'V23: «Descargar» baja {descarga.value.suggested_filename}, un PDF')
            registro['pdf'] = {'numero': 'OC-0012', 'archivo': ruta}
            foto(dire, f'{tag}-V23-ordenes')
            sin_desborde(dire, ancho, 'V23')

            # ── COT-018 sale de V20 (ms-cotizaciones recibió orden.generada: CON_ORDEN) ──
            for _ in range(20):
                dire.goto(BASE + '/cotizaciones/opcionadas'); dire.wait_for_selector(f'tr:has-text("{cot22}")')
                if not dire.locator('tr', has=dire.locator('b', has_text='COT-018')).count(): break
                time.sleep(0.5)
            ok(not dire.locator('tr', has=dire.locator('b', has_text='COT-018')).count(), f'V20: COT-018 ya no sale (CON_ORDEN); queda {cot22}')
            dire.goto(BASE + '/cotizaciones/todas?sel=COT-018'); dire.wait_for_selector('.ticket.side .ticket-code')
            ok('CON ORDEN' in dire.locator('.ticket.side').inner_text(), 'V17: el tiquete de COT-018 dice CON ORDEN')
            dire.goto(BASE + '/inicio'); dire.wait_for_selector('.kpis.four')
            dire.wait_for_function("() => [...document.querySelectorAll('.kpis strong')].every(e => e.innerText !== '—')")
            ordenes_kpi = dire.locator('.kpi', has=dire.locator('span', has_text='Órdenes de compra')).locator('strong').inner_text()
            ok(ordenes_kpi == '1', f'V16: «Órdenes de compra» cuenta las de verdad ({ordenes_kpi})')
            foto(dire, f'{tag}-V16-inicio-directora')

            # ── Jefe: SOL-031 completada (ms-solicitudes recibió orden.generada) ──
            for _ in range(20):
                jefe.goto(BASE + '/solicitudes?sel=SOL-031'); jefe.wait_for_selector('.detail-head h3:has-text("SOL-031")')
                if jefe.locator('.detail-head .badge').first.inner_text().upper() == 'COMPLETADA': break
                time.sleep(0.5)
            ok(jefe.locator('.detail-head .badge').first.inner_text().upper() == 'COMPLETADA', 'V10: SOL-031 pasó a COMPLETADA')
            ok(jefe.locator('tr', has=jefe.locator('b', has_text='SOL-032')).locator('.badge').inner_text().upper() == 'EN TRÁMITE',
               'V10: SOL-032 sigue EN TRÁMITE (OC-0012 no la incluye)')
            foto(jefe, f'{tag}-V10-sol031-completada')

            # ── Alarma de stock bajo en vivo: el pollo cruza el 25 % ──
            pedido_grande('Pechuga a la plancha', 50, 2)
            alarma_en_vivo(jefe, tag, 'Pollo', ancho)
        else:
            # ── Directora a 390 px: V20, V21 y V23 ──
            dire.goto(BASE + '/cotizaciones/opcionadas'); dire.wait_for_selector('tbody tr:has-text("COT-0")')
            filas = tarjeta(dire, 'Comparación de cotizaciones').locator('tbody tr').all_inner_texts()
            ok(len(filas) == 1 and 'Proveedor B' in filas[0], f'V20: a 390 px solo queda la opcionada sin orden ({len(filas)})')
            sin_desborde(dire, ancho, 'V20')
            foto(dire, f'{tag}-V20-opcionadas')
            tarjeta(dire, 'Detalle de cotización opcionada').get_by_role('button', name='Seleccionar para compra').click()
            v21 = dire.locator('[role=dialog]', has=dire.locator('h2', has_text='Generar orden de compra'))
            expect(v21).to_be_visible()
            sin_desborde(dire, ancho, 'V21')
            foto(dire, f'{tag}-V21-generar-orden')
            v21.get_by_role('button', name='Cancelar').click()
            expect(v21).to_have_count(0)
            dire.goto(BASE + '/ordenes-compra'); dire.wait_for_selector('.oc-head h3:has-text("OC-0012")')
            sin_desborde(dire, ancho, 'V23')
            foto(dire, f'{tag}-V23-ordenes')
            # ── V23 a 390 px: el pedido llegó y la directora marca OC-0012 como recibida (DEC-80) ──
            det = tarjeta(dire, 'Detalle de orden de compra')
            det.get_by_role('button', name='Marcar como recibida').click()
            conf = dire.locator('[role=dialog]', has=dire.locator('h2', has_text='Marcar como recibida'))
            expect(conf).to_be_visible()
            ok('OC-0012' in conf.inner_text() and 'Proveedor ABC' in conf.inner_text(), 'V23: la confirmación nombra OC-0012 y al Proveedor ABC')
            sin_desborde(dire, ancho, 'V23 (confirmación)')
            foto(dire, f'{tag}-V23-confirmar-recibida')
            conf.get_by_role('button', name='Cancelar').click()
            expect(conf).to_have_count(0)
            ok('EN PROCESO' in det.inner_text(), 'V23: al cancelar, OC-0012 sigue EN PROCESO')
            det.get_by_role('button', name='Marcar como recibida').click()
            with dire.expect_response(lambda r: r.url.endswith('/api/ordenes-compra/OC-0012/finalizar') and r.request.method == 'PATCH') as resp:
                conf.get_by_role('button', name='Confirmar recepción').click()
            r = resp.value
            ok(r.status == 200 and r.json()['estado'] == 'FINALIZADA', f'PATCH /api/ordenes-compra/OC-0012/finalizar → {r.status} {r.json().get("estado")}')
            registro['finalizada'] = {'numero': 'OC-0012', 'correlacion': r.headers.get('x-correlation-id')}
            expect(dire.locator('#toast')).to_contain_text('La orden OC-0012 quedó finalizada.')
            expect(det.locator('.oc-head')).to_contain_text('FINALIZADA')
            ok(det.get_by_role('button', name='Marcar como recibida').count() == 0 and 'Siguiente paso' not in det.inner_text(),
               'V23: OC-0012 FINALIZADA, sin «Marcar como recibida» ni siguiente paso')
            dire.get_by_label('Estado').select_option(label='Finalizada')
            lista = tarjeta(dire, 'Listado de órdenes de compra').locator('tbody tr').all_inner_texts()
            ok(len(lista) == 1 and 'OC-0012' in lista[0], f'V23: el filtro «Finalizada» trae OC-0012 ({len(lista)})')
            sin_desborde(dire, ancho, 'V23 (finalizada)')
            foto(dire, f'{tag}-V23-finalizada')
            dire.get_by_label('Estado').select_option(label='En proceso')
            ok(tarjeta(dire, 'Listado de órdenes de compra').locator('tbody tr', has=dire.locator('b', has_text='OC-0012')).count() == 0,
               'V23: y el filtro «En proceso» ya no')
            dire.get_by_label('Estado').select_option(label='Todos los estados')
            # ── Jefe a 390 px: la alarma de la papa ──
            pedido_grande('Ajiaco', 70, 3)
            alarma_en_vivo(jefe, tag, 'Papa', ancho, solicitar=True)
            sin_desborde(jefe, ancho, 'V05')
            foto(jefe, f'{tag}-V05-alertas')

        errores = [e for e in errores if not re.search(r'status of (400|401|404|409)', e)]
        ok(not errores, f'sin errores en la consola ({errores[:3]})')
        for c in ctx.values(): c.close()
    nav.close()
json.dump(registro, open(f'{OUT}/corte5.json', 'w'), indent=2)
print('\nFALLAS:' if fallas else '\nTodo bien.', *fallas, sep='\n  ')
sys.exit(1 if fallas else 0)
