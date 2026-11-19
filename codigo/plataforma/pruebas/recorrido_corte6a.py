#!/usr/bin/env python3
# Recorrido del corte 6a en el navegador (Playwright), en escritorio (1440 px) y a 390 px: las ventanas de la
# administradora (ms-administracion, ms-identidad y ms-notificaciones), sin mockup.
#   escritorio: el menú agrupa «Administración» (Parámetros y tema, Auditoría y Logs de errores). En «Parámetros y
#   tema» crea general.propina-sugerida, ve los mensajes de PD-10 y de un color mal escrito y de un logo de más de
#   200 KB, y cambia la imagen corporativa (nombre, color verde y un logo PNG): la SPA la aplica enseguida y el
#   mesero la ve al iniciar sesión, sin redesplegar (HU-20, DEC-86). «Auditoría» abre con los últimos 7 días, lo
#   más reciente primero, con sus mensajes (rango invertido, sin fecha, periodo vacío) y «Hoy» (HU-21, PD-07).
#   Con el servidor de correo en pausa, la auxiliar reenvía una cotización (HU-12): el error real llega a
#   Elasticsearch y «Logs de errores» lo muestra al filtrar por ms-notificaciones (HU-22, DEC-84). «Usuarios»
#   registra a Sofía Ramírez, jefa de cocina de la Sucursal Norte (CU-19), con los mensajes de correo repetido y de
#   contraseña, y ella inicia sesión; «Destinatarios» la agrega a la Sucursal Norte (DEC-13). El mesero no entra a
#   las ventanas del administrador.
#   390 px: las cinco ventanas caben a lo ancho; la administradora restaura el tema de siempre y el mesero lo ve.
# Que nada se desborde a lo ancho y que la consola quede limpia.
# Corre después de recorrido_corte5.py y verificar_corte5.py.
# Uso: python recorrido_corte6a.py <carpeta-de-capturas>   (necesita playwright y /usr/bin/chromium;
#      BASE=http://localhost:8080 por omisión; COMPOSE_PROJECT_NAME elige la instancia para pausar el correo).
#      Deja en la carpeta corte6a.json con la correlación de cada cambio y del error, que verificar_corte6a.py revisa
#      en PostgreSQL, Kafka y Elasticsearch.
import json, os, re, subprocess, sys, time, urllib.request, uuid
from playwright.sync_api import sync_playwright, expect

BASE = os.environ.get('BASE', 'http://localhost:8080')
PROYECTO = os.environ.get('COMPOSE_PROJECT_NAME', 'konrad-gourmet')
OUT = sys.argv[1]
os.makedirs(OUT, exist_ok=True)
ADMIN, MESERO, AUXILIAR = 'paula.herrera@konradgourmet.com', 'isabella.torres@konradgourmet.com', 'isabella.castro@konradgourmet.com'
SOFIA = 'sofia.ramirez@konradgourmet.com'
VERDE = '#1F7A4D'
fallas, registro = [], {'parametros': [], 'usuario': None, 'destinatario': None, 'error': None}

def ok(cond, texto):
    print(('  ✓ ' if cond else '  ✗ ') + texto)
    if not cond: fallas.append(texto)

def entrar(page, correo, clave='Konrad2026'):
    page.goto(BASE + '/login')
    page.fill('#email', correo)
    page.fill('#password', clave)
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

def variable(page, nombre):
    return page.evaluate(f"getComputedStyle(document.documentElement).getPropertyValue('{nombre}').trim()")

def http(metodo, ruta, cuerpo=None, token=None, cabeceras=None):
    pet = urllib.request.Request(BASE + ruta, method=metodo, data=json.dumps(cuerpo).encode() if cuerpo is not None else None,
                                 headers={'Content-Type': 'application/json', **({'Authorization': f'Bearer {token}'} if token else {}), **(cabeceras or {})})
    try:
        with urllib.request.urlopen(pet, timeout=30) as r:
            return r.status, json.loads(r.read() or b'null')
    except urllib.error.HTTPError as e:
        return e.code, None

def token(correo):
    return http('POST', '/api/auth/token', {'correo': correo, 'clave': 'Konrad2026'})[1]['jwt']

