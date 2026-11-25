"""Genera guias_sustentacion/guia_plataforma.html: qué hace la plataforma, sus 33 ventanas y la demo en vivo.

Las imágenes y los números de la historia (SOL, COT, OC, la alarma, los correos) salen de img/plataforma/, que deja
imagenes_plataforma.py al ensayar la demo en la instancia kg-demo (DEC-111). Uso, desde FinalProyecto:
    python3 documentos/guias_sustentacion/fuente/generar_guia_plataforma.py
"""
import json
import re
import sys
from pathlib import Path

AQUI = Path(__file__).resolve().parent
sys.path.insert(0, str(AQUI))
from estilo import E, pagina, visor   # noqa: E402

GUIAS = AQUI.parent
IMG = GUIAS / "img" / "plataforma"
D = json.loads((IMG / "datos.json").read_text(encoding="utf-8"))
H = D["historia"]
COT1, COT2 = H["cotizaciones"]
SOL, OC, PED = H["solicitud"], H["orden"], H["pedido"]


def pesos(n):
    return "$" + f"{n:,.0f}".replace(",", ".")


def c(t):
    return f"<code>{E(t)}</code>"


def et(*ts):
    return "".join(f'<span class="etiqueta">{t}</span>' for t in ts)


def tabla(cabeza, filas, clases=None):
    th = "".join(f'<th{f" class={clases[i]}" if clases and clases[i] else ""}>{E(x)}</th>' for i, x in enumerate(cabeza))
    cuerpo = "".join("<tr>" + "".join(f"<td>{x}</td>" for x in f) + "</tr>" for f in filas)
    return f"<table><thead><tr>{th}</tr></thead><tbody>{cuerpo}</tbody></table>"


def imagen(nombre, alt, pie=""):
    i = D["imagenes"][nombre]
    return (f'<figure class="vent"><a href="img/plataforma/{nombre}.png" target="_blank" rel="noopener">'
            f'<img src="img/plataforma/{nombre}.png" alt="{E(alt)}" width="{i["ancho"]}" height="{i["alto"]}" loading="lazy"></a>'
            f'{f"<figcaption>{pie}</figcaption>" if pie else ""}</figure>')


def con_pines(nombre, alt, textos):
    """Visor con los círculos de la imagen y, debajo, qué hacer o señalar en cada uno."""
    i = D["imagenes"][nombre]
    pines = i["pines"]
    assert len(pines) == len(textos), f"{nombre}: {len(pines)} círculos y {len(textos)} textos"
    vid = "v-" + nombre
    filas = "".join(
        f'<tr id="{vid}-n{n}"><td class="num"><button class="pin" data-visor="{vid}" data-n="{n}">{n}</button></td>'
        f"<td>{t}</td></tr>" for (n, _, _), t in zip(pines, textos))
    tabla_pines = (f'<table class="pines"><thead><tr><th class="c-num">N.º</th><th>Qué hacer o señalar</th></tr></thead>'
                   f"<tbody>{filas}</tbody></table>") if pines else ""
    return visor(vid, f"img/plataforma/{nombre}.png", i["ancho"], i["alto"],
                 [(n, x, y) for n, x, y in pines], i["diametro"], alt) + tabla_pines


CSS_LOCAL = """<style>
.ventanas{display:grid;grid-template-columns:repeat(auto-fill,minmax(330px,1fr));gap:16px;margin:12px 0}
.ventana{border:1px solid var(--borde);border-radius:10px;padding:10px 12px;background:var(--papel)}
.ventana h4{margin:0 0 6px;font-size:1rem}
.ventana dl{margin:8px 0 0;font-size:.9rem;display:grid;grid-template-columns:max-content 1fr;gap:2px 10px}
.ventana dt{color:var(--suave)}
.ventana dd{margin:0;overflow-wrap:anywhere}
figure.vent{margin:0}
figure.vent img{display:block;width:100%;height:auto;border:1px solid var(--borde);border-radius:6px}
figure.vent figcaption{font-size:.82rem;color:var(--suave);margin-top:4px}
.par{display:grid;grid-template-columns:1fr 1fr;gap:14px;margin:12px 0}
.movil{display:grid;grid-template-columns:repeat(3,minmax(0,220px));gap:14px;margin:12px 0}
@media (max-width:700px){.ventanas{grid-template-columns:1fr}.par{grid-template-columns:1fr}.movil{grid-template-columns:repeat(2,minmax(0,1fr))}}
table.pines{margin:8px 0 14px}
.paso-demo{border-top:3px solid var(--acento);margin-top:30px;padding-top:6px}
.debe{background:var(--codigo);border-radius:8px;padding:8px 12px;margin:10px 0}
.lista-control li{margin:4px 0}
pre{white-space:pre-wrap;overflow-wrap:anywhere}
</style>"""


