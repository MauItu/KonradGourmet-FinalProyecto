#!/usr/bin/env python3
# Recorrido del corte 7 en el navegador (Playwright): una sola historia que cruza los 11 servicios y los 6 roles,
# del plato nuevo a la orden de compra recibida, y que después cada rol ve en su ventana.
#   escritorio (1440 px):
#   1. El jefe de cocina crea en V06 el «Arroz con verduras» (Fuertes, $22.000, 250 g de arroz) con su imagen
#      (ms-menu y el almacén S3); menu.actualizado lo lleva al catálogo del mesero (ms-pedidos), a las recetas
#      (ms-inventario) y a los nombres de ms-analitica.
#   2. El mesero lo pide en V03 para un evento (las unidades que hacen falta para que el arroz cruce el 25 %) y V04
#      confirma el pedido (ms-pedidos, pedido.registrado).
#   3. El jefe ejecuta el cierre del día en V07 (ms-inventario): el arroz queda bajo el 25 % y la alarma llega en
#      vivo por WebSocket (ms-notificaciones, Redis); «Solicitar» abre V08 con el arroz ya escogido (DEC-100) y
#      el jefe pide 40 kg (V09, ms-solicitudes); la alerta del arroz sale de V05.
#   4. La auxiliar de compras, desde V12, cotiza la solicitud con el Proveedor ABC y el Proveedor C (V13 y V14 con
#      «Correo enviado»): ms-cotizaciones pide el precio del Ministerio a ms-integracion (sin la caché, así que
#      llama al Ministerio una vez), guarda el PDF y ms-notificaciones lo envía; el jefe ve la solicitud EN TRÁMITE.
#   5. La directora de compras registra los valores (ABC $8.300 y C $7.900 por kilo) y valida: las dos OPCIONADAS
#      (+3,75 % y −1,25 %); V20 las compara, escoge la del Proveedor C y genera la orden (V21, V22 y V23 con su PDF,
#      ms-ordenes); la marca como recibida y queda FINALIZADA. El jefe ve la solicitud COMPLETADA.
#   6. El director comercial consulta el tablero BAM de hoy (ms-analitica): el arroz con verduras en la demanda y
#      el Proveedor C con mejores precios.
#   7. La administradora ve los servicios activos en Eureka, la historia en «Auditoría» (ms-administracion) y los
#      correos de la historia en la libreta de destinatarios de la sucursal.
#   390 px: cada uno de los 6 roles entra y ve el resultado en su ventana, sin desbordarse a lo ancho.
# Que la consola quede limpia.
# Corre después de los cortes 3 a 6b (probar_todo.sh los corre en orden sobre una instancia limpia).
# Uso: python recorrido_corte7.py <carpeta-de-capturas>   (necesita playwright y /usr/bin/chromium;
#      BASE=http://localhost:8080 por omisión; COMPOSE_PROJECT_NAME elige la instancia para vaciar la caché del
#      precio del arroz). Deja en la carpeta corte7.json los números y la correlación de cada paso, que
#      verificar_corte7.py revisa en los 11 servicios.
import datetime, json, math, os, re, struct, subprocess, sys, time, urllib.request, zlib
from playwright.sync_api import sync_playwright, expect

BASE = os.environ.get('BASE', 'http://localhost:8080')
PROYECTO = os.environ.get('COMPOSE_PROJECT_NAME', 'konrad-gourmet')
OUT = sys.argv[1]
os.makedirs(OUT, exist_ok=True)
MESERO, JEFE = 'isabella.torres@konradgourmet.com', 'carlos.mendoza@konradgourmet.com'
AUXILIAR, DIRECTORA = 'isabella.castro@konradgourmet.com', 'laura.gomez@konradgourmet.com'
DIRECTOR, ADMIN = 'andres.rojas@konradgourmet.com', 'paula.herrera@konradgourmet.com'
PLATO, ARROZ_POR_PLATO, PRECIO_PLATO = 'Arroz con verduras', 0.25, 22000
KILOS = 40
OFERTAS = {'Proveedor ABC': 8300, 'Proveedor C': 7900}   # por kilo; el Ministerio: $8.000
HOY = datetime.date.today()
fallas = []
registro = {'plato': None, 'pedido': None, 'alarma': None, 'solicitud': None, 'cotizaciones': {}, 'validaciones': {},
            'orden': None, 'finalizada': None, 'logins': {}, 'arroz_antes': None, 'ministerio_antes': None}

