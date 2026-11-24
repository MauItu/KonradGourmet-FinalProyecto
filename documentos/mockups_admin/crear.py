"""Mockups locales: HTML/CSS → Chromium. No importa ni modifica la aplicación."""
from pathlib import Path
from shutil import copyfile
from html import escape
from PIL import Image
import json
R=Path(__file__).resolve().parent
ICONOS=json.loads((R/'assets/iconos.json').read_text())
def ico(k='file'):
 k={'doc':'file','exit':'logout','mail':'send'}.get(k,k)
 return f'<svg class="ico" viewBox="0 0 24 24" aria-hidden="true">{ICONOS[k]}</svg>'
def marca(cls='brand'):
 return f'<div class="{cls}"><img src="../assets/logo-original.png" alt="Logo original Konrad Gourmet"><div><b>Konrad <span>Gourmet</span></b><small>Sistema de Abastecimiento</small></div></div>'
SCREENS=[('01_inicio','Inicio','Administrador · Sucursal Centro','home'),('02_parametros_tema','Parámetros y tema','Imagen corporativa y parámetros generales del sistema','settings'),('03_auditoria','Auditoría','Quién hizo cada acción en el sistema y cuándo','clipboard'),('04_logs_errores','Logs de errores','Errores registrados por los servicios del sistema','alert'),('05_usuarios','Usuarios','Alta de usuarios con su rol y su sucursal','users'),('06_destinatarios','Destinatarios','Correos de los jefes de cocina que reciben las alertas de stock bajo','bell')]
MANIFEST=[]
def button(t,action='',primary=False,extra=''):
 icon={'Consultar':'search','Hoy':'calendar','Actualizar':'refresh','Subir logo':'upload','Restaurar':'refresh','Guardar imagen corporativa':'check','Crear parámetro':'plus','Guardar cambios':'check','Registrar usuario':'plusCircle','Agregar destinatario':'plus','Ver PDF':'file','Descargar':'download','Marcar como recibida':'box','Confirmar recepción':'check','Seleccionar para compra':'cart','Ver cotización':'file','Reintentar envío':'send','Solicitar':'cart'}.get(t)
 t=(ico(icon) if icon else '')+f'<span>{t}</span>'
 return f'<button class="{"primary" if primary else ""}" onclick="{action}" {extra}>{t}</button>'
def notice(t,kind='success'):return f'<div class="notice {kind}" role="status">{ico("alert" if kind=="danger" else "circleCheck" if kind=="success" else "info")}<span>{t}</span></div>'
def field(t,v='',kind='text',id='',extra=''):return f'<label class="field">{t}<input type="{kind}" value="{v}" id="{id}" {extra}></label>'
def select(t,opts,id=''):return f'<label>{t}<select id="{id}">'+''.join(f'<option>{x}</option>' for x in opts)+'</select></label>'
def card(title,body,subtitle='',icon='doc',action='',cls=''):
 icon={'Tus ventanas':'home','Imagen corporativa':'image','Parámetros generales':'settings','Movimientos de auditoría':'clipboard','Errores registrados':'alert','Nuevo usuario':'user','Registrados ahora':'users','Agregar destinatario':'plusCircle','Destinatarios de la Sucursal Centro':'bell','Comparación de cotizaciones':'scale','Detalle de cotización opcionada':'file','Detalle de orden de compra':'file','Filtros de órdenes':'filter','Consultar por servicio':'filter','Consultar sucursal':'store'}.get(title,icon)
 return f'<section class="card {cls}"><div class="section-title"><span class="symbol">{ico(icon)}</span><div><h2>{title}</h2>'+ (f'<p>{subtitle}</p>' if subtitle else '')+f'</div>{action}</div>{body}</section>'
def table(headers,rows,cls=''):return f'<div class="table-wrap"><table class="{cls}"><thead><tr>'+''.join(f'<th>{h}</th>' for h in headers)+'</tr></thead><tbody>'+''.join('<tr>'+''.join(f'<td>{c}</td>' for c in row)+'</tr>' for row in rows)+'</tbody></table></div>'
def pager(label,many=False):return f'<div class="pagination"><span>{label}</span><div class="pages"><button aria-label="Página anterior">‹</button><button class="primary">1</button>'+('<button>2</button><span>…</span><button>16</button>' if many else '')+'<button aria-label="Página siguiente">›</button></div></div>'
def empty(t):return f'<div class="empty">{t}</div>'
def director_avatar():
 return '<div class="avatar initials" aria-label="Laura Gómez, iniciales LG"><span>LG</span><i class="online"></i></div>'