# ─────────────────────────── las 33 ventanas ───────────────────────────
# (código, título, imagen, qué hace, servicio, rutas, eventos, arquitectura y patrones)
VENTANAS = {
    "Todos los roles": [
        ("V01", "Inicio de sesión", "v01",
         "Correo y clave, con un botón por rol que llena los datos de demostración. Al entrar, la SPA recibe un JWT con el menú del rol y aplica la imagen corporativa.",
         "ms-identidad", "POST /api/auth/token", "auditoria.evento (inicio de sesión)",
         et("Microservicios", "OAuth 2.0 / JWT", "Factory Method", "Composite")),
    ],
    "Mesero (Isabella Torres)": [
        ("V02", "Inicio del mesero", "v02", "Resumen del turno: los pedidos que registró hoy y un acceso a «Nuevo pedido».",
         "ms-pedidos", "GET /api/pedidos?turno=actual", "—", et("Microservicios", "REST")),
        ("V03", "Nuevo pedido", "d02_V03_pedido",
         "Catálogo de platos con su foto si la tiene (si no, su ícono) y precio, cantidades, mesa y total. El catálogo es una copia local que ms-pedidos arma con los eventos de ms-menu.",
         "ms-pedidos (la foto, de ms-menu)", "GET /api/pedidos/catalogo, GET /api/pedidos/mesas, POST /api/pedidos",
         "consume menu.actualizado", et("Microservicios", "Eventos", "Hexagonal")),
        ("V04", "Pedido registrado", "d03_V04_registrado",
         f"Confirmación con el número del pedido ({PED}). La respuesta es inmediata; el descuento de inventario y los indicadores llegan después por evento.",
         "ms-pedidos", "201 Created", "publica pedido.registrado → ms-inventario y ms-analitica", et("Eventos")),
    ],
    "Jefe de cocina (Carlos Mendoza)": [
        ("V05", "Inicio del jefe", "v05",
         "Alertas de inventario, la alarma de stock bajo en vivo (arriba a la derecha, con «Solicitar») y las solicitudes recientes.",
         "ms-inventario y ms-notificaciones", "GET /api/inventario/alertas; WebSocket /api/notificaciones/alarmas",
         "inventario.stock-bajo → ms-notificaciones → Redis → WebSocket", et("Eventos", "Factory Method", "PD-01: Redis pub/sub")),
        ("V06", "Nuevo plato", "v06",
         "Nombre, categoría, precio de venta, imagen y receta (producto, cantidad y unidad). La imagen va al almacén de objetos.",
         "ms-menu", "POST /api/platos, PUT /api/platos/{id}/imagen", "publica menu.actualizado → ms-pedidos, ms-inventario y ms-analitica",
         et("Hexagonal", "Eventos")),
        ("V07", "Inventario", "v07",
         "Existencias de la sucursal frente al stock de referencia y el umbral del 25 %. «Cierre del día (23:59)» adelanta la tarea programada que descuenta el consumo y genera las alertas.",
         "ms-inventario", "GET /api/inventario/insumos, POST /api/inventario/cierre", "consume pedido.registrado y menu.actualizado; publica inventario.stock-bajo",
         et("Eventos", "Tarea programada con ShedLock")),
        ("V08", "Nueva solicitud de alimentos", "d06_V08_solicitud",
         "Categoría, producto, cantidad, unidad y marca. Desde la alarma o una alerta, «Solicitar» la abre con el producto ya escogido.",
         "ms-solicitudes", "POST /api/solicitudes; GET /api/platos/productos?categoria=…", "—", et("Hexagonal")),
        ("V09", "Solicitud registrada", "d07_V09_registrada", f"Confirmación con el número ({SOL}) en estado PENDIENTE.",
         "ms-solicitudes", "201 Created", "publica solicitud.registrada → ms-notificaciones, ms-analitica y ms-inventario (alerta atendida)",
         et("Eventos")),
        ("V10", "Mis solicitudes", "v10",
         "Las solicitudes del jefe con su estado. PENDIENTE pasa a EN TRÁMITE y luego a COMPLETADA sin que el jefe haga nada: lo cambian los eventos.",
         "ms-solicitudes", "GET /api/solicitudes?solicitante=yo", "consume cotizacion.enviada y orden.generada; publica solicitud.en-tramite",
         et("Eventos")),
    ],
    "Auxiliar de compras (Isabella Castro)": [
        ("V11", "Inicio del auxiliar", "v11", "Solicitudes pendientes de todas las sucursales y accesos a cotizaciones y proveedores.",
         "ms-solicitudes", "GET /api/solicitudes?estado=PENDIENTE", "—", et("Microservicios")),
        ("V12", "Solicitudes, con «Generar cotización»", "v12",
         "Filtros de HU-07 (fechas, producto, marca y estado) y el detalle de la solicitud con quién la pidió y el botón para cotizarla.",
         "ms-solicitudes", "GET /api/solicitudes, GET /api/solicitudes/{id}", "—", et("Microservicios", "REST")),
        ("V13", "Generar solicitud de cotización", "d10_V13_generar",
         "Una o varias solicitudes, los productos a cotizar, el proveedor y la fecha límite. Un clic pide el precio del Ministerio, guarda el PDF y lo envía por correo.",
         "ms-cotizaciones (precio de ms-integracion)", "POST /api/cotizaciones; GET /api/proveedores; GET /interno/precios/{productoId}",
         "publica cotizacion.enviada → ms-notificaciones y ms-solicitudes", et("Facade", "Adapter", "Proxy", "SOA (SOAP)")),
        ("V14", "Cotización enviada", "d11_V14_enviada",
         "Número, proveedor y productos, con «PDF generado» y «Correo enviado» (consulta la traza de ms-notificaciones) y «Ver PDF».",
         "ms-cotizaciones y ms-notificaciones", "GET /api/cotizaciones/{id}/pdf; GET /api/notificaciones/envios?correlacion=…", "—",
         et("Decorator (correo certificado)", "Almacén de objetos")),
        ("V15", "Cotizaciones enviadas", "v15", "Las cotizaciones del auxiliar con su estado y su detalle.",
         "ms-cotizaciones", "GET /api/cotizaciones", "consume orden.generada (CON ORDEN)", et("Microservicios")),
    ],
    "Directora de compras (Laura Gómez)": [
        ("V16", "Inicio de la directora", "v16", "Cotizaciones pendientes de atención y accesos a validar, opcionadas y órdenes.",
         "ms-cotizaciones", "GET /api/cotizaciones/pendientes", "—", et("Microservicios")),
        ("V17", "Todas las cotizaciones", "v17", "Todas las cotizaciones, con filtros y el detalle de cada una (estado y diferencia).",
         "ms-cotizaciones", "GET /api/cotizaciones", "—", et("Microservicios")),
        ("V18", "Registrar valores de cotización", "d14_V18_valores",
         "El precio por unidad que respondió el proveedor junto al estándar del Ministerio; el total y la diferencia se recalculan al escribir.",
         "ms-cotizaciones", "PUT /api/cotizaciones/{id}/valores", "—", et("Hexagonal")),
        ("V19", "Validación completada", "d15_V19_opcionada",
         "El estado que calculó la cadena de reglas: más de 25 % por encima, RECHAZADA; más de 50 % por debajo, SOSPECHOSA; el resto, OPCIONADA.",
         "ms-cotizaciones", "POST /api/cotizaciones/{id}/validacion", "publica cotizacion.validada → ms-analitica",
         et("Chain of Responsibility", "Hexagonal")),
        ("V20", "Cotizaciones opcionadas", "v20", "Compara las opcionadas de una solicitud, la de menor diferencia primero, y permite seleccionar una para compra.",
         "ms-cotizaciones", "GET /api/cotizaciones?estado=OPCIONADA, GET /api/cotizaciones/{id}", "—", et("Microservicios")),
        ("V21", "Generar orden de compra", "d18_V21_generar", "Datos de la cotización, productos y total, con «Confirmar orden de compra».",
         "ms-ordenes (lee la cotización de ms-cotizaciones)", "POST /api/ordenes-compra (y GET /api/cotizaciones/{id}, síncrono)",
         "publica orden.generada", et("Microservicios", "REST síncrono")),
        ("V22", "Orden generada", "d19_V22_orden", f"Confirmación con el número de la orden ({OC}) y su total.",
         "ms-ordenes", "201 Created", "orden.generada → ms-solicitudes, ms-cotizaciones, ms-notificaciones y ms-analitica", et("Eventos")),
        ("V23", "Órdenes de compra", "v23", "Seguimiento de las órdenes: «Ver PDF», «Descargar» y «Marcar como recibida» (de EN PROCESO a FINALIZADA).",
         "ms-ordenes", "GET /api/ordenes-compra, GET …/{id}/pdf, PATCH …/{id}/finalizar", "—", et("Microservicios", "Almacén de objetos")),
    ],
}
OTRAS = [
    ("Jefe de cocina", "Platos: editar, desactivar y activar", "platos",
     "La lista de platos de la sucursal (con los desactivados) y el detalle con «Editar plato» y «Desactivar» o «Activar». Un plato desactivado sale del catálogo del mesero.",
     "ms-menu", "GET /api/platos, PUT /api/platos/{id}, PATCH …/{id}/desactivar y …/{id}/reactivar", "publica menu.actualizado",
     et("Hexagonal", "Eventos")),
    ("Jefe de cocina", "Promociones sugeridas", "promociones",
     "Sugerencias del análisis semanal que cruza las tendencias de las redes con los platos más pedidos; el jefe las acepta o las descarta.",
     "ms-analitica", "GET /api/bam/promociones, PATCH …/{id}/aceptar y …/{id}/descartar", "TendenciasJob publica tendencia.detectada → correo a los jefes",
     et("Eventos", "Tarea programada")),
    ("Auxiliar de compras", "Proveedores", "proveedores",
     "Lista de proveedores y formulario para registrar, editar o inactivar uno (NIT único, correo válido).",
     "ms-cotizaciones", "GET, POST y PUT /api/proveedores; PATCH /api/proveedores/{id}/inactivar", "—", et("Hexagonal")),
    ("Auxiliar de compras", "Reenviar una cotización pendiente de precio", "reenviar",
     "Si el Ministerio no responde, la cotización se guarda PENDIENTE DE PRECIO sin enviarse; cuando vuelve, «Reenviar» la completa y la envía.",
     "ms-cotizaciones", "POST /api/cotizaciones/{id}/reenvio", "publica cotizacion.enviada al reenviarla", et("Resiliencia", "SOA")),
    ("Director comercial", "Tablero BAM", "bam",
     "Producto y sucursal con mayor demanda y proveedor con mejores precios, con su ranking; filtros de fechas y sucursal.",
     "ms-analitica", "GET /api/bam/kpis?desde=…&hasta=…&sucursalId=…",
     "consume pedido.registrado, cotizacion.validada, inventario.stock-bajo, solicitud.registrada, orden.generada y menu.actualizado",
     et("Strategy", "Eventos", "Almacén de hechos propio")),
    ("Administrador", "Parámetros y tema", "parametros",
     "Imagen corporativa (nombre, color y logo) y parámetros generales; la SPA aplica el tema al iniciar sesión, sin redesplegar.",
     "ms-administracion", "GET y PUT /api/admin/parametros; GET /api/admin/parametros/tema", "—", et("Singleton")),
    ("Administrador", "Auditoría", "auditoria", "Quién hizo cada acción y cuándo, en un rango de fechas, lo más reciente primero.",
     "ms-administracion", "GET /api/admin/auditoria?desde=…&hasta=…", "consume auditoria.evento de los otros diez servicios",
     et("Eventos", "Outbox")),
    ("Administrador", "Logs de errores", "logs", "Los errores que registran los servicios, filtrables por servicio.",
     "ms-administracion", "GET /api/admin/logs?servicio=…", "— (los lee de Elasticsearch)", et("ELK")),
    ("Administrador", "Usuarios", "usuarios", "Alta de usuarios con su rol y sucursal (correo único y regla de contraseña).",
     "ms-identidad", "POST /api/usuarios", "auditoria.evento", et("Microservicios")),
    ("Administrador", "Destinatarios de alertas", "destinatarios", "Los correos que reciben las alertas de stock bajo de cada sucursal.",
     "ms-notificaciones", "GET y POST /api/notificaciones/destinatarios", "—", et("Microservicios")),
]


