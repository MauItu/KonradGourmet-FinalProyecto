from pathlib import Path
import json
from PIL import Image
R=Path(__file__).resolve().parent
manifest=json.loads((R/'manifest.json').read_text())
assert len(manifest)==11
count=0
for m in manifest:
 folder=R/m['carpeta']
 assert (folder/'index.html').is_file()
 for state in m['estados']:
  for mobile,size in [(False,(1440,900)),(True,(390,844))]:
   file=('movil' if mobile else 'captura')+'.png' if state=='principal' else state+('_movil' if mobile else '')+'.png'
   im=Image.open(folder/file)
   assert im.size==size,(file,im.size)
   count+=1
for v in json.loads((R/'verificacion.json').read_text()):
 assert v['contenido']['w']==v['viewport'][0]
 if v['viewport'][0]==1440:assert v['contenido']['h']==900
required=['Imagen corporativa actualizada: la verán todos los usuarios al iniciar sesión.','Parámetro actualizado.','Seleccione la fecha inicial y la fecha final.','La fecha inicial no puede ser posterior a la fecha final.','No hay movimientos de auditoría en el periodo seleccionado.','No hay errores registrados para el servicio seleccionado.','No se pudieron consultar los logs de errores. Intente de nuevo en unos minutos.','Usuario sofia.ramirez@konradgourmet.com registrado como Jefe de cocina.','Esta sucursal todavía no tiene destinatarios: nadie recibiría sus alertas.','La orden OC-0012 quedó finalizada.','No hay cotizaciones opcionadas disponibles','No se pudo enviar el correo al proveedor. La cotización COT-018 está guardada; puede reintentar el envío.']
html='\n'.join((R/m['carpeta']/'index.html').read_text() for m in manifest)
for t in required:assert t in html,t
assert '#F26B21' in (R/'estilo.css').read_text()
print(f'OK: 11 ventanas, {count} capturas de estados, dimensiones y textos solicitados.')
files=sorted(p for p in R.rglob('*') if p.is_file() and p.name!='ARCHIVOS.md')
(R/'ARCHIVOS.md').write_text('# Archivos creados\n\nTodas las rutas son relativas a `mockups_admin/`.\n\n'+'\n'.join(f'- [{p.relative_to(R)}]({p.relative_to(R).as_posix()})' for p in files)+'\n- [ARCHIVOS.md](ARCHIVOS.md)\n')
