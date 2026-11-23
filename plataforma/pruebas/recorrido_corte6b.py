#!/usr/bin/env python3
# Recorrido del corte 6b en el navegador (Playwright), en escritorio (1440 px) y a 390 px: las dos ventanas de
# ms-analitica, sin mockup.
#   escritorio: el director comercial entra directo al «Tablero BAM» (HU-18): los tres indicadores con su valor, su
#   gráfica y su periodo (sin fechas, la demanda del último mes y el proveedor del último año, PD-03), en menos de 5 s;
#   la Sucursal Norte cambia la demanda y no el proveedor; un rango de la temporada de lluvias cambia el producto;
#   los mensajes de PD-10 (rango invertido, una sola fecha y un periodo sin datos); un pedido nuevo del mesero se
#   ve al volver a consultar. El jefe de cocina abre «Promociones sugeridas» (HU-19): las sugerencias del análisis
#   semanal con producto, motivo y vigencia; acepta el Ajiaco y descarta la Limonada; si otra jefa (Sofía, del corte
#   6a) ya resolvió una sugerencia, su intento recibe «Esta sugerencia ya fue aceptada o descartada.»; el filtro por
#   fechas y su mensaje de periodo vacío. Cada rol ve solo su ventana en el menú.
#   390 px: las dos ventanas caben a lo ancho; el jefe acepta la Pechuga.
# Que nada se desborde a lo ancho y que la consola quede limpia.
# Corre después de recorrido_corte6a.py y verificar_corte6a.py, con la plataforma recién levantada (TendenciasJob ya
# corrió: cada 30 s en el compose).
# Uso: python recorrido_corte6b.py <carpeta-de-capturas>   (necesita playwright y /usr/bin/chromium;
#      BASE=http://localhost:8080 por omisión). Deja en la carpeta corte6b.json con las correlaciones del pedido y de
#      cada decisión, que verificar_corte6b.py revisa en PostgreSQL y en Kafka.
import datetime, json, os, re, sys, time, urllib.request
from playwright.sync_api import sync_playwright, expect

BASE = os.environ.get('BASE', 'http://localhost:8080')
OUT = sys.argv[1]
os.makedirs(OUT, exist_ok=True)
DIRECTOR, JEFE, SOFIA, MESERO = ('andres.rojas@konradgourmet.com', 'carlos.mendoza@konradgourmet.com',
                                 'sofia.ramirez@konradgourmet.com', 'isabella.torres@konradgourmet.com')
SIN_DATOS = 'No hay información suficiente para el periodo seleccionado'
HOY = datetime.date.today()
fallas, registro = [], {'pedido': None, 'decisiones': [], 'carga_ms': None}

def ok(cond, texto):
    print(('  ✓ ' if cond else '  ✗ ') + texto)
    if not cond: fallas.append(texto)

def entrar(page, correo, destino, clave='Konrad2026'):
    page.goto(BASE + '/login')
    page.fill('#email', correo)
    page.fill('#password', clave)
    page.click('button.btn-login')
    page.wait_for_url(re.compile(destino))

def foto(page, nombre):
    page.wait_for_timeout(400)
    page.screenshot(path=f'{OUT}/{nombre}.png', full_page=True)

def sin_desborde(page, ancho, nombre):
    doc = page.evaluate('document.documentElement.scrollWidth')
    ok(doc <= ancho + 1, f'{nombre}: sin desplazamiento horizontal ({doc} ≤ {ancho})')

def http(metodo, ruta, cuerpo=None, token=None):
    pet = urllib.request.Request(BASE + ruta, method=metodo, data=json.dumps(cuerpo).encode() if cuerpo is not None else None,
                                 headers={'Content-Type': 'application/json', **({'Authorization': f'Bearer {token}'} if token else {})})
    try:
        with urllib.request.urlopen(pet, timeout=30) as r:
            return r.status, json.loads(r.read() or b'null'), r.headers.get('X-Correlation-Id')
    except urllib.error.HTTPError as e:
        return e.code, None, None