def sidebar(active,admin=True,chef=False):
 name,role=('Paula Herrera','Administrador') if admin else ('Carlos Mendoza','Jefe de cocina') if chef else ('Laura Gómez','Director de compras') if active in ['Cotizaciones opcionadas','Órdenes de compra'] else ('Isabella Castro','Director de compras')
 avatar='<div class="avatar initials" aria-label="Avatar PH"><span>PH</span><i class="online"></i></div>' if admin else '<div class="avatar"><img src="../assets/carlos.png" alt="Carlos Mendoza"><i class="online"></i></div>' if chef else director_avatar() if active in ['Cotizaciones opcionadas','Órdenes de compra'] else '<div class="avatar"><img src="../assets/isabella.png" alt="Isabella Castro"><i class="online"></i></div>'
 s=f'<aside class="sidebar" aria-label="Navegación principal">{marca()}<div class="profile">{avatar}<strong>{name}</strong><p>{role}</p><p>Sucursal Centro</p></div><nav>'
 for i,(slug,title,sub,icon) in enumerate(SCREENS if admin else [('inicio','Inicio','','home'),('platos','Platos','','utensils'),('inventario','Inventario','','box'),('solicitudes','Solicitudes','','clipboard')] if chef else [('inicio','Inicio','','home'),('cotizaciones','Cotizaciones','','doc'),('ordenes','Órdenes de compra','','doc')]):
  if admin and i==1:s+=f'<div class="navgroup">{ico("settings")}Administración {ico("chevD")}</div>'
  cl=('sub ' if admin and 1<=i<=3 else '')+('active' if title==active else '')
  s+=f'<a class="{cl}" href="../{slug}/index.html" '+('aria-current="page"' if title==active else '')+'>'+('<i class="nav-dot"></i>' if admin and 1<=i<=3 else ico(icon))+title+'</a>'
 return s+'</nav><div class="account-links"><a href="#" onclick="return false">'+ico('user')+'Mi perfil</a><a href="#" onclick="return false">'+ico('exit')+'Cerrar sesión</a></div></aside>'
def shell(title,sub,body,admin=True,chef=False):
 heading='Buenos días, <span>Carlos Mendoza</span>' if chef else 'Buenos días, <span>Paula</span>' if title=='Inicio' else title
 titleicon='' if title=='Inicio' or chef else '<span class="title-ico">'+ico(next((x[3] for x in SCREENS if x[1]==title),'file'))+'</span>'
 return f'<div class="app">{sidebar(title,admin,chef)}<main class="main"><button class="menu-toggle" onclick="document.querySelector(\'.sidebar\').classList.toggle(\'visible\')" aria-label="Abrir menú">{marca("mobile-brand")}<span>{ico("chevR")}</span></button><header><div class="header-title">{titleicon}<div><h1>{heading}</h1><p>{sub}</p></div></div><div class="chips"><span class="chip">{ico("calendar")} Miércoles, 18 de noviembre de 2026</span><span class="chip">{ico("clock")} 10:24 a. m.</span></div></header>{body}</main></div>'
