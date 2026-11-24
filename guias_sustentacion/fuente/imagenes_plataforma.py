#!/usr/bin/env python3
"""Ensaya la demo en vivo en la instancia kg-demo y saca las imágenes de la guía de la plataforma (DEC-111).

Recorre la historia de la sustentación como la haría el equipo (pedido, cierre del día y alarma, solicitud,
cotizaciones con el precio del Ministerio, validación, orden de compra, correos, tablero BAM, auditoría, logs y
servicios activos) y después saca una imagen de cada una de las 33 ventanas. En las imágenes de la demo pone
círculos numerados sobre lo que se pulsa o se señala, en la posición que da Playwright.

Deja en img/plataforma/ los PNG y datos.json (los números de la historia y la posición de cada círculo), que lee
generar_guia_plataforma.py. Cambia los datos de kg-demo: al terminar hay que restablecerla con restablecer_demo.sh.

Uso, desde FinalProyecto, con kg-demo recién restablecida y encendida (y ~75 s de espera por Eureka):
    COMPOSE_PROJECT_NAME=kg-demo mockups_nuevos/.venv/bin/python documentos/guias_sustentacion/fuente/imagenes_plataforma.py
"""
import json, os, re, shutil, subprocess, sys, time, urllib.request
from pathlib import Path
os.environ['LANG'] = os.environ['LC_ALL'] = 'es_CO.UTF-8'   # Chromium escribe las fechas como dd/mm/aaaa
from PIL import Image, ImageDraw, ImageFont
from playwright.sync_api import sync_playwright, expect

PROYECTO = os.environ.get('COMPOSE_PROJECT_NAME', '')
if PROYECTO != 'kg-demo':
    sys.exit('Solo corre sobre la instancia kg-demo (COMPOSE_PROJECT_NAME=kg-demo): cambia sus datos.')
BASE = 'http://localhost:8080'
AQUI = Path(__file__).resolve().parent
OUT = AQUI.parent / 'img' / 'plataforma'
FALTANTES = AQUI.parent.parent / 'mockups_faltantes'
CLAVE = 'Konrad2026'
MESERO, JEFE, AUX, DIRECTORA, COMERCIAL, ADMIN = (f'{u}@konradgourmet.com' for u in (
    'isabella.torres', 'carlos.mendoza', 'isabella.castro', 'laura.gomez', 'andres.rojas', 'paula.herrera'))
PEDIDO = [('Bandeja paisa', 1), ('Limonada natural', 1)]
MESA = '3'
KILOS = 40                                        # de res
OFERTAS = [('Proveedor ABC', 21000), ('Proveedor C', 26000)]   # por kilo; el Ministerio: $20.000
W, H = 1440, 900
R = 17                                            # radio de los círculos
FUENTE = ImageFont.truetype('/usr/share/fonts/liberation/LiberationSans-Bold.ttf', 19)
datos = {'imagenes': {}, 'historia': {}}
fallas = []


def ok(cond, texto):
    print(('  ✓ ' if cond else '  ✗ ') + texto)
    if not cond:
        fallas.append(texto)


def http(metodo, ruta, cuerpo=None, token=None, base=BASE):
    pet = urllib.request.Request(base + ruta, method=metodo, data=json.dumps(cuerpo).encode() if cuerpo is not None else None,
                                 headers={'Content-Type': 'application/json', **({'Authorization': f'Bearer {token}'} if token else {})})
    with urllib.request.urlopen(pet, timeout=30) as r:
        return json.loads(r.read() or b'null')


def token(correo):
    return http('POST', '/api/auth/token', {'correo': correo, 'clave': CLAVE})['jwt']


def pesos(n):
    return '$' + f'{n:,.0f}'.replace(',', '.')


def entrar(page, correo, destino=r'/inicio'):
    page.goto(BASE + '/login')
    page.fill('#email', correo)
    page.fill('#password', CLAVE)
    page.click('button.btn-login')
    page.wait_for_url(re.compile(destino))
    page.wait_for_timeout(500)


def tarjeta(page, titulo):
    return page.locator('.card', has=page.locator('h2', has_text=titulo))


