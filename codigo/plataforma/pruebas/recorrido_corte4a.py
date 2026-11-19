#!/usr/bin/env python3
# Recorrido del corte 4a en el navegador (Playwright), en escritorio (1440 px) y a 390 px:
#   jefe de cocina: V08 (la primera solicitud trae la de la demo: res 20 kg «Marca X» y arroz 15 kg), V09,
#   V10 (Mis solicitudes con su detalle) y V05 (indicadores y tabla de solicitudes; la alerta de la res se
#   atiende cuando ms-inventario recibe solicitud.registrada); a 390 px, otra solicitud desde «Solicitar»
#   en V07 (papa 10 kg). Auxiliar de compras: V11 (indicadores y recientes) y V12 (todas las solicitudes,
#   filtros de HU-07 y detalle). Que nada se desborde a lo ancho y que la consola quede limpia.
# Corre después de recorrido_corte3.py: el cierre del día de ese recorrido deja la res en alerta.
# Uso: python recorrido_corte4a.py <carpeta-de-capturas>   (necesita playwright y /usr/bin/chromium;
#      BASE=http://localhost:8080 por omisión). Deja en la carpeta corte4a.json con los números y la
#      correlación de cada solicitud, que verificar_corte4a.py revisa en PostgreSQL, Kafka y Redis.
import json, os, re, sys, time
from playwright.sync_api import sync_playwright, expect

BASE = os.environ.get('BASE', 'http://localhost:8080')
OUT = sys.argv[1]
os.makedirs(OUT, exist_ok=True)
JEFE, AUXILIAR = 'carlos.mendoza@konradgourmet.com', 'isabella.castro@konradgourmet.com'
fallas, registro = [], {}

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

def kpi(page, etiqueta):
    return page.locator('.kpi', has=page.locator('span', has_text=etiqueta)).locator('strong').inner_text()

def alertas_v05(page):
    page.goto(BASE + '/inicio'); page.wait_for_selector('.alert-list')
    page.wait_for_function("() => { const k = [...document.querySelectorAll('.kpis strong')]; return k.length === 4 && k.every(e => e.innerText !== '—'); }")
    page.wait_for_timeout(300)
    return page.locator('.alert-row b').all_inner_texts()

def registrar(page, tag):
    """Envía la ventana V08 abierta y devuelve el número y la correlación que devolvió el servicio."""
    with page.expect_response(lambda r: r.url.endswith('/api/solicitudes') and r.request.method == 'POST') as resp:
        page.get_by_role('button', name='Enviar solicitud').click()
    r = resp.value
    ok(r.status == 201, f'{tag}: POST /api/solicitudes responde 201 ({r.status})')
    cuerpo = r.json()
    v09 = page.locator('[role=dialog]', has=page.locator('h2', has_text='Solicitud registrada'))
    expect(v09).to_be_visible()
    numero = v09.locator('.code-big').inner_text()
    ok(numero == cuerpo['numero'] and re.fullmatch(r'SOL-\d{3}', numero) is not None, f'{tag} V09: solicitud registrada {numero}')
    ok('PENDIENTE' in v09.locator('.estado-pill').inner_text(), f'{tag} V09: estado PENDIENTE')
    return numero, r.headers.get('x-correlation-id'), v09