JS='''<script>
const estado=new URLSearchParams(location.search).get('estado')||'principal';
function go(s){location.search='?estado='+s}
function closeModal(){document.querySelectorAll('.shade').forEach(e=>e.classList.remove('open'))}
document.addEventListener('keydown',e=>{if(e.key==='Escape'){closeModal();document.querySelector('.sidebar')?.classList.remove('visible')}});
function password(b){const i=document.querySelector('#password');i.type=i.type==='password'?'text':'password';b.textContent=i.type==='password'?'Mostrar':'Ocultar'}
function audit(){let a=document.querySelector('#desde').value,b=document.querySelector('#hasta').value;go(!a||!b?'fechas_obligatorias':a>b?'fechas_invertidas':'principal')}
function register(){let e=document.querySelector('#email').value,p=document.querySelector('#password').value;if(!/^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$/.test(e))go('correo_invalido');else if(p.length<8||!/[A-Z]/.test(p)||!/[0-9]/.test(p))go('contrasena_invalida');else go(e==='sofia.ramirez@konradgourmet.com'?'correo_duplicado':'registrado')}
function corporate(){const c=document.querySelector('#colorhex').value;const f=document.querySelector('#logo').files[0];if(!/^#[0-9a-f]{6}$/i.test(c))go('color_invalido');else if(f&&(!['image/png','image/jpeg','image/svg+xml','image/webp'].includes(f.type)||f.size>204800))go('logo_invalido');else go('imagen_guardada')}
function parameter(){let k=document.querySelector('#clave').value,v=document.querySelector('#valor').value;go(!k.trim()?'clave_obligatoria':!v.trim()?'valor_obligatorio':!/^[a-z0-9.-]+$/.test(k)?'clave_invalida':'parametro_guardado')}
function recipient(){let e=document.querySelector('#destcorreo').value;go(/^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$/.test(e)?'principal':'correo_invalido')}
</script>'''
def write(slug,title,sub,variants,admin=True,custom=False):
 folder=R/slug;folder.mkdir(parents=True,exist_ok=True)
 # Each state is a complete static section; query parameter selects the reviewable variant.
 parts=[]
 for state,body in variants.items():
  content=body if custom else shell(title,sub,body,admin)
  parts.append(f'<template id="state-{state}">{content}</template>')
 html='<!doctype html><html lang="es"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1"><title>Konrad Gourmet · '+title+'</title><link rel="stylesheet" href="../estilo.css"></head><body class="page-'+slug+'"><div id="screen"></div>'+''.join(parts)+JS+'''<script>document.querySelector('#screen').innerHTML=(document.getElementById('state-'+estado)||document.getElementById('state-principal')).innerHTML;
if(document.body.classList.contains('page-04_logs_errores'))document.querySelector('#servicio').value=estado==='principal'?'ms-notificaciones':'Todos';
if(document.body.classList.contains('page-06_destinatarios'))document.querySelector('#sucursal').addEventListener('change',e=>{let t=document.querySelectorAll('#screen .card h2')[1];t.textContent='Destinatarios de la Sucursal '+e.target.value;document.querySelectorAll('#screen .card')[1].querySelector('.table-wrap,.empty').outerHTML='<div class="empty">Esta sucursal todavía no tiene destinatarios: nadie recibiría sus alertas.</div>'});
if(document.querySelector('#ordenar'))document.querySelector('#ordenar').addEventListener('change',e=>{const tb=document.querySelector('#screen tbody');const col={'Diferencia':5,'Valor total':4,'Número':0}[e.target.value];const val=r=>parseFloat(r.cells[col].textContent.replace(/[^0-9,−-]/g,'').replace('−','-').replace(',','.'))||0;[...tb.rows].sort((a,b)=>val(a)-val(b)).forEach(r=>tb.appendChild(r))});
</script></body></html>'''
 if slug=='06_destinatarios':html=html.replace("t.textContent='Destinatarios de la Sucursal '+e.target.value","t.textContent='Destinatarios de la '+e.target.value")
 (folder/'index.html').write_text(html,encoding='utf-8')
 MANIFEST.append({'carpeta':slug,'titulo':title,'estados':list(variants)})
# 01
links=''
desc=['Personaliza la imagen corporativa y los parámetros del sistema.','Consulta quién hizo cada acción y cuándo.','Revisa los errores registrados por los servicios.','Registra usuarios y asigna su rol y sucursal.','Administra los correos que reciben las alertas de stock bajo.']
for (slug,title,_,icon),d in zip(SCREENS[1:],desc):links+=f'<a class="access" href="../{slug}/index.html"><span class="symbol">{ico({"Auditoría":"clipboard","Destinatarios":"bell"}.get(title,icon))}</span><div><b>{title}</b><p>{d}</p></div><span class="arrow">›</span></a>'
write(*SCREENS[0][:3],{'principal':card('Tus ventanas','<div class="accesses">'+links+'</div>','Accede a las herramientas de administración.','home')})
# 02
corp_errors={'color_invalido':'El color debe tener el formato #RRGGBB (por ejemplo, #F28705).','logo_invalido':'El logo debe ser una imagen PNG, JPG, SVG o WebP de máximo 200 KB.'}
param_errors={'clave_obligatoria':'La clave del parámetro es obligatoria.','valor_obligatorio':'El valor del parámetro general.moneda es obligatorio.','clave_invalida':'La clave del parámetro solo puede tener minúsculas, números, guiones y puntos (por ejemplo, general.moneda).'}
variants={}
for state in ['principal','editar','imagen_guardada','parametro_guardado',*corp_errors,*param_errors]:
 corp='<div class="preview"><img class="preview-logo" src="../assets/logo-original.png" alt="Logo original"><b>Konrad Gourmet</b><p>Sistema de Abastecimiento</p><button style="background:#F28705;border-color:#F28705;color:white">Botón principal</button></div>'+field('Nombre del sistema *','Konrad Gourmet',id='nombre')+'<label class="field">Color principal *<div class="color-row"><input aria-label="Selector de color" type="color" value="#F28705" oninput="document.querySelector(\'#colorhex\').value=this.value"><input id="colorhex" value="#F28705" aria-label="Color principal hexadecimal"></div></label><label>Logo (PNG, JPG, SVG o WebP, máx. 200 KB)<input id="logo" type="file" accept="image/png,image/jpeg,image/svg+xml,image/webp" hidden><div class="upload">'+button('Subir logo',"document.querySelector('#logo').click()")+button('Restaurar',"document.querySelector('#logo').value='';document.querySelector('#colorhex').value='#F28705'")+'</div></label>'
 if state in corp_errors:corp+=notice(corp_errors[state],'danger')
 corp+=button('Guardar imagen corporativa','corporate()',True,'style="margin-top:22px"')
 if state=='imagen_guardada':corp+=notice('Imagen corporativa actualizada: la verán todos los usuarios al iniciar sesión.')
 rows=[[k,v,'<button class="text-button" onclick="go(\'editar\')">Editar &gt;</button>'] for k,v in [('general.correo-soporte','soporte@konradgourmet.com'),('general.moneda','COP'),('general.propina-sugerida','10 %'),('general.zona-horaria','America/Bogota')]]
 params=table(['Clave','Valor','Acción'],rows,'mini-table')+pager('1–4 de 4 parámetros')+'<div class="editform"><div class="fields">'+field('Clave *','' if state in ['principal','clave_obligatoria'] else 'General Moneda' if state=='clave_invalida' else 'general.moneda',id='clave')+field('Valor *','' if state in ['principal','valor_obligatorio'] else 'COP',id='valor')+'</div>'
 if state in param_errors:params+=notice(param_errors[state],'danger')
 params+=button('Guardar cambios' if state=='editar' else 'Crear parámetro','parameter()',True)+'</div>'
 if state=='parametro_guardado':params+=notice('Parámetro actualizado.')
 variants[state]='<div class="grid">'+card('Imagen corporativa',corp,icon='settings')+card('Parámetros generales',params,'4 parámetros','doc','<button class="primary right" onclick="document.querySelector(\'#clave\').value=\'\';document.querySelector(\'#valor\').value=\'\';document.querySelector(\'#clave\').focus()">Nuevo parámetro</button>')+'</div>'