def tarjeta_ventana(codigo, titulo, img, que, servicio, rutas, eventos, arq):
    cab = f"{codigo} · {titulo}" if codigo else titulo
    return (f'<div class="ventana"><h4>{E(cab)}</h4>{imagen(img, cab)}'
            f"<p>{que}</p><dl><dt>Servicio</dt><dd>{E(servicio)}</dd><dt>Rutas</dt><dd>{c(rutas)}</dd>"
            f"<dt>Eventos</dt><dd>{E(eventos)}</dd><dt>Se ve</dt><dd>{arq}</dd></dl></div>")


def seccion_ventanas():
    partes = ["""<p>La SPA tiene <b>33 ventanas</b>: las 23 del prototipo (V01 a V23) y otras diez para funciones que el
prototipo no dibujaba. Cada rol ve solo las suyas: el menú lateral llega dentro del JWT, armado por ms-identidad. Las
imágenes son de la aplicación con los datos de la demo; al tocarlas se abren en tamaño real. Las de V03, V04, V08,
V09, V13, V14, V18, V19, V21 y V22 son las del recorrido de la demo (V04, V09, V14, V18, V19, V21 y V22 se abren
después de una acción).</p>"""]
    for rol, lista in VENTANAS.items():
        partes.append(f"<h3>{E(rol)}</h3><div class=\"ventanas\">" + "".join(tarjeta_ventana(*v) for v in lista) + "</div>")
    partes.append("<h3>Las otras diez ventanas</h3><div class=\"ventanas\">" + "".join(
        tarjeta_ventana("", f"{t} ({r.lower()})", *resto) for r, t, *resto in OTRAS) + "</div>")
    partes.append(f"""<h3>Páginas de apoyo que no se cuentan entre las 33</h3>
<p>«Inicio» del administrador (accesos a sus cinco ventanas), «Pedidos del turno» del mesero (la lista completa del
turno, {c('/pedidos')}) y «Mi perfil» (los datos del usuario, en todos los roles). Una dirección que no está en el menú
del rol lleva a su inicio.</p>
<h3>Responsive: las mismas ventanas a 390 px</h3>
<p>Todas las ventanas caben en un teléfono sin desplazarse a lo ancho; el menú lateral pasa a un botón. Si preguntan, se
muestra con las herramientas del navegador (modo de dispositivo) o reduciendo la ventana.</p>
<div class="movil">{imagen('m_v03', 'V03 a 390 px', 'V03 · Nuevo pedido')}{imagen('m_v07', 'V07 a 390 px', 'V07 · Inventario')}{imagen('m_bam', 'Tablero BAM a 390 px', 'Tablero BAM')}</div>""")
    return "".join(partes)


# ─────────────────────────── la demo ───────────────────────────
def paso(n, titulo, quien, duracion, cuerpo, arquitectura, guion):
    return (f'<div class="paso-demo" id="paso{n}"><h3>Paso {n} · {E(titulo)}</h3>'
            f'<p class="meta"><span>👤 {quien}</span><span>⏱ {duracion}</span></p>{cuerpo}'
            f'<p><b>Qué arquitectura se ve:</b> {arquitectura}</p>'
            f'<div class="guion"><h3>🎤 Qué decir</h3>{guion}</div></div>')


def correos_textos():
    """Un texto por cada círculo de la bandeja de Mailpit (los correos van del más reciente al más antiguo)."""
    motivo = [
        (r"^Orden de compra", "orden.generada: la orden con su PDF, al proveedor"),
        (r"^Solicitud de cotización", "cotizacion.enviada: la cotización con su PDF, al proveedor"),
        (r"en trámite$", "solicitud.en-tramite: aviso al jefe de cocina"),
        (r"registrada$", "solicitud.registrada: confirmación al jefe de cocina"),
        (r"^Stock bajo", "inventario.stock-bajo: la alerta al jefe de cocina"),
        (r"^Promociones", "tendencia.detectada: las promociones de la semana (llega al encender la plataforma)"),
    ]
    textos = []
    for m in H["correos"][:len(D["imagenes"]["d24_mailpit"]["pines"])]:
        por = next((t for p, t in motivo if re.search(p, m["asunto"])), "")
        adj = " · con PDF adjunto" if m["adjuntos"] else ""
        textos.append(f"«{E(m['asunto'])}» → {E(m['para'])}{adj}. <i>{E(por)}</i>")
    return textos


