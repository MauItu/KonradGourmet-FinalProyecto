#!/usr/bin/env python3
# Recorrido del corte 3 en el navegador (Playwright), en escritorio (1440 px) y a 390 px:
#   mesero: V02, V03 (filtro por categoría, $82.000) y V04; jefe: V06 (crear con imagen, editar, desactivar)
#   y el catálogo del mesero cambia por menu.actualizado; V07 (cierre del día) y la alerta en V05;
#   sesión vencida (token con la firma alterada) y que nada se desborde a lo ancho.
# Uso: python recorrido_corte3.py <carpeta-de-capturas>   (necesita playwright y /usr/bin/chromium;
#      BASE=http://localhost:8080 por omisión). Después, verificar_corte3.py revisa la base y Kafka.
import os, re, struct, sys, time, zlib
from playwright.sync_api import sync_playwright, expect

BASE = os.environ.get('BASE', 'http://localhost:8080')
OUT = sys.argv[1]
os.makedirs(OUT, exist_ok=True)
SUF = time.strftime('%H%M%S')
PLATO = f'Sancocho {SUF}'
fallas = []

def png(path, color=(230, 120, 30)):
    w = h = 64
    raw = b''.join(b'\x00' + bytes(color) * w for _ in range(h))
    def chunk(t, d): return struct.pack('>I', len(d)) + t + d + struct.pack('>I', zlib.crc32(t + d) & 0xffffffff)
    open(path, 'wb').write(b'\x89PNG\r\n\x1a\n' + chunk(b'IHDR', struct.pack('>IIBBBBB', w, h, 8, 2, 0, 0, 0)) + chunk(b'IDAT', zlib.compress(raw)) + chunk(b'IEND', b''))

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

def catalogo_mesero(p):
    p.goto(BASE + '/pedidos/nuevo'); p.wait_for_selector('.dish')
    nombres = p.locator('.dish h4').all_inner_texts()
    return dict(zip(nombres, p.locator('.dish .price').all_inner_texts()))