write(*SCREENS[1][:3],variants)
# Datos ilustrativos proporcionados por el usuario; sin consulta al backend.
audit_rows=json.loads((R/'datos_correccion.json').read_text())['auditoria']
variants={}
for state in ['principal','fechas_obligatorias','fechas_invertidas','vacio']:
 filters='<div class="toolbar">'+field('Desde *','' if state=='fechas_obligatorias' else '2026-11-18' if state=='fechas_invertidas' else '2026-11-10','date','desde')+field('Hasta *','' if state=='fechas_obligatorias' else '2026-11-17','date','hasta')+button('Consultar','audit()',True)+button('Hoy',"document.querySelector('#desde').value='2026-11-18';document.querySelector('#hasta').value='2026-11-18'")+'</div>'
 if state in ['fechas_obligatorias','fechas_invertidas']:filters+=notice('Seleccione la fecha inicial y la fecha final.' if state=='fechas_obligatorias' else 'La fecha inicial no puede ser posterior a la fecha final.','danger')
 body=empty('No hay movimientos de auditoría en el periodo seleccionado.') if state=='vacio' else table(['Fecha','Hora','Usuario','Acción'],audit_rows,'audit-table')+pager(f'Mostrando 1–{len(audit_rows)} de 160 movimientos',True)
 variants[state]=card('Periodo de consulta',filters,icon='calendar')+card('Movimientos de auditoría',body,'160 movimientos del 10/11/2026 al 17/11/2026' if state!='vacio' else '0 movimientos en el periodo seleccionado','clock')
write(*SCREENS[2][:3],variants)
# 04
services=['Todos']+['ms-'+s for s in ['identidad','pedidos','menu','inventario','solicitudes','cotizaciones','ordenes','notificaciones','integracion','analitica','administracion']]
logmsg='No se pudo notificar un evento de cotizacion.enviada después de 3 intentos; pasa a la cola de errores notificaciones.dlq · java.net.SocketTimeoutException: Read timed out'
logrows=[[d,t,'<span class="pill red">ms-notificaciones</span>',logmsg] for d,t in [('17/11/2026','23:34:30'),('17/11/2026','23:30:36'),('17/11/2026','23:29:04'),('17/11/2026','23:27:34')]]
variants={}
for state in ['principal','vacio','error']:
 filters='<div class="toolbar">'+select('Servicio',services,'servicio')+button('Actualizar',"document.querySelector('#log-status').textContent='Consulta actualizada · '+document.querySelector('#servicio').value",True)+'</div><p id="log-status" class="subtle" role="status"></p>'
 content=table(['Fecha','Hora','Servicio','Mensaje'],logrows,'log-table')+pager('Mostrando 1–4 de 4 errores') if state=='principal' else empty('No hay errores registrados para el servicio seleccionado.') if state=='vacio' else notice('No se pudieron consultar los logs de errores. Intente de nuevo en unos minutos.','danger')
 variants[state]=card('Consultar por servicio',filters,icon='settings')+card('Errores registrados',content,'4 errores · ms-notificaciones · los más recientes primero' if state=='principal' else 'Consulta de errores','alert')