def seccion_demo():
    res_antes, res_desp = H["res_antes"], H["res_despues"]
    est = 20000 * 40
    alarma = H["alarma"]
    pasos = []
    pasos.append(paso(1, "El mesero registra un pedido", f"Mesero · {c('isabella.torres@konradgourmet.com')}", "≈ 1½ min",
        con_pines("d01_login", "Inicio de sesión", [
            "«Mesero» en los accesos de demostración: llena el correo y la clave.",
            "El correo del mesero (la clave es Konrad2026).",
            "«Iniciar sesión». La SPA recibe el JWT y muestra el menú del mesero."])
        + con_pines("d02_V03_pedido", "V03 con el pedido armado", [
            "«Agregar» en la Bandeja paisa.", "«Agregar» en la Limonada natural.", "Mesa 3.",
            f"El total: {pesos(H['pedido_total'])}.", "«Registrar pedido»."])
        + con_pines("d03_V04_registrado", "V04 con el número del pedido", [f"El número del pedido: {PED}."])
        + f'<p class="debe"><b>Debe verse:</b> V04 con {PED} por {pesos(H["pedido_total"])}.</p>',
        et("Microservicios", "OAuth 2.0 / JWT", "Composite (menú)", "REST síncrono", "pedido.registrado"),
        f"""<p>«Empezamos como mesero. El inicio de sesión lo atiende ms-identidad: devuelve un token JWT que trae el rol y
el menú, por eso cada rol ve un menú distinto sin que la SPA sepa de roles.»</p>
<p>«Registramos una bandeja paisa y una limonada en la mesa 3. ms-pedidos responde de inmediato con {PED}: el
mesero no espera nada más. Al mismo tiempo publica el evento <i>pedido.registrado</i>, que escuchan inventario, para
el consumo del día, y analítica, para los indicadores.»</p>"""))

    pasos.append(paso(2, "Cierre del día y alarma de stock bajo", f"Jefe de cocina · {c('carlos.mendoza@konradgourmet.com')}", "≈ 1½ min",
        con_pines("d04_V07_antes", "V07 antes del cierre", [
            f"La res: {E(res_antes[3])} kg, justo en el mínimo ({E(res_antes[5])} kg, el 25 % de 50 kg); estado «{E(res_antes[6])}».",
            "«Cierre del día (23:59)»."])
        + con_pines("d05_V07_alarma", "V07 con la alarma en vivo", [
            f"La alarma llega sola, sin recargar: «{E(alarma)}».", "«Solicitar» (paso 3).",
            f"La res ya descontada: {E(res_desp[3])} kg, «{E(res_desp[6])}»."])
        + f'<p class="debe"><b>Debe verse:</b> la alarma roja «{E(alarma)}» arriba a la derecha y, en Mailpit, el correo «Stock bajo: Res».</p>'
        + f'<p class="suave">Si preguntan por qué la res baja {E(res_antes[3])} → {E(res_desp[3])} kg si la bandeja paisa '
        'lleva 0,5 kg: el cierre descuenta todo lo vendido desde el último cierre, que es PED-0251 de los datos de partida '
        '(2 bandejas paisas y un ajiaco, que no lleva res) y PED-0252 (1 bandeja paisa): 3 × 0,5 kg = 1,5 kg.</p>',
        et("Eventos", "inventario.stock-bajo", "Tarea programada con ShedLock", "PD-01: Redis pub/sub y WebSocket", "Factory Method"),
        f"""<p>«Ahora el jefe de cocina. Cada noche, a las 23:59, una tarea programada descuenta lo que se vendió en el
día; con ShedLock la ejecuta una sola réplica. Aquí la adelantamos con este botón.»</p>
<p>«La res estaba en el 25 % y con lo vendido queda por debajo. ms-inventario publica <i>inventario.stock-bajo</i>;
ms-notificaciones lo recibe, lo publica en Redis y la réplica que tiene abierta la sesión del jefe le entrega la
alarma por WebSocket. Por eso aparece sin recargar. Además le llega un correo.»</p>"""))

    pasos.append(paso(3, "El jefe pide la res", "Jefe de cocina (la misma pestaña)", "≈ 1 min",
        con_pines("d06_V08_solicitud", "V08 con la res", [
            "El producto ya viene escogido: Res.", "La cantidad: 40 (kg).", "«Agregar».",
            "La línea de la solicitud.", "«Enviar solicitud»."])
        + con_pines("d07_V09_registrada", "V09 con el número", [f"El número: {SOL}.", "Estado PENDIENTE."])
        + con_pines("d08_V05_atendida", "V05 después de la solicitud", [
            "Las alertas: la de la res ya no está (quedó atendida).", f"{SOL} en «Solicitudes recientes»."])
        + f'<p class="debe"><b>Debe verse:</b> {SOL} PENDIENTE y, al volver al inicio, la alerta de la res atendida.</p>',
        et("Hexagonal", "solicitud.registrada", "Consistencia eventual"),
        f"""<p>«Desde la alarma, “Solicitar” abre la solicitud con la res ya escogida. Pedimos 40 kilos y ms-solicitudes
responde {SOL}, PENDIENTE.»</p>
<p>«La validación del formulario vive en el dominio, que es Java puro: es la arquitectura hexagonal. Y el evento
<i>solicitud.registrada</i> lo escuchan tres servicios: notificaciones manda la confirmación, analítica la cuenta e
inventario marca la alerta como atendida. Ninguno de ellos llamó a otro.»</p>"""))

    pasos.append(paso(4, "El auxiliar cotiza con el precio del Ministerio", f"Auxiliar de compras · {c('isabella.castro@konradgourmet.com')}", "≈ 2 min",
        con_pines("d09_V12_detalle", "V12 con la solicitud", [
            f"{SOL} en la lista de solicitudes.", "El detalle: quién la pidió, la res y la cantidad.",
            f"«Generar cotización con {SOL}»."])
        + con_pines("d10_V13_generar", "V13 con el proveedor", [
            f"{SOL} viene marcada (se pueden sumar varias solicitudes).", "La res, incluida en la cotización.",
            f"El proveedor: {COT1['proveedor']}.", "«GENERAR Y ENVIAR COTIZACIÓN»."])
        + con_pines("d11_V14_enviada", "V14 con PDF y correo", [
            f"El número: {COT1['numero']}.", "PDF generado (guardado en el almacén de objetos).",
            "Correo enviado: la marca aparece cuando ms-notificaciones confirma el envío.", "«Ver PDF»."])
        + f'<div class="par">{imagen("d12_pdf_cotizacion", "PDF de la cotización", f"El PDF de {COT1["numero"]}, con el precio estándar del Ministerio")}'
        + f'<div><p>Se repite con el <b>{COT2["proveedor"]}</b> ({COT2["numero"]}): «Cerrar», otra vez «Generar cotización con {SOL}» y otro proveedor. '
        f'Esta segunda vez el precio de la res ya no se pide al Ministerio: sale de la caché de Redis.</p>'
        + con_pines("d13_V10_en_tramite", "V10 del jefe", [f"{SOL} EN TRÁMITE (en la pestaña del jefe)."]) + "</div></div>"
        + f'<p class="debe"><b>Debe verse:</b> {COT1["numero"]} y {COT2["numero"]} ENVIADAS con «PDF generado» y «Correo enviado», y en la pestaña del jefe {SOL} EN TRÁMITE.</p>',
        et("Facade", "SOA: SOAP con el Ministerio", "Adapter", "Proxy (caché en Redis)", "Almacén de objetos", "cotizacion.enviada", "solicitud.en-tramite"),
        f"""<p>«La auxiliar de compras ve la solicitud y genera la cotización. Este botón es una fachada: un solo clic
coordina el precio de referencia, el PDF, el guardado y el evento.»</p>
<p>«El precio estándar lo da el Ministerio de Comercio por SOAP. Solo ms-integracion habla SOAP: el adaptador traduce
el servicio web a un puerto del dominio y un proxy guarda el precio en Redis, así la segunda cotización ya no llama al
Ministerio.»</p>
<p>«ms-cotizaciones publica <i>cotizacion.enviada</i>: notificaciones envía el PDF al proveedor por correo certificado
y solicitudes pasa la solicitud del jefe a EN TRÁMITE. Si cambiamos a la pestaña del jefe, ya lo ve.»</p>"""))

    pasos.append(paso(5, "La directora registra los valores y valida", f"Directora de compras · {c('laura.gomez@konradgourmet.com')}", "≈ 1½ min",
        con_pines("d14_V18_valores", "V18 con el precio del proveedor", [
            "El estándar del Ministerio: $20.000 por kilo.",
            f"El precio que respondió el {COT1['proveedor']}: {pesos(COT1['precio'])} por kilo.",
            f"Los totales: estándar {pesos(est)} y proveedor {pesos(COT1['precio'] * 40)}.", "«Validar cotización»."])
        + '<div class="par">'
        + "<div>" + con_pines("d15_V19_opcionada", "V19 OPCIONADA", [
            "Estándar y valor del proveedor.", f"La diferencia: {E(COT1['diferencia'].replace('Diferencia ', ''))}.",
            f"Resultado: {COT1['estado']}."]) + "</div>"
        + "<div>" + con_pines("d16_V19_rechazada", "V19 RECHAZADA", [
            "Estándar y valor del proveedor.", f"La diferencia: {E(COT2['diferencia'].replace('Diferencia ', ''))}.",
            f"Resultado: {COT2['estado']}."]) + "</div></div>"
        + f'<p class="debe"><b>Debe verse:</b> {COT1["numero"]} OPCIONADA ({E(COT1["diferencia"].replace("Diferencia ", ""))}) y {COT2["numero"]} RECHAZADA ({E(COT2["diferencia"].replace("Diferencia ", ""))}).</p>'
        + f'<p>Ruta: «Cotizaciones» → «Pendientes de validar», la cotización, «Registrar valores». Se escribe {pesos(COT1["precio"])} para {COT1["numero"]} y {pesos(COT2["precio"])} para {COT2["numero"]}.</p>',
        et("Hexagonal", "Chain of Responsibility", "cotizacion.validada"),
        f"""<p>«El proveedor responde por correo, fuera del sistema; la directora registra su precio. La validación es una
regla de negocio pura: más de 25 % por encima del Ministerio, rechazada; más de 50 % por debajo, sospechosa; el resto,
opcionada.»</p>
<p>«Está hecha como cadena de responsabilidad: cada regla es una clase y la cadena se arma en un solo lugar. El
{COT1['proveedor']} queda {E(COT1['diferencia'].replace('Diferencia ', ''))}: opcionada. El {COT2['proveedor']},
{E(COT2['diferencia'].replace('Diferencia ', ''))}: rechazada. Cada validación publica <i>cotizacion.validada</i> para el
indicador de proveedores.»</p>"""))

    pasos.append(paso(6, "La orden de compra", "Directora de compras (la misma pestaña)", "≈ 1½ min",
        con_pines("d17_V20_opcionadas", "V20 con la comparación", [
            f"La solicitud: {SOL}.", f"{COT1['numero']}, la única opcionada de {SOL}.", "«Seleccionar para compra»."])
        + con_pines("d18_V21_generar", "V21", [f"La solicitud de origen: {SOL}.", f"El total: {pesos(H['orden_total'])}.",
                                                "«Confirmar orden de compra»."])
        + con_pines("d19_V22_orden", "V22", [f"El número de la orden: {OC}.", "«VER ORDEN»."])
        + con_pines("d20_V23_orden", "V23", [f"{OC} EN PROCESO.", "«Descargar» (o «Ver PDF»).", "«Marcar como recibida» (opcional)."])
        + f'<div class="par">{imagen("d21_pdf_orden", "PDF de la orden", f"El PDF de {OC}")}<div>'
        + con_pines("d22_V23_finalizada", "V23 FINALIZADA", [f"{OC} FINALIZADA, después de «Confirmar recepción»."])
        + con_pines("d23_V10_completada", "V10 del jefe", [f"{SOL} COMPLETADA (en la pestaña del jefe)."]) + "</div></div>"
        + f'<p class="debe"><b>Debe verse:</b> {OC} por {pesos(H["orden_total"])}, su PDF y, en la pestaña del jefe, {SOL} COMPLETADA.</p>',
        et("Microservicios", "REST síncrono entre servicios", "orden.generada", "Almacén de objetos"),
        f"""<p>«En opcionadas la directora compara las cotizaciones de la solicitud y escoge una. Al confirmar, ms-ordenes
consulta la cotización a ms-cotizaciones de forma síncrona, porque la directora espera el número: {OC}.»</p>
<p>«Todo lo demás es asíncrono con <i>orden.generada</i>: la cotización pasa a CON ORDEN, la solicitud del jefe a
COMPLETADA, notificaciones envía el PDF de la orden al proveedor y analítica actualiza los indicadores. Cuando llega el
pedido, la directora la marca como recibida y queda FINALIZADA.»</p>"""))

    pasos.append(paso(7, "Los correos de la historia", f"Mailpit · {c('http://localhost:8025')}", "≈ 1 min",
        con_pines("d24_mailpit", "Bandeja de Mailpit", correos_textos())
        + con_pines("d25_mailpit_orden", "Correo de la orden", [f"El asunto, con {OC}.", f"El adjunto {OC}.pdf."])
        + '<p class="debe"><b>Debe verse:</b> un correo por cada evento de la historia, del más reciente al más antiguo. '
          'En la vista de un correo, la pestaña «Headers» muestra la huella SHA-256 y la estampa cronológica del correo certificado.</p>',
        et("Decorator (correo certificado y estampa cronológica)", "Factory Method", "Reintentos y cola de errores"),
        """<p>«Cada evento que vimos terminó en un correo. En el equipo los recibe este servidor de correo local, que hace las
veces del servidor de correo certificado. El correo se arma con decoradores: el certificado y la estampa de tiempo se
suman al envío SMTP sin cambiarlo; en las cabeceras se ve la huella del mensaje.»</p>
<p>«Si el servidor de correo no responde, ms-notificaciones reintenta y, si sigue fallando, el mensaje va a una cola
de errores que se vuelve a procesar sola.»</p>"""))

    bam = "".join(f"<li>{E(x)}</li>" for x in H["bam"])
    pasos.append(paso(8, "El tablero BAM", f"Director comercial · {c('andres.rojas@konradgourmet.com')}", "≈ 1 min",
        con_pines("d26_bam", "Tablero BAM", [
            "Los filtros: desde, hasta y sucursal (sin filtro: la demanda del último mes y el proveedor del último año).",
            "Producto con mayor demanda.", "Sucursal con mayor demanda.", "Proveedor con mejores precios."])
        + f"<p class=\"debe\"><b>Debe verse</b> (con los datos de la demo):</p><ul>{bam}</ul>",
        et("Strategy", "Eventos", "Almacén de hechos propio"),
        """<p>«El director comercial entra directo a su tablero. Los indicadores no consultan las bases de los otros
servicios: ms-analitica arma su propio almacén con los eventos que escucha (pedidos, validaciones, solicitudes,
órdenes y stock bajo).»</p>
<p>«Cada indicador es una estrategia intercambiable sobre los mismos hechos: agregar uno es agregar una clase.»</p>"""))

    pasos.append(paso(9, "Auditoría, logs y servicios activos", f"Administradora · {c('paula.herrera@konradgourmet.com')}", "≈ 1½ min",
        con_pines("d27_auditoria", "Auditoría de hoy", ["«Hoy».", "Lo más reciente primero: quién, qué acción y cuándo."])
        + imagen("d28_logs", "Logs de errores", "Logs de errores (Elasticsearch): en la demo sale vacío")
        + '<p>En la demo «Logs de errores» sale <b>vacío</b>, y es lo esperado: un error llega aquí cuando algo falla, por '
          'ejemplo si el servidor de correo o el Ministerio no responden. Si en el plan B hubo alguno, aparece con su servicio, '
          'su mensaje y su hora. Se puede filtrar por servicio.</p>'
        + '<div class="par"><div>' + con_pines("d29_plataforma", "Servicios activos", ["Los 11 servicios, activos en Eureka, con su prefijo en el gateway."])
        + "</div><div>" + con_pines("d30_eureka", "Eureka", ["Las instancias registradas en Eureka."]) + "</div></div>"
        + f'<p class="debe"><b>Debe verse:</b> en la auditoría, la historia completa (el pedido, el cierre, {SOL}, las cotizaciones, {OC}…) '
          'con el usuario de cada acción; los 11 servicios activos en '
          f'{c("http://localhost:8080/plataforma/")}.</p>',
        et("Eventos", "auditoria.evento", "Outbox transaccional", "ELK", "Service registry (Eureka)", "Balanceador"),
        """<p>«Por último, la administradora. Cada servicio publica un evento de auditoría en cada operación y
ms-administracion los guarda: aquí está la historia que acabamos de hacer, con quién hizo cada cosa. Para que no se
pierda ningún evento, cada servicio lo guarda en una tabla outbox en la misma transacción que el cambio y un relé lo
publica en Kafka.»</p>
<p>«Los errores de todos los servicios llegan a Elasticsearch y se consultan aquí; hoy no hubo ninguno. Y en esta página se ven los once
microservicios registrados en Eureka, que es donde el gateway los busca para enrutar y balancear.»</p>"""))

    resumen = f"""<p>Una sola historia que cruza los 11 servicios y los 6 roles, con los datos de la demo (la instancia
<code>kg-demo</code>, sección 2). Los números son los que salen al hacerla en ese orden sobre la instancia
restablecida: <b>{PED}</b>, la alarma de la res, <b>{SOL}</b>, <b>{COT1['numero']}</b> y <b>{COT2['numero']}</b>,
<b>{OC}</b>. Si se repite un paso, los números siguientes cambian y no pasa nada. Duración: unos 12 a 13 minutos; si
hay menos tiempo, los pasos 7 y 9 se pueden acortar a una imagen cada uno.</p>
<div class="flujo">
<div class="paso"><b>1 · Mesero</b>Pedido {PED}</div><span class="flecha">→</span>
<div class="paso"><b>2 · Jefe</b>Cierre y alarma</div><span class="flecha">→</span>
<div class="paso"><b>3 · Jefe</b>Solicitud {SOL}</div><span class="flecha">→</span>
<div class="paso"><b>4 · Auxiliar</b>{COT1['numero']} y {COT2['numero']}</div><span class="flecha">→</span>
<div class="paso"><b>5 · Directora</b>Validación</div><span class="flecha">→</span>
<div class="paso"><b>6 · Directora</b>Orden {OC}</div><span class="flecha">→</span>
<div class="paso"><b>7 · Mailpit</b>Correos</div><span class="flecha">→</span>
<div class="paso"><b>8 · Comercial</b>Tablero BAM</div><span class="flecha">→</span>
<div class="paso"><b>9 · Administradora</b>Auditoría y servicios</div>
</div>
<p>Las imágenes tienen círculos numerados en lo que se pulsa o se señala; la tabla de debajo dice qué es cada uno
(tocar un número lleva a su fila y al revés).</p>
<div class="guion"><h3>🎤 Para abrir la demo</h3>
<p>«Ahora vamos a ver la plataforma funcionando. Es la arquitectura del diagrama de contenedores corriendo en este
equipo: once microservicios, Kafka, las bases de datos y la SPA. Vamos a seguir una compra completa, desde un pedido en
la mesa hasta la orden de compra, pasando por los seis roles.»</p></div>"""
    cierre = """<div class="guion"><h3>🎤 Para cerrar la demo</h3>
<p>«En resumen: el usuario solo esperó en las acciones donde necesitaba una respuesta, como el número del pedido, de la
solicitud o de la orden. Todo lo demás, el inventario, los estados, los correos, los indicadores y la auditoría, ocurrió
por eventos, sin que un servicio llamara a otro. Eso es lo que permite escalar cada servicio por separado y que la
falla de uno no detenga a los demás.»</p></div>"""
    return resumen + "".join(pasos) + cierre