def foto(page, nombre, pines=(), completa=False):
    """Guarda la imagen; pines = [(n, locator)], cada círculo sobre la esquina superior izquierda del elemento."""
    page.wait_for_timeout(500)
    ruta = OUT / f'{nombre}.png'
    page.screenshot(path=str(ruta), full_page=completa)
    img = Image.open(ruta).convert('RGB')
    ancho, alto = img.size
    marcas = []
    if pines:
        dib = ImageDraw.Draw(img)
        sx, sy = (page.evaluate('[scrollX, scrollY]') if completa else (0, 0))
        for n, loc in pines:
            caja = loc.first.bounding_box()
            if caja is None:
                ok(False, f'{nombre}: el círculo {n} no encontró su elemento')
                continue
            x, y = caja['x'] + sx + 2, caja['y'] + sy + 2
            x, y = min(max(x, R + 2), ancho - R - 2), min(max(y, R + 2), alto - R - 2)
            dib.ellipse((x - R, y - R, x + R, y + R), fill=(216, 27, 96), outline=(255, 255, 255), width=3)
            dib.text((x, y), str(n), font=FUENTE, fill='white', anchor='mm')
            marcas.append([n, round(x), round(y)])
        img.save(ruta)
    datos['imagenes'][nombre] = {'ancho': ancho, 'alto': alto, 'pines': marcas, 'diametro': 2 * R}
    print(f'     imagen {nombre}.png ({ancho}×{alto}, {len(marcas)} círculos)')


def pdf_a_png(pdf, nombre):
    subprocess.run(['pdftoppm', '-png', '-r', '110', '-singlefile', str(pdf), str(OUT / nombre)], check=True)
    img = Image.open(OUT / f'{nombre}.png')
    datos['imagenes'][nombre] = {'ancho': img.width, 'alto': img.height, 'pines': [], 'diametro': 2 * R}


def esperar(cond, segundos=20):
    for _ in range(segundos * 2):
        if cond():
            return True
        time.sleep(0.5)
    return False


OUT.mkdir(parents=True, exist_ok=True)
for viejo in OUT.glob('*.png'):
    viejo.unlink()
h = datos['historia']