write(*SCREENS[3][:3],variants)
# 05
user_errors={'correo_duplicado':'Ya existe un usuario registrado con ese correo.','contrasena_invalida':'La contraseña debe tener 8 caracteres o más, una mayúscula, un número.','correo_invalido':'El formato del correo electrónico no es válido.'}
variants={}
for state in ['principal','registrado',*user_errors]:
 form='<div class="fields">'+field('Nombre completo *','Sofía Ramírez',id='fullname')+field('Correo *','sofia.ramirez@konradgourmet' if state=='correo_invalido' else 'sofia.ramirez@konradgourmet.com','email','email')+select('Rol *',['Jefe de cocina','Mesero','Auxiliar de compras','Director de compras','Director comercial','Administrador'],'rol')+select('Sucursal *',['Sucursal Norte','Sucursal Centro','Sucursal Chapinero'],'sucursal')+'<label class="full">Contraseña inicial *<div class="password"><input id="password" type="password" value="Sofia2026"><button onclick="password(this)">Mostrar</button></div><span class="help">Al menos 8 caracteres, con una mayúscula, una minúscula y un número.</span></label></div>'
 if state in user_errors:form+=notice(user_errors[state],'danger')
 form+=button('Registrar usuario','register()',True,'style="margin-top:24px"')
 recent=table(['Nombre y correo','Rol','Sucursal'],[['Sofía Ramírez<small>sofia.ramirez@konradgourmet.com</small>','<span class="pill">Jefe de cocina</span>','Sucursal Norte']])
 if state=='registrado':recent=notice('Usuario sofia.ramirez@konradgourmet.com registrado como Jefe de cocina.')+recent
 variants[state]='<div class="grid">'+card('Nuevo usuario',form,icon='users')+card('Registrados ahora',recent,'Usuarios registrados en esta sesión','users')+'</div>'
write(*SCREENS[4][:3],variants)
# 06
variants={}
for state in ['principal','vacio','correo_invalido']:
 listing=empty('Esta sucursal todavía no tiene destinatarios: nadie recibiría sus alertas.') if state=='vacio' else table(['Correo','Recibe'],[['carlos.mendoza@konradgourmet.com','<span class="pill">Alarma y correo de stock bajo</span>']])
 form=field('Correo del jefe de cocina *','','email','destcorreo',extra='placeholder="nombre@konradgourmet.com"')
 if state=='correo_invalido':form+=notice('El formato del correo electrónico no es válido.','danger')
 form+=button('Agregar destinatario','recipient()',True)
 variants[state]=card('Consultar sucursal','<div class="toolbar">'+select('Sucursal',['Sucursal Centro','Sucursal Norte','Sucursal Chapinero'],'sucursal')+'</div>',icon='settings')+card('Destinatarios de la Sucursal Centro',listing,icon='mail')+card('Agregar destinatario',form,icon='users')
write(*SCREENS[5][:3],variants)
# V05/V07 use the source portraits and food crops, with native HTML and project icons.
refs=R/'referencias'
source=Image.open(refs/'ref_V05_jefe_inicio_alertas.png')
source.crop((83,273,158,349)).save(R/'assets/carlos.png')
for name,box in [('res',(226,470,267,507)),('arroz',(226,517,267,553)),('papa',(226,561,267,598)),('tomate',(226,609,267,645)),('pollo',(226,654,267,692))]:source.crop(box).save(R/'assets'/(name+'.png'))
inventory_source=Image.open(refs/'ref_V07_inventario.png')
for name,box in [('lechuga',(216,602,253,628)),('leche',(216,631,253,659)),('queso mozzarella',(216,660,253,689))]:inventory_source.crop(box).save(R/'assets'/(name+'.png'))
def food(name):return f'<img class="food-thumb" src="../assets/{name}.png" alt="{name.capitalize()}">'
def stock_toast():return '<aside class="toast" role="status"><span class="toast-icon">'+ico('bell')+'</span><div><b>Alarma de stock bajo</b><p>Pollo al 24,5 %: realice una solicitud</p><div class="bottom"><time>10:24 a. m.</time>'+button('Solicitar',"document.querySelector('#stock-modal').classList.add('open')",True)+'</div></div></aside>'
stock_modal='<div id="stock-modal" class="shade"><section class="modal" role="dialog" aria-modal="true"><h2>Nueva solicitud de alimentos</h2><p>Pollo · Sucursal Centro</p><footer>'+button('Cerrar','closeModal()')+'</footer></section></div>'
def kpis(items):
 return '<div class="kpis">'+''.join(f'<div class="kpi {cls}"><span>{ico(icon)}</span><div><b>{num}</b><small>{label}</small></div></div>' for num,label,icon,cls in items)+'</div>'
