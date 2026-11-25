# Mockups del Administrador · Konrad Gourmet

Abre [index.html](index.html) para revisar las 6 ventanas nuevas, las 5 modificaciones y todos los estados. Todo está contenido en esta carpeta; los originales permanecen intactos.

Método: HTML y CSS local, generación con Python, capturas con Playwright y `/usr/bin/chromium`, como `mockups_nuevos/07_proveedores`. `estilo.css` conserva el método HTML/CSS del ejemplo, pero ahora reproduce los componentes y la identidad de la interfaz real de `KonradGourmet/web/`: fondo crema, bordes cálidos, tarjetas, marca, avatar con indicador, navegación, encabezado, botones e iconos. Primario `#F26B21`; `#F28705` se utiliza solo en la vista previa y los campos corporativos expresamente solicitados.

## Ventanas nuevas

Cada subcarpeta contiene `index.html`, `captura.png` de **1440×900**, `movil.png` de **390×844** y `movil_completa.png` para revisar el contenido que requiere desplazamiento vertical. Las tablas tienen desplazamiento horizontal; el menú móvil se abre con el botón «Abrir menú».

| Carpeta | Ventana | Estados adicionales (PNG de escritorio y móvil) |
| --- | --- | --- |
| [01_inicio](01_inicio/index.html) | Inicio | `menu_movil.png` muestra la navegación compartida |
| [02_parametros_tema](02_parametros_tema/index.html) | Parámetros y tema | `editar`, `imagen_guardada`, `parametro_guardado`, `color_invalido`, `logo_invalido`, `clave_obligatoria`, `valor_obligatorio`, `clave_invalida` |
| [03_auditoria](03_auditoria/index.html) | Auditoría | `fechas_obligatorias`, `fechas_invertidas`, `vacio` |
| [04_logs_errores](04_logs_errores/index.html) | Logs de errores | `vacio`, `error` |
| [05_usuarios](05_usuarios/index.html) | Usuarios | `registrado`, `correo_duplicado`, `contrasena_invalida`, `correo_invalido` |
| [06_destinatarios](06_destinatarios/index.html) | Destinatarios | `vacio`, `correo_invalido` |

Los estados se seleccionan desde la galería o añadiendo `?estado=nombre` al HTML de cada pantalla. Por ejemplo, [error de color](02_parametros_tema/index.html?estado=color_invalido). La captura de un estado se llama `nombre.png`, y su variante móvil `nombre_movil.png`.

## Cambios a ventanas existentes

| Carpeta | Cambio | Imágenes clave |
| --- | --- | --- |
| [V23_ordenes](V23_ordenes/index.html) | PDF bajo el total, Descargar y recepción solo en proceso; confirmación, aviso y estado final | [captura.png](V23_ordenes/captura.png), [confirmar.png](V23_ordenes/confirmar.png), [finalizada.png](V23_ordenes/finalizada.png) |
| [V14_correo](V14_correo/index.html) | Correo enviado con ✓, esperando sin marca y fallo con mensaje y reintento | [captura.png](V14_correo/captura.png), [esperando.png](V14_correo/esperando.png), [no_enviado.png](V14_correo/no_enviado.png) |
| [V20_opcionadas](V20_opcionadas/index.html) | Nº y Fecha, Ordenar por y vacío | [captura.png](V20_opcionadas/captura.png), [vacio.png](V20_opcionadas/vacio.png) |
| [V05_stock_bajo](V05_stock_bajo/index.html) | Alarma arriba a la derecha, Pollo al 24,5 %, Solicitar y hora | [captura.png](V05_stock_bajo/captura.png) |
| [V07_stock_bajo](V07_stock_bajo/index.html) | La misma alarma sobre el inventario original | [captura.png](V07_stock_bajo/captura.png) |

Las cinco carpetas también incluyen `movil.png` de 390×844 y variantes móviles de sus estados. V05 y V07 se reconstruyeron en HTML con los datos y fotografías de las referencias, y comparten los mismos componentes adaptables. V14 también tiene un fondo de cotizaciones en HTML; ya no depende de una captura ampliada. El botón Solicitar muestra una indicación local: el formulario completo de solicitud está fuera de este cambio.

## Referencias y datos que faltan

