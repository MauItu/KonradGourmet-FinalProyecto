#!/usr/bin/env python3
# Recorrido del corte 4b en el navegador (Playwright), en escritorio (1440 px) y a 390 px:
#   escritorio: la auxiliar de compras abre V13 desde «Generar cotización con SOL-031» de V12 y envía COT-018
#   al Proveedor ABC (V14: PDF generado); desde V15 genera COT-019 para el Proveedor C y descarga su PDF. El
#   jefe ve SOL-031 EN TRÁMITE en V10 y V05. La directora de compras, desde V16, registra los valores de
#   COT-018 (res $21.250, arroz $8.000) y valida: V19 dice OPCIONADA, +4,81 %; en «Pendientes de validar»
#   guarda los de COT-019 (res $29.000) y luego valida: RECHAZADA. La auxiliar registra un proveedor (HU-15),
#   lo edita y lo inactiva, y ya no sale en V13.
#   390 px: la auxiliar genera COT-020 con SOL-031 y SOL-032 (DEC-05) para el Proveedor D; la directora la
#   valida con la res a $4.000: SOSPECHOSA. El jefe ve SOL-032 EN TRÁMITE.
# Que nada se desborde a lo ancho y que la consola quede limpia.
# Corre después de recorrido_corte4a.py (SOL-031: res 20 kg y arroz 15 kg; SOL-032: papa 10 kg).
# Uso: python recorrido_corte4b.py <carpeta-de-capturas>   (necesita playwright y /usr/bin/chromium;
#      BASE=http://localhost:8080 por omisión). Deja en la carpeta corte4b.json con la correlación de cada
#      petición y el PDF descargado, que verificar_corte4b.py revisa en PostgreSQL, Kafka, Redis y el almacén.
import json, os, re, sys, time
from playwright.sync_api import sync_playwright, expect

BASE = os.environ.get('BASE', 'http://localhost:8080')
OUT = sys.argv[1]
os.makedirs(OUT, exist_ok=True)
JEFE, AUXILIAR, DIRECTORA = 'carlos.mendoza@konradgourmet.com', 'isabella.castro@konradgourmet.com', 'laura.gomez@konradgourmet.com'
fallas, registro = [], {'generadas': {}, 'validadas': {}}

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

def generar(page, tag, solicitudes, proveedor, esperado):
    """En V13 abierta: marca las solicitudes, escoge el proveedor, envía y revisa V14. Devuelve el número."""
    v13 = page.locator('[role=dialog]', has=page.locator('h2', has_text='Generar solicitud de cotización'))
    expect(v13).to_be_visible()
    for s in solicitudes:
        casilla = v13.get_by_label(f'Incluir {s}')
        if not casilla.is_checked(): casilla.check()
    v13.get_by_label('Proveedor').select_option(label=proveedor)
    resumen = v13.locator('.m-sec', has=page.locator('h3', has_text='Resumen')).inner_text()
    ok(all(p in resumen for p in esperado), f'{tag} V13: el resumen suma los productos de {", ".join(solicitudes)} ({" · ".join(esperado)})')
    foto(page, f'{tag}-V13-{proveedor.replace(" ", "-")}')
    with page.expect_response(lambda r: r.url.endswith('/api/cotizaciones') and r.request.method == 'POST') as resp:
        v13.get_by_role('button', name='GENERAR Y ENVIAR COTIZACIÓN').click()
    r = resp.value
    cuerpo = r.json()
    ok(r.status == 201 and cuerpo['estado'] == 'ENVIADA' and cuerpo['pdf'], f'{tag}: POST /api/cotizaciones → {r.status}, {cuerpo.get("numero")} ENVIADA con PDF')
    v14 = page.locator('.ticket-modal')
    expect(v14).to_be_visible()
    numero = v14.locator('.code-big').inner_text()
    texto = v14.inner_text()
    ok(numero == cuerpo['numero'] and proveedor in texto and 'ENVIADA' in texto, f'{tag} V14: tiquete de {numero} para {proveedor}, ENVIADA')
    ok(v14.locator('.ticket-checks .ok').count() == 1, f'{tag} V14: «PDF generado» con su visto')
    registro['generadas'][numero] = {'correlacion': r.headers.get('x-correlation-id'), 'solicitudes': solicitudes, 'proveedor': proveedor}
    foto(page, f'{tag}-V14-{numero}')
    v14.get_by_role('button', name='Cerrar').click()
    expect(v14).to_have_count(0)
    return numero