alerts=''
for name,pct in [('res','23'),('papa','18'),('tomate','24'),('pollo','24,5')]:alerts+=f'<div class="alert-row">{food(name)}<b>{name.capitalize()}</b><strong>{pct} %</strong><span class="meter"><i style="width:{pct.replace(",", ".")}%"></i></span><button class="round-button" aria-label="Ver {name}">{ico("chevR")}</button></div>'

recent=table(['Nº solicitud','Fecha','Productos','Estado','Acción'],[[n,d,c,'<span class="pill '+cl+'">'+st+'</span>',ico('chevR')] for n,d,c,st,cl in [('SOL-031','22/09/2025','4','Pendiente','orange'),('SOL-028','20/09/2025','3','En trámite','blue'),('SOL-027','18/09/2025','5','En trámite','blue'),('SOL-026','17/09/2025','4','Completada','green'),('SOL-025','12/09/2025','2','Completada','green')]])
body=kpis([('18','Platos registrados','bowl',''),('4','Stock bajo','alert','danger'),('3','Solicitudes pendientes','file',''),('2','Solicitudes en trámite','refresh','')])+'<div class="grid">'+card('Alertas de inventario',alerts,icon='bell')+card('Solicitudes recientes',recent,icon='file')+'</div>'
write('V05_stock_bajo','V05 · Alarma de stock bajo','',{'principal':shell('Inicio','Jefe de cocina · Sucursal Centro',body,False,True)+stock_toast()+stock_modal},custom=True)
inventory=[]
for name,cat,stock,unit,minim,status,expire in [('res','Carnes','11,5','kg','12,5','Stock bajo','28/09/2025'),('arroz','Granos','25','kg','20','Normal','15/12/2025'),('papa','Tubérculos','8','kg','15','Próximo a vencer','26/09/2025'),('tomate','Verduras','12','kg','15','Stock bajo','30/09/2025'),('pollo','Carnes','9,8','kg','10','Stock bajo','02/10/2025'),('lechuga','Verduras','6','unidades','10','Stock bajo','23/09/2025'),('leche','Lácteos','18','litros','20','Normal','05/10/2025'),('queso mozzarella','Lácteos','5','kg','10','Stock bajo','27/09/2025')]:
 thumb=food(name) if (R/'assets'/(name+'.png')).exists() else '<span class="food-placeholder">'+ico('box')+'</span>'
 inventory.append([thumb,name.capitalize(),cat,stock+('<small>24,5 % disponible</small>' if name=='pollo' else ''),unit,minim,'<span class="pill '+('red' if status=='Stock bajo' else 'orange' if status=='Próximo a vencer' else 'green')+'">'+status+'</span>',expire,button('Solicitar')])
body=kpis([('24','Productos en inventario','box',''),('5','Stock bajo','alert','danger'),('19','Stock normal','circleCheck','ok'),('3','Próximos a vencer','clipboard','')])+card('Inventario de insumos','<div class="inventory-tools"><div class="search">'+ico('search')+'<input placeholder="Buscar ingrediente, categoría o código…" aria-label="Buscar ingrediente"></div>'+select('',['Todas las categorías'])+button('Nuevo insumo','',True)+'</div>'+table(['Imagen','Nombre del insumo','Categoría','Stock actual','Unidad','Stock mínimo','Estado','Vencimiento','Acción'],inventory),icon='box')
write('V07_stock_bajo','V07 · Alarma de stock bajo','',{'principal':shell('Inventario','Jefe de cocina · Sucursal Centro',body,False,True)+stock_toast()+stock_modal},custom=True)
# V20
comparison=[[n,d,p,'$520.000',t,'<span class="pill red">'+diff+'</span>','<span class="pill">OPCIONADA</span>','<button class="text-button">Ver ›</button>'] for n,d,p,t,diff in json.loads((R/'datos_correccion.json').read_text())['cotizaciones']]
producttable=table(['Producto','Cantidad','Valor'],[['Res','20 kg','$425.000'],['Arroz','15 kg','$120.000']])
variants={}
for state in ['principal','vacio']:
 filters='<div class="toolbar">'+select('Solicitud',['SOL-031'])+select('Ordenar por',['Diferencia','Valor total','Número'],'ordenar')+'</div>'
 detail='<dl><dt>Cotización</dt><dd>COT-018</dd><dt>Proveedor</dt><dd>Proveedor ABC</dd><dt>Solicitud</dt><dd>SOL-031</dd><dt>Estado</dt><dd><span class="pill">OPCIONADA</span></dd></dl><h3>Comparación económica</h3><dl><dt>Ministerio</dt><dd>$520.000</dd><dt>Proveedor</dt><dd>$545.000</dd><dt>Diferencia</dt><dd style="color:var(--rojo)">+4,81 %</dd></dl><h3>Productos</h3>'+producttable+button('Seleccionar para compra','',True,'style="width:100%;margin-top:15px"')+button('Ver cotización','','', 'style="width:100%;margin-top:10px"')
 listing=empty('No hay cotizaciones opcionadas disponibles') if state=='vacio' else table(['Nº','Fecha','Proveedor','Valor estándar','Valor total','Diferencia','Estado','Ver'],comparison)+pager('Mostrando 1–2 de 2 cotizaciones')
 variants[state]='<div class="grid" style="grid-template-columns:1fr 330px"><div>'+card('Cotizaciones opcionadas',filters,'Compara y selecciona la mejor opción.')+card('Comparación de cotizaciones',listing)+'</div>'+ (card('Detalle de cotización opcionada',detail,cls='detail') if state!='vacio' else '')+'</div>'