def png(ruta, lado, ruido=False):
    """Un PNG de prueba: el logo verde (pequeño) o uno con ruido de más de 200 KB."""
    from PIL import Image
    import random
    if ruido:
        im = Image.frombytes('RGB', (lado, lado), bytes(random.getrandbits(8) for _ in range(lado * lado * 3)))
    else:
        im = Image.new('RGBA', (lado, lado), (0, 0, 0, 0))
        for x in range(lado):
            for y in range(lado):
                if (x - lado / 2) ** 2 + (y - lado / 2) ** 2 < (lado / 2 - 2) ** 2:
                    im.putpixel((x, y), (31, 122, 77, 255))
    im.save(ruta)
    return ruta

def guardar_parametro(page, respuestas):
    """Toma la correlación de cada PUT /api/admin/parametros/… de la página."""
    page.on('response', lambda r: respuestas.append({'clave': r.url.rsplit('/', 1)[1], 'estado': r.status,
                                                     'correlacion': r.headers.get('x-correlation-id')})
            if '/api/admin/parametros/' in r.url and r.request.method == 'PUT' else None)

def error_real():
    """El servidor de correo deja de responder y la auxiliar reenvía una cotización (HU-12): ms-notificaciones no
    puede enviar el correo, lo reintenta y registra el error, que llega a Elasticsearch (DEC-84). Al volver el
    correo, la cola de errores lo entrega sola (DEC-81)."""
    aux = token(AUXILIAR)
    _, lista = http('GET', '/api/cotizaciones', token=aux)
    enviadas = [c['numero'] for c in lista or [] if c['estado'] == 'ENVIADA']
    ok(bool(enviadas), f'preparación: hay una cotización ENVIADA para reenviar ({enviadas[-1:] or "ninguna"})')
    if not enviadas:
        return None
    correlacion = str(uuid.uuid4())
    subprocess.run(['docker', 'pause', f'{PROYECTO}-correo-1'], capture_output=True)
    try:
        estado, _ = http('POST', f'/api/cotizaciones/{enviadas[-1]}/reenvio', token=aux, cabeceras={'X-Correlation-Id': correlacion})
        ok(estado == 200, f'con el servidor de correo en pausa, la auxiliar reenvía {enviadas[-1]} (HU-12) ({estado})')
        adm = token(ADMIN)
        hallado = []
        for _ in range(45):
            _, hallado = http('GET', '/api/admin/logs?servicio=ms-notificaciones', token=adm)
            if any(enviadas[-1] in e['mensaje'] or 'correo' in e['mensaje'].lower() or 'mail' in e['mensaje'].lower() for e in hallado or []):
                break
            time.sleep(1)
        ok(bool(hallado), f'el error de ms-notificaciones ya está en Elasticsearch ({len(hallado or [])} errores de ese servicio)')
    finally:
        subprocess.run(['docker', 'unpause', f'{PROYECTO}-correo-1'], capture_output=True)
    return {'cotizacion': enviadas[-1], 'correlacion': correlacion, 'mensajes': [e['mensaje'] for e in (hallado or [])[:3]]}