def ok(cond, texto):
    print(('  ✓ ' if cond else '  ✗ ') + texto)
    if not cond: fallas.append(texto)

def png(ruta, color=(120, 170, 60)):
    w = h = 64
    crudo = b''.join(b'\x00' + bytes(color) * w for _ in range(h))
    def trozo(t, d): return struct.pack('>I', len(d)) + t + d + struct.pack('>I', zlib.crc32(t + d) & 0xffffffff)
    open(ruta, 'wb').write(b'\x89PNG\r\n\x1a\n' + trozo(b'IHDR', struct.pack('>IIBBBBB', w, h, 8, 2, 0, 0, 0))
                           + trozo(b'IDAT', zlib.compress(crudo)) + trozo(b'IEND', b''))

def entrar(page, correo, destino=r'/inicio'):
    """Inicia sesión y guarda la correlación del login (ms-identidad lo deja en la auditoría)."""
    page.goto(BASE + '/login')
    page.fill('#email', correo)
    page.fill('#password', 'Konrad2026')
    with page.expect_response(lambda r: r.url.endswith('/api/auth/token')) as resp:
        page.click('button.btn-login')
    page.wait_for_url(re.compile(destino))
    registro['logins'].setdefault(correo, []).append(resp.value.headers.get('x-correlation-id'))

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
        with urllib.request.urlopen(pet, timeout=30) as r:
            return r.status, json.loads(r.read() or b'null')
    except urllib.error.HTTPError as e:
        return e.code, None

def token(correo):
    return http('POST', '/api/auth/token', {'correo': correo, 'clave': 'Konrad2026'})[1]['jwt']

def docker(contenedor, *orden):
    return subprocess.run(['docker', 'exec', f'{PROYECTO}-{contenedor}-1', *orden], capture_output=True, text=True).stdout

def llamadas_ministerio(producto):
    patron = json.dumps({'method': 'POST', 'url': '/precios', 'bodyPatterns': [{'contains': f'<codigoProducto>{producto}</codigoProducto>'}]})
    return json.loads(docker('ministerio', 'curl', '-s', '-X', 'POST', '-d', patron, 'http://127.0.0.1:8080/__admin/requests/count') or '{"count": -1}')['count']

def pesos(n):
    return '$' + f'{n:,.0f}'.replace(',', '.')

def respuesta(page, patron, metodo='POST'):
    return page.expect_response(lambda r: re.search(patron, r.url) is not None and r.request.method == metodo)

def estado_solicitud(page, numero):
    page.goto(BASE + f'/solicitudes?sel={numero}')
    page.wait_for_selector(f'.detail-head h3:has-text("{numero}")')
    return page.locator('.detail-head .badge').first.inner_text().upper()