write('V20_opcionadas','Cotizaciones opcionadas','Compara las cotizaciones y selecciona la mejor opción.',variants,False)
# V23
variants={}
for state in ['principal','confirmar','finalizada']:
 final=state=='finalizada';pill='<span class="pill '+('green' if final else 'orange')+'">'+('FINALIZADA' if final else 'EN PROCESO')+'</span>'
 rows=[['OC-0012','22/09/2026','Proveedor ABC','COT-018','$545.000',pill,'<button class="text-button">Ver ›</button>'],['OC-0011','20/09/2026','Proveedor A','COT-015','$680.000','<span class="pill green">FINALIZADA</span>','Ver ›'],['OC-0010','18/09/2026','Proveedor C','COT-013','$595.000','<span class="pill orange">EN PROCESO</span>','Ver ›'],['OC-0009','15/09/2026','Proveedor D','COT-010','$430.000','<span class="pill green">FINALIZADA</span>','Ver ›']]
 listing=table(['Orden','Fecha','Proveedor','Cotización','Total','Estado','Ver'],rows)+pager('Mostrando 1–4 de 4 órdenes')
 detail=f'<div class="order-number"><div>ORDEN DE COMPRA<b>OC-0012</b><small>Fecha: 22/09/2026</small></div>{pill}</div><dl><dt>Proveedor</dt><dd>Proveedor ABC</dd><dt>Solicitud</dt><dd>SOL-031</dd><dt>Cotización</dt><dd>COT-018</dd></dl>'
 if not final:detail+=notice('La orden está en proceso. Continúe con el seguimiento al proveedor y la verificación de entrega.','')
 detail+='<h3>Productos</h3>'+producttable+'<div class="total"><strong>TOTAL:</strong><b>$545.000</b></div>'+button('Ver PDF','',True,'style="width:100%;margin-top:14px"')+button('Descargar','','', 'style="width:100%;margin-top:8px"')
 if not final:detail+=button('Marcar como recibida',"document.querySelector('#recepcion').classList.add('open')",True,'style="width:100%;margin-top:12px"')
 content=card('Filtros de órdenes','<div class="toolbar">'+select('Fecha',['Todas las fechas'])+select('Proveedor',['Todos los proveedores','Proveedor ABC'])+select('Estado',['Todos los estados','EN PROCESO','FINALIZADA'])+'</div>')
 if final:content+=notice('La orden OC-0012 quedó finalizada.')
 content='<div class="grid" style="grid-template-columns:1fr 350px"><div>'+content+card('Listado de órdenes de compra',listing)+'</div>'+card('Detalle de orden de compra',detail,cls='detail')+'</div>'
 content+='<div class="shade '+('open' if state=='confirmar' else '')+'" id="recepcion"><section class="modal" role="dialog" aria-modal="true" aria-labelledby="recepcion-title"><h2 id="recepcion-title">Marcar como recibida</h2><p>¿El pedido de la orden OC-0012 a Proveedor ABC llegó completo? La orden pasará a FINALIZADA y no se podrá deshacer.</p><footer>'+button('Cancelar','closeModal()')+button('Confirmar recepción',"go('finalizada')",True)+'</footer></section></div>'
 variants[state]=content