with sync_playwright() as pw:
    nav = pw.chromium.launch(executable_path='/usr/bin/chromium')
    errores = []

    def contexto(ancho=W, alto=H):
        c = nav.new_context(viewport={'width': ancho, 'height': alto}, locale='es-CO', timezone_id='America/Bogota',
                            accept_downloads=True)
        p = c.new_page()
        p.on('console', lambda m: errores.append(m.text) if m.type == 'error' else None)
        return p

    mes, jefe, aux, dire, com, adm = (contexto() for _ in range(6))

    # ════════════ Paso 1 · Mesero: el pedido (ms-identidad, ms-pedidos) ════════════
    print('-- 1. Mesero: inicio de sesión y pedido')
    mes.goto(BASE + '/login')
    mes.get_by_role('button', name='Mesero', exact=True).click()
    foto(mes, 'd01_login', [(1, mes.locator('.demo-access__roles button').first), (2, mes.locator('#email')),
                            (3, mes.locator('button.btn-login'))])
    mes.click('button.btn-login'); mes.wait_for_url(re.compile('/inicio'))
    mes.goto(BASE + '/pedidos/nuevo'); mes.wait_for_selector('.dish')
    if mes.get_by_role('button', name='Cancelar').count():
        mes.get_by_role('button', name='Cancelar').click()
    for plato, n in PEDIDO:
        t = mes.locator('.dish', has=mes.locator('h4', has_text=plato))
        for _ in range(n - 1):
            t.get_by_role('button', name='Más').click()
        t.get_by_role('button', name='Agregar').click()
    mes.select_option('select[aria-label="Mesa"]', MESA)
    total = mes.locator('.order-tot .big').inner_text().replace('\xa0', ' ')
    esperado = sum(n * {'Bandeja paisa': 32000, 'Limonada natural': 6000}[p] for p, n in PEDIDO)
    ok(total == pesos(esperado), f'V03: el pedido suma {total}')
    foto(mes, 'd02_V03_pedido', [
        (1, mes.locator('.dish', has=mes.locator('h4', has_text='Bandeja paisa')).get_by_role('button', name='Agregar')),
        (2, mes.locator('.dish', has=mes.locator('h4', has_text='Limonada natural')).get_by_role('button', name='Agregar')),
        (3, mes.locator('select[aria-label="Mesa"]')), (4, mes.locator('.order-tot')),
        (5, mes.get_by_role('button', name='Registrar pedido'))])
    with mes.expect_response(lambda r: r.url.endswith('/api/pedidos') and r.request.method == 'POST') as resp:
        mes.get_by_role('button', name='Registrar pedido').click()
    modal = mes.locator('[role=dialog]'); expect(modal).to_be_visible()
    h['pedido'] = modal.locator('.code-pill').inner_text()
    h['pedido_total'] = esperado
    ok(resp.value.status == 201, f'V04: {h["pedido"]} registrado')
    foto(mes, 'd03_V04_registrado', [(1, modal.locator('.code-pill'))])

    # ════════════ Paso 2 · Jefe: cierre del día y alarma (ms-inventario, ms-notificaciones) ════════════
    print('-- 2. Jefe de cocina: cierre del día y alarma en vivo')
    entrar(jefe, JEFE)
    jefe.goto(BASE + '/inventario'); jefe.wait_for_selector('table tbody tr')
    jefe.wait_for_timeout(1500)   # el WebSocket ya abrió
    res = jefe.locator('table tbody tr', has_text='Res')
    h['res_antes'] = res.inner_text().split('\t')
    foto(jefe, 'd04_V07_antes', [(1, res), (2, jefe.get_by_role('button', name=re.compile('Cierre del día')))])
    with jefe.expect_response(lambda r: r.url.endswith('/api/inventario/cierre')):
        jefe.get_by_role('button', name=re.compile('Cierre del día')).click()
    alarma = jefe.locator('.alarma-viva', has_text='Res')
    expect(alarma).to_be_visible(timeout=20000)
    h['alarma'] = alarma.locator('p').inner_text()
    ok(re.fullmatch(r'Res al \d+(,\d+)? %: realice una solicitud', h['alarma']) is not None, f'la alarma llega en vivo: «{h["alarma"]}»')
    jefe.wait_for_timeout(1200)
    h['res_despues'] = jefe.locator('table tbody tr', has_text='Res').inner_text().split('\t')
    foto(jefe, 'd05_V07_alarma', [(1, alarma), (2, alarma.get_by_role('button', name='Solicitar')),
                                  (3, jefe.locator('table tbody tr', has_text='Res'))])

    # ════════════ Paso 3 · Jefe: la solicitud (ms-solicitudes) ════════════
    print('-- 3. Jefe de cocina: solicitud de la res')
    alarma.get_by_role('button', name='Solicitar').click()
    jefe.wait_for_url(re.compile(r'/solicitudes/nueva'))
    v08 = jefe.locator('[role=dialog]', has=jefe.locator('h2', has_text='Nueva solicitud de alimentos'))
    expect(v08).to_be_visible()
    escogido = v08.get_by_label('Producto').evaluate('s => s.options[s.selectedIndex].text')
    ok(escogido == 'Res', f'V08 llega con la res escogida ({escogido})')
    while v08.locator('button[aria-label^="Quitar "]').count():
        v08.locator('button[aria-label^="Quitar "]').first.click()
    v08.get_by_label('Cantidad').fill(str(KILOS))
    v08.get_by_role('button', name='Agregar').click()
    foto(jefe, 'd06_V08_solicitud', [(1, v08.get_by_label('Producto')), (2, v08.get_by_label('Cantidad')),
                                     (3, v08.get_by_role('button', name='Agregar')), (4, v08.locator('tbody tr').first),
                                     (5, v08.get_by_role('button', name='Enviar solicitud'))])
    with jefe.expect_response(lambda r: r.url.endswith('/api/solicitudes') and r.request.method == 'POST'):
        v08.get_by_role('button', name='Enviar solicitud').click()
    v09 = jefe.locator('[role=dialog]', has=jefe.locator('h2', has_text='Solicitud registrada'))
    expect(v09).to_be_visible()
    h['solicitud'] = sol = v09.locator('.code-big').inner_text()
    ok(bool(re.fullmatch(r'SOL-\d{3}', sol)), f'V09: {sol} PENDIENTE')
    foto(jefe, 'd07_V09_registrada', [(1, v09.locator('.code-big')), (2, v09.locator('.estado-pill'))])
    v09.get_by_role('button', name='VOLVER AL INICIO').click(); jefe.wait_for_url(re.compile('/inicio'))
    jefe.wait_for_timeout(1500); jefe.reload(); jefe.wait_for_selector('main .card'); jefe.wait_for_timeout(800)
    foto(jefe, 'd08_V05_atendida', [(1, tarjeta(jefe, 'Alertas')), (2, tarjeta(jefe, 'Solicitudes recientes'))])

    # ════════════ Paso 4 · Auxiliar: dos cotizaciones (ms-cotizaciones, ms-integracion, ms-notificaciones) ═══════
    print('-- 4. Auxiliar de compras: cotizaciones con el precio del Ministerio')
    entrar(aux, AUX)
    h['cotizaciones'] = []
    for k, (proveedor, precio) in enumerate(OFERTAS):
        aux.goto(BASE + f'/solicitudes?sel={sol}'); aux.wait_for_selector(f'.detail-head h3:has-text("{sol}")')
        boton = aux.get_by_role('button', name=f'Generar cotización con {sol}')
        if k == 0:
            foto(aux, 'd09_V12_detalle', [(1, aux.locator('tbody tr', has_text=sol).first),
                                          (2, tarjeta(aux, 'Detalle de solicitud')), (3, boton)])
        boton.click()
        aux.wait_for_url(re.compile(r'/cotizaciones/nueva'))
        v13 = aux.locator('[role=dialog]', has=aux.locator('h2', has_text='Generar solicitud de cotización'))
        expect(v13).to_be_visible()
        v13.get_by_label('Proveedor').select_option(label=proveedor)
        if k == 0:
            foto(aux, 'd10_V13_generar', [(1, v13.get_by_label(f'Incluir {sol}')), (2, v13.get_by_label('Cotizar Res')),
                                          (3, v13.get_by_label('Proveedor')),
                                          (4, v13.get_by_role('button', name='GENERAR Y ENVIAR COTIZACIÓN'))])
        with aux.expect_response(lambda r: r.url.endswith('/api/cotizaciones') and r.request.method == 'POST') as resp:
            v13.get_by_role('button', name='GENERAR Y ENVIAR COTIZACIÓN').click()
        cot = resp.value.json()['numero']
        v14 = aux.locator('.ticket-modal'); expect(v14).to_be_visible()
        expect(v14.locator('.ticket-checks span', has_text='Correo enviado').locator('.visto')).to_be_visible(timeout=30000)
        h['cotizaciones'].append({'numero': cot, 'proveedor': proveedor, 'precio': precio})
        ok(True, f'V14: {cot} ENVIADA al {proveedor}, con PDF y correo')
        if k == 0:
            foto(aux, 'd11_V14_enviada', [(1, v14.locator('.code-big')), (2, v14.locator('.ticket-checks span').nth(0)),
                                          (3, v14.locator('.ticket-checks span').nth(1)),
                                          (4, v14.get_by_role('button', name='Ver PDF'))])
            pdf = aux.request.get(BASE + f'/api/cotizaciones/{cot}/pdf',
                                  headers={'Authorization': 'Bearer ' + token(AUX)})
            (OUT / 'cotizacion.pdf').write_bytes(pdf.body())
            pdf_a_png(OUT / 'cotizacion.pdf', 'd12_pdf_cotizacion'); (OUT / 'cotizacion.pdf').unlink()
        v14.get_by_role('button', name='Cerrar').click()
    # el precio salió del Ministerio una vez; la segunda cotización lo tomó de la caché de Redis
    llaves = subprocess.run(['docker', 'exec', f'{PROYECTO}-redis-1', 'redis-cli', '--scan', '--pattern', 'precio-referencia:*'],
                            capture_output=True, text=True).stdout.split()
    h['cache'] = llaves
    jt = token(JEFE)
    ok(esperar(lambda: http('GET', f'/api/solicitudes/{sol}', token=jt)['estado'] == 'EN_TRAMITE'), f'{sol} pasa a EN TRÁMITE')
    jefe.goto(BASE + f'/solicitudes?sel={sol}'); jefe.wait_for_selector(f'.detail-head h3:has-text("{sol}")')
    foto(jefe, 'd13_V10_en_tramite', [(1, jefe.locator('.detail-head .badge').first)])

    # ════════════ Paso 5 · Directora: valores y validación (ms-cotizaciones) ════════════
    print('-- 5. Directora de compras: valores y validación')
    entrar(dire, DIRECTORA)
    for k, c in enumerate(h['cotizaciones']):
        cot = c['numero']
        dire.goto(BASE + f'/cotizaciones/pendientes?sel={cot}&valores={cot}')
        v18 = dire.locator('[role=dialog]', has=dire.locator('h2', has_text='Registrar valores de cotización'))
        expect(v18).to_be_visible()
        v18.get_by_label(re.compile('Precio del proveedor por .* de Res')).fill(str(c['precio']))
        expect(v18.locator('.totals b').nth(1)).to_have_text(pesos(KILOS * c['precio']))
        if k == 0:
            foto(dire, 'd14_V18_valores', [(1, v18.locator('tbody tr').first.locator('td').nth(2)),
                                           (2, v18.get_by_label(re.compile('Precio del proveedor por .* de Res'))),
                                           (3, v18.locator('.totals')), (4, v18.get_by_role('button', name='Validar cotización'))])
        with dire.expect_response(lambda r: r.url.endswith(f'/{cot}/validacion')) as resp:
            v18.get_by_role('button', name='Validar cotización').click()
        c['estado'] = resp.value.json()['estado']
        v19 = dire.locator('.ticket-modal', has=dire.locator('h2', has_text='Validación completada'))
        expect(v19).to_be_visible()
        c['diferencia'] = v19.locator('.diff-row').inner_text().replace('\n', ' ')
        ok(c['estado'] == ('OPCIONADA', 'RECHAZADA')[k], f'V19 {cot}: {c["estado"]} ({c["diferencia"]})')
        foto(dire, f'd{15 + k}_V19_{c["estado"].lower()}', [(1, v19.locator('.cmp')), (2, v19.locator('.diff-row')), (3, v19.locator('.result-pill'))])
        v19.get_by_role('button', name='Cerrar').click()

    # ════════════ Paso 6 · Directora: la orden de compra (ms-ordenes) ════════════
    print('-- 6. Directora de compras: orden de compra')
    opcionada = h['cotizaciones'][0]['numero']
    dire.goto(BASE + '/cotizaciones/opcionadas'); dire.wait_for_selector('.card')
    dire.get_by_label('Solicitud').select_option(sol)
    comparacion = tarjeta(dire, 'Comparación de cotizaciones')
    expect(comparacion.locator('tbody tr')).to_have_count(1)
    comparacion.locator('tr', has=dire.locator('b', has_text=opcionada)).click()
    seleccionar = tarjeta(dire, 'Detalle de cotización opcionada').get_by_role('button', name='Seleccionar para compra')
    foto(dire, 'd17_V20_opcionadas', [(1, dire.get_by_label('Solicitud')), (2, comparacion.locator('tbody tr').first),
                                      (3, seleccionar)])
    seleccionar.click()
    v21 = dire.locator('[role=dialog]', has=dire.locator('h2', has_text='Generar orden de compra'))
    expect(v21).to_be_visible()
    confirmar = v21.get_by_role('button', name='Confirmar orden de compra')
    foto(dire, 'd18_V21_generar', [(1, v21.get_by_text(sol).first), (2, v21.locator('.total-row')),
                                   (3, confirmar)])
    with dire.expect_response(lambda r: r.url.endswith('/api/ordenes-compra') and r.request.method == 'POST') as resp:
        confirmar.click()
    h['orden'] = oc = resp.value.json()['numero']
    h['orden_total'] = KILOS * OFERTAS[0][1]
    v22 = dire.locator('.ticket-modal', has=dire.locator('h2', has_text='Orden generada'))
    expect(v22).to_be_visible()
    ok(oc in v22.inner_text(), f'V22: {oc}')
    foto(dire, 'd19_V22_orden', [(1, v22.locator('.code-big')), (2, v22.get_by_role('button', name='VER ORDEN'))])
    v22.get_by_role('button', name='VER ORDEN').click()
    dire.wait_for_selector(f'.oc-head h3:has-text("{oc}")')
    det = tarjeta(dire, 'Detalle de orden de compra')
    foto(dire, 'd20_V23_orden', [(1, det.locator('.oc-head')), (2, det.get_by_role('button', name='Descargar')),
                                 (3, det.get_by_role('button', name='Marcar como recibida'))])
    pdf = dire.request.get(BASE + f'/api/ordenes-compra/{oc}/pdf', headers={'Authorization': 'Bearer ' + token(DIRECTORA)})
    (OUT / 'orden.pdf').write_bytes(pdf.body())
    pdf_a_png(OUT / 'orden.pdf', 'd21_pdf_orden'); (OUT / 'orden.pdf').unlink()
    det.get_by_role('button', name='Marcar como recibida').click()
    conf = dire.locator('[role=dialog]', has=dire.locator('h2', has_text='Marcar como recibida'))
    conf.get_by_role('button', name='Confirmar recepción').click()
    expect(det.locator('.oc-head')).to_contain_text('FINALIZADA')
    foto(dire, 'd22_V23_finalizada', [(1, det.locator('.oc-head'))])
    ok(esperar(lambda: http('GET', f'/api/solicitudes/{sol}', token=jt)['estado'] == 'COMPLETADA'), f'{sol} pasa a COMPLETADA')
    jefe.goto(BASE + f'/solicitudes?sel={sol}'); jefe.wait_for_selector(f'.detail-head h3:has-text("{sol}")')
    foto(jefe, 'd23_V10_completada', [(1, jefe.locator('.detail-head .badge').first)])

    # ════════════ Paso 7 · Los correos (Mailpit) ════════════
    print('-- 7. Los correos de la historia')
    time.sleep(3)
    lista = http('GET', '/api/v1/messages?limit=50', base='http://localhost:8025')
    h['correos'] = [{'asunto': m['Subject'], 'para': m['To'][0]['Address'], 'adjuntos': m.get('Attachments', 0)}
                    for m in lista['messages']]
    for m in h['correos']:
        print('     correo:', m['asunto'], '→', m['para'], f'({m["adjuntos"]} adjuntos)')
    correo = contexto()
    correo.goto('http://localhost:8025/'); correo.wait_for_timeout(1500)
    filas = correo.locator('.message')
    foto(correo, 'd24_mailpit', [(i + 1, filas.nth(i)) for i in range(min(filas.count(), 7))])
    de_oc = next(m for m in lista['messages'] if oc in m['Subject'])
    correo.goto(f'http://localhost:8025/view/{de_oc["ID"]}'); correo.wait_for_timeout(1500)
    foto(correo, 'd25_mailpit_orden', [(1, correo.locator('tr', has=correo.locator('strong.text-spaces', has_text=oc))),
                                                (2, correo.locator('.card-footer', has_text=f'{oc}.pdf'))])

    # ════════════ Paso 8 · Director comercial: tablero BAM (ms-analitica) ════════════
    print('-- 8. Director comercial: tablero BAM')
    entrar(com, COMERCIAL, r'/bam')
    expect(com.locator('section.bam-kpi')).to_have_count(3)
    com.wait_for_timeout(1500)
    kpis = com.locator('section.bam-kpi')
    h['bam'] = [kpis.nth(i).locator('h2').inner_text() + ': ' + kpis.nth(i).locator('.bam-valor').inner_text().replace('\n', ' ')
                for i in range(3)]
    print('     ', h['bam'])
    foto(com, 'd26_bam', [(1, com.get_by_label('Desde', exact=True)), (2, kpis.nth(0).locator('h2')),
                          (3, kpis.nth(1).locator('h2')), (4, kpis.nth(2).locator('h2'))])

    # ════════════ Paso 9 · Administradora: auditoría, logs y servicios activos ════════════
    print('-- 9. Administradora: auditoría, logs de errores y servicios')
    entrar(adm, ADMIN)
    adm.goto(BASE + '/admin/auditoria')
    auditoria = tarjeta(adm, 'Movimientos de auditoría')
    adm.get_by_role('button', name='Hoy').click()
    expect(auditoria.locator('.card-title p')).to_contain_text('del ')
    adm.wait_for_timeout(800)
    h['auditoria'] = auditoria.locator('tbody tr').all_inner_texts()[:12]
    foto(adm, 'd27_auditoria', [(1, adm.get_by_role('button', name='Hoy')), (2, auditoria.locator('tbody tr').first)])
    adm.goto(BASE + '/admin/logs'); adm.wait_for_selector('.card'); adm.wait_for_timeout(1000)
    h['logs'] = adm.locator('main').inner_text()[:600]
    foto(adm, 'd28_logs')
    adm.goto(BASE + '/plataforma/')
    expect(adm.locator('#servicios tr')).to_have_count(11)
    adm.wait_for_function("() => [...document.querySelectorAll('#servicios .pill')].every(p => p.textContent === 'Activo')", timeout=20000)
    foto(adm, 'd29_plataforma', [(1, adm.locator('#servicios tr').first)])
    eureka = contexto()
    eureka.goto('http://localhost:8761/'); eureka.wait_for_timeout(1000)
    foto(eureka, 'd30_eureka', [(1, eureka.get_by_text('Instances currently registered with Eureka'))])

    # ════════════ Las 33 ventanas ════════════
    print('-- Las 33 ventanas')
    c1 = contexto()
    c1.goto(BASE + '/login'); c1.wait_for_selector('#email'); foto(c1, 'v01')
    for pag, nombre, ruta, espera in (
            (mes, 'v02', '/inicio', 'main .card'),
            (jefe, 'v05', '/inicio', 'main .card'),
            (jefe, 'v07', '/inventario', 'table tbody tr'),
            (jefe, 'v10', f'/solicitudes?sel={sol}', '.detail-head'),
            (aux, 'v11', '/inicio', 'main .card'),
            (aux, 'v12', '/solicitudes?sel=SOL-031', '.detail-head'),
            (aux, 'v15', f'/cotizaciones?sel={h["cotizaciones"][0]["numero"]}', 'main .card'),
            (dire, 'v16', '/inicio', 'main .card'),
            (dire, 'v17', '/cotizaciones/todas?sel=COT-018', 'main .card'),
            (dire, 'v23', '/ordenes-compra?sel=OC-0012', '.oc-head'),
            (jefe, 'promociones', '/promociones', 'main .card'),
            (aux, 'proveedores', '/proveedores', 'main .card'),
            (com, 'bam', '/bam', 'section.bam-kpi'),
            (adm, 'parametros', '/admin/parametros', 'main .card'),
            (adm, 'auditoria', '/admin/auditoria', 'main .card'),
            (adm, 'logs', '/admin/logs', 'main .card'),
            (adm, 'usuarios', '/usuarios', 'main .card'),
            (adm, 'destinatarios', '/destinatarios', 'main .card'),
            (adm, 'inicio_admin', '/inicio', 'main .card')):
        pag.goto(BASE + ruta); pag.wait_for_selector(espera); pag.wait_for_timeout(900)
        foto(pag, nombre)
    jefe.goto(BASE + '/platos'); jefe.wait_for_selector('.plate-detail .detail-head')
    jefe.get_by_role('button', name='Nuevo plato').click(); jefe.wait_for_selector('[role=dialog]')
    foto(jefe, 'v06')
    jefe.goto(BASE + '/platos'); jefe.wait_for_selector('.plate-detail .detail-head')
    jefe.locator('tr.clickable', has_text='Ajiaco').first.click()
    jefe.wait_for_selector('.plate-detail h3:has-text("Ajiaco")')
    foto(jefe, 'platos')
    dire.goto(BASE + '/cotizaciones/opcionadas'); dire.wait_for_selector('.card')
    dire.get_by_label('Solicitud').select_option('SOL-031'); dire.wait_for_timeout(800)
    foto(dire, 'v20')
    # las ventanas que solo aparecen tras una acción ya salieron en la demo (V03, V04, V08, V09, V13, V14, V18, V19,
    # V21 y V22); la cotización pendiente de precio necesita al Ministerio caído y se toma de mockups_faltantes
    shutil.copy(FALTANTES / 'reenviar_cotizacion' / 'pendiente_de_precio.png', OUT / 'reenviar.png')
    img = Image.open(OUT / 'reenviar.png')
    datos['imagenes']['reenviar'] = {'ancho': img.width, 'alto': img.height, 'pines': [], 'diametro': 2 * R}

    # ════════════ A 390 px ════════════
    print('-- A 390 px')
    for correo_, nombre, ruta, espera in ((MESERO, 'm_v03', '/pedidos/nuevo', '.dish'),
                                          (JEFE, 'm_v07', '/inventario', 'table tbody tr'),
                                          (COMERCIAL, 'm_bam', '/bam', 'section.bam-kpi')):
        p = contexto(390, 844)
        entrar(p, correo_, r'/(inicio|bam)')
        p.goto(BASE + ruta); p.wait_for_selector(espera); p.wait_for_timeout(800)
        doc = p.evaluate('document.documentElement.scrollWidth')
        ok(doc <= 391, f'{nombre}: sin desbordarse a lo ancho ({doc})')
        foto(p, nombre)

    errores = [e for e in errores if not re.search(r'status of (400|401|403|404|409)', e)]
    ok(not errores, f'sin errores en la consola ({errores[:3]})')
    nav.close()

json.dump(datos, open(OUT / 'datos.json', 'w', encoding='utf-8'), ensure_ascii=False, indent=1)
print('\nFALLAS:' if fallas else '\nTodo bien.', *fallas, sep='\n  ')
sys.exit(1 if fallas else 0)