png(f'{OUT}/arroz-con-verduras.png')
with sync_playwright() as pw:
    nav = pw.chromium.launch(executable_path='/usr/bin/chromium')

    # ════════════════════════ escritorio: la historia ════════════════════════
    ancho, alto, tag = 1440, 900, 'escritorio'
    print(f'== {tag} ({ancho} px)')
    errores = []
    ctx = {q: nav.new_context(viewport={'width': ancho, 'height': alto}, locale='es-CO', timezone_id='America/Bogota', accept_downloads=True)
           for q in ('jefe', 'mesero', 'aux', 'dir', 'comercial', 'admin')}
    pag = {q: c.new_page() for q, c in ctx.items()}
    for p in pag.values():
        p.on('console', lambda m: errores.append(m.text) if m.type == 'error' else None)
    jefe, mes, aux, dire, com, adm = (pag[q] for q in ('jefe', 'mesero', 'aux', 'dir', 'comercial', 'admin'))

    # ── 1. Jefe de cocina: el plato nuevo (ms-menu) ──
    print('-- 1. El jefe crea el plato (ms-menu → ms-pedidos, ms-inventario, ms-analitica)')
    entrar(jefe, JEFE)
    jefe.goto(BASE + '/platos'); jefe.wait_for_selector('.plate-detail .detail-head')
    jefe.get_by_role('button', name='Nuevo plato').click()
    v = jefe.locator('[role=dialog]'); expect(v).to_be_visible()
    v.get_by_placeholder('Ej. Bandeja paisa').fill(PLATO)
    v.locator('select').first.select_option('Fuertes')
    v.get_by_placeholder('$ 0').fill(str(PRECIO_PLATO))
    v.get_by_label('Imagen del plato').set_input_files(f'{OUT}/arroz-con-verduras.png')
    v.locator('.frow.four select').nth(0).select_option('Cereal')
    v.locator('.frow.four select').nth(1).select_option(label='Arroz')
    v.get_by_placeholder('0.00').fill(str(int(ARROZ_POR_PLATO * 1000))); v.locator('.frow.four select').nth(2).select_option('g')
    v.get_by_role('button', name='Agregar ingrediente').click()
    foto(jefe, f'{tag}-1-V06-nuevo-plato')
    with respuesta(jefe, r'/api/platos$') as resp:
        v.get_by_role('button', name='Guardar plato').click()
    r = resp.value
    ok(r.status == 201, f'V06: POST /api/platos → {r.status} ({PLATO})')
    registro['plato'] = {'id': r.json().get('id') if r.status == 201 else None, 'nombre': PLATO, 'correlacion': r.headers.get('x-correlation-id')}
    expect(jefe.locator('#toast.show')).to_contain_text('guardado')
    jefe.wait_for_selector(f'.plate-detail h3:has-text("{PLATO}")')
    jefe.wait_for_function("() => { const i = document.querySelector('.plate-detail .thumb.hero img'); return i && i.complete && i.naturalWidth > 0; }")
    ok(True, 'V06: el plato queda guardado y su imagen vuelve del almacén S3')
    foto(jefe, f'{tag}-1-V06-plato-creado')

    # ── 2. Mesero: el pedido del evento (ms-pedidos) ──
    print('-- 2. El mesero lo pide (ms-pedidos → ms-inventario, ms-analitica)')
    jt = token(JEFE)
    arroz = next(i for i in http('GET', '/api/inventario/insumos', token=jt)[1] if i['nombre'] == 'Arroz')
    registro['arroz_antes'] = arroz
    actual, referencia = float(arroz['stockActual']), float(arroz['stockReferencia'])
    # lo que falta para bajar del 25 % (al 18 %), en platos de 250 g; lo que vendieron los cortes anteriores después
    # del último cierre también se descuenta, así que con esto alcanza
    unidades = max(1, math.ceil((actual - 0.18 * referencia) / ARROZ_POR_PLATO))
    ok(actual / referencia > 0.25, f'V07: el arroz está en {actual:g} de {referencia:g} kg ({actual / referencia:.0%}), sobre el umbral; '
                                   f'el evento pide {unidades} platos')
    entrar(mes, MESERO)
    mes.goto(BASE + '/pedidos/nuevo'); mes.wait_for_selector('.dish')
    mes.get_by_role('button', name='Cancelar').click()
    t = mes.locator('.dish', has=mes.locator('h4', has_text=PLATO))
    expect(t).to_be_visible(timeout=15000)
    ok(t.locator('.price').inner_text().replace('\xa0', ' ') == pesos(PRECIO_PLATO), f'V03: el plato nuevo está en el catálogo del mesero a {pesos(PRECIO_PLATO)} (menu.actualizado)')
    for _ in range(unidades - 1):
        t.get_by_role('button', name='Más').click()
    t.get_by_role('button', name='Agregar').click()
    mesas = mes.locator('select[aria-label="Mesa"] option').evaluate_all('o => o.map(x => x.value).filter(Boolean)')
    mesa = mesas[-1]
    mes.select_option('select[aria-label="Mesa"]', mesa)
    total = mes.locator('.order-tot .big').inner_text().replace('\xa0', ' ')
    ok(total == pesos(unidades * PRECIO_PLATO), f'V03: {unidades} × {PLATO} en la mesa {mesa} suman {total}')
    foto(mes, f'{tag}-2-V03-pedido-del-evento')
    with respuesta(mes, r'/api/pedidos$') as resp:
        mes.get_by_role('button', name='Registrar pedido').click()
    r = resp.value
    modal = mes.locator('[role=dialog]'); expect(modal).to_be_visible()
    numero = modal.locator('.code-pill').inner_text()
    ok(r.status == 201 and r.json()['numero'] == numero, f'V04: pedido {numero} registrado ({r.status})')
    registro['pedido'] = {'numero': numero, 'unidades': unidades, 'mesa': int(mesa), 'total': unidades * PRECIO_PLATO, 'correlacion': r.headers.get('x-correlation-id')}
    foto(mes, f'{tag}-2-V04-pedido-registrado')

    # ── 3. Jefe: el cierre, la alarma en vivo y la solicitud (ms-inventario, ms-notificaciones, ms-solicitudes) ──
    print('-- 3. Cierre del día, alarma en vivo y solicitud (ms-inventario → ms-notificaciones; ms-solicitudes)')
    jefe.goto(BASE + '/inventario'); jefe.wait_for_selector('table tbody tr')
    jefe.wait_for_timeout(1200)  # el WebSocket ya abrió
    with jefe.expect_response(lambda r: r.url.endswith('/api/inventario/cierre')) as resp:
        jefe.get_by_role('button', name=re.compile('Cierre del día')).click()
    ok(resp.value.status in (200, 204), f'V07: «Cierre del día» → {resp.value.status}')
    alarma = jefe.locator('.alarma-viva', has_text='Arroz')
    expect(alarma).to_be_visible(timeout=15000)
    aviso = alarma.locator('p').inner_text()
    m = re.fullmatch(r'Arroz al (\d+(?:,\d+)?) %: realice una solicitud', aviso)
    ok(m is not None and float(m.group(1).replace(',', '.')) < 25, f'la alarma llega en vivo a V07 por WebSocket, sin recargar: «{aviso}»')
    registro['alarma'] = aviso
    foto(jefe, f'{tag}-3-V07-alarma-arroz')
    alarma.get_by_role('button', name='Solicitar').click()
    jefe.wait_for_url(re.compile(r'/solicitudes/nueva'))
    v08 = jefe.locator('[role=dialog]', has=jefe.locator('h2', has_text='Nueva solicitud de alimentos'))
    expect(v08).to_be_visible()
    ok(True, '«Solicitar» abre V08 (HU-16)')
    categoria = v08.locator('label', has_text='Categoría').locator('select').input_value()
    escogido = v08.get_by_label('Producto').evaluate('s => s.options[s.selectedIndex].text')
    ok(categoria == 'Cereal' and escogido == 'Arroz', f'V08 llega con el arroz ya escogido, como desde V05 y V07 (DEC-100): {categoria} · {escogido}')
    while v08.locator('button[aria-label^="Quitar "]').count():
        v08.locator('button[aria-label^="Quitar "]').first.click()
    v08.get_by_label('Cantidad').fill(str(KILOS))
    v08.get_by_role('button', name='Agregar').click()
    filas = v08.locator('tbody tr').all_inner_texts()
    ok(len(filas) == 1 and 'Arroz' in filas[0] and str(KILOS) in filas[0], f'V08: {KILOS} kg de arroz ({filas})')
    foto(jefe, f'{tag}-3-V08-solicitud-arroz')
    with respuesta(jefe, r'/api/solicitudes$') as resp:
        v08.get_by_role('button', name='Enviar solicitud').click()
    r = resp.value
    v09 = jefe.locator('[role=dialog]', has=jefe.locator('h2', has_text='Solicitud registrada'))
    expect(v09).to_be_visible()
    sol = v09.locator('.code-big').inner_text()
    ok(r.status == 201 and r.json()['numero'] == sol and 'PENDIENTE' in v09.locator('.estado-pill').inner_text(), f'V09: solicitud {sol} PENDIENTE ({r.status})')
    registro['solicitud'] = {'numero': sol, 'kilos': KILOS, 'correlacion': r.headers.get('x-correlation-id')}
    foto(jefe, f'{tag}-3-V09-solicitud-registrada')
    v09.get_by_role('button', name='VOLVER AL INICIO').click(); jefe.wait_for_url(re.compile('/inicio'))
    for _ in range(20):
        jefe.goto(BASE + '/inicio'); jefe.wait_for_selector('.alert-list'); jefe.wait_for_timeout(500)
        if 'Arroz' not in jefe.locator('.alert-row b').all_inner_texts(): break
        time.sleep(0.5)
    ok('Arroz' not in jefe.locator('.alert-row b').all_inner_texts(), f'V05: la alerta del arroz quedó atendida (ms-inventario recibió solicitud.registrada)')
    ok(sol in tarjeta(jefe, 'Solicitudes recientes').inner_text(), f'V05: {sol} está en «Solicitudes recientes»')
    foto(jefe, f'{tag}-3-V05-inicio-jefe')

    # ── 4. Auxiliar: dos cotizaciones (ms-cotizaciones, ms-integracion, ms-notificaciones) ──
    print('-- 4. La auxiliar cotiza (ms-cotizaciones → ms-integracion → Ministerio; ms-notificaciones; ms-solicitudes)')
    registro['ministerio_antes'] = llamadas_ministerio(2)
    docker('redis', 'redis-cli', 'DEL', 'precio-referencia:2')  # sin la caché: ms-integracion vuelve a preguntar al Ministerio
    entrar(aux, AUXILIAR)
    for k, proveedor in enumerate(OFERTAS):
        aux.goto(BASE + f'/solicitudes?sel={sol}'); aux.wait_for_selector(f'.detail-head h3:has-text("{sol}")')
        if k == 0:
            detalle = tarjeta(aux, 'Detalle de solicitud').inner_text()
            ok(JEFE in detalle and 'Arroz' in detalle, f'V12: el detalle de {sol} dice quién la pidió y el arroz')
            foto(aux, f'{tag}-4-V12-{sol}')
        aux.get_by_role('button', name=f'Generar cotización con {sol}').click()
        aux.wait_for_url(re.compile(rf'/cotizaciones/nueva\?solicitud={sol}'))
        v13 = aux.locator('[role=dialog]', has=aux.locator('h2', has_text='Generar solicitud de cotización'))
        expect(v13).to_be_visible()
        ok(v13.get_by_label(f'Incluir {sol}').is_checked(), f'V13: {sol} viene marcada')
        v13.get_by_label('Proveedor').select_option(label=proveedor)
        with respuesta(aux, r'/api/cotizaciones$') as resp:
            v13.get_by_role('button', name='GENERAR Y ENVIAR COTIZACIÓN').click()
        r = resp.value
        cot = r.json()['numero'] if r.status == 201 else None
        ok(r.status == 201 and r.json()['estado'] == 'ENVIADA' and r.json()['pdf'], f'V13: {cot} ENVIADA al {proveedor} con su PDF ({r.status})')
        registro['cotizaciones'][cot] = {'proveedor': proveedor, 'precio': OFERTAS[proveedor], 'correlacion': r.headers.get('x-correlation-id')}
        v14 = aux.locator('.ticket-modal'); expect(v14).to_be_visible()
        expect(v14.locator('.ticket-checks span', has_text='Correo enviado').locator('.visto')).to_be_visible(timeout=20000)
        ok(cot in v14.inner_text() and proveedor in v14.inner_text(), f'V14: {cot} para el {proveedor}, «PDF generado» y «Correo enviado» (ms-notificaciones)')
        foto(aux, f'{tag}-4-V14-{cot}')
        v14.get_by_role('button', name='Cerrar').click()
    cots = list(registro['cotizaciones'])
    for _ in range(20):
        if estado_solicitud(jefe, sol) == 'EN TRÁMITE': break
        time.sleep(0.5)
    ok(estado_solicitud(jefe, sol) == 'EN TRÁMITE', f'V10: el jefe ve {sol} EN TRÁMITE (ms-solicitudes recibió cotizacion.enviada)')
    foto(jefe, f'{tag}-4-V10-en-tramite')

    # ── 5. Directora: valores, validación, V20, la orden y su recepción (ms-cotizaciones, ms-ordenes) ──
    print('-- 5. La directora valida y compra (ms-cotizaciones; ms-ordenes → ms-cotizaciones, ms-solicitudes, ms-notificaciones)')
    entrar(dire, DIRECTORA)
    for cot, datos in registro['cotizaciones'].items():
        dire.goto(BASE + f'/cotizaciones/pendientes?sel={cot}&valores={cot}')
        v18 = dire.locator('[role=dialog]', has=dire.locator('h2', has_text='Registrar valores de cotización'))
        expect(v18).to_be_visible()
        ok(pesos(KILOS * 8000) in v18.locator('.totals').inner_text(), f'V18 {cot}: el valor estándar total es {pesos(KILOS * 8000)} (el Ministerio, por ms-integracion)')
        v18.get_by_label(re.compile('Precio del proveedor por .* de Arroz')).fill(str(datos['precio']))
        expect(v18.locator('.totals b').nth(1)).to_have_text(pesos(KILOS * datos['precio']))
        with respuesta(dire, rf'/{cot}/validacion$') as resp:
            v18.get_by_role('button', name='Validar cotización').click()
        r = resp.value
        esperado = f"{(datos['precio'] - 8000) / 8000 * 100:+.2f} %".replace('.', ',').replace('-', '−')
        v19 = dire.locator('.ticket-modal', has=dire.locator('h2', has_text='Validación completada'))
        expect(v19).to_be_visible()
        ok(r.status == 200 and r.json()['estado'] == 'OPCIONADA' and esperado in v19.locator('.diff-row').inner_text(),
           f'V19 {cot}: OPCIONADA, {esperado} frente al Ministerio')
        registro['validaciones'][cot] = {'estado': r.json().get('estado'), 'correlacion': r.headers.get('x-correlation-id')}
        foto(dire, f'{tag}-5-V19-{cot}')
        if cot == cots[-1]:
            v19.get_by_role('button', name='Ver cotizaciones opcionadas').click()
            dire.wait_for_url(re.compile(r'/cotizaciones/opcionadas'))
        else:
            v19.get_by_role('button', name='Cerrar').click()
    dire.get_by_label('Solicitud').select_option(sol)
    comparacion = tarjeta(dire, 'Comparación de cotizaciones')
    expect(comparacion.locator('tbody tr')).to_have_count(2)
    filas = comparacion.locator('tbody tr').all_inner_texts()
    barata = min(registro['cotizaciones'], key=lambda c: registro['cotizaciones'][c]['precio'])
    ok(filas[0].startswith(barata) and 'Proveedor C' in filas[0] and '−1,25 %' in filas[0] and '+3,75 %' in filas[1],
       f'V20: compara las dos de {sol}, la de menor diferencia primero ({[f.split(chr(9))[:2] for f in filas]})')
    comparacion.locator('tr', has=dire.locator('b', has_text=barata)).click()
    foto(dire, f'{tag}-5-V20-opcionadas')
    tarjeta(dire, 'Detalle de cotización opcionada').get_by_role('button', name='Seleccionar para compra').click()
    v21 = dire.locator('[role=dialog]', has=dire.locator('h2', has_text='Generar orden de compra'))
    expect(v21).to_be_visible()
    total = pesos(KILOS * OFERTAS['Proveedor C'])
    ok(all(t in v21.inner_text() for t in (barata, sol, 'Proveedor C', 'Arroz', f'{KILOS} kg', total)), f'V21: {barata} de {sol}, Proveedor C, {KILOS} kg de arroz, {total}')
    with respuesta(dire, r'/api/ordenes-compra$') as resp:
        v21.get_by_role('button', name='Confirmar orden de compra').click()
    r = resp.value
    oc = r.json()['numero'] if r.status == 201 else None
    ok(r.status == 201 and re.fullmatch(r'OC-\d{4}', oc or '') is not None, f'POST /api/ordenes-compra → {r.status} {oc}')
    registro['orden'] = {'numero': oc, 'cotizacion': barata, 'total': KILOS * OFERTAS['Proveedor C'], 'correlacion': r.headers.get('x-correlation-id')}
    v22 = dire.locator('.ticket-modal', has=dire.locator('h2', has_text='Orden generada'))
    expect(v22).to_be_visible()
    ok(oc in v22.inner_text() and total in v22.inner_text(), f'V22: {oc} por {total}')
    foto(dire, f'{tag}-5-V22-orden-generada')
    v22.get_by_role('button', name='VER ORDEN').click()
    dire.wait_for_url(re.compile(rf'/ordenes-compra\?sel={oc}'))
    dire.wait_for_selector(f'.oc-head h3:has-text("{oc}")')
    det = tarjeta(dire, 'Detalle de orden de compra')
    with dire.expect_download() as descarga:
        det.get_by_role('button', name='Descargar').click()
    ruta = f'{OUT}/{oc}.pdf'
    descarga.value.save_as(ruta)
    ok(open(ruta, 'rb').read(5) == b'%PDF-', f'V23: «Descargar» baja {descarga.value.suggested_filename}')
    registro['orden']['pdf'] = ruta
    det.get_by_role('button', name='Marcar como recibida').click()
    conf = dire.locator('[role=dialog]', has=dire.locator('h2', has_text='Marcar como recibida'))
    with respuesta(dire, rf'/api/ordenes-compra/{oc}/finalizar$', 'PATCH') as resp:
        conf.get_by_role('button', name='Confirmar recepción').click()
    r = resp.value
    expect(det.locator('.oc-head')).to_contain_text('FINALIZADA')
    ok(r.status == 200 and r.json()['estado'] == 'FINALIZADA', f'V23: el arroz llegó y {oc} queda FINALIZADA ({r.status})')
    registro['finalizada'] = {'numero': oc, 'correlacion': r.headers.get('x-correlation-id')}
    foto(dire, f'{tag}-5-V23-finalizada')
    for _ in range(20):
        if estado_solicitud(jefe, sol) == 'COMPLETADA': break
        time.sleep(0.5)
    ok(estado_solicitud(jefe, sol) == 'COMPLETADA', f'V10: el jefe ve {sol} COMPLETADA (ms-solicitudes recibió orden.generada)')
    foto(jefe, f'{tag}-5-V10-completada')

    # ── 6. Director comercial: el tablero BAM de hoy (ms-analitica) ──
    print('-- 6. El director comercial consulta el tablero (ms-analitica)')
    entrar(com, DIRECTOR, r'/bam')
    expect(com.locator('section.bam-kpi')).to_have_count(3)
    com.get_by_label('Desde', exact=True).fill(HOY.isoformat())
    com.get_by_label('Hasta', exact=True).fill(HOY.isoformat())
    barra, proveedor = None, None
    for _ in range(20):
        with com.expect_response(re.compile(r'/api/bam/kpis')):
            com.get_by_role('button', name='Consultar').click()
        com.wait_for_timeout(300)
        demanda = com.locator('section.bam-kpi', has=com.locator('h2', has_text='Producto con mayor demanda'))
        barra = demanda.locator('.bam-barras li', has_text=PLATO)
        proveedor = com.locator('section.bam-kpi', has=com.locator('h2', has_text='Proveedor con mejores precios')).locator('.bam-valor strong').inner_text()
        if barra.count() and proveedor == 'Proveedor C': break
        time.sleep(1)
    cifra = barra.locator('.bam-cifra').inner_text() if barra.count() else None
    ok(cifra is not None and int(cifra.replace('.', '')) >= unidades, f'el tablero de hoy cuenta el {PLATO} ({cifra} platos; el evento pidió {unidades})')
    ok(proveedor == 'Proveedor C', f'y el proveedor con mejores precios de hoy es el Proveedor C ({proveedor}, −1,25 %)')
    sin_desborde(com, ancho, 'Tablero BAM')
    foto(com, f'{tag}-6-bam-hoy')

    # ── 7. Administradora: los servicios, la auditoría de la historia y los destinatarios ──
    print('-- 7. La administradora revisa (ms-administracion, ms-identidad, ms-notificaciones; Eureka)')
    adm.goto(BASE + '/plataforma/')
    expect(adm.locator('#servicios tr')).to_have_count(11)
    adm.wait_for_function("() => [...document.querySelectorAll('#servicios .pill')].every(p => p.textContent === 'Activo')", timeout=15000)
    ok(True, 'la página de la plataforma muestra los 11 servicios activos en Eureka')
    foto(adm, f'{tag}-7-plataforma')
    entrar(adm, ADMIN)
    adm.goto(BASE + '/admin/auditoria')
    auditoria = tarjeta(adm, 'Movimientos de auditoría')
    adm.get_by_role('button', name='Hoy').click()
    expect(auditoria.locator('.card-title p')).to_contain_text('del ')
    filas = auditoria.locator('tbody tr').all_inner_texts()
    ok(filas and 'Inicio de sesión (ADMINISTRADOR)' in filas[0] and ADMIN in filas[0], f'«Auditoría» de hoy: lo más reciente es su propio inicio de sesión ({filas[0].replace(chr(9), " · ") if filas else "—"})')
    ok(any(f'Orden {oc} finalizada' in f and DIRECTORA in f for f in filas), f'y en la primera página está la recepción de {oc}, a nombre de la directora')
    sin_desborde(adm, ancho, 'Auditoría')
    foto(adm, f'{tag}-7-auditoria-hoy')
    adm.goto(BASE + '/destinatarios')
    expect(tarjeta(adm, 'Destinatarios de la Sucursal Centro').locator('tbody')).to_contain_text(JEFE)
    ok(True, f'«Destinatarios»: el jefe que recibió la alarma y los avisos es destinatario de la Sucursal Centro')

    errores = [e for e in errores if not re.search(r'status of (400|401|403|404|409)', e)]
    ok(not errores, f'escritorio: sin errores en la consola ({errores[:3]})')
    for c in ctx.values(): c.close()

    # ════════════════════════ 390 px: cada rol ve el resultado ════════════════════════
    ancho, alto, tag = 390, 844, 'movil'
    print(f'== {tag} ({ancho} px)')
    errores = []
    for correo, destino, ruta, espera, texto, nombre in (
            (MESERO, r'/inicio', f'/pedidos?sel={numero}', '.detail-head h3', PLATO, 'mesero-pedido'),
            (JEFE, r'/inicio', f'/solicitudes?sel={sol}', '.detail-head .badge', 'COMPLETADA', 'jefe-solicitud'),
            (AUXILIAR, r'/inicio', f'/cotizaciones?sel={barata}', '.card h2:has-text("Detalle de cotización")', 'Con orden', 'auxiliar-cotizacion'),
            (DIRECTORA, r'/inicio', f'/ordenes-compra?sel={oc}', '.oc-head h3', 'FINALIZADA', 'directora-orden'),
            (DIRECTOR, r'/bam', '/bam', 'section.bam-kpi', 'Producto con mayor demanda', 'director-bam'),
            (ADMIN, r'/inicio', '/admin/auditoria', '.card-title p', 'Inicio de sesión (ADMINISTRADOR)', 'admin-auditoria')):
        c = nav.new_context(viewport={'width': ancho, 'height': alto}, locale='es-CO', timezone_id='America/Bogota')
        p = c.new_page()
        p.on('console', lambda m: errores.append(m.text) if m.type == 'error' else None)
        entrar(p, correo, destino)
        p.goto(BASE + ruta); p.wait_for_selector(espera)
        try:
            expect(p.locator('main')).to_contain_text(re.compile(re.escape(texto), re.I), timeout=10000)
            visto = True
        except AssertionError:
            visto = False
        ok(visto, f'{correo.split("@")[0]}: {ruta} muestra «{texto}»')
        sin_desborde(p, ancho, ruta)
        foto(p, f'{tag}-{nombre}')
        c.close()
    errores = [e for e in errores if not re.search(r'status of (400|401|403|404|409)', e)]
    ok(not errores, f'390 px: sin errores en la consola ({errores[:3]})')
    nav.close()
json.dump(registro, open(f'{OUT}/corte7.json', 'w'), indent=2)
print('\nFALLAS:' if fallas else '\nTodo bien.', *fallas, sep='\n  ')
sys.exit(1 if fallas else 0)