with sync_playwright() as pw:
    nav = pw.chromium.launch(executable_path='/usr/bin/chromium')
    for ancho, alto, tag in ((1440, 900, 'escritorio'), (390, 844, 'movil')):
        print(f'== {tag} ({ancho} px)')
        errores = []
        jctx = nav.new_context(viewport={'width': ancho, 'height': alto}, locale='es-CO', timezone_id='America/Bogota')
        actx = nav.new_context(viewport={'width': ancho, 'height': alto}, locale='es-CO', timezone_id='America/Bogota')
        jefe, aux = jctx.new_page(), actx.new_page()
        for p in (jefe, aux):
            p.on('console', lambda m: errores.append(m.text) if m.type == 'error' else None)

        # ── Jefe de cocina ──
        entrar(jefe, JEFE)
        antes = alertas_v05(jefe)
        pendientes_antes = int(kpi(jefe, 'Solicitudes pendientes'))
        foto(jefe, f'{tag}-V05-inicio-jefe-antes')
        if tag == 'escritorio':
            ok('Res' in antes, f'V05: la res está en alerta antes de pedirla ({antes}); si no, corre antes recorrido_corte3.py')
            jefe.goto(BASE + '/solicitudes'); jefe.wait_for_selector('.filters-line')
            jefe.get_by_role('button', name='Nueva solicitud').click()
            ok(jefe.url.endswith('/solicitudes/nueva'), 'V10: «Nueva solicitud» abre /solicitudes/nueva')
            v08 = jefe.locator('[role=dialog]'); expect(v08).to_be_visible()
            ok('Carlos Mendoza' in v08.locator('.auto').inner_text(), 'V08: el solicitante es el del token (no editable)')
            filas = v08.locator('tbody tr').all_inner_texts()
            if pendientes_antes == 0:
                ok(len(filas) == 2 and 'Res' in filas[0] and 'Marca X' in filas[0] and 'Arroz' in filas[1],
                   f'V08: la primera solicitud trae la de la demo ({[f.split(chr(9))[:3] for f in filas]})')
            foto(jefe, f'{tag}-V08-nueva-solicitud')
            numero, correlacion, v09 = registrar(jefe, tag)
            foto(jefe, f'{tag}-V09-solicitud-registrada')
            v09.get_by_role('button', name='VER SOLICITUD').click()
            jefe.wait_for_url(re.compile(r'/solicitudes\?sel=')); jefe.wait_for_selector('.detail-head h3')
            ok(numero in jefe.locator('.detail-head h3').inner_text(), f'V10: el detalle muestra {numero}')
            detalle = jefe.locator('.card', has=jefe.locator('h2', has_text='Detalle de solicitud')).inner_text()
            ok('Res' in detalle and 'Arroz' in detalle and 'Marca X' in detalle, 'V10: con la res «Marca X» y el arroz')
            foto(jefe, f'{tag}-V10-mis-solicitudes')
        else:
            # a 390 px: desde «Solicitar» en V07, con el producto de la fila (papa) y sin la precarga
            jefe.goto(BASE + '/inventario'); jefe.wait_for_selector('table tbody tr')
            jefe.locator('tr', has=jefe.locator('b', has_text='Papa')).get_by_role('button', name='Solicitar').click()
            jefe.wait_for_url(re.compile(r'/solicitudes/nueva\?producto=3'))
            v08 = jefe.locator('[role=dialog]'); expect(v08).to_be_visible()
            ok(v08.get_by_label('Producto').input_value() == '3', 'V08 desde V07: la papa viene escogida')
            ok(v08.locator('tbody tr td.empty').count() == 1, 'V08: sin la precarga de la demo (ya hay solicitudes)')
            v08.get_by_label('Cantidad').fill('10')
            v08.get_by_role('button', name='Agregar').click()
            ok('Papa' in v08.locator('tbody tr').first.inner_text(), 'V08: «Agregar» suma la papa a la tabla')
            foto(jefe, f'{tag}-V08-nueva-solicitud')
            numero, correlacion, v09 = registrar(jefe, tag)
            foto(jefe, f'{tag}-V09-solicitud-registrada')
            v09.get_by_role('button', name='VOLVER AL INICIO').click(); jefe.wait_for_url(re.compile('/inicio'))
            jefe.goto(BASE + f'/solicitudes?sel={numero}'); jefe.wait_for_selector('.detail-head h3')
            ok(numero in jefe.locator('.detail-head h3').inner_text(), f'V10: el detalle muestra {numero}')
            foto(jefe, f'{tag}-V10-mis-solicitudes')
        registro[tag] = {'numero': numero, 'correlacion': correlacion}
        sin_desborde(jefe, ancho, 'V10')

        # V05: indicadores y tabla; la alerta de la res se atiende por el evento (ms-inventario)
        for _ in range(20):
            despues = alertas_v05(jefe)
            if tag == 'movil' or 'Res' not in despues: break
            time.sleep(0.5)
        ok(int(kpi(jefe, 'Solicitudes pendientes')) == pendientes_antes + 1,
           f'V05: «Solicitudes pendientes» pasa de {pendientes_antes} a {kpi(jefe, "Solicitudes pendientes")}')
        ok(numero in jefe.locator('.card', has=jefe.locator('h2', has_text='Solicitudes recientes')).inner_text(), f'V05: {numero} está en «Solicitudes recientes»')
        if tag == 'escritorio':
            ok('Res' not in despues, f'V05: ms-inventario recibió solicitud.registrada y la alerta de la res quedó atendida ({despues})')
        foto(jefe, f'{tag}-V05-inicio-jefe')
        sin_desborde(jefe, ancho, 'V05')

        # ── Auxiliar de compras: V11 y V12 ──
        entrar(aux, AUXILIAR)
        aux.wait_for_selector('.kpi-strip'); aux.wait_for_selector(f'tr:has-text("{numero}")')
        ok(int(aux.locator('.kpi-strip strong').first.inner_text()) >= 1, f'V11: {aux.locator(".kpi-strip strong").first.inner_text()} solicitudes pendientes')
        foto(aux, f'{tag}-V11-inicio-auxiliar')
        sin_desborde(aux, ancho, 'V11')
        aux.locator('tr', has=aux.locator('b', has_text=numero)).click()
        aux.wait_for_url(re.compile(rf'/solicitudes\?sel={numero}'))
        aux.wait_for_selector(f'.detail-head h3:has-text("{numero}")')
        detalle = aux.locator('.card', has=aux.locator('h2', has_text='Detalle de solicitud')).inner_text()
        ok(JEFE in detalle, f'V12: el detalle de {numero} dice quién la pidió ({JEFE})')
        ok(('Res' in detalle and 'Arroz' in detalle) if tag == 'escritorio' else 'Papa' in detalle, 'V12: con sus productos (nombres de ms-menu)')
        foto(aux, f'{tag}-V12-detalle')
        if tag == 'escritorio':
            filtros = aux.locator('section.filters')
            filtros.locator('select').nth(0).select_option(label='Res')
            filtros.get_by_role('button', name='Buscar').click(); aux.wait_for_timeout(600)
            lista = aux.locator('.card', has=aux.locator('h2', has_text='Solicitudes de alimentos'))
            ok(numero in lista.inner_text(), 'V12 (HU-07): filtrar por producto «Res» la encuentra')
            filtros.locator('input').nth(2).fill('Marca Y')
            filtros.get_by_role('button', name='Buscar').click(); aux.wait_for_timeout(600)
            ok('No se encontraron solicitudes' in lista.inner_text(), 'V12 (HU-07): res «Marca Y» no da resultados')
            foto(aux, f'{tag}-V12-filtro-sin-resultados')
            filtros.locator('input').nth(0).fill('2026-12-31'); filtros.locator('input').nth(1).fill('2026-01-01')
            filtros.get_by_role('button', name='Buscar').click()
            expect(filtros.locator('.form-error')).to_contain_text('posterior')
            ok(True, 'V12 (HU-07): «desde» posterior a «hasta» muestra el error del servicio')
            filtros.get_by_role('button', name='Limpiar').click(); aux.wait_for_timeout(400)
            ok(numero in lista.inner_text(), 'V12: «Limpiar» vuelve a mostrar todas')
        sin_desborde(aux, ancho, 'V12')

        # el 400 es el del filtro con «desde» posterior a «hasta», provocado a propósito
        errores = [e for e in errores if not re.search(r'status of (400|401|404)', e)]
        ok(not errores, f'sin errores en la consola ({errores[:3]})')
        jctx.close(); actx.close()
    nav.close()
json.dump(registro, open(f'{OUT}/corte4a.json', 'w'), indent=2)
print(f'\nSolicitudes registradas: {registro}')
print('\nFALLAS:' if fallas else '\nTodo bien.', *fallas, sep='\n  ')
sys.exit(1 if fallas else 0)