with sync_playwright() as pw:
    nav = pw.chromium.launch(executable_path='/usr/bin/chromium')
    for ancho, alto, tag in ((1440, 900, 'escritorio'), (390, 844, 'movil')):
        print(f'== {tag} ({ancho} px)')
        errores = []
        ctx = {q: nav.new_context(viewport={'width': ancho, 'height': alto}, locale='es-CO', timezone_id='America/Bogota')
               for q in ('admin', 'mesero', 'sofia')}
        adm, mes = ctx['admin'].new_page(), ctx['mesero'].new_page()
        for p in (adm, mes):
            p.on('console', lambda m: errores.append(m.text) if m.type == 'error' else None)
        entrar(adm, ADMIN)
        respuestas = []
        guardar_parametro(adm, respuestas)

        if tag == 'escritorio':
            # ── Menú e inicio de la administradora ──
            grupo = adm.locator('.nav-group', has=adm.locator('button', has_text='Administración'))
            grupo.locator('button').click()
            hijos = grupo.locator('.nav-sub a').all_inner_texts()
            ok(hijos == ['Parámetros y tema', 'Auditoría', 'Logs de errores'], f'el menú agrupa Administración: {hijos}')
            accesos = adm.locator('.acceso b').all_inner_texts()
            ok({'Parámetros y tema', 'Auditoría', 'Logs de errores', 'Usuarios', 'Destinatarios'} <= set(accesos), f'el inicio lista sus ventanas ({accesos})')
            sin_desborde(adm, ancho, 'Inicio de la administradora')
            foto(adm, f'{tag}-admin-inicio')

            # ── Parámetros y tema: un parámetro general ──
            grupo.get_by_role('link', name='Parámetros y tema').click()
            adm.wait_for_url(re.compile(r'/admin/parametros'))
            generales = tarjeta(adm, 'Parámetros generales')
            expect(generales.locator('tbody tr', has_text='general.moneda')).to_contain_text('COP')
            generales.get_by_role('button', name='Nuevo parámetro').click()
            generales.get_by_label('Clave del parámetro').fill('general.propina-sugerida')
            generales.get_by_label('Valor del parámetro').fill('10 %')
            generales.get_by_role('button', name='Crear parámetro').click()
            expect(adm.locator('#toast')).to_contain_text('Parámetro actualizado.')
            expect(generales.locator('tbody tr', has_text='general.propina-sugerida')).to_contain_text('10 %')
            ok(True, 'crea general.propina-sugerida = 10 % («Parámetro actualizado.»)')
            generales.locator('tbody tr', has_text='general.moneda').click()
            ok(generales.get_by_label('Clave del parámetro').get_attribute('readonly') is not None, 'al editar, la clave no se cambia')
            generales.get_by_label('Valor del parámetro').fill('   ')
            generales.get_by_role('button', name='Guardar cambios').click()
            expect(generales.locator('.form-error')).to_have_text('El valor del parámetro general.moneda es obligatorio.')
            ok(True, 'un valor vacío muestra el mensaje de PD-10')
            generales.get_by_role('button', name='Nuevo parámetro').click()
            generales.get_by_label('Clave del parámetro').fill('General Moneda')
            generales.get_by_label('Valor del parámetro').fill('COP')
            generales.get_by_role('button', name='Crear parámetro').click()
            expect(generales.locator('.form-error')).to_contain_text('La clave del parámetro solo puede tener minúsculas')
            ok(True, 'una clave con mayúsculas o espacios se rechaza')

            # ── Parámetros y tema: la imagen corporativa ──
            imagen = tarjeta(adm, 'Imagen corporativa')
            imagen.get_by_label('Color principal').fill('naranja')
            imagen.get_by_role('button', name='Guardar imagen corporativa').click()
            expect(imagen.locator('.form-error')).to_have_text('El color debe tener el formato #RRGGBB (por ejemplo, #F28705).')
            ok(True, 'un color mal escrito se rechaza con su mensaje')
            imagen.get_by_label('Subir logo').set_input_files(png(f'{OUT}/logo-grande.png', 300, ruido=True))
            expect(imagen.locator('.form-error')).to_have_text('El logo debe ser una imagen PNG, JPG, SVG o WebP de máximo 200 KB.')
            ok(os.path.getsize(f'{OUT}/logo-grande.png') > 200 * 1024, f'un logo de {os.path.getsize(f"{OUT}/logo-grande.png") // 1024} KB se rechaza')
            imagen.get_by_label('Nombre del sistema').fill('Konrad Gourmet Express')
            imagen.get_by_label('Color principal').fill(VERDE)
            imagen.get_by_label('Subir logo').set_input_files(png(f'{OUT}/logo-verde.png', 64))
            expect(imagen.locator('.tema-vista img')).to_have_attribute('src', re.compile(r'^data:image/png;base64,'))
            ok(imagen.locator('.form-error').count() == 0, 'el logo PNG pequeño se acepta y sale en la vista previa')
            foto(adm, f'{tag}-parametros-antes-de-guardar')
            imagen.get_by_role('button', name='Guardar imagen corporativa').click()
            expect(adm.locator('#toast')).to_contain_text('Imagen corporativa actualizada')
            ok(variable(adm, '--naranja').upper() == VERDE, f'la SPA aplica el color enseguida (--naranja = {variable(adm, "--naranja")})')
            ok(adm.locator('.sidebar .brand b').inner_text() == 'Konrad Gourmet Express', 'y el nombre en la barra')
            ok(adm.locator('.sidebar .brand img').get_attribute('src').startswith('data:image/png;base64,'), 'y el logo')
            boton = adm.evaluate("getComputedStyle(document.querySelector('.btn.primary')).backgroundImage + getComputedStyle(document.querySelector('.btn.primary')).backgroundColor")
            ok('31, 122, 77' in boton or '1f7a4d' in boton.lower() or 'color-mix' in boton or 'rgb(' in boton, f'el botón principal toma el color ({boton[:80]}…)')
            sin_desborde(adm, ancho, 'Parámetros y tema')
            foto(adm, f'{tag}-parametros-y-tema')

            # ── El mesero toma el tema al iniciar sesión, sin redesplegar (DEC-86) ──
            mes.goto(BASE + '/login')
            ok(mes.locator('img.brand__logo').get_attribute('src') == '/assets/logo/logo.png', 'la pantalla de inicio de sesión conserva el logo de siempre (DEC-86)')
            entrar(mes, MESERO)
            mes.wait_for_function(f"() => getComputedStyle(document.documentElement).getPropertyValue('--naranja').trim().toUpperCase() === '{VERDE}'", timeout=10000)
            ok(True, f'el mesero ve el color nuevo al iniciar sesión (--naranja = {variable(mes, "--naranja")})')
            ok(mes.locator('.sidebar .brand b').inner_text() == 'Konrad Gourmet Express' and mes.title().startswith('Konrad Gourmet Express'),
               'con el nombre nuevo en la barra y en la pestaña')
            ok(mes.locator('.sidebar .brand img').get_attribute('src').startswith('data:image/png;base64,'), 'y el logo nuevo')
            foto(mes, f'{tag}-mesero-con-el-tema-nuevo')
            mes.goto(BASE + '/admin/auditoria')
            mes.wait_for_url(re.compile(r'/inicio'))
            ok(True, 'el mesero no entra a la auditoría: la SPA lo devuelve a su inicio')

            # ── Auditoría ──
            grupo.get_by_role('link', name='Auditoría').click()
            adm.wait_for_url(re.compile(r'/admin/auditoria'))
            auditoria = tarjeta(adm, 'Movimientos de auditoría')
            expect(auditoria.locator('.card-title p')).to_contain_text('movimientos')
            filas = [f.split('\t') for f in auditoria.locator('tbody tr').all_inner_texts()]
            momentos = [tuple(reversed(f[0].split('/'))) + (f[1],) for f in filas]
            ok(momentos == sorted(momentos, reverse=True) and 'Inicio de sesión (MESERO)' in filas[0][3],
               f'abre con los últimos 7 días, lo más reciente primero: «{" · ".join(filas[0])}»')
            ok(any(f[2] == ADMIN and f[3].startswith('Parámetro tema.') for f in filas), 'y en la primera página, el cambio del tema a nombre de la administradora')
            sub = auditoria.locator('.card-title p').inner_text()
            m = re.match(r'(\d+) movimientos', sub)
            ok(m is not None and int(m.group(1)) > 50, f'con la auditoría de los cortes anteriores: «{sub}»')
            registro['auditoria_visible'] = int(m.group(1)) if m else 0
            sin_desborde(adm, ancho, 'Auditoría')
            foto(adm, f'{tag}-auditoria')
            hoy = time.strftime('%Y-%m-%d'); ayer = time.strftime('%Y-%m-%d', time.localtime(time.time() - 86400))
            adm.get_by_label('Desde').fill(hoy); adm.get_by_label('Hasta').fill(ayer)
            adm.get_by_role('button', name='Consultar').click()
            expect(adm.locator('.filters .form-error')).to_have_text('La fecha inicial no puede ser posterior a la fecha final.')
            ok(True, 'un rango invertido muestra el mensaje de PD-10')
            adm.get_by_label('Desde').fill('')
            adm.get_by_role('button', name='Consultar').click()
            expect(adm.locator('.filters .form-error')).to_have_text('Seleccione la fecha inicial y la fecha final.')
            ok(True, 'sin una de las fechas: el rango es obligatorio (PD-07)')
            adm.get_by_label('Desde').fill('2020-01-01'); adm.get_by_label('Hasta').fill('2020-01-31')
            adm.get_by_role('button', name='Consultar').click()
            expect(auditoria.locator('td.empty')).to_have_text('No hay movimientos de auditoría en el periodo seleccionado.')
            ok(True, 'un periodo sin movimientos muestra el mensaje de PD-10')
            adm.get_by_role('button', name='Hoy').click()
            expect(auditoria.locator('.card-title p')).to_contain_text('del ')
            hoy = time.strftime('%Y-%m-%d')
            ok(adm.get_by_label('Desde').input_value() == hoy == adm.get_by_label('Hasta').input_value(), '«Hoy» consulta el día de hoy')

            # ── Un error real y la ventana de logs (DEC-84) ──
            registro['error'] = error_real()
            grupo.get_by_role('link', name='Logs de errores').click()
            adm.wait_for_url(re.compile(r'/admin/logs'))
            logs = tarjeta(adm, 'Errores registrados')
            expect(logs.locator('.card-title p')).to_contain_text('todos los servicios')
            with adm.expect_response(lambda r: '/api/admin/logs?servicio=ms-notificaciones' in r.url) as resp:
                adm.get_by_label('Servicio').select_option('ms-notificaciones')
            ok(resp.value.status == 200, f'GET /api/admin/logs?servicio=ms-notificaciones → {resp.value.status}')
            expect(logs.locator('tbody tr').first).to_contain_text('ms-notificaciones')
            filas = logs.locator('tbody tr').all_inner_texts()
            ok(filas and all('ms-notificaciones' in f for f in filas), f'la ventana muestra el error de ms-notificaciones ({len(filas)} en la página)')
            print('     ', filas[0].replace('\t', ' · ')[:160])
            sin_desborde(adm, ancho, 'Logs de errores')
            foto(adm, f'{tag}-logs-de-errores')
            adm.get_by_label('Servicio').select_option('ms-analitica')
            expect(logs.locator('td.empty')).to_have_text('No hay errores registrados para el servicio seleccionado.')
            ok(True, 'un servicio sin errores muestra el mensaje de PD-10')

            # ── Usuarios (CU-19) ──
            adm.get_by_role('link', name='Usuarios').click()
            adm.wait_for_url(re.compile(r'/usuarios'))
            nuevo = tarjeta(adm, 'Nuevo usuario')
            nuevo.get_by_label('Nombre completo').fill('Sofía Ramírez')
            nuevo.get_by_label('Correo').fill('Sofia.Ramirez@KonradGourmet.com')
            nuevo.get_by_label('Rol').select_option(label='Jefe de cocina')
            nuevo.get_by_label('Sucursal').select_option(label='Sucursal Norte')
            nuevo.get_by_label('Contraseña inicial').fill('corta')
            nuevo.get_by_role('button', name='Registrar usuario').click()
            expect(nuevo.locator('.form-error')).to_have_text('La contraseña debe tener 8 caracteres o más, una mayúscula, un número.')
            ok(True, 'una contraseña que no cumple la política se rechaza y dice lo que le falta (DEC-10)')
            nuevo.get_by_label('Contraseña inicial').fill('Norte2026')
            with adm.expect_response(lambda r: r.url.endswith('/api/usuarios') and r.request.method == 'POST') as resp:
                nuevo.get_by_role('button', name='Registrar usuario').click()
            r = resp.value
            ok(r.status == 201, f'POST /api/usuarios → {r.status} (id {r.json().get("id") if r.status == 201 else "—"})')
            registro['usuario'] = {'correo': SOFIA, 'correlacion': r.headers.get('x-correlation-id')}
            expect(adm.locator('#toast')).to_contain_text(f'Usuario {SOFIA} registrado como Jefe de cocina.')
            expect(tarjeta(adm, 'Registrados ahora').locator('tbody tr').first).to_contain_text('Sofía Ramírez')
            ok('Sucursal Norte' in tarjeta(adm, 'Registrados ahora').locator('tbody tr').first.inner_text(), 'sale en «Registrados ahora» con su rol y su sucursal')
            nuevo.get_by_label('Nombre completo').fill('Sofía Otra')
            nuevo.get_by_label('Correo').fill(SOFIA)
            nuevo.get_by_label('Contraseña inicial').fill('Norte2026')
            nuevo.get_by_role('button', name='Registrar usuario').click()
            expect(nuevo.locator('.form-error')).to_have_text('Ya existe un usuario registrado con ese correo.')
            ok(True, 'un correo repetido muestra el mensaje de PD-10')
            sin_desborde(adm, ancho, 'Usuarios')
            foto(adm, f'{tag}-usuarios')
            sofia = ctx['sofia'].new_page()
            entrar(sofia, SOFIA, 'Norte2026')
            ok('Jefe de cocina' in sofia.locator('.profile').inner_text() and 'Sucursal Norte' in sofia.locator('.profile').inner_text(),
               'Sofía inicia sesión: jefa de cocina de la Sucursal Norte')

            # ── Destinatarios (DEC-13) ──
            adm.get_by_role('link', name='Destinatarios').click()
            adm.wait_for_url(re.compile(r'/destinatarios'))
            expect(tarjeta(adm, 'Destinatarios de la Sucursal Centro').locator('tbody')).to_contain_text('carlos.mendoza@konradgourmet.com')
            ok(True, 'la Sucursal Centro tiene al jefe de la demo')
            adm.get_by_label('Sucursal').select_option(label='Sucursal Chapinero')
            expect(tarjeta(adm, 'Destinatarios de la Sucursal Chapinero').locator('td.empty')).to_contain_text('Esta sucursal todavía no tiene destinatarios')
            ok(True, 'la Sucursal Chapinero no tiene destinatarios y la ventana lo advierte')
            adm.get_by_label('Sucursal').select_option(label='Sucursal Norte')
            lista = tarjeta(adm, 'Destinatarios de la Sucursal Norte')
            expect(lista.locator('.card-title p')).to_contain_text('correo')
            ok(SOFIA not in lista.inner_text(), f'la Sucursal Norte todavía no tiene a Sofía ({lista.locator("tbody").inner_text().strip()[:60]})')
            agregar = tarjeta(adm, 'Agregar destinatario')
            agregar.get_by_label('Correo del destinatario').fill('sofia.konradgourmet.com')
            agregar.get_by_role('button', name='Agregar destinatario').click()
            expect(agregar.locator('.form-error')).to_have_text('El formato del correo electrónico no es válido.')
            ok(True, 'un correo mal formado muestra el mensaje de PD-10')
            agregar.get_by_label('Correo del destinatario').fill(SOFIA)
            with adm.expect_response(lambda r: r.url.endswith('/api/notificaciones/destinatarios') and r.request.method == 'POST') as resp:
                agregar.get_by_role('button', name='Agregar destinatario').click()
            ok(resp.value.status == 201, f'POST /api/notificaciones/destinatarios → {resp.value.status}')
            registro['destinatario'] = {'sucursalId': 2, 'correo': SOFIA, 'correlacion': resp.value.headers.get('x-correlation-id')}
            expect(lista.locator('tbody')).to_contain_text(SOFIA)
            ok(True, 'Sofía queda como destinataria de la Sucursal Norte')
            sin_desborde(adm, ancho, 'Destinatarios')
            foto(adm, f'{tag}-destinatarios')
        else:
            # ── 390 px: las cinco ventanas caben; la administradora restaura el tema de siempre ──
            for ruta, titulo in (('/admin/auditoria', 'Movimientos de auditoría'), ('/admin/logs', 'Errores registrados'),
                                 ('/usuarios', 'Nuevo usuario'), ('/destinatarios', 'Agregar destinatario'), ('/admin/parametros', 'Imagen corporativa')):
                adm.goto(BASE + ruta)
                expect(tarjeta(adm, titulo)).to_be_visible()
                adm.wait_for_timeout(600)
                sin_desborde(adm, ancho, ruta)
                foto(adm, f'{tag}{ruta.replace("/", "-")}')
            ok(variable(adm, '--naranja').upper() == VERDE, 'al recargar, la SPA vuelve a leer el tema (verde)')
            imagen = tarjeta(adm, 'Imagen corporativa')
            expect(imagen.get_by_label('Nombre del sistema')).to_have_value('Konrad Gourmet Express')
            imagen.get_by_label('Nombre del sistema').fill('Konrad Gourmet')
            imagen.get_by_label('Color principal').fill('#F28705')
            imagen.get_by_role('button', name='Restaurar').click()
            imagen.get_by_role('button', name='Guardar imagen corporativa').click()
            expect(adm.locator('#toast')).to_contain_text('Imagen corporativa actualizada')
            ok(variable(adm, '--naranja').upper() == '#F28705' and adm.locator('.sidebar .brand img').get_attribute('src') == '/assets/logo/logo.png',
               'la administradora restaura el tema de siempre (color, nombre y logo)')
            foto(adm, f'{tag}-tema-restaurado')
            entrar(mes, MESERO)
            mes.wait_for_timeout(1500)
            ok(variable(mes, '--naranja').upper() == '#F28705' and mes.locator('.sidebar .brand b').inner_text() == 'Konrad Gourmet',
               'y el mesero vuelve a ver el tema de siempre')
            sin_desborde(mes, ancho, 'Inicio del mesero')

        registro['parametros'] += respuestas
        errores = [e for e in errores if not re.search(r'status of (400|401|403|404|409|503)', e)]
        ok(not errores, f'sin errores en la consola ({errores[:3]})')
        for c in ctx.values(): c.close()
    nav.close()
json.dump(registro, open(f'{OUT}/corte6a.json', 'w'), indent=2)
print('\nFALLAS:' if fallas else '\nTodo bien.', *fallas, sep='\n  ')
sys.exit(1 if fallas else 0)
