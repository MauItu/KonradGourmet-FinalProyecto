"""Comprueba los datos solicitados en el DOM antes de tomar cada captura principal."""
import json
from pathlib import Path
from datetime import datetime
R=Path(__file__).resolve().parent

def verificar_pantalla(page, slug):
 data=json.loads((R/'datos_correccion.json').read_text())
 icons=json.loads((R/'assets/iconos.json').read_text())
 def comprobar_icono(locator,nombre):
  expected=page.evaluate("s=>{const e=document.createElementNS('http://www.w3.org/2000/svg','svg');e.innerHTML=s;return e.innerHTML}",icons[nombre])
  assert locator.inner_html()==expected,(slug,nombre)
 if slug[0].isdigit():
  comprobar_icono(page.locator('#screen .sidebar nav a').filter(has_text='Destinatarios').locator('svg'),'bell')
 if slug=='03_auditoria':
  comprobar_icono(page.locator('#screen header .title-ico svg'),'clipboard')
 if slug=='03_auditoria':
  rows=page.locator('#screen tbody tr').evaluate_all('(rs)=>rs.map(r=>[...r.cells].map(c=>c.innerText.trim()))')
  assert rows==data['auditoria']
  assert len(rows)==10 and all(all(v and v!='—' for v in row) for row in rows)
  stamps=[datetime.strptime(row[0]+' '+row[1],'%d/%m/%Y %H:%M:%S') for row in rows]
  assert stamps==sorted(stamps,reverse=True)
 elif slug=='04_logs_errores':
  rows=page.locator('#screen tbody tr').evaluate_all('(rs)=>rs.map(r=>[...r.cells].map(c=>c.innerText.trim()))')
  assert [r[:2] for r in rows]==[['17/11/2026',t] for t in ['23:34:30','23:30:36','23:29:04','23:27:34']]
 elif slug in ['V20_opcionadas','V23_ordenes']:
  profile=page.locator('#screen .profile')
  assert profile.locator('strong').inner_text()=='Laura Gómez'
  assert profile.locator('.avatar span').inner_text()=='LG'
  assert profile.locator('.avatar img').count()==0
  assert profile.locator('p').first.inner_text()=='Director de compras'
  detail=page.locator('#screen .detail').inner_text()
  assert all(s in detail for s in ['COT-018','Proveedor ABC','$425.000','$120.000','$545.000'])
  assert '$510.000' not in detail and '$395.000' not in detail and '$115.000' not in detail
  if slug=='V20_opcionadas':
   rows=page.locator('#screen tbody').first.locator('tr').evaluate_all('(rs)=>rs.map(r=>[...r.cells].map(c=>c.innerText.trim()))')
   assert len(rows)==2
   assert [[r[0],r[1],r[2],r[4],r[5]] for r in rows]==data['cotizaciones']
   assert all(r[3]=='$520.000' for r in rows)
   assert '+4,81 %' in detail
  else:
   row=page.locator('#screen tbody tr').first.inner_text()
   assert all(s in row for s in ['OC-0012','22/09/2026','Proveedor ABC','COT-018','$545.000'])
   assert 'Fecha: 22/09/2026' in detail
 elif slug=='01_inicio':
  icons=json.loads((R/'assets/iconos.json').read_text())
  for title,name in [('Auditoría','clipboard'),('Destinatarios','bell')]:
   icon=page.locator('#screen .access').filter(has=page.get_by_text(title,exact=True)).locator('svg')
   expected=page.evaluate("s=>{const e=document.createElementNS('http://www.w3.org/2000/svg','svg');e.innerHTML=s;return e.innerHTML}",icons[name])
   assert icon.inner_html()==expected
 elif slug in ['05_usuarios','06_destinatarios']:
  options=page.locator('#screen #sucursal option').all_text_contents()
  assert sorted(options)==sorted(['Sucursal Centro','Sucursal Norte','Sucursal Chapinero'])
 elif slug=='V05_stock_bajo':
  alerts=page.locator('#screen .alert-row')
  assert alerts.locator('b').all_text_contents()==['Res','Papa','Tomate','Pollo']
  chicken=alerts.filter(has=page.get_by_text('Pollo',exact=True))
  assert chicken.locator('strong').inner_text()=='24,5 %'
  assert alerts.count()==4
  kpi=page.locator('#screen .kpi').filter(has=page.get_by_text('Stock bajo',exact=True))
  assert kpi.locator('b').inner_text()=='4'
 elif slug=='V07_stock_bajo':
  chicken=page.locator('#screen tbody tr').filter(has=page.get_by_text('Pollo',exact=True))
  assert '24,5 % disponible' in chicken.inner_text()
  assert 'Stock bajo' in chicken.inner_text() and 'Normal' not in chicken.inner_text()
  assert '9,8' in chicken.inner_text()
  assert 9.8/40*100==24.5 or abs(9.8/40*100-24.5)<1e-10

  bajo=page.locator('#screen .kpi').filter(has=page.get_by_text('Stock bajo',exact=True)).locator('b').inner_text()
  normal=page.locator('#screen .kpi').filter(has=page.get_by_text('Stock normal',exact=True)).locator('b').inner_text()
  total=page.locator('#screen .kpi').filter(has=page.get_by_text('Productos en inventario',exact=True)).locator('b').inner_text()
  filas_bajas=page.locator('#screen tbody .pill.red').count()
  assert int(bajo)==filas_bajas==5
  assert int(normal)==19 and int(total)==int(bajo)+int(normal)==24
