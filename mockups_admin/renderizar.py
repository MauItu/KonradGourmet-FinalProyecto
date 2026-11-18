from pathlib import Path
import json
from playwright.sync_api import sync_playwright
from PIL import Image
from comprobar_correcciones import verificar_pantalla
R=Path(__file__).resolve().parent
manifest=json.loads((R/'manifest.json').read_text())
results=[]
with sync_playwright() as p:
 browser=p.chromium.launch(executable_path='/usr/bin/chromium',args=['--no-sandbox'])
 page=browser.new_page(device_scale_factor=1,locale="es-CO")
 for m in manifest:
  folder=R/m['carpeta']
  errors=[]
  page.on('pageerror',lambda e:errors.append(str(e)))
  for state in m['estados']:
   for width,height,suffix in [(1440,900,''),(390,844,'_movil')]:
    page.set_viewport_size({'width':width,'height':height})
    page.goto(folder.as_uri()+'/index.html?estado='+state)
    page.wait_for_function("document.querySelector('#screen').children.length>0")
    page.evaluate('document.fonts.ready')
    assert page.evaluate("document.fonts.check('400 13px Poppins') && [...document.querySelectorAll('#screen img')].every(i=>i.complete&&i.naturalWidth>0)"),(m['carpeta'],state,'Recursos incompletos')
    if state=='principal':verificar_pantalla(page,m['carpeta'])
    page.screenshot(path=str(folder/(('captura' if not suffix else 'movil')+'.png' if state=='principal' else state+suffix+'.png')))
    dims=page.evaluate('({w:document.documentElement.scrollWidth,h:document.documentElement.scrollHeight})')
    assert dims['w']<=width,(m['carpeta'],state,width,dims)
    if state=='principal' and width==390 and m['carpeta'][0].isdigit():page.screenshot(path=str(folder/'movil_completa.png'),full_page=True)
    if state=='principal' and width==1440:assert dims['h']==900,(m['carpeta'],dims)
    if state=='principal':results.append({'pantalla':m['carpeta'],'viewport':[width,height],'contenido':dims})
  assert not errors,(m['carpeta'],errors)
  print(m['carpeta'], 'OK',flush=True)
 # Meaningful interactions requested in the changed screens.
 page.set_viewport_size({'width':1440,'height':900})
 page.goto((R/'V23_ordenes/index.html').as_uri())
 page.get_by_role('button',name='Marcar como recibida',exact=True).click()
 assert page.get_by_role('dialog').is_visible()
 page.get_by_role('button',name='Cancelar',exact=True).click()
 assert not page.get_by_role('dialog').is_visible()
 page.get_by_role('button',name='Marcar como recibida',exact=True).click()
 page.get_by_role('button',name='Confirmar recepción',exact=True).click()
 assert page.get_by_text('La orden OC-0012 quedó finalizada.',exact=True).is_visible()
 assert page.get_by_role('button',name='Marcar como recibida',exact=True).count()==0
 page.goto((R/'05_usuarios/index.html').as_uri())
 page.get_by_role('button',name='Mostrar',exact=True).click()
 assert page.locator('#password').get_attribute('type')=='text'
 page.goto((R/'03_auditoria/index.html').as_uri())
 assert page.locator('#screen tbody tr').count()==10
 page.locator('#desde').fill('2026-11-18');page.locator('#hasta').fill('2026-11-10')
 page.get_by_role('button',name='Consultar',exact=True).click()
 assert page.get_by_text('La fecha inicial no puede ser posterior a la fecha final.',exact=True).is_visible()
 page.set_viewport_size({'width':390,'height':844})
 page.goto((R/'01_inicio/index.html').as_uri())
 page.get_by_role('button',name='Abrir menú',exact=True).click()
 assert page.locator('#screen .sidebar').is_visible()
 page.screenshot(path=str(R/'01_inicio/menu_movil.png'))
 browser.close()
for m in manifest:
 for name,size in [('captura.png',(1440,900)),('movil.png',(390,844))]:
  assert Image.open(R/m['carpeta']/name).size==size
(R/'verificacion.json').write_text(json.dumps(results,ensure_ascii=False,indent=2))
print('Dimensiones, estados, JavaScript e interacciones verificados.')