def secciones():
    s = []
    s.append(("resumen", "Qué es la plataforma", f"""
<p>La plataforma es <b>el diagrama de contenedores (página 3a) funcionando en un solo equipo</b> con Docker Compose:
22 contenedores, de los cuales 11 son los microservicios en Spring Boot y uno es la SPA en React. No es una maqueta:
cada ventana llama a su servicio por el gateway, los servicios se hablan por Kafka y cada uno tiene su base en
PostgreSQL. El código está en <code>KonradGourmet/</code> (rama <code>stack-real</code>, guía del código) y lo que hace
cada contenedor, en <code>KonradGourmet/plataforma/LEEME.md</code>.</p>
{tabla(["Dirección", "Qué es"], [
        [c("http://localhost:8080"), "La aplicación: Nginx sirve la SPA y pasa /api/** al gateway"],
        [c("http://localhost:8080/plataforma/"), "Los 11 servicios en Eureka y su prefijo en el gateway"],
        [c("http://localhost:8761"), "Eureka: las instancias registradas"],
        [c("http://localhost:8025"), "Mailpit: los correos que envía ms-notificaciones, con sus adjuntos y cabeceras"]])}
<h3>Cómo viaja una petición</h3>
<div class="flujo">
<div class="paso"><b>Navegador</b>La SPA, con el JWT</div><span class="flecha">→</span>
<div class="paso"><b>Nginx</b>Única entrada</div><span class="flecha">→</span>
<div class="paso"><b>API Gateway</b>Valida el JWT, limita la tasa y enruta</div><span class="flecha">→</span>
<div class="paso"><b>Eureka</b>Dónde está cada servicio</div><span class="flecha">→</span>
<div class="paso"><b>Microservicio</b>Su base y su outbox</div><span class="flecha">→</span>
<div class="paso"><b>Kafka</b>Los otros servicios reaccionan</div>
</div>
<p>Además corren Redis (caché de precios, canal de alarmas y límite de tasa), el almacén de objetos S3 (los PDF y las
imágenes de los platos), Elasticsearch (los logs de error), Mailpit (el correo) y dos servicios que hacen las veces
del Ministerio de Comercio y de la API de redes sociales. Lo que está fuera de la aplicación se explica en la guía de
lo externo; cada microservicio, en la guía de los microservicios.</p>
<div class="aviso">En la sustentación se presenta como «el entorno local de la plataforma». No hace falta hablar de
qué reemplaza a cada sistema externo salvo que lo pregunten (ver «Preguntas probables»).</div>
"""))

    s.append(("levantar", "Cómo se levanta, se comprueba y se apaga", """
<p>Solo hace falta Docker. Los comandos se corren desde la carpeta <code>FinalProyecto</code>. Todas las instancias
usan los mismos puertos, así que <b>solo puede estar encendida una a la vez</b>: el nombre de la instancia lo da
<code>COMPOSE_PROJECT_NAME</code>.</p>
<h3>Para la demo: la instancia kg-demo</h3>
<pre><code># 1. Dejarla con los datos de la demo (copia los datos de kg-mockups; detiene las dos si están encendidas)
documentos/guias_sustentacion/fuente/restablecer_demo.sh

# 2. Encenderla (unos 4 minutos)
COMPOSE_PROJECT_NAME=kg-demo KonradGourmet/plataforma/levantar.sh --sin-compilar

# 3. Esperar ~75 s más: el script termina cuando el gateway ya enruta, pero los servicios
#    tardan un poco en encontrarse entre sí en Eureka (si no, la primera cotización da 503)

# 4. Apagarla al terminar (conserva los datos)
COMPOSE_PROJECT_NAME=kg-demo docker compose -f KonradGourmet/plataforma/docker-compose.yml stop</code></pre>
<p>La demo cambia los datos (crea el pedido, la solicitud, las cotizaciones y la orden). Para repetirla desde el
mismo punto, por ejemplo después de un ensayo, se vuelve al paso 1.</p>
<h3>Cómo saber que está lista</h3>
<ul>
<li><code>http://localhost:8080/plataforma/</code> muestra los 11 servicios «Activo».</li>
<li>En el inventario del jefe, la res está en 12,5 kg de 50 (25 %): es el punto de partida de la alarma.</li>
<li>Mailpit está casi vacío (solo «Promociones sugeridas», que llega al encender).</li>
</ul>
<h3>Las otras instancias del equipo</h3>
<table><thead><tr><th>Instancia</th><th>Para qué</th></tr></thead><tbody>
<tr><td><code>kg-demo</code></td><td>La demo de la sustentación; se restablece desde kg-mockups</td></tr>
<tr><td><code>kg-mockups</code></td><td>Los datos de partida de la demo (COT-018 a COT-022, OC-0012); no se usa en vivo</td></tr>
<tr><td><code>kg-limpio</code></td><td>Las pruebas de punta a punta (<code>probar_todo.sh</code>)</td></tr>
<tr><td><code>konrad-gourmet</code></td><td>La instancia de trabajo, con datos de pruebas manuales</td></tr>
</tbody></table>
"""))

    s.append(("roles", "Los seis roles", """
<p>Todos con la clave <code>Konrad2026</code>; la pantalla de inicio de sesión tiene un botón por rol que llena el
correo y la clave. El menú de cada rol lo arma ms-identidad (Factory Method, un <code>construirMenu()</code> por rol)
como un árbol de grupos y opciones (Composite) y viaja dentro del JWT. La sesión dura 10 horas y, si no se marca
«Recordarme», vive en la pestaña: <b>cada pestaña puede tener un rol distinto</b>.</p>
<table><thead><tr><th>Rol</th><th>Usuario</th><th>Su menú</th></tr></thead><tbody>
<tr><td>Mesero</td><td><code>isabella.torres@konradgourmet.com</code></td><td>Inicio (V02), Pedidos del turno, Nuevo pedido (V03)</td></tr>
<tr><td>Jefe de cocina</td><td><code>carlos.mendoza@konradgourmet.com</code></td><td>Inicio (V05), Platos (V06 y editar), Inventario (V07), Solicitudes (V08 y V10), Promociones</td></tr>
<tr><td>Auxiliar de compras</td><td><code>isabella.castro@konradgourmet.com</code></td><td>Inicio (V11), Todas las solicitudes (V12), Generar cotización (V13), Cotizaciones enviadas (V15), Proveedores</td></tr>
<tr><td>Directora de compras</td><td><code>laura.gomez@konradgourmet.com</code></td><td>Inicio (V16), Todas (V17), Pendientes de validar (V18), Opcionadas (V20), Órdenes de compra (V23)</td></tr>
<tr><td>Director comercial</td><td><code>andres.rojas@konradgourmet.com</code></td><td>Tablero BAM (entra directo a él)</td></tr>
<tr><td>Administradora</td><td><code>paula.herrera@konradgourmet.com</code></td><td>Inicio, Parámetros y tema, Auditoría, Logs de errores, Usuarios, Destinatarios</td></tr>
</tbody></table>
<p>Todos tienen además «Mi perfil» y «Cerrar sesión». El mesero y el jefe de cocina son de la Sucursal Centro.</p>
"""))

    s.append(("ventanas", "Las 33 ventanas", seccion_ventanas()))
    s.append(("demo", "La demo en vivo, paso a paso", seccion_demo()))

    s.append(("planb", "Si algo falla en vivo", f"""
<table><thead><tr><th class="c-nombre">Qué pasa</th><th>Qué hacer y qué decir</th></tr></thead><tbody>
<tr><td>Un clic responde «Servicio no disponible» (503) al empezar</td><td>Eureka todavía no propagó. Esperar un minuto y
repetir; abrir <code>/plataforma/</code> para ver los 11 activos. «Los servicios se están registrando en Eureka; el
gateway solo enruta a las instancias vivas.»</td></tr>
<tr><td>Un servicio no responde a mitad de la demo</td><td><code>COMPOSE_PROJECT_NAME=kg-demo docker compose -f KonradGourmet/plataforma/docker-compose.yml restart ms-…</code>
y esperar un minuto. Aprovecharlo: «los demás servicios siguen funcionando; los eventos que le llegaron esperan en
Kafka y los procesa al volver».</td></tr>
<tr><td>La alarma no aparece en vivo</td><td>Recargar la página: V05 vuelve a leer las alertas. El correo «Stock bajo: Res»
llega igual a Mailpit.</td></tr>
<tr><td>«Cierre ejecutado: ningún insumo quedó bajo el 25 %»</td><td>La instancia no estaba restablecida (ya se había
hecho el cierre). Seguir desde el paso 3 con «Solicitudes» → «Nueva solicitud» y la res a mano, y restablecerla antes de
la próxima vez.</td></tr>
<tr><td>V14 tarda en marcar «Correo enviado»</td><td>Esperar unos segundos: la marca sale cuando ms-notificaciones
confirma el envío. Si el correo falla, reintenta solo y luego pasa a la cola de errores.</td></tr>
<tr><td>El Ministerio no responde</td><td>La cotización queda PENDIENTE DE PRECIO y se guarda sin enviar (ventana
«Reenviar una cotización»): mostrarlo como resiliencia y reenviarla desde «Cotizaciones enviadas» cuando vuelva.</td></tr>
<tr><td>Se hizo un paso dos veces</td><td>No pasa nada: los números siguientes cambian (por ejemplo, la solicitud sale
{SOL[:-3]}{int(SOL[-3:]) + 1:03d} en vez de {SOL}). Seguir con el número que muestre la pantalla.</td></tr>
<tr><td>La plataforma no enciende o el equipo está muy lento</td><td>Hacer la demo con las imágenes de la sección 5 de esta
guía, en el mismo orden, y decir lo mismo.</td></tr>
</tbody></table>
"""))

    s.append(("lista", "Antes de entrar", f"""
<ol class="lista-control">
<li><b>El día anterior:</b> <code>restablecer_demo.sh</code>, encender <code>kg-demo</code> y comprobar los 11 servicios.
Si se ensaya la demo, volver a restablecerla después.</li>
<li><b>Con tiempo antes de la sustentación</b> (unos 10 minutos): ninguna otra instancia encendida
(<code>docker ps</code>), encender <code>kg-demo</code> y esperar ~75 s más.</li>
<li><code>/plataforma/</code> con los 11 «Activo»; la res en 12,5 kg (25 %) en el inventario del jefe.</li>
<li>Pestañas abiertas en este orden, cada una con su sesión (sin «Recordarme»): <b>1</b> mesero en la pantalla de
inicio de sesión (el inicio de sesión se muestra en vivo), <b>2</b> jefe de cocina en «Inventario», <b>3</b> auxiliar
en «Todas las solicitudes», <b>4</b> directora en «Inicio», <b>5</b> Mailpit, <b>6</b> director comercial en el tablero,
<b>7</b> administradora en «Auditoría», <b>8</b> <code>/plataforma/</code>.</li>
<li>Zoom del navegador al 100 % o 110 %, pantalla completa, notificaciones del sistema silenciadas, equipo conectado
a la corriente.</li>
<li>Esta guía abierta en otro dispositivo o en otra ventana, por si hay que pasar al plan B.</li>
<li>Al terminar, apagar <code>kg-demo</code> con <code>stop</code>.</li>
</ol>
"""))

    s.append(("preguntas", "Preguntas probables", f"""
<dl class="preguntas">
<dt>¿Esto corre en la nube?</dt><dd>No: es la misma arquitectura en un equipo con Docker Compose, un contenedor por
pieza del diagrama de contenedores. En producción cambia el despliegue (página 5: réplicas, balanceador, centro alterno),
no el código.</dd>
<dt>¿Qué pasa si se cae un microservicio?</dt><dd>Los demás siguen. Lo que le llegue por Kafka espera en el tópico y lo
procesa al volver; si alguien llama a su ruta, el gateway responde que no está disponible. En producción hay 2 a 4
réplicas por servicio.</dd>
<dt>¿Cómo se asegura que un evento no se pierda ni se procese dos veces?</dt><dd>Outbox: el evento se guarda en la misma
transacción que el cambio y un relé lo publica en Kafka. Y cada consumidor guarda el identificador de los eventos que ya
procesó y descarta los repetidos.</dd>
<dt>¿Por qué la alarma llega en vivo si hay varias réplicas de notificaciones?</dt><dd>La réplica que recibe el evento lo
publica en un canal de Redis y cada réplica se lo entrega a los jefes que tiene conectados por WebSocket (PD-01).</dd>
<dt>¿De dónde sale el precio del Ministerio?</dt><dd>De un servicio web SOAP del Ministerio de Comercio, que solo consulta
ms-integracion; el precio queda en caché en Redis. En el equipo, ese servicio lo atiende un servidor local que responde
como el del Ministerio (guía de lo externo).</dd>
<dt>¿Los correos salen de verdad?</dt><dd>Salen por SMTP a un servidor de correo local que los guarda para verlos, con sus
adjuntos y cabeceras; en producción se cambia la dirección del servidor de correo certificado, no el código.</dd>
<dt>¿Por qué «Cierre del día» es un botón?</dt><dd>Para no esperar a las 23:59: ejecuta la misma tarea programada que corre
cada noche, con ShedLock para que la haga una sola réplica.</dd>
<dt>¿Cómo se sabe quién hizo cada cosa?</dt><dd>El usuario sale del JWT y viaja en cada evento; la auditoría lo muestra
con la fecha y la acción, y cada historia comparte un identificador de correlación.</dd>
<dt>¿Cómo se probó?</dt><dd><code>probar_todo.sh</code> corre sobre una instalación limpia 1.254 comprobaciones sin fallas:
400 del núcleo, 91 pruebas JUnit y 763 de recorridos en el navegador y verificadores, uno de ellos esta misma historia
de punta a punta.</dd>
<dt>¿Funciona en el celular?</dt><dd>Sí, las 33 ventanas son responsive (sección 4, al final).</dd>
</dl>
"""))
    return s


def main():
    s = secciones()
    indice = '<nav class="indice"><strong>Contenido</strong><ol>' + "".join(
        f'<li><a href="#{i}">{E(t)}</a></li>' for i, t, _ in s) + "</ol></nav>"
    cuerpo = CSS_LOCAL + indice + "".join(f'<section class="bloque" id="{i}"><h2>{n}. {E(t)}</h2>{h}</section>'
                                          for n, (i, t, h) in enumerate(s, 1))
    sub = ("Qué hace la plataforma de Konrad Gourmet, cómo se levanta, sus seis roles y sus 33 ventanas, y el "
           "recorrido de la demo en vivo paso a paso: qué pulsar, qué decir y qué debe verse en pantalla.")
    html = pagina("Guía de la plataforma · Konrad Gourmet", sub, cuerpo, con_visor=True)
    (GUIAS / "guia_plataforma.html").write_text(html, encoding="utf-8")
    print(f"guia_plataforma.html: {len(s)} secciones, {len(D['imagenes'])} imágenes")


if __name__ == "__main__":
    main()