def token(correo):
    return http('POST', '/api/auth/token', {'correo': correo, 'clave': 'Konrad2026'})[1]['jwt']

def dmy(d):
    return d.strftime('%d/%m/%Y')

def indicador(page, nombre):
    return page.locator('section.bam-kpi', has=page.locator('h2', has_text=nombre))

def ganador(page, nombre):
    return indicador(page, nombre).locator('.bam-valor strong').inner_text()

def fechas(page, desde, hasta):
    page.get_by_label('Desde', exact=True).fill(desde)
    page.get_by_label('Hasta', exact=True).fill(hasta)

def consultar(page):
    with page.expect_response(re.compile(r'/api/bam/kpis')):
        page.get_by_role('button', name='Consultar').click()
    page.wait_for_timeout(300)

def fila(page, producto):
    return page.locator('tbody tr', has=page.locator('td b', has_text=re.compile(f'^{producto}$')))

def decisiones(page):
    page.on('response', lambda r: registro['decisiones'].append({'url': r.url.split('/api/', 1)[1], 'estado': r.status,
                                                                  'correlacion': r.headers.get('x-correlation-id')})
            if '/api/bam/promociones/' in r.url and r.request.method == 'PATCH' else None)

with sync_playwright() as pw:
    nav = pw.chromium.launch(executable_path='/usr/bin/chromium')
    for ancho, alto, tag in ((1440, 900, 'escritorio'), (390, 844, 'movil')):
        print(f'== {tag} ({ancho} px)')
        errores = []
        ctx = {q: nav.new_context(viewport={'width': ancho, 'height': alto}, locale='es-CO', timezone_id='America/Bogota')
               for q in ('director', 'jefe', 'sofia')}
        dir_, jefe = ctx['director'].new_page(), ctx['jefe'].new_page()
        for p in (dir_, jefe):
            p.on('console', lambda m: errores.append(m.text) if m.type == 'error' else None)

        # ── Tablero BAM del director comercial ──
        entrar(dir_, DIRECTOR, r'/bam')
        inicio = time.time()
        expect(dir_.locator('section.bam-kpi')).to_have_count(3)
        expect(indicador(dir_, 'Producto con mayor demanda').locator('.bam-valor strong')).to_be_visible()
        carga = int((time.time() - inicio) * 1000)
        ok(carga < 5000, f'el director entra directo al tablero y los tres indicadores se ven {carga} ms después de abrirlo (< 5 s, HU-18)')
        if tag == 'escritorio':
            registro['carga_ms'] = carga
            menu = [t.strip() for t in dir_.locator('.sidebar .nav a, .sidebar .nav button').all_inner_texts() if t.strip()]
            ok('Tablero BAM' in ' '.join(menu) and 'Promociones' not in ' '.join(menu), f'el menú del director comercial: {menu}')
            producto = indicador(dir_, 'Producto con mayor demanda')
            ok(producto.locator('.bam-valor span').inner_text().endswith('platos vendidos'), f'el producto con su valor numérico ({producto.locator(".bam-valor").inner_text()!r})')
            ok(producto.locator('.bam-barras li').count() == 5, 'y su gráfica con los 5 platos más pedidos')
            ok(producto.locator('.bam-barras li').first.locator('i.gana').count() == 1, 'el ganador va primero y resaltado')
            ok(producto.locator('.card-head p').inner_text() == f'Del {dmy(HOY - datetime.timedelta(days=29))} al {dmy(HOY)}',
               f'sin fechas, la demanda es de los últimos 30 días ({producto.locator(".card-head p").inner_text()})')
            proveedor = indicador(dir_, 'Proveedor con mejores precios')
            ok(proveedor.locator('.card-head p').inner_text() == f'Del {dmy(HOY - datetime.timedelta(days=364))} al {dmy(HOY)}',
               f'y el proveedor, de los últimos 365 ({proveedor.locator(".card-head p").inner_text()})')
            ok('frente al precio estándar' in proveedor.locator('.bam-valor span').inner_text() and proveedor.locator('.bam-pista.eje').count() >= 2,
               f'el proveedor con su diferencia promedio y la gráfica contra el precio estándar ({ganador(dir_, "Proveedor con mejores precios")})')
            ok(indicador(dir_, 'Sucursal con mayor demanda').locator('.bam-barras li').count() == 3, 'la sucursal, con las tres sucursales en la gráfica')
            antes = {'producto': ganador(dir_, 'Producto con mayor demanda'), 'proveedor': ganador(dir_, 'Proveedor con mejores precios')}
            sin_desborde(dir_, ancho, 'Tablero BAM')
            foto(dir_, f'{tag}-bam')

            # la sucursal filtra solo la demanda
            dir_.get_by_label('Sucursal', exact=True).select_option(label='Sucursal Norte')
            consultar(dir_)
            ok(ganador(dir_, 'Producto con mayor demanda') == 'Ajiaco' and ganador(dir_, 'Sucursal con mayor demanda') == 'Sucursal Norte',
               f'en la Sucursal Norte gana el Ajiaco ({ganador(dir_, "Producto con mayor demanda")})')
            ok(ganador(dir_, 'Proveedor con mejores precios') == antes['proveedor'], 'y el proveedor no cambia: las compras son centrales')
            ok('filtra solo la demanda' in dir_.locator('.bam-nota').inner_text(), 'la nota lo dice')
            foto(dir_, f'{tag}-bam-norte')

            # un rango de la temporada de lluvias (entre 60 y 150 días atrás) cambia el producto
            dir_.get_by_label('Sucursal', exact=True).select_option(label='Todas')
            d, h = HOY - datetime.timedelta(days=120), HOY - datetime.timedelta(days=70)
            fechas(dir_, d.isoformat(), h.isoformat())
            consultar(dir_)
            ok(ganador(dir_, 'Producto con mayor demanda') == 'Ajiaco', f'del {dmy(d)} al {dmy(h)} gana el Ajiaco ({ganador(dir_, "Producto con mayor demanda")})')
            ok(all(indicador(dir_, n).locator('.card-head p').inner_text() == f'Del {dmy(d)} al {dmy(h)}'
                   for n in ('Producto con mayor demanda', 'Sucursal con mayor demanda', 'Proveedor con mejores precios')),
               'con rango, los tres indicadores dicen el mismo periodo (PD-03)')
            foto(dir_, f'{tag}-bam-rango')

            # mensajes de PD-10
            fechas(dir_, HOY.isoformat(), (HOY - datetime.timedelta(days=5)).isoformat())
            consultar(dir_)
            expect(dir_.locator('.filters .form-error')).to_have_text('La fecha inicial no puede ser posterior a la fecha final.')
            ok(True, 'rango invertido: «La fecha inicial no puede ser posterior a la fecha final.»')
            foto(dir_, f'{tag}-bam-rango-invertido')
            fechas(dir_, HOY.isoformat(), '')
            consultar(dir_)
            expect(dir_.locator('.filters .form-error')).to_have_text('Seleccione la fecha inicial y la fecha final.')
            ok(True, 'una sola fecha: «Seleccione la fecha inicial y la fecha final.»')
            fechas(dir_, '2020-01-01', '2020-01-31')
            consultar(dir_)
            expect(dir_.locator('.bam-vacio')).to_have_count(3)
            ok(all(t.strip() == SIN_DATOS for t in dir_.locator('.bam-vacio p').all_inner_texts()), f'un periodo sin datos: «{SIN_DATOS}» en los tres')
            foto(dir_, f'{tag}-bam-sin-datos')

            # un pedido nuevo se ve al volver a consultar
            dir_.get_by_role('button', name='Restablecer periodo').click()
            expect(indicador(dir_, 'Producto con mayor demanda').locator('.bam-barras li')).to_have_count(5)
            punta = indicador(dir_, 'Producto con mayor demanda').locator('.bam-barras li', has_text='Punta de anca')
            valor_antes = punta.locator('.bam-cifra').inner_text() if punta.count() else None
            mes = token(MESERO)
            carta = {p['nombre']: p for p in http('GET', '/api/pedidos/catalogo', token=mes)[1]}
            estado, cuerpo, correlacion = http('POST', '/api/pedidos', {'mesa': 8, 'detalles': [
                {'platoId': carta['Punta de anca']['platoId'], 'cantidad': 3, 'nota': '', 'precioUnitario': carta['Punta de anca']['precio']}]}, mes)
            ok(estado == 201, f'el mesero registra 3 Punta de anca en la mesa 8 ({estado}, {cuerpo and cuerpo["numero"]})')
            registro['pedido'] = {'numero': cuerpo and cuerpo['numero'], 'correlacion': correlacion, 'plato': 'Punta de anca', 'cantidad': 3, 'mesa': 8}
            nuevo = None
            for _ in range(20):
                time.sleep(1)
                consultar(dir_)
                nuevo = punta.locator('.bam-cifra').inner_text() if punta.count() else None
                if nuevo != valor_antes: break
            ok(valor_antes and nuevo and int(nuevo.replace('.', '')) == int(valor_antes.replace('.', '')) + 3,
               f'al volver a consultar, la Punta de anca sube de {valor_antes} a {nuevo} (el evento llegó a ms-analitica)')
        else:
            ok(dir_.locator('.bam-grid').evaluate("e => getComputedStyle(e).gridTemplateColumns.split(' ').length") == 1,
               'a 390 px los indicadores van uno debajo del otro')
            sin_desborde(dir_, ancho, 'Tablero BAM')
            foto(dir_, f'{tag}-bam')

        # ── Promociones sugeridas del jefe de cocina ──
        entrar(jefe, JEFE, r'/inicio')
        decisiones(jefe)
        jefe.get_by_role('link', name='Promociones').click()
        jefe.wait_for_url(re.compile(r'/promociones'))
        expect(jefe.locator('tbody tr td b').first).to_be_visible()
        if tag == 'escritorio':
            menu = ' '.join(jefe.locator('.sidebar .nav').all_inner_texts())
            ok('Tablero BAM' not in menu, 'el jefe de cocina no tiene el tablero en su menú')
            filas = jefe.locator('tbody tr').count()
            ok(filas >= 4, f'el histórico tiene las sugerencias del análisis semanal ({filas})')
            aj = fila(jefe, 'Ajiaco')
            ok(aj.count() >= 1 and 'Tendencia en redes y entre los 3 más pedidos de la semana' in aj.first.inner_text(), 'el Ajiaco, por las redes y por los pedidos')
            ok(f'{dmy(HOY)} – {dmy(HOY + datetime.timedelta(days=6))}' in aj.first.inner_text(), 'vigente 7 días desde hoy')
            ok('Tendencia en redes' in fila(jefe, 'Limonada natural').first.inner_text(), 'la Limonada natural, por las redes')
            ok('Entre los 3 más pedidos de la semana' in fila(jefe, 'Bandeja paisa').first.inner_text(), 'la Bandeja paisa, entre los 3 más pedidos')
            sin_desborde(jefe, ancho, 'Promociones sugeridas')
            foto(jefe, f'{tag}-promociones')

            aj.first.get_by_role('button', name='Aceptar Ajiaco').click()
            expect(jefe.locator('#toast')).to_contain_text('Aceptaste la promoción de Ajiaco.')
            expect(fila(jefe, 'Ajiaco').first).to_contain_text('Aceptada')
            ok(fila(jefe, 'Ajiaco').first.get_by_role('button').count() == 0, 'acepta el Ajiaco: queda Aceptada y sin botones')
            fila(jefe, 'Limonada natural').first.get_by_role('button', name='Descartar Limonada natural').click()
            expect(jefe.locator('#toast')).to_contain_text('Descartaste la promoción de Limonada natural.')
            expect(fila(jefe, 'Limonada natural').first).to_contain_text('Descartada')
            ok(True, 'descarta la Limonada natural')
            foto(jefe, f'{tag}-promociones-decididas')

            # otra jefa resuelve la Bandeja paisa mientras Carlos tiene la lista abierta
            sofia = ctx['sofia'].new_page()
            try:
                entrar(sofia, SOFIA, r'/inicio')
                sofia.get_by_role('link', name='Promociones').click()
                fila(sofia, 'Bandeja paisa').first.get_by_role('button', name='Aceptar Bandeja paisa').click()
                expect(sofia.locator('#toast')).to_contain_text('Aceptaste la promoción de Bandeja paisa.')
                quien = 'Sofía Ramírez (Sucursal Norte)'
            except Exception:
                http('PATCH', f"/api/bam/promociones/{[s for s in http('GET', '/api/bam/promociones', token=token(JEFE))[1] if s['producto'] == 'Bandeja paisa'][0]['id']}/aceptar",
                     token=token(JEFE))
                quien = 'otra sesión'
            fila(jefe, 'Bandeja paisa').first.get_by_role('button', name='Descartar Bandeja paisa').click()
            expect(jefe.locator('#toast')).to_contain_text('Esta sugerencia ya fue aceptada o descartada.')
            expect(fila(jefe, 'Bandeja paisa').first).to_contain_text('Aceptada')
            ok(True, f'{quien} aceptó la Bandeja paisa: Carlos, con la lista vieja, recibe «Esta sugerencia ya fue aceptada o descartada.» y la lista se actualiza')
            foto(jefe, f'{tag}-promociones-ya-resuelta')

            # filtro por fechas
            jefe.get_by_label('Desde', exact=True).fill(HOY.isoformat())
            jefe.get_by_label('Hasta', exact=True).fill((HOY - datetime.timedelta(days=1)).isoformat())
            jefe.get_by_role('button', name='Consultar').click()
            expect(jefe.locator('.filters .form-error')).to_have_text('La fecha inicial no puede ser posterior a la fecha final.')
            ok(True, 'rango invertido con su mensaje de PD-10')
            jefe.get_by_label('Desde', exact=True).fill('2020-01-01')
            jefe.get_by_label('Hasta', exact=True).fill('2020-01-31')
            jefe.get_by_role('button', name='Consultar').click()
            expect(jefe.locator('tbody td.empty')).to_have_text('No hay promociones sugeridas para el periodo seleccionado.')
            ok(True, 'un periodo sin sugerencias: «No hay promociones sugeridas para el periodo seleccionado.»')
            foto(jefe, f'{tag}-promociones-sin-datos')
            jefe.get_by_label('Desde', exact=True).fill(HOY.isoformat())
            jefe.get_by_label('Hasta', exact=True).fill(HOY.isoformat())
            jefe.get_by_role('button', name='Consultar').click()
            expect(jefe.locator('.card-head p', has_text='vigentes el')).to_be_visible()
            ok(jefe.locator('tbody tr').count() >= 4, 'las vigentes hoy')
            jefe.get_by_role('button', name='Todo el histórico').click()
            expect(jefe.locator('.card-head p', has_text='todo el histórico')).to_be_visible()
        else:
            aceptar = fila(jefe, 'Pechuga a la plancha').first.get_by_role('button', name='Aceptar Pechuga a la plancha')
            expect(aceptar).to_be_visible()
            aceptar.click()
            expect(jefe.locator('#toast')).to_contain_text('Aceptaste la promoción de Pechuga a la plancha.')
            expect(fila(jefe, 'Pechuga a la plancha').first).to_contain_text('Aceptada')
            ok(True, 'a 390 px el jefe acepta la Pechuga a la plancha')
            sin_desborde(jefe, ancho, 'Promociones sugeridas')
            foto(jefe, f'{tag}-promociones')

        errores = [e for e in errores if not re.search(r'status of (400|401|403|404|409|503)', e)]
        ok(not errores, f'sin errores en la consola ({errores[:3]})')
        for c in ctx.values(): c.close()
    nav.close()
json.dump(registro, open(f'{OUT}/corte6b.json', 'w'), indent=2)
print('\nFALLAS:' if fallas else '\nTodo bien.', *fallas, sep='\n  ')
sys.exit(1 if fallas else 0)