def valores(page, tag, numero, precios, validar, estado=None, diferencia=None, total=None):
    """En V18 abierta: escribe los precios y guarda o valida (V19)."""
    v18 = page.locator('[role=dialog]', has=page.locator('h2', has_text='Registrar valores de cotización'))
    expect(v18).to_be_visible()
    ok(numero in v18.locator('.info-box').first.inner_text(), f'{tag} V18: es la cotización {numero}')
    for producto, precio in precios.items():
        v18.get_by_label(re.compile(f'Precio del proveedor por .* de {producto}')).fill(str(precio))
    if total:
        expect(v18.locator('.totals b').nth(1)).to_have_text(total)
        ok(True, f'{tag} V18: el valor cotizado total se recalcula mientras se escribe ({total})')
    foto(page, f'{tag}-V18-{numero}')
    if not validar:
        with page.expect_response(lambda r: r.url.endswith(f'/{numero}/valores') and r.request.method == 'PUT') as resp:
            v18.get_by_role('button', name='Guardar').click()
        ok(resp.value.status == 200, f'{tag} V18: «Guardar» registra los valores ({resp.value.status})')
        expect(v18).to_have_count(0)
        return
    with page.expect_response(lambda r: r.url.endswith(f'/{numero}/validacion') and r.request.method == 'POST') as resp:
        v18.get_by_role('button', name='Validar cotización').click()
    r = resp.value
    ok(r.status == 200 and r.json()['estado'] == estado, f'{tag}: POST …/{numero}/validacion → {r.status} {r.json().get("estado")}')
    registro['validadas'][numero] = {'correlacion': r.headers.get('x-correlation-id'), 'estado': estado}
    v19 = page.locator('.ticket-modal', has=page.locator('h2', has_text='Validación completada'))
    expect(v19).to_be_visible()
    ok(v19.locator('.result-pill').inner_text().strip() == estado, f'{tag} V19: la cadena de reglas dio {estado}')
    ok(diferencia in v19.locator('.diff-row').inner_text(), f'{tag} V19: diferencia {diferencia} contra el Ministerio')
    ok(v19.locator('.result-chips .on').inner_text().strip() == estado, f'{tag} V19: entre los posibles resultados queda marcado {estado}')
    foto(page, f'{tag}-V19-{numero}')
    v19.get_by_role('button', name='Cerrar').click()