write('V23_ordenes','Órdenes de compra','Consulta y revisa las órdenes de compra generadas.',variants,False)
producttable=table(['Producto','Cantidad','Valor'],[['Res','20 kg','$395.000'],['Arroz','15 kg','$115.000']])
# V14: HTML receipt over the native quotes shell, keeping reference contents.
quote_rows=[[n,p,'22/09/2026','30/09/2026','<span class="pill orange">ENVIADA</span>','Ver detalle ›'] for n,p in [('COT-018','Proveedor ABC'),('COT-017','Proveedor XYZ'),('COT-016','Alimentos del Sur'),('COT-015','Distribuidora Lima'),('COT-014','Proveedor Andino')]]
quote_bg=card('Consultar cotizaciones','<div class="toolbar">'+select('Proveedor',['Todos'])+select('Fecha',['Todas'])+select('Estado',['Todos'])+'</div>',icon='filter')+'<div class="grid" style="grid-template-columns:1fr 330px">'+card('Cotizaciones enviadas',table(['Cotización','Proveedor','Fecha envío','Fecha límite','Estado','Acción'],quote_rows)+pager('Mostrando 1–5 de 5 cotizaciones'),icon='file')+card('Detalle de cotización','<dl><dt>Cotización</dt><dd>COT-018</dd><dt>Solicitud origen</dt><dd>SOL-031</dd><dt>Proveedor</dt><dd>Proveedor ABC</dd></dl><h3>Productos</h3>'+producttable+button('Ver PDF','',True)+button('Descargar'),cls='detail')+'</div>'
quote_shell=shell('Cotizaciones','Consulta y revisa las cotizaciones enviadas a proveedores.',quote_bg,False).replace('Director de compras','Auxiliar de compras')
variants={}
for state in ['principal','esperando','no_enviado']:
 status='Correo enviado ✓' if state=='principal' else 'Correo enviado' if state=='esperando' else 'Correo no enviado'
 ticket='<div class="check">'+ico('check')+'</div><h2>Solicitud de cotización<br>guardada</h2><h3>COT-018</h3><span class="pill '+('orange' if state=='principal' else 'gray' if state=='esperando' else 'red')+'">'+('ENVIADA' if state=='principal' else 'Correo pendiente' if state=='esperando' else 'Correo no enviado')+'</span><p class="subtle">'+('El PDF fue generado y enviado al proveedor.' if state=='principal' else 'El PDF fue generado y la cotización está guardada.')+'</p><dl><dt>Solicitud origen:</dt><dd>SOL-031</dd><dt>Proveedor:</dt><dd>Proveedor ABC</dd><dt>Fecha de envío:</dt><dd>22/09/2026</dd><dt>Fecha límite:</dt><dd>30/09/2026</dd></dl><hr style="border:0;border-top:1px dashed #e4d7c3"><b>Productos solicitados</b><dl><dt>Res</dt><dd>20 kg</dd><dt>Arroz</dt><dd>15 kg</dd></dl><div class="statusline"><span>PDF generado ✓</span><span>'+status+'</span></div>'
 if state=='principal':ticket=ticket.replace('Solicitud de cotización<br>guardada','Solicitud de cotización<br>enviada')
 if state=='no_enviado':ticket+=notice('No se pudo enviar el correo al proveedor. La cotización COT-018 está guardada; puede reintentar el envío.','danger')+button('Reintentar envío',"go('esperando')",True)
 ticket+='<small>KONRAD GOURMET</small>'
 variants[state]=quote_shell+'<div class="shade open"><section class="ticket" role="dialog" aria-modal="true" aria-label="Estado de cotización">'+ticket+'</section></div>'
write('V14_correo','V14 · Estado del correo','',variants,custom=True)
(R/'manifest.json').write_text(json.dumps(MANIFEST,ensure_ascii=False,indent=2))
# Reviewer gallery: state controls belong here, outside product screens.
items=''
for m in MANIFEST:
 items+=f'<article><h2>{m["titulo"]}</h2><a href="{m["carpeta"]}/index.html"><img src="{m["carpeta"]}/captura.png" alt="Vista previa de {m["titulo"]}"></a><p>'
 for state in m['estados']:items+=f'<a href="{m["carpeta"]}/index.html?estado={state}">{state.replace("_"," ")}</a> · '
 items+='</p><a href="'+m['carpeta']+'/movil.png">Captura móvil</a></article>'
(R/'index.html').write_text('<!doctype html><html lang="es"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Revisión de mockups</title><link rel="stylesheet" href="estilo.css"><style>body{font:14px Poppins,Arial;color:#2E2A26;background:#FAF7F3;max-width:1200px;margin:auto;padding:30px}h1{color:#F26B21}.grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(330px,1fr));gap:24px}article{padding:20px;background:white;border-radius:18px}img{width:100%}a{color:#ad470f;line-height:2}p{font-size:13px}</style><h1>Konrad Gourmet · Revisión de mockups</h1><p>6 ventanas de Administrador y 5 modificaciones. Selecciona una vista o uno de sus estados ilustrativos. Sin conexión al backend.</p><div class="grid">'+items+'</div></html>')
print(f'{len(MANIFEST)} pantallas; {sum(len(x["estados"]) for x in MANIFEST)} estados HTML.')