- Se leyeron `mockups_nuevos/LEEME.md`, `estilo.css`, `plantilla_base.html`, `crear.py`, `renderizar.py` y la pantalla 07.
- Se inspeccionaron las 31 páginas de `documentos/DISEÑO UXUI.pdf`. V15 está en la página física 21; V14 en la 20; V20 en la 27; V23 en la 30; V05 en la 10 y V07 en la 12. V14 y V23 sí existen en el PDF, aunque no tenían una captura individual entre las referencias.
- **Tipografía:** se encontró Poppins declarada en `KonradGourmet/web/index.html` y `src/estilos/app.css`. Ahora se incorpora localmente en los pesos 400, 500, 600 y 700. Esto confirma la tipografía de la interfaz implementada; no se afirma que se haya extraído la fuente del PDF.
- **Paleta:** fondo `#FAF7F3`, texto `#2E2A26`, texto secundario `#5F5953`, durazno `#FFF4E8`, bordes `#F0E8E0` y estados de `app.css`. Se conserva el primario solicitado `#F26B21`; el formulario de tema conserva el ejemplo `#F28705`.
- **Logo:** copia exacta de `KonradGourmet/web/public/assets/logo/logo.png`, con el nombre y subtítulo en HTML, como hace el componente `Barra.tsx`. No se ha generado ni redibujado una marca.
- **Iconos:** los 44 SVG se copiaron de `src/componentes/Icono.tsx` a `assets/iconos.json`; se utiliza el mismo trazo de 1,8 px. Los controles reutilizan los iconos existentes. Las fotografías de Carlos e insumos se recortaron de las referencias del proyecto.
- **Fuentes locales:** archivos oficiales de Google Fonts (`google/fonts`, directorio `ofl/poppins`), con la licencia OFL incluida. Las pantallas funcionan sin pedir fuentes externas.
- **Comparación visual:** [antes y después](comparacion/index.html). Se conservan dos capturas anteriores para revisar el cambio de identidad.
- **Datos ilustrativos:** el usuario proporcionó explícitamente los diez movimientos del 17/11/2026 y las cuatro horas de los logs. Se reproducen como datos de mockup, sin presentarlos como registros obtenidos del sistema. [datos_correccion.json](datos_correccion.json) conserva los datos y su procedencia.
- **Auditoría:** diez filas completas, sin guiones, ordenadas de 23:31:08 a 23:25:10. El resumen de 160 movimientos es el texto del diseño; no se han consultado 160 registros.
- **Logs:** 17/11/2026 a las 23:34:30, 23:30:36, 23:29:04 y 23:27:34. Se conserva el mensaje de ejemplo de ms-notificaciones.
- **V20:** COT-018 (22/09/2026, Proveedor ABC, $545.000, +4,81 %) y COT-021 (23/09/2026, Proveedor B, $560.000, +7,69 %), con estándar $520.000. El detalle muestra COT-018 y sus productos. La última aclaración del usuario pide COT-021, y prevalece sobre COT-022 del mensaje previo.
- **V23:** OC-0012 de COT-018, Proveedor ABC, fecha 22/09/2026; Res 20 kg por $425.000 y Arroz 15 kg por $120.000, total $545.000. Los otros tres ejemplos históricos de la tabla se conservan.
- **Directora:** Laura Gómez, etiqueta «Director de compras» y avatar de iniciales «LG», con el mismo componente de «PH». No se usa una foto ni se cambia V14.
- **Iconos:** Auditoría usa `clipboard` tanto en Inicio como en el título de su ventana; Destinatarios usa `bell` tanto en Inicio como en la barra lateral de todas las ventanas del Administrador.
- **Sucursales:** Usuarios y Destinatarios muestran «Sucursal Centro», «Sucursal Norte» y «Sucursal Chapinero», también al cambiar el selector.
- **V05/V07:** Pollo al 24,5 % aparece tanto en la alarma como en las filas. V05 muestra Res, Papa, Tomate y Pollo, con contador 4; se elimina Arroz de las alertas. Pollo al 25 % no aparece. En V07 Pollo está en «Stock bajo»: 9,8 kg sobre referencia 40 kg, con mínimo 10 kg (25 %); la referencia de 40 kg procede de la semilla del inventario. V07 muestra 5 de stock bajo, coincidiendo con las cinco filas etiquetadas (Res, Tomate, Pollo, Lechuga y Queso), y 19 de stock normal: el total se conserva en 24. Ninguna regla ni dato del backend se ha modificado.
- Las ventanas de Administrador son nuevas; no se presentan como pantallas ya existentes en el PDF. Los textos, personas y valores son los ejemplos de la tarea, sin inferir datos reales ni endpoints.
- No se consultó un backend ni se simulan envíos reales, persistencia, generación/descarga de PDFs o los otros 150 movimientos. «Ver PDF», «Descargar», la paginación y las acciones fuera del alcance son controles visuales. La recepción, las validaciones, Mostrar/Ocultar, el menú móvil y el selector de orden tienen comportamiento local de demostración.

## Reproducir y verificar

Desde la carpeta que contiene `documentos/` y `mockups_nuevos/`, con el entorno de `mockups_nuevos`:

```sh
mockups_nuevos/.venv/bin/python documentos/mockups_admin/crear.py
mockups_nuevos/.venv/bin/python documentos/mockups_admin/renderizar.py
mockups_nuevos/.venv/bin/python documentos/mockups_admin/verificar.py
```

Dependencias: Python, Pillow, Playwright y Chromium. `referencias/` tiene las dos capturas de V05 y V07 de las que `crear.py` recorta las fotografías. No se regeneran los mockups anteriores. `renderizar.py` comprueba errores JavaScript, carga de Poppins y de las imágenes, ausencia de desbordamiento horizontal, los tamaños de escritorio, el menú móvil, las diez filas exactas de auditoría, las cuatro horas de logs, Laura con avatar LG, los importes de V20/V23, las sucursales, los iconos del inicio y la coherencia de la alarma mediante `comprobar_correcciones.py`, Mostrar contraseña y la recepción de V23 (cancelar y confirmar; botón ausente en FINALIZADA). `verificacion.json` registra las dimensiones de cada vista principal.

[ARCHIVOS.md](ARCHIVOS.md) contiene el inventario completo de archivos entregados. No se modificó `KonradGourmet/`, ningún `.drawio`, el PDF ni `mockups_nuevos/`.