def estado_solicitud(page, numero):
    page.goto(BASE + f'/solicitudes?sel={numero}')
    page.wait_for_selector(f'.detail-head h3:has-text("{numero}")')
    return page.locator('.detail-head .badge').first.inner_text()

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
            # ── Auxiliar: V12 → V13 con SOL-031 → V14 ──
            aux.goto(BASE + '/solicitudes?sel=SOL-031'); aux.wait_for_selector('.detail-head h3:has-text("SOL-031")')
            ok(estado_solicitud(aux, 'SOL-031').upper() == 'PENDIENTE', 'V12: SOL-031 está PENDIENTE antes de cotizar')
            aux.get_by_role('button', name='Generar cotización con SOL-031').click()
            aux.wait_for_url(re.compile(r'/cotizaciones/nueva\?solicitud=SOL-031'))
            v13 = aux.locator('[role=dialog]')
            ok(v13.get_by_label('Incluir SOL-031').is_checked() and not v13.get_by_label('Incluir SOL-032').is_checked(),
               'V13 desde V12: SOL-031 viene marcada y SOL-032 no')
            opciones = v13.get_by_label('Proveedor').locator('option').all_inner_texts()
            ok(opciones[:4] == ['Proveedor ABC', 'Proveedor B', 'Proveedor C', 'Proveedor D'], f'V13: los 4 proveedores de la demo ({opciones})')
            cot18 = generar(aux, tag, ['SOL-031'], 'Proveedor ABC', ['Res', '20', 'Arroz', '15'])
            ok(cot18 == 'COT-018', f'la numeración sigue a la de la demo: {cot18}')
            # V15: la lista, el detalle y el PDF
            aux.wait_for_url(re.compile(r'/cotizaciones\?sel=COT-018'))
            detalle = tarjeta(aux, 'Detalle de cotización').inner_text()
            ok('Proveedor ABC' in detalle and 'SOL-031' in detalle and 'Res' in detalle, 'V15: el detalle de COT-018 con su proveedor, solicitud y productos')
            # V15: «Generar cotización» del encabezado, sin solicitud marcada (hay dos por cotizar)
            aux.get_by_role('button', name='Generar cotización').first.click()
            aux.wait_for_url(re.compile(r'/cotizaciones/nueva$'))
            v13 = aux.locator('[role=dialog]')
            ok(not v13.get_by_label('Incluir SOL-031').is_checked(), 'V13 desde V15: ninguna solicitud viene marcada')
            ok(v13.get_by_role('button', name='GENERAR Y ENVIAR COTIZACIÓN').is_disabled(), 'V13: sin solicitudes no se puede enviar («Debe seleccionar al menos una solicitud»)')
            cot19 = generar(aux, tag, ['SOL-031'], 'Proveedor C', ['Res', 'Arroz'])
            aux.locator('tr', has=aux.locator('b', has_text=cot19)).click()
            with aux.expect_download() as descarga:
                tarjeta(aux, 'Detalle de cotización').get_by_role('button', name='Descargar').click()
            ruta = f'{OUT}/{cot19}.pdf'
            descarga.value.save_as(ruta)
            ok(open(ruta, 'rb').read(5) == b'%PDF-', f'V15: «Descargar» baja {descarga.value.suggested_filename}, un PDF')
            registro['pdf'] = {'numero': cot19, 'archivo': ruta}
            foto(aux, f'{tag}-V15-cotizaciones')
            sin_desborde(aux, ancho, 'V15')
            aux.goto(BASE + '/inicio'); aux.wait_for_selector('.kpi-strip')
            aux.wait_for_function("() => document.querySelectorAll('.kpi-strip strong')[2].innerText !== '—'")
            ok(aux.locator('.kpi-strip strong').nth(2).inner_text() == '2', 'V11: «Cotizaciones enviadas» cuenta 2')
            ok('2 cotizaciones enviadas' in aux.locator('.timeline').inner_text(), 'V11: las actividades del día cuentan las enviadas')
            foto(aux, f'{tag}-V11-inicio-auxiliar')

            # ── Jefe: SOL-031 en trámite (ms-solicitudes recibió cotizacion.enviada) ──
            for _ in range(20):
                if estado_solicitud(jefe, 'SOL-031').upper() == 'EN TRÁMITE': break
                time.sleep(0.5)
            ok(estado_solicitud(jefe, 'SOL-031').upper() == 'EN TRÁMITE', 'V10: SOL-031 pasó a EN TRÁMITE')
            foto(jefe, f'{tag}-V10-sol031-en-tramite')
            jefe.goto(BASE + '/inicio'); jefe.wait_for_function("() => [...document.querySelectorAll('.kpis strong')].every(e => e.innerText !== '—')")
            tramite = jefe.locator('.kpi', has=jefe.locator('span', has_text='Solicitudes en trámite')).locator('strong').inner_text()
            ok(tramite == '1', f'V05: «Solicitudes en trámite» = {tramite}')

            # ── Directora: V16 → V18 → V19 ──
            dire.goto(BASE + '/inicio'); dire.wait_for_selector('.kpis.four')
            dire.wait_for_function("() => [...document.querySelectorAll('.kpis strong')].slice(0, 3).every(e => e.innerText !== '—')")
            ok(dire.locator('.kpi', has=dire.locator('span', has_text='Cotizaciones por registrar')).locator('strong').inner_text() == '2',
               'V16: 2 cotizaciones por registrar')
            foto(dire, f'{tag}-V16-inicio-directora')
            sin_desborde(dire, ancho, 'V16')
            tarjeta(dire, 'Pendientes de atención').locator('tr', has=dire.locator('b', has_text=cot18)).get_by_role('button', name=re.compile('Registrar valores')).click()
            dire.wait_for_url(re.compile(rf'/cotizaciones/pendientes\?sel={cot18}&valores={cot18}'))
            v18 = dire.locator('[role=dialog]')
            ok('$520.000' in v18.locator('.totals').inner_text(), 'V18: el valor estándar total es $520.000 (precio del Ministerio por la caché)')
            valores(dire, tag, cot18, {'Res': 21250, 'Arroz': 8000}, True, 'OPCIONADA', '+4,81 %', '$545.000')
            # COT-019: primero «Guardar», luego «Validar» desde la lista
            dire.goto(BASE + '/cotizaciones/pendientes'); dire.wait_for_selector(f'tr:has-text("{cot19}")')
            dire.locator('tr', has=dire.locator('b', has_text=cot19)).get_by_role('button', name=re.compile('Registrar valores')).click()
            valores(dire, tag, cot19, {'Res': 29000, 'Arroz': 8000}, False)
            fila = dire.locator('tr', has=dire.locator('b', has_text=cot19))
            expect(fila.get_by_role('button', name=re.compile('Validar'))).to_be_visible()
            ok(True, f'V17: con los valores guardados, {cot19} ofrece «Validar»')
            fila.get_by_role('button', name=re.compile('Validar')).click()
            ok(dire.locator('[role=dialog]').get_by_label(re.compile('de Res')).input_value() == '29000', 'V18: trae los valores que se guardaron')
            valores(dire, tag, cot19, {}, True, 'RECHAZADA', '+34,62 %')
            dire.goto(BASE + f'/cotizaciones/todas?sel={cot18}'); dire.wait_for_selector('.ticket.side .ticket-code')
            ticket = dire.locator('.ticket.side').inner_text()
            ok(cot18 in ticket and 'OPCIONADA' in ticket and '$545.000' in ticket, 'V17: el tiquete de COT-018 dice OPCIONADA y $545.000')
            kpis = dire.locator('.kpis.three strong').all_inner_texts()
            ok(kpis == ['2', '1', '1'], f'V17: enviadas, opcionadas y rechazadas ({kpis})')
            foto(dire, f'{tag}-V17-todas')
            sin_desborde(dire, ancho, 'V17')

            # ── Auxiliar: proveedores (HU-15) ──
            aux.goto(BASE + '/proveedores'); aux.wait_for_selector('tr:has-text("Proveedor ABC")')
            aux.get_by_label('Razón social').fill('Frutas del Valle')
            aux.get_by_label('NIT').fill('900123456-1')
            aux.get_by_label('Correo').fill('ventas@frutasdelvalle.com')
            aux.get_by_role('button', name='Registrar proveedor').click()
            expect(aux.locator('.form-error')).to_contain_text('Ya existe un proveedor con el NIT 900123456-1')
            ok(True, 'Proveedores: un NIT repetido muestra el error del servicio')
            aux.get_by_label('NIT').fill('901555666-7')
            aux.get_by_label('Categorías').fill('Frutas, Verduras')
            aux.get_by_role('button', name='Registrar proveedor').click()
            aux.wait_for_url(re.compile(r'/proveedores\?sel=\d+'))
            ok('Frutas, Verduras' in aux.locator('tr', has=aux.locator('b', has_text='Frutas del Valle')).inner_text(), 'Proveedores: «Frutas del Valle» registrado con sus categorías')
            aux.get_by_label('Teléfono').fill('6024445566')
            aux.get_by_role('button', name='Guardar cambios').click()
            expect(aux.locator('#toast')).to_contain_text('actualizado')
            ok(True, 'Proveedores: editar guarda el teléfono')
            aux.once('dialog', lambda d: d.accept())
            aux.get_by_role('button', name='Inactivar').click()
            expect(aux.locator('tr', has=aux.locator('b', has_text='Frutas del Valle')).locator('.badge')).to_have_text('Inactivo')
            ok(True, 'Proveedores: inactivar lo deja «Inactivo» sin borrarlo')
            foto(aux, f'{tag}-proveedores')
            sin_desborde(aux, ancho, 'Proveedores')
            aux.goto(BASE + '/cotizaciones/nueva'); aux.wait_for_selector('[role=dialog]')
            opciones = aux.locator('[role=dialog]').get_by_label('Proveedor').locator('option').all_inner_texts()
            ok('Frutas del Valle' not in opciones, 'V13: el proveedor inactivo no sale en el desplegable')
            aux.keyboard.press('Escape')
        else:
            # ── Auxiliar a 390 px: una cotización con dos solicitudes (DEC-05) ──
            aux.goto(BASE + '/cotizaciones'); aux.wait_for_selector('tr:has-text("COT-018")')
            aux.get_by_role('button', name='Generar cotización').first.click()
            cot20 = generar(aux, tag, ['SOL-031', 'SOL-032'], 'Proveedor D', ['Res', 'Arroz', 'Papa'])
            sin_desborde(aux, ancho, 'V15')
            foto(aux, f'{tag}-V15-cotizaciones')
            # ── Directora: SOSPECHOSA ──
            dire.goto(BASE + '/cotizaciones/pendientes'); dire.wait_for_selector(f'tr:has-text("{cot20}")')
            sin_desborde(dire, ancho, 'V17 pendientes')
            dire.locator('tr', has=dire.locator('b', has_text=cot20)).get_by_role('button', name=re.compile('Registrar valores')).click()
            valores(dire, tag, cot20, {'Res': 4000, 'Arroz': 8000, 'Papa': 1000}, True, 'SOSPECHOSA', '−62,16 %', '$210.000')
            dire.goto(BASE + '/inicio'); dire.wait_for_selector('.kpis.four')
            sin_desborde(dire, ancho, 'V16')
            foto(dire, f'{tag}-V16-inicio-directora')
            # ── Jefe: SOL-032 en trámite ──
            for _ in range(20):
                if estado_solicitud(jefe, 'SOL-032').upper() == 'EN TRÁMITE': break
                time.sleep(0.5)
            ok(estado_solicitud(jefe, 'SOL-032').upper() == 'EN TRÁMITE', 'V10: SOL-032 pasó a EN TRÁMITE con COT-020')
            sin_desborde(jefe, ancho, 'V10')
            foto(jefe, f'{tag}-V10-sol032-en-tramite')
            aux.goto(BASE + '/proveedores'); aux.wait_for_selector('tr:has-text("Proveedor ABC")')
            sin_desborde(aux, ancho, 'Proveedores')
            foto(aux, f'{tag}-proveedores')

        # el 409 es el del NIT repetido, provocado a propósito
        errores = [e for e in errores if not re.search(r'status of (400|401|404|409)', e)]
        ok(not errores, f'sin errores en la consola ({errores[:3]})')
        for c in ctx.values(): c.close()
    nav.close()
json.dump(registro, open(f'{OUT}/corte4b.json', 'w'), indent=2)
print(f'\nCotizaciones: {list(registro["generadas"])}')
print('\nFALLAS:' if fallas else '\nTodo bien.', *fallas, sep='\n  ')
sys.exit(1 if fallas else 0)