png(f'{OUT}/plato.png')
with sync_playwright() as pw:
    nav = pw.chromium.launch(executable_path='/usr/bin/chromium')
    for ancho, alto, tag in ((1440, 900, 'escritorio'), (390, 844, 'movil')):
        print(f'== {tag} ({ancho} px)')
        ctx = nav.new_context(viewport={'width': ancho, 'height': alto}, locale='es-CO', timezone_id='America/Bogota')
        page = ctx.new_page()
        errores = []
        page.on('console', lambda m: errores.append(m.text) if m.type == 'error' else None)

        # ── Mesero: V02, V03 y V04 ──
        entrar(page, 'isabella.torres@konradgourmet.com')
        page.wait_for_selector('.kpi-cta'); foto(page, f'{tag}-V02-inicio-mesero')
        page.goto(BASE + '/pedidos/nuevo'); page.wait_for_selector('.dish')
        page.get_by_role('button', name='Cancelar').click()
        tarjeta = page.locator('.dish', has=page.locator('h4', has_text='Bandeja paisa'))
        tarjeta.get_by_role('button', name='Más').click()
        tarjeta.get_by_label('Nota para Bandeja paisa').fill('Sin arepa')
        tarjeta.get_by_role('button', name='Agregar').click()
        page.locator('.dish', has=page.locator('h4', has_text='Ajiaco')).get_by_role('button', name='Agregar').click()
        page.select_option('select[aria-label="Mesa"]', '5')
        ok(page.locator('.order-tot .big').inner_text().replace('\xa0', ' ') == '$82.000', 'V03: 2 bandejas paisas y 1 ajiaco suman $82.000')
        page.get_by_role('button', name='Sopas').click(); page.wait_for_timeout(200)
        ok(page.locator('.dish h4').all_inner_texts() == ['Ajiaco'], 'V03: el filtro «Sopas» deja solo el ajiaco (categoría de la carta)')
        page.get_by_role('button', name='Todos').click()
        foto(page, f'{tag}-V03-nuevo-pedido')
        page.get_by_role('button', name='Registrar pedido').click()
        modal = page.locator('[role=dialog]'); expect(modal).to_be_visible()
        numero = modal.locator('.code-pill').inner_text()
        ok(bool(re.fullmatch(r'PED-\d{4}', numero)), f'V04: pedido registrado {numero}')
        foto(page, f'{tag}-V04-pedido-registrado')
        modal.get_by_role('button', name='Ver pedido').click()
        page.wait_for_url(re.compile('/pedidos\\?sel=')); page.wait_for_selector('.detail-head')
        ok(numero in page.locator('.detail-head h3').inner_text(), 'Pedidos del turno: el detalle muestra el pedido recién registrado')
        foto(page, f'{tag}-V02-pedidos-del-turno')

        # ── Jefe de cocina: V06 crea, edita y desactiva; el catálogo del mesero cambia ──
        jctx = nav.new_context(viewport={'width': ancho, 'height': alto}, locale='es-CO', timezone_id='America/Bogota')
        jefe = jctx.new_page(); jefe.on('console', lambda m: errores.append(m.text) if m.type == 'error' else None)
        entrar(jefe, 'carlos.mendoza@konradgourmet.com')
        jefe.wait_for_selector('.kpis'); foto(jefe, f'{tag}-V05-inicio-jefe-antes')
        if tag == 'escritorio':
            jefe.goto(BASE + '/platos'); jefe.wait_for_selector('.plate-detail .detail-head')
            foto(jefe, f'{tag}-V06-platos')
            jefe.get_by_role('button', name='Nuevo plato').click()
            v = jefe.locator('[role=dialog]'); expect(v).to_be_visible()
            ok(jefe.url.endswith('/platos/nuevo'), 'V06: «Nuevo plato» abre /platos/nuevo')
            v.get_by_placeholder('Ej. Bandeja paisa').fill(PLATO)
            v.locator('select').first.select_option('Sopas')
            v.get_by_placeholder('$ 0').fill('28000')
            v.get_by_label('Imagen del plato').set_input_files(f'{OUT}/plato.png')
            v.locator('.frow.four select').nth(0).select_option('Proteína')
            v.locator('.frow.four select').nth(1).select_option(label='Pollo')
            v.get_by_placeholder('0.00').fill('250'); v.locator('.frow.four select').nth(2).select_option('g')
            v.get_by_role('button', name='Agregar ingrediente').click()
            foto(jefe, f'{tag}-V06-nuevo-plato')
            v.get_by_role('button', name='Guardar plato').click()
            expect(jefe.locator('#toast.show')).to_contain_text('guardado')
            jefe.wait_for_selector(f'.plate-detail h3:has-text("{PLATO}")')
            jefe.wait_for_selector('.plate-detail .thumb.hero img')
            jefe.wait_for_function("() => { const i = document.querySelector('.plate-detail .thumb.hero img'); return i && i.complete; }")
            ancho_img = jefe.evaluate("() => document.querySelector('.plate-detail .thumb.hero img').naturalWidth")
            ok(ancho_img == 64, f'V06: la imagen del plato nuevo vuelve del almacén S3 y se ve ({ancho_img} px)')
            foto(jefe, f'{tag}-V06-plato-creado')
            time.sleep(2)
            c = catalogo_mesero(page)
            ok(PLATO in c, 'menu.actualizado: el plato nuevo aparece en el catálogo del mesero')
            jefe.get_by_role('button', name='Editar plato').click()
            v = jefe.locator('[role=dialog]'); v.get_by_placeholder('$ 0').fill('29500')
            v.get_by_role('button', name='Guardar cambios').click()
            expect(jefe.locator('#toast.show')).to_contain_text('quedaron al día')
            time.sleep(2)
            c = catalogo_mesero(page)
            ok(c.get(PLATO, '').replace('\xa0', ' ') == '$29.500', f'menu.actualizado: el mesero ve el precio nuevo ({c.get(PLATO)})')
            jefe.get_by_role('button', name='Desactivar').click()
            expect(jefe.locator('#toast.show')).to_contain_text('desactivado')
            time.sleep(2)
            c = catalogo_mesero(page)
            ok(PLATO not in c, 'menu.actualizado: el plato desactivado sale del catálogo del mesero')
            jefe.wait_for_selector('.plate-detail .badge.gris')
            foto(jefe, f'{tag}-V06-plato-desactivado')
            # V07 y cierre del día
            jefe.goto(BASE + '/inventario'); jefe.wait_for_selector('table tbody tr')
            foto(jefe, f'{tag}-V07-inventario-antes')
            jefe.get_by_role('button', name=re.compile('Cierre del día')).click()
            expect(jefe.locator('#toast.show')).to_contain_text('Cierre ejecutado')
            print('     aviso:', jefe.locator('#toast').inner_text())
            jefe.wait_for_timeout(800); foto(jefe, f'{tag}-V07-inventario-despues')
        else:
            for ruta, nombre in (('/platos', 'V06-platos'), ('/inventario', 'V07-inventario')):
                jefe.goto(BASE + ruta); jefe.wait_for_selector('table tbody tr'); foto(jefe, f'{tag}-{nombre}')
            jefe.goto(BASE + '/platos/nuevo'); jefe.wait_for_selector('[role=dialog]'); foto(jefe, f'{tag}-V06-nuevo-plato')
        jefe.goto(BASE + '/inicio'); jefe.wait_for_selector('.alert-list')
        alertas = jefe.locator('.alert-row b').all_inner_texts()
        ok('Res' in alertas, f'V05: aparece la alerta de stock bajo de la res ({alertas})')
        foto(jefe, f'{tag}-V05-inicio-jefe')
        # sin desbordar a lo ancho
        for p, n in ((page, 'mesero'), (jefe, 'jefe')):
            ancho_doc = p.evaluate('document.documentElement.scrollWidth')
            ok(ancho_doc <= ancho + 1, f'{n}: sin desplazamiento horizontal ({ancho_doc} ≤ {ancho})')

        # ── Sesión vencida: un token que el gateway rechaza vuelve al login con el aviso ──
        page.goto(BASE + '/pedidos'); page.wait_for_selector('table')
        page.evaluate("""() => { const t = localStorage.getItem('kg-token') || sessionStorage.getItem('kg-token');
            const p = t.split('.'); const firma = p[2].slice(0, -4) + (p[2].endsWith('AAAA') ? 'BBBB' : 'AAAA');
            localStorage.setItem('kg-token', [p[0], p[1], firma].join('.')); sessionStorage.removeItem('kg-token'); }""")
        page.reload(); page.wait_for_url(re.compile('/login'))
        ok('venció' in page.locator('body').inner_text(), 'Sesión vencida: el 401 del gateway lleva al login con «Tu sesión venció»')
        foto(page, f'{tag}-V01-sesion-vencida')
        errores = [e for e in errores if '401' not in e and '404' not in e]
        ok(not errores, f'sin errores en la consola ({errores[:3]})')
        ctx.close(); jctx.close()
    nav.close()
print('\nFALLAS:' if fallas else '\nTodo bien.', *fallas, sep='\n  ')
sys.exit(1 if fallas else 0)
