"""Genera guias_sustentacion/guia_microservicios.html: los 11 microservicios uno por uno, el gateway y Eureka.

Lo que se puede contar se lee de KonradGourmet/ al generar (endpoints, tópicos que publica y consume cada servicio,
tablas de Flyway, clases del núcleo, adaptadores Spring, prefijos del gateway y réplicas de la página 5), así la guía
no puede decir otra cosa que el código. Los recortes de cada paquete salen de las imágenes de la guía de los
diagramas (img/diagramas/, con sus mismos números) y van a img/microservicios/. Uso, desde la carpeta FinalProyecto:
    mockups_nuevos/.venv/bin/python documentos/guias_sustentacion/fuente/generar_guia_microservicios.py
(Pillow solo hace falta para los recortes; sin él, se usan los que ya están en img/microservicios/.)
"""
import html as H
import json
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

AQUI = Path(__file__).resolve().parent
sys.path.insert(0, str(AQUI))
from estilo import E, pagina, visor              # noqa: E402
import contenido_diagramas as CD                 # noqa: E402
import generar_guia_plataforma as GP             # noqa: E402

GUIAS = AQUI.parent
DOCS = GUIAS.parent
KG = DOCS.parent / "KonradGourmet"
NUCLEO = KG / "src/co/edu/konradlorenz"
SERV = KG / "servicios"
DRAWIO = DOCS / "Arquitectura_Konrad_Gourmet.drawio"
IMG_D = GUIAS / "img" / "diagramas"
IMG_M = GUIAS / "img" / "microservicios"
IMG_P = GUIAS / "img" / "plataforma"
DATOS_P = json.loads((IMG_P / "datos.json").read_text(encoding="utf-8"))

ORDEN = ["identidad", "pedidos", "menu", "inventario", "solicitudes", "cotizaciones", "ordenes", "notificaciones",
         "integracion", "administracion", "analitica"]
TOPICOS = ["pedido.registrado", "menu.actualizado", "inventario.stock-bajo", "solicitud.registrada",
           "solicitud.en-tramite", "cotizacion.enviada", "cotizacion.validada", "orden.generada",
           "tendencia.detectada", "auditoria.evento", "notificaciones.dlq"]


def c(t):
    return f"<code>{E(t)}</code>"


def et(*ts):
    return "".join(f'<span class="etiqueta">{E(t)}</span>' for t in ts)


def tabla(cabeza, filas, clases=None, extra=""):
    th = "".join(f'<th{f" class={clases[i]}" if clases and clases[i] else ""}>{E(x)}</th>' for i, x in enumerate(cabeza))
    cuerpo = "".join("<tr>" + "".join(f"<td>{x}</td>" for x in f) + "</tr>" for f in filas)
    return f"<table{extra}><thead><tr>{th}</tr></thead><tbody>{cuerpo}</tbody></table>"


def java(ruta):
    return ruta.read_text(encoding="utf-8")


def paquete(s):
    return SERV / f"ms-{s}/src/main/java/co/edu/konradlorenz/{s}"


# ───────────────────────── lo que se lee del código ─────────────────────────
def clases_nucleo():
    """{módulo: {capa: [clases]}} del núcleo (src/), sin demo."""
    res = {}
    for m in sorted(p for p in NUCLEO.iterdir() if p.is_dir() and p.name != "demo"):
        capas = {}
        for f in sorted(m.rglob("*.java")):
            capa = "/".join(f.relative_to(m).parts[:-1])
            capas.setdefault(capa, []).append(f.stem)
        res[m.name] = capas
    return res


def tablas_flyway(s):
    texto = "".join(java(f) for f in sorted((SERV / f"ms-{s}/src/main/resources/db/migration").glob("*.sql")))
    return re.findall(r"create table (?:if not exists )?([a-z_]+)", texto, re.I)


def propiedades(s):
    """Valores sencillos del application.yml (clave: valor), para resolver ${…} de las anotaciones."""
    texto = java(SERV / f"ms-{s}/src/main/resources/application.yml")
    return {m.group(1): m.group(2).strip('"') for m in re.finditer(r"^\s*([a-z-]+):\s*(\S.*)$", texto, re.M)}


def resolver(valor, props):
    """${clave:defecto}: el valor del application.yml (o el defecto) y, si ese valor es ${VARIABLE:defecto}, su defecto."""
    def uno(m):
        clave, _, defecto = m.group(1).partition(":")
        return props.get(clave.split(".")[-1], defecto)
    for _ in range(3):
        valor = re.sub(r"\$\{([^}]+)\}", uno, valor)
    return valor


DIAS = {"MON": "los lunes", "*": "todos los días"}


def cuando(cron):
    """«0 59 23 * * *» → «todos los días a las 23:59»; «PT1M» → «cada minuto»."""
    if cron.startswith("PT"):
        return {"PT1M": "cada minuto"}.get(cron, cron)
    seg, minu, hora, _, _, dia = cron.split()
    return f"{DIAS[dia]} a las {int(hora)}:{int(minu):02d} ({cron})"


def entradas(s):
    """Lo que entra al servicio: REST, Kafka y tareas programadas, con su clase (adaptadores de entrada y configuración)."""
    props = propiedades(s)
    res = []
    archivos = sorted((paquete(s) / "adaptadores/entrada").glob("*.java")) + sorted((paquete(s) / "configuracion").glob("*.java"))
    for f in archivos:
        t = java(f)
        base = ""
        m = re.search(r'^@RequestMapping\("([^"]+)"\)', t, re.M)
        if m:
            base = m.group(1)
        for m in re.finditer(r"@(Get|Post|Put|Patch|Delete)Mapping(\(([^)]*)\))?", t):
            args = m.group(3) or ""
            ruta = re.search(r'(?:value\s*=\s*|path\s*=\s*|^)"([^"]*)"', args.strip())
            ruta = ruta.group(1) if ruta else ""
            params = re.search(r'params\s*=\s*"([^"]+)"', args)
            q = ""
            if params:
                p = params.group(1)
                q = "?" + (p if "=" in p else p + "=…")
            res.append(("REST", f"{m.group(1).upper()} {base}{ruta}{q}", f.stem))
        for m in re.finditer(r"@KafkaListener\(([^)]*)\)", t, re.S):
            for top in re.findall(r'"([^"]+)"', m.group(1).split("topics", 1)[1]):
                if "prefijo" in top or "topico" in top:
                    res.append(("Kafka", resolver(top, props).replace("${konrad.kafka.prefijo-topico:}", ""), f.stem))
        for m in re.finditer(r'@Scheduled\((?:cron\s*=\s*"([^"]+)"|initialDelayString\s*=\s*"([^"]+)")', t):
            res.append(("Tarea", cuando(resolver(m.group(1) or m.group(2), props)), f.stem))
    return res


def consumidores():
    res = {}
    for s in ORDEN:
        for tipo, que, clase in entradas(s):
            if tipo == "Kafka":
                res.setdefault(que, []).append((s, clase))
    return res


def publicadores():
    """{tópico: [módulos]}: el tópico sale del tipo() de cada evento; publica el módulo que hace new XEvent(…)."""
    eventos = {}
    for f in NUCLEO.rglob("*Event.java"):
        if "demo" in f.parts:
            continue
        m = re.search(r'return "([a-z]+\.[a-z-]+)";', java(f))
        if m:
            eventos[f.stem] = m.group(1)
    res = {}
    for mod in ORDEN:
        textos = "".join(java(f) for f in (NUCLEO / mod).rglob("*.java")) + \
                 "".join(java(f) for f in paquete(mod).rglob("*.java"))
        for clase, top in eventos.items():
            if re.search(rf"new {clase}\(", textos):
                res.setdefault(top, []).append(mod)
    return res


def adaptadores_spring(s):
    """{entrada|salida|configuracion: [clases]} del servicio Spring; las entidades y repositorios JPA aparte."""
    res = {}
    for capa, carpeta in (("entrada", "adaptadores/entrada"), ("salida", "adaptadores/salida"), ("configuracion", "configuracion")):
        res[capa] = sorted(f.stem for f in (paquete(s) / carpeta).glob("*.java"))
    return res


def rutas_gateway():
    texto = java(SERV / "api-gateway/src/main/resources/application.yml")
    return re.findall(r"- id: (\S+)\s+uri: lb://(\S+)\s+predicates:\s+- Path=(\S+)", texto)


def replicas():
    raiz = ET.parse(DRAWIO).getroot()
    pag5 = next(d for d in raiz.findall("diagram") if d.get("name").startswith("5"))
    textos = {c.get("id"): re.sub(r"\s+", " ", re.sub("<[^>]+>", " ", H.unescape(c.get("value") or "")))
              for c in pag5.iter("mxCell")}
    res = {}
    for m in re.finditer(r"ms-([a-z]+) (\d+) → (\d+)", textos["v15"]):
        res[m.group(1)] = f"{m.group(2)} a {m.group(3)}"
    resto = re.search(r"ms-menu, (.+?) (\d+) → (\d+)", textos["v15"])
    for s in re.findall(r"ms-([a-z]+)", "ms-menu, " + resto.group(1)):
        res[s] = f"{resto.group(2)} a {resto.group(3)}"
    gw = re.search(r"×(\d+)", textos["v11"]).group(1)
    eu = re.search(r"×(\d+)", textos["v13"]).group(1)
    return res, gw, eu


def ventanas():
    """{servicio: [(código o nombre, imagen, en parte)]} a partir de las 33 ventanas de la guía de la plataforma."""
    res = {s: [] for s in ORDEN}
    todas = [(v[0], v[1], v[2], v[4]) for lista in GP.VENTANAS.values() for v in lista] + \
            [("", t, img, serv) for _, t, img, _, serv, *_ in GP.OTRAS]
    for cod, titulo, img, serv in todas:
        # «ms-x y ms-y»: las dos la atienden; «ms-x (… de ms-y)»: ms-y, en parte
        antes, _, entre = serv.partition("(")
        nombre = f"{cod} · {titulo}" if cod else titulo
        for s in re.findall(r"ms-([a-z]+)", antes):
            res[s].append((nombre, img, False))
        for s in re.findall(r"ms-([a-z]+)", entre):
            res[s].append((nombre, img, True))
    return res, len(todas)


# ───────────────────────── recortes de los diagramas de clases ─────────────────────────
def _diagramas():
    raiz = ET.fromstring(DRAWIO.read_text(encoding="utf-8"))
    return {d.get("name").split(" ")[0]: d for d in raiz.findall("diagram")}


def _ancla(diag, clave):
    """El mismo cálculo de pines.exportar(): dónde cae (0, 0) de la imagen, el diámetro y los centros de los números."""
    import pines as P
    celdas, absoluta = P._geometrias(diag[clave])
    xs, ys = [], []
    for i, (_, geo, puntos, vert) in celdas.items():
        if geo is not None and (vert or celdas[i][0] not in (None, "0")):
            x, y, w, h = absoluta(i) if vert else (geo[0], geo[1], 0, 0)
            xs.append(x); ys.append(y)
        for px, py in puntos:
            xs.append(px); ys.append(py)
    d = P.diametro(max(xs) - min(xs))
    cent = []
    for n, (cid, pos, nombre, _) in enumerate(CD.DIAGRAMAS[clave]["elementos"], 1):
        x, y, w, h = absoluta(cid)
        cx, cy = {"si": (x, y), "sd": (x + w, y), "ii": (x, y + h), "id": (x + w, y + h), "c": (x + w / 2, y + h / 2)}[pos or "si"]
        cent.append((n, nombre, cx, cy))
    ax = round(min(min(xs), min(c[2] for c in cent) - d / 2) - 40)
    ay = round(min(min(ys), min(c[3] for c in cent) - d / 2) - 40)
    return ax, ay, absoluta, d, cent


RECORTES = {   # nombre: (página, celdas que debe cubrir, margen extra)
    "gateway": ("3a", ["c3a_registry", "c3a_web", "c3a_spa", "c3a_gateway"], 0),
    "compartido": ("4a", ["pkg_compartido"], 0),
    "identidad": ("4a", ["pkg_identidad"], 0),
    "pedidos": ("4b", ["pkg_pedidos"], 0), "menu": ("4b", ["pkg_menu"], 0), "inventario": ("4b", ["pkg_inventario"], 0),
    "solicitudes": ("4c", ["pkg_solicitudes"], 0), "cotizaciones": ("4c", ["pkg_cotizaciones"], 0),
    "ordenes": ("4d", ["pkg_ordenes"], 0), "notificaciones": ("4d", ["pkg_notificaciones"], 0),
    "integracion": ("4e", ["pkg_integracion"], 0), "administracion": ("4e", ["pkg_administracion"], 0),
    "analitica": ("4e", ["pkg_analitica"], 0),
}


def recortes():
    """Recorta cada paquete de su página y devuelve {nombre: datos del visor y de sus números}."""
    diag = _diagramas()
    IMG_M.mkdir(parents=True, exist_ok=True)
    try:
        from PIL import Image
    except ImportError:
        Image = None
    res = {}
    for nombre, (clave, celdas, extra) in RECORTES.items():
        ax, ay, absoluta, d, cent = _ancla(diag, clave)
        cajas = [absoluta(cid) for cid in celdas]
        x0 = min(b[0] for b in cajas) - ax - d - extra
        y0 = min(b[1] for b in cajas) - ay - d - extra
        x1 = max(b[0] + b[2] for b in cajas) - ax + d + extra
        y1 = max(b[1] + b[3] for b in cajas) - ay + d + extra
        destino = IMG_M / f"{nombre}.png"
        if Image is not None:
            with Image.open(IMG_D / f"{clave}.png") as im:
                x0, y0 = max(0, x0), max(0, y0)
                x1, y1 = min(im.size[0], x1), min(im.size[1], y1)
                im.crop((x0, y0, x1, y1)).save(destino, optimize=True)
        elif not destino.exists():
            sys.exit("Falta Pillow para recortar los diagramas: usa mockups_nuevos/.venv/bin/python")
        else:
            x0, y0 = max(0, x0), max(0, y0)
            with open(destino, "rb") as f:
                f.seek(16)
                ancho, alto = int.from_bytes(f.read(4), "big"), int.from_bytes(f.read(4), "big")
            x1, y1 = x0 + ancho, y0 + alto
        dentro = [(n, nom, round(cx - ax - x0), round(cy - ay - y0)) for n, nom, cx, cy in cent
                  if x0 <= cx - ax <= x1 and y0 <= cy - ay <= y1]
        if nombre == "gateway":   # en la 3a solo interesan el servidor web, la SPA, el gateway y el registro
            dentro = [p for p in dentro if p[1] in ("Servidor web (Nginx)", "SPA Konrad Gourmet", "API Gateway «Facade»",
                                                     "Service Registry (Eureka)")]
        res[nombre] = dict(pagina=clave, ancho=x1 - x0, alto=y1 - y0, d=d, pines=dentro)
    return res


# ───────────────────────── lo que se escribe a mano (y se comprueba) ─────────────────────────
SERVICIOS = {
    "identidad": dict(
        titulo="ms-identidad · quién es quién",
        responsabilidad="Inicio de sesión con OAuth 2.0: comprueba la clave con BCrypt y emite el JWT firmado con RS256, que "
        "trae el rol, la sucursal y el menú del rol. Publica su llave pública (JWKS) para que el gateway y los demás "
        "servicios validen el token sin llamarlo en cada petición. El administrador da de alta usuarios con la regla de contraseña.",
        frase="ms-identidad es el único que sabe quién es cada usuario: emite el token y el menú de cada rol; los demás solo validan la firma.",
        patrones=["Factory Method (construirMenu() por rol)", "Composite (ElementoMenu)", "OAuth 2.0 / JWT RS256", "Hexagonal"],
        principales=["Usuario", "IniciarSesionService", "RegistrarUsuarioService", "PoliticaContrasena", "ElementoMenu",
                     "GrupoMenu", "OpcionMenu", "JwtEmisorAdapter", "BCryptCifradorAdapter", "UsuarioJpaAdapter"],
        sincrono="Lo llaman a él: el gateway y cada servicio leen la llave pública en <code>GET /api/auth/jwks</code> "
        "(dentro de la red, sin pasar por el gateway). Él no llama a ningún servicio.",
        caida="Nadie puede iniciar sesión ni crear usuarios. Las sesiones abiertas siguen: el gateway y los servicios validan "
        "la firma con la llave pública que ya descargaron, mientras la llave no cambie. Por eso en el despliegue es, después "
        "de ms-pedidos, el que más réplicas tiene.",
        notas="La llave privada se guarda en su base (<code>llave_firma</code>) y no sale del servicio. El intento fallido de "
        "inicio de sesión también se audita: es el único endpoint que escribe sin transacción, para que el evento quede "
        "aunque la respuesta sea un error."),
    "pedidos": dict(
        titulo="ms-pedidos · lo que piden las mesas",
        responsabilidad="Los pedidos del mesero por mesa: el catálogo con foto y precio, las mesas de la sucursal, el registro "
        "del pedido y la lista del turno. El catálogo es una copia local que arma con <code>menu.actualizado</code>, así no "
        "depende de ms-menu para tomar un pedido.",
        frase="ms-pedidos registra lo que pide cada mesa y avisa con pedido.registrado; es el que más carga tiene en hora pico.",
        patrones=["Hexagonal", "Publish-Subscribe", "Copia local (catálogo)", "Outbox"],
        principales=["Pedido", "DetallePedido", "Mesa", "CatalogoPlatos", "PedidoService", "PedidoRegistradoEvent",
                     "PedidoJpaAdapter", "CatalogoJpaAdapter"],
        sincrono="Ninguno. La foto del plato la pide la SPA a ms-menu, no ms-pedidos.",
        caida="Los meseros no pueden registrar pedidos. Nada más se detiene: el inventario y la analítica simplemente no "
        "reciben pedidos nuevos. Si lo que se cae es ms-menu, ms-pedidos sigue tomando pedidos con su copia del catálogo.",
        notas="Es el servicio con más réplicas del despliegue porque los meseros de todas las sucursales registran pedidos "
        "todo el día."),
    "menu": dict(
        titulo="ms-menu · la carta",
        responsabilidad="Los platos con su categoría, su receta (ingredientes con cantidad y unidad) y su imagen, y la lista "
        "de productos que usan las solicitudes. Cada cambio de un plato (crear, editar, desactivar, activar) publica "
        "<code>menu.actualizado</code> con el plato completo.",
        frase="ms-menu es la fuente de la carta: cuando un plato cambia, lo publica y los demás actualizan su copia.",
        patrones=["Hexagonal", "Publish-Subscribe (estado transferido en el evento)", "Almacén de objetos (imágenes)"],
        principales=["Plato", "Ingrediente", "Producto", "Categoria", "PlatoService", "MenuActualizadoEvent",
                     "PlatoJpaAdapter", "AlmacenObjetosAdapter"],
        sincrono="Ninguno con otros servicios; guarda las imágenes en el almacén de objetos (bucket <code>imagenes-platos</code>).",
        caida="El jefe de cocina no puede crear ni editar platos, las fotos de los platos que la tienen no cargan en V03 (se ve su ícono) y V08 no puede "
        "listar los productos para registrar una solicitud. Los pedidos siguen, porque ms-pedidos tiene su copia del catálogo, "
        "y ms-inventario conserva sus recetas.",
        notas="Tres servicios escuchan sus eventos: ms-pedidos (catálogo), ms-inventario (recetas) y ms-analitica (nombres "
        "de los platos)."),
    "inventario": dict(
        titulo="ms-inventario · las existencias",
        responsabilidad="Las existencias por sucursal, las recetas locales (de <code>menu.actualizado</code>) y el consumo de "
        "cada pedido (de <code>pedido.registrado</code>). A las 23:59 descuenta el consumo del día y, si un insumo cruza el "
        "25 % de su stock de referencia, publica <code>inventario.stock-bajo</code> una sola vez. Con "
        "<code>solicitud.registrada</code> marca la alerta como atendida.",
        frase="ms-inventario descuenta lo que se consumió cada noche y avisa una sola vez cuando un insumo baja del 25 %.",
        patrones=["Hexagonal", "Publish-Subscribe", "Tarea programada con ShedLock", "Consumidor idempotente"],
        principales=["Inventario", "InsumoInventario", "RecetaPlato", "UmbralStock", "InventarioService", "StockBajoEvent",
                     "ConsumoDiarioJob", "PedidoRegistradoListener", "InventarioJpaAdapter"],
        sincrono="Ninguno.",
        caida="V05 y V07 no cargan. Los pedidos siguen registrándose y sus eventos esperan en Kafka: al volver los procesa "
        "y el consumo queda pendiente hasta el siguiente cierre, que lo descuenta completo y sin repetirlo. Con varias "
        "réplicas, ShedLock deja que el cierre lo corra una sola.",
        notas="El cierre corre en una sola transacción (inventario, consumo pendiente y outbox): si se cae a mitad no queda "
        "nada aplicado. «Cierre del día» de V07 lo dispara a mano para la demo."),
    "solicitudes": dict(
        titulo="ms-solicitudes · lo que pide la cocina",
        responsabilidad="Las solicitudes de alimentos del jefe de cocina y sus estados (PENDIENTE, EN TRÁMITE, COMPLETADA). "
        "El jefe ve las suyas y el auxiliar de compras ve todas con los filtros de HU-07. Con <code>cotizacion.enviada</code> "
        "pasa a EN TRÁMITE las que quedaron cotizadas y con <code>orden.generada</code> las completa.",
        frase="ms-solicitudes lleva el estado de cada solicitud de la cocina; los cambios de estado llegan por eventos de compras.",
        patrones=["Hexagonal", "Publish-Subscribe", "Consumidor idempotente (por cotización)"],
        principales=["SolicitudAlimentos", "ItemSolicitud", "EstadoSolicitud", "CriteriosConsulta", "SolicitudService",
                     "SolicitudRegistradaEvent", "SolicitudEnTramiteEvent", "CotizacionEventsListener", "SolicitudJpaAdapter"],
        sincrono="Ninguno.",
        caida="No se registran ni se consultan solicitudes (V08 a V12) y el auxiliar no llega a V13 desde V12. Las "
        "cotizaciones y órdenes que se emitan mientras tanto dejan sus eventos en Kafka y las solicitudes cambian de estado "
        "al volver.",
        notas="La tabla <code>cotizacion_aplicada</code> recuerda qué cotizaciones ya aplicó, para no repetir el cambio de estado."),
    "cotizaciones": dict(
        titulo="ms-cotizaciones · el corazón de compras",
        responsabilidad="La solicitud de cotización de una o varias solicitudes, con el precio de referencia del Ministerio "
        "y el PDF que se envía al proveedor; el registro de los valores del proveedor y la validación con la cadena de "
        "reglas (RECHAZADA, SOSPECHOSA u OPCIONADA); el reenvío de las que quedaron pendientes de precio; y los "
        "proveedores (HU-15).",
        frase="ms-cotizaciones arma la cotización con el precio del Ministerio, la envía en PDF y la valida con tres reglas en cadena.",
        patrones=["Facade (GenerarCotizacionFacade)", "Chain of Responsibility (ReglaValidacion)", "Claim check (PDF en S3)",
                  "Hexagonal", "Publish-Subscribe"],
        principales=["Cotizacion", "ItemCotizacion", "EstadoCotizacion", "Proveedor", "GenerarCotizacionFacade",
                     "CotizacionService", "ProveedorService", "ReglaValidacion", "ReglaRechazada", "ReglaSospechosa",
                     "ReglaOpcionada", "PrecioSugeridoRestAdapter", "PdfCotizacionAdapter", "AlmacenObjetosAdapter"],
        sincrono="Llama a ms-integracion (<code>GET /interno/precios/{id}</code>, por Eureka y con el JWT del auxiliar) "
        "para el precio de referencia. Lo llama ms-ordenes para leer una cotización OPCIONADA.",
        caida="No se cotiza ni se valida (V13 a V20 y Proveedores) y ms-ordenes no puede generar órdenes: responde 503 "
        "con «No se pudo leer la cotización…», que la directora puede reintentar. Las órdenes ya generadas dejan "
        "<code>orden.generada</code> en Kafka y la cotización pasa a CON ORDEN al volver.",
        notas="Si ms-integracion o el Ministerio no responden, la fachada guarda la cotización como PENDIENTE DE PRECIO sin "
        "enviarla y el auxiliar la reenvía después. El correo no lleva el PDF: lleva su ubicación en el almacén (claim check) "
        "y ms-notificaciones lo descarga."),
    "ordenes": dict(
        titulo="ms-ordenes · la compra",
        responsabilidad="La orden de compra que la directora genera desde una cotización OPCIONADA (una sola por cotización), "
        "con su PDF en el almacén de objetos, y su seguimiento hasta que el pedido llega y pasa a FINALIZADA.",
        frase="ms-ordenes convierte una cotización opcionada en orden de compra, con su PDF, y avisa con orden.generada.",
        patrones=["Hexagonal", "Publish-Subscribe", "Claim check (PDF en S3)"],
        principales=["OrdenCompra", "ItemOrden", "EstadoOrden", "CotizacionOpcionada", "OrdenCompraService",
                     "OrdenGeneradaEvent", "CotizacionRestClient", "PdfOrdenAdapter", "OrdenJpaAdapter"],
        sincrono="Llama a ms-cotizaciones (<code>GET /api/cotizaciones/{numero}</code>, por Eureka y con el JWT de la "
        "directora) para leer la cotización; solo acepta las OPCIONADAS.",
        caida="La directora no puede generar ni ver órdenes (V21 a V23). Las solicitudes y las cotizaciones siguen igual: "
        "solo cambian de estado cuando hay orden.",
        notas="Es el único servicio cuyo caso de uso necesita una respuesta inmediata de otro: la orden se arma con los "
        "precios y cantidades de la cotización en ese momento."),
    "notificaciones": dict(
        titulo="ms-notificaciones · los avisos",
        responsabilidad="Los correos certificados (huella SHA-256 y estampa cronológica) de la alarma de stock bajo, la "
        "solicitud registrada, la solicitud en trámite, la cotización enviada al proveedor, la orden generada y las "
        "promociones, con el PDF adjunto; la alarma en vivo al jefe de cocina (Redis y WebSocket); la libreta de "
        "destinatarios; y la traza de envíos que muestra V14. Si un correo falla, reintenta y lo deja en la cola de errores.",
        frase="ms-notificaciones solo escucha: convierte seis tipos de evento en correos certificados y en la alarma en vivo.",
        patrones=["Factory Method (CreadorNotificacion)", "Decorator (CorreoDecorator)", "Publish-Subscribe",
                  "Cola de errores (DLQ)", "Redis pub/sub (PD-01)"],
        principales=["NotificacionService", "CreadorNotificacion", "CreadorCorreo", "CreadorAlarmaApp", "Notificacion",
                     "CorreoDecorator", "CorreoCertificadoDecorator", "EstampaCronologicaDecorator", "CorreoSmtpAdapter",
                     "AlarmaPushAdapter", "NotificacionListeners", "EnvioJpaAdapter"],
        sincrono="Ninguno con otros servicios. Hacia afuera: el servidor de correo (SMTP), Redis para la alarma y el almacén "
        "de objetos para descargar el PDF adjunto.",
        caida="No salen correos ni la alarma en vivo, pero nada se pierde: los eventos esperan en Kafka y al volver envía "
        "los pendientes. V05 sigue mostrando las alertas, porque las lee de ms-inventario. Si el que falla es el servidor de "
        "correo, reintenta, deja el envío en <code>notificaciones.dlq</code> y lo vuelve a procesar cada 5 minutos, hasta "
        "tres veces.",
        notas="Es el único que publica y consume su propio tópico (la cola de errores). Marca el evento como procesado "
        "después de enviar, para que la traza del fallo quede igual."),
    "integracion": dict(
        titulo="ms-integracion · la frontera SOA",
        responsabilidad="El único que habla SOAP: consulta el precio estándar al servicio web del Ministerio de Comercio, lo "
        "guarda un día en una caché de Redis compartida por las réplicas, y expone el servicio SOAP de abastecimiento del "
        "restaurante en <code>/api/integracion/abastecimiento</code> (WSDL en <code>?wsdl</code>).",
        frase="ms-integracion traduce el SOAP del Ministerio a un puerto del dominio y guarda los precios un día en caché.",
        patrones=["Adapter (MinisterioComercioAdapter)", "Proxy (PrecioReferenciaCacheProxy)", "SOA / SOAP", "Hexagonal"],
        principales=["PrecioEstandar", "ConsultarPrecioService", "PrecioReferenciaPort", "PrecioReferenciaCacheProxy",
                     "MinisterioComercioAdapter", "MinisterioSoapClient", "PrecioRestController", "AbastecimientoSoapEndpoint"],
        sincrono="Lo llama ms-cotizaciones (REST interno, que no se publica en el gateway). Él llama al Ministerio por SOAP.",
        caida="Las cotizaciones que se generen mientras tanto se guardan PENDIENTE DE PRECIO sin enviarse; cuando vuelve, el auxiliar las reenvía "
        "desde «Reenviar». Si el que no responde es el Ministerio, los precios que están en la caché siguen sirviendo.",
        notas="Su base solo tiene la tabla <code>outbox</code>: no guarda datos de negocio, solo los eventos de auditoría "
        "que publica."),
    "administracion": dict(
        titulo="ms-administracion · parámetros, auditoría y logs",
        responsabilidad="Los parámetros del sistema y la imagen corporativa (el Singleton <code>ParametrosSistema</code>, que "
        "cada réplica refresca de la base), la bitácora de auditoría de todos los servicios (escucha "
        "<code>auditoria.evento</code>) y los logs de errores, que consulta en Elasticsearch.",
        frase="ms-administracion guarda los parámetros y el tema, y la bitácora de lo que hacen todos los demás.",
        patrones=["Singleton (ParametrosSistema)", "Publish-Subscribe (consumidor de auditoría)", "Consumidor idempotente", "Hexagonal"],
        principales=["ParametrosSistema", "Bitacora", "RegistroAuditoria", "LogError", "AdministracionService",
                     "AuditoriaListener", "ElasticsearchLogsAdapter", "AuditoriaJpaAdapter", "ParametrosJpaAdapter"],
        sincrono="Ninguno con otros servicios; consulta Elasticsearch para los logs.",
        caida="El administrador no tiene sus ventanas y la SPA inicia sesión con el tema de siempre. Los eventos de "
        "auditoría esperan en Kafka y la bitácora se completa al volver, sin repetir los que ya tenía.",
        notas="No publica <code>auditoria.evento</code>: sus propias acciones las escribe directo en su bitácora, para no "
        "mandarse un evento a sí mismo."),
    "analitica": dict(
        titulo="ms-analitica · el tablero y las promociones",
        responsabilidad="El tablero BAM del director comercial (producto y sucursal con mayor demanda y proveedor con mejores "
        "precios, con su ranking y su periodo) sobre su propio almacén de hechos, que arma con los eventos del negocio; y "
        "las promociones sugeridas al jefe de cocina, que salen de cruzar las tendencias de las redes sociales con los "
        "platos más pedidos.",
        frase="ms-analitica arma sus propios hechos con los eventos y calcula cada KPI con una estrategia intercambiable.",
        patrones=["Strategy (CalculadorKpi)", "Publish-Subscribe", "Tarea programada con ShedLock", "Consumidor idempotente"],
        principales=["HechoNegocio", "CalculadorKpi", "ProductoMayorDemanda", "SucursalMayorDemanda", "ProveedorMejoresPrecios",
                     "SugerenciaPromocion", "TableroService", "TendenciaDetectadaEvent", "NegocioEventsListener",
                     "TendenciasJob", "RedesSocialesAdapter"],
        sincrono="Ninguno con otros servicios; consulta la API de tendencias de las redes sociales.",
        caida="No hay tablero ni promociones. Los eventos del negocio esperan en Kafka y los hechos se ponen al día al "
        "volver; ningún otro servicio depende de ms-analitica.",
        notas="No lee las bases de los otros servicios: guarda solo los hechos que necesita (tabla <code>hecho</code>). "
        "<code>TendenciasJob</code> corre los lunes a las 6:00 (en el equipo, cada 30 segundos, para la demo)."),
}

EXTRA_ENTRADAS = {   # lo que no es una anotación de Spring MVC
    "notificaciones": [("WebSocket", "/api/notificaciones/alarmas", "ConfiguracionWebSocket")],
    "integracion": [("SOAP", "/api/integracion/abastecimiento (WSDL en ?wsdl)", "AbastecimientoSoapEndpoint")],
}


PARA_QUE = {   # (servicio, ruta, tópico o clase de la tarea): para qué es; la guía se niega a salir si falta uno
    ("identidad", "POST /api/auth/token"): "V01: inicio de sesión; devuelve el JWT con el rol, la sucursal y el menú",
    ("identidad", "POST /api/usuarios"): "Alta de usuarios del administrador (correo único y regla de contraseña)",
    ("identidad", "GET /api/auth/jwks"): "La llave pública del JWT; la leen el gateway y los servicios",
    ("pedidos", "menu.actualizado"): "Mantiene la copia local del catálogo de platos",
    ("pedidos", "POST /api/pedidos"): "V03: registra el pedido de la mesa y publica pedido.registrado",
    ("pedidos", "GET /api/pedidos"): "V02 y «Pedidos del turno»: los pedidos del mesero en su turno",
    ("pedidos", "GET /api/pedidos/catalogo"): "V03: los platos activos de la sucursal con su precio",
    ("pedidos", "GET /api/pedidos/mesas"): "V03: las mesas de la sucursal",
    ("menu", "POST /api/platos"): "V06: crea el plato con su receta",
    ("menu", "GET /api/platos"): "Platos de la sucursal (la sucursal sale del JWT)",
    ("menu", "PUT /api/platos/{id}"): "Editar plato",
    ("menu", "PATCH /api/platos/{id}/desactivar"): "Desactivar: sale del catálogo del mesero",
    ("menu", "PATCH /api/platos/{id}/reactivar"): "Activar de nuevo",
    ("menu", "GET /api/platos/productos"): "V06 y V08: los productos por categoría",
    ("menu", "PUT /api/platos/{id}/imagen"): "V06: sube la imagen al almacén de objetos",
    ("menu", "GET /api/platos/{id}/imagen"): "La foto del plato (V03 y V06)",
    ("inventario", "ConsumoDiarioJob"): "Cierre del día: descuenta el consumo y publica inventario.stock-bajo",
    ("inventario", "menu.actualizado"): "Mantiene las recetas locales",
    ("inventario", "solicitud.registrada"): "Marca como atendida la alerta del insumo solicitado",
    ("inventario", "GET /api/inventario/alertas"): "V05: las alertas de stock bajo del jefe",
    ("inventario", "GET /api/inventario/insumos"): "V07: existencias con su porcentaje",
    ("inventario", "pedido.registrado"): "Suma el consumo pendiente del pedido",
    ("inventario", "POST /api/inventario/cierre"): "Botón «Cierre del día» de V07, solo para la demo",
    ("solicitudes", "cotizacion.enviada"): "Pasa a EN TRÁMITE las solicitudes cotizadas y publica solicitud.en-tramite",
    ("solicitudes", "orden.generada"): "Completa las solicitudes de la orden",
    ("solicitudes", "POST /api/solicitudes"): "V08: registra la solicitud y publica solicitud.registrada",
    ("solicitudes", "GET /api/solicitudes?solicitante=…"): "V10: las solicitudes del jefe",
    ("solicitudes", "GET /api/solicitudes"): "V11 y V12: todas, con los filtros de HU-07, de a 25",
    ("solicitudes", "GET /api/solicitudes/{numero}"): "V12: el detalle con sus productos",
    ("cotizaciones", "POST /api/cotizaciones"): "V13: genera, arma el PDF y publica cotizacion.enviada",
    ("cotizaciones", "GET /api/cotizaciones/{numero}/pdf"): "V14: el PDF de la cotización",
    ("cotizaciones", "GET /api/cotizaciones"): "V15 y V17: la lista con filtros",
    ("cotizaciones", "GET /api/cotizaciones/pendientes"): "V16: las que esperan valores",
    ("cotizaciones", "PUT /api/cotizaciones/{numero}/valores"): "V18: los valores del proveedor",
    ("cotizaciones", "POST /api/cotizaciones/{numero}/validacion"): "V19: la cadena de reglas; publica cotizacion.validada",
    ("cotizaciones", "GET /api/cotizaciones?estado=OPCIONADA"): "V20: las opcionadas",
    ("cotizaciones", "GET /api/cotizaciones/{numero}"): "V20 y ms-ordenes: el detalle",
    ("cotizaciones", "POST /api/cotizaciones/{numero}/reenvio"): "Reenviar una cotización pendiente de precio",
    ("cotizaciones", "orden.generada"): "Pasa la cotización a CON ORDEN",
    ("cotizaciones", "POST /api/proveedores"): "Registrar proveedor (NIT único)",
    ("cotizaciones", "GET /api/proveedores"): "Lista; V13 usa solo los activos",
    ("cotizaciones", "GET /api/proveedores/{id}"): "Detalle del proveedor",
    ("cotizaciones", "PUT /api/proveedores/{id}"): "Editar proveedor",
    ("cotizaciones", "PATCH /api/proveedores/{id}/inactivar"): "Inactivar proveedor",
    ("ordenes", "POST /api/ordenes-compra"): "V21: genera la orden y publica orden.generada",
    ("ordenes", "GET /api/ordenes-compra"): "V23: las órdenes",
    ("ordenes", "GET /api/ordenes-compra/{numero}/pdf"): "V22 y V23: el PDF de la orden",
    ("ordenes", "PATCH /api/ordenes-compra/{numero}/finalizar"): "V23: «Marcar como recibida», pasa a FINALIZADA",
    ("notificaciones", "POST /api/notificaciones/destinatarios"): "Destinatarios de alertas: agregar",
    ("notificaciones", "GET /api/notificaciones/destinatarios"): "Destinatarios de alertas: la libreta",
    ("notificaciones", "inventario.stock-bajo"): "Correo de stock bajo y alarma en vivo al jefe",
    ("notificaciones", "solicitud.registrada"): "Correo de solicitud registrada (V09)",
    ("notificaciones", "solicitud.en-tramite"): "Correo al jefe: su solicitud está en trámite",
    ("notificaciones", "cotizacion.enviada"): "Correo al proveedor con el PDF (V14)",
    ("notificaciones", "orden.generada"): "Correo al proveedor con la orden (V22)",
    ("notificaciones", "tendencia.detectada"): "Correo de promociones a los jefes de todas las sucursales",
    ("notificaciones", "GET /api/notificaciones/envios"): "V14: la traza de envíos de un recorrido (?correlacion=…)",
    ("notificaciones", "notificaciones.dlq"): "Vuelve a procesar los envíos fallidos cada 5 minutos, hasta tres veces",
    ("notificaciones", "/api/notificaciones/alarmas"): "V05: la alarma en vivo (token en ?access_token=)",
    ("integracion", "GET /interno/precios/{productoId}"): "El precio de referencia para ms-cotizaciones; no se publica en el gateway",
    ("integracion", "/api/integracion/abastecimiento (WSDL en ?wsdl)"): "El servicio SOAP del restaurante hacia afuera",
    ("administracion", "PUT /api/admin/parametros/{clave}"): "Parámetros y tema: guardar",
    ("administracion", "GET /api/admin/parametros"): "Parámetros y tema: leer",
    ("administracion", "GET /api/admin/parametros/tema"): "El tema que la SPA aplica al iniciar sesión (cualquier rol)",
    ("administracion", "GET /api/admin/auditoria"): "Auditoría por rango de fechas",
    ("administracion", "GET /api/admin/logs"): "Logs de errores, desde Elasticsearch",
    ("administracion", "auditoria.evento"): "Escribe cada evento en la bitácora, sin repetirlo",
    ("administracion", "RefrescoParametros"): "Recarga ParametrosSistema de la base en cada réplica",
    ("analitica", "pedido.registrado"): "Hecho de demanda (producto y sucursal)",
    ("analitica", "cotizacion.validada"): "Hecho de precios (proveedor con mejores precios)",
    ("analitica", "inventario.stock-bajo"): "Hecho de stock bajo",
    ("analitica", "solicitud.registrada"): "Hecho de solicitud",
    ("analitica", "orden.generada"): "Hecho de compra",
    ("analitica", "GET /api/bam/promociones"): "Promociones sugeridas al jefe",
    ("analitica", "PATCH /api/bam/promociones/{id}/aceptar"): "Aceptar una promoción",
    ("analitica", "PATCH /api/bam/promociones/{id}/descartar"): "Descartar una promoción",
    ("analitica", "GET /api/bam/kpis"): "Tablero BAM: los tres indicadores con su ranking",
    ("analitica", "TendenciasJob"): "Consulta las redes, cruza con la demanda y publica tendencia.detectada",
    ("analitica", "menu.actualizado"): "Copia local de los nombres de los platos",
}


def para_que(s, tipo, que, clase):
    clave = (s, clase) if tipo == "Tarea" else (s, que)
    assert clave in PARA_QUE, f"falta «para qué» de {clave}"
    return PARA_QUE[clave]


# ───────────────────────── piezas de la página ─────────────────────────
def visor_recorte(nombre, rec, titulo):
    vid = "vm-" + nombre
    v = visor(vid, f"img/microservicios/{nombre}.png", rec["ancho"], rec["alto"],
              [(n, x, y) for n, _, x, y in rec["pines"]], rec["d"], titulo)
    filas = "".join(f'<tr id="{vid}-n{n}"><td class="num"><button class="pin" data-visor="{vid}" data-n="{n}">{n}</button>'
                    f"</td><td>{E(nom)}</td></tr>" for n, nom, _, _ in rec["pines"])
    pag = rec["pagina"]
    t = (f'<table class="pines"><thead><tr><th class="c-num">N.º</th><th>Qué marca (página {pag}, los mismos números de '
         f'la guía de los diagramas)</th></tr></thead><tbody>{filas}</tbody></table>') if filas else ""
    return v + t


def lista_clases(nombres, principales):
    return ", ".join(f"<b>{c(n)}</b>" if n in principales else c(n) for n in nombres)


def seccion_servicio(n, s, datos):
    S = SERVICIOS[s]
    nuc = datos["nucleo"][s]
    total = sum(len(v) for v in nuc.values())
    todas = {x for v in nuc.values() for x in v}
    spring = datos["spring"][s]
    todas_spring = {x for v in spring.values() for x in v}
    faltan = [x for x in S["principales"] if x not in todas and x not in todas_spring]
    assert not faltan, f"ms-{s}: clases que no existen {faltan}"
    pref = [p for _, serv, p in datos["rutas"] if serv == f"ms-{s}"]
    prefijos = ", ".join(c(p.split(",")[0]) for p in pref)
    publica = [t for t in TOPICOS if s in datos["pub"].get(t, [])]
    consume = [t for t in TOPICOS if any(x == s for x, _ in datos["cons"].get(t, []))]
    tablas = datos["tablas"][s]
    vents = datos["vent"][s]
    chips = "".join(f'<a class="vchip" href="img/plataforma/{img}.png" target="_blank" rel="noopener">'
                    f'<img src="img/plataforma/{img}.png" alt="" loading="lazy">'
                    f'<span>{E(t)}{" (en parte)" if parte else ""}</span></a>' for t, img, parte in vents)
    ent = datos["entradas"][s] + EXTRA_ENTRADAS.get(s, [])
    filas_ent = [[E(t), c(q), E(para_que(s, t, q, k)), c(k)] for t, q, k in ent]

    def destinos(top):
        return ", ".join(f"ms-{x}" for x, _ in datos["cons"].get(top, [])) or "—"

    pub_html = "<br>".join(f"{c(t)} → {E(destinos(t))}" for t in publica) or "Nada"
    cons_html = "<br>".join(f"{c(t)} ← {E(', '.join('ms-' + x for x in datos['pub'].get(t, [])) or 'ms-notificaciones')}"
                            for t in consume) or "Nada"
    if s == "notificaciones":
        pub_html += f"<br>{c('notificaciones.dlq')} → ms-notificaciones (los envíos que fallaron)"
        cons_html += f"<br>{c('notificaciones.dlq')} ← ms-notificaciones (los vuelve a procesar)"

    capas_nucleo = []
    for capa, nombres in nuc.items():
        capas_nucleo.append(f"<li><span class=\"suave\">{E(capa)}/</span> {lista_clases(nombres, S['principales'])}</li>")
    jpa = [x for x in spring["salida"] if x.endswith("Entidad") or x.endswith("JpaRepository")]
    sal = [x for x in spring["salida"] if x not in jpa]
    rec = datos["recortes"][s]
    return f"""
<p class="frase">«{E(S['frase'])}»</p>
<div class="meta"><span>🚪 Gateway: {prefijos}</span><span>🗄️ Base <code>ms_{s}</code></span>
<span>📦 {total} clases en el núcleo</span><span>⚖️ {E(datos['replicas'].get(s, '—'))} réplicas en el despliegue</span>
<span>🗺️ Página {rec['pagina']} del .drawio</span></div>
<p>{S['responsabilidad']}</p>
<dl class="ficha">
<dt>Publica</dt><dd>{pub_html}</dd>
<dt>Consume</dt><dd>{cons_html}</dd>
<dt>Base y tablas</dt><dd>{", ".join(c(t) for t in tablas)}</dd>
<dt>Síncrono</dt><dd>{S['sincrono']}</dd>
<dt>Patrones</dt><dd>{et(*S['patrones'])}</dd>
</dl>
<h3>Ventanas que atiende</h3>
<div class="vchips">{chips}</div>
<h3>Lo que entra al servicio</h3>
{tabla(["Tipo", "Ruta, tópico o cuándo corre", "Para qué", "Clase"], filas_ent, [None, None, None, None], ' class="entradas"')}
<h3>Sus clases</h3>
<div class="capas">
<div class="nucleo"><h4>Núcleo ({total} clases, Java sin frameworks)</h4><ul class="clases">{"".join(capas_nucleo)}</ul></div>
<div><h4>Adaptadores de entrada (Spring)</h4><p>{lista_clases(spring['entrada'], S['principales'])}</p></div>
<div><h4>Adaptadores de salida (Spring)</h4><p>{lista_clases(sal, S['principales'])}</p>
<p class="suave">Además, {len(jpa)} entidades y repositorios JPA internos de sus adaptadores.</p></div>
<div><h4>Configuración</h4><p>{lista_clases(spring['configuracion'], S['principales'])}</p></div>
</div>
<p class="suave">En negrita, las que conviene nombrar. Los adaptadores del núcleo tienen la misma forma que los de Spring;
el servicio usa los de Spring.</p>
<div class="caida"><b>Si se cae:</b> {S['caida']}</div>
<p>{S['notas']}</p>
<h3>Su paquete en el diagrama de clases</h3>
{visor_recorte(s, rec, f"Paquete ms-{s}")}
"""


def matriz(datos):
    cab = "".join(f"<th><span>{E(t).replace('.', '.<wbr>').replace('-', '-<wbr>')}</span></th>" for t in TOPICOS)
    filas = []
    for s in ORDEN:
        celdas = []
        for t in TOPICOS:
            p = s in datos["pub"].get(t, []) or (s == "notificaciones" and t == "notificaciones.dlq")
            k = any(x == s for x, _ in datos["cons"].get(t, []))
            txt = "P·C" if p and k else "P" if p else "C" if k else ""
            celdas.append(f'<td class="{"p" if p else ""}{"c" if k else ""}">{txt}</td>')
        filas.append(f"<tr><th>ms-{s}</th>{''.join(celdas)}</tr>")
    return (f'<div class="desliza"><table class="matriz"><thead><tr><th>Servicio</th>{cab}</tr></thead>'
            f'<tbody>{"".join(filas)}</tbody></table></div>')


def por_topico(datos):
    filas = []
    for t in TOPICOS:
        pub = datos["pub"].get(t, [])
        if t == "notificaciones.dlq":
            pub = ["notificaciones"]
        cons = datos["cons"].get(t, [])
        filas.append([c(t), E(", ".join("ms-" + x for x in pub)) if len(pub) < 5 else f"Los otros {len(pub)} servicios",
                      E(", ".join(f"ms-{x}" for x, _ in cons)), ", ".join(c(k) for _, k in cons)])
    return tabla(["Tópico", "Lo publica", "Lo consume", "Con qué clase"], filas, ["c-nombre", None, None, None])


def seccion_general(datos):
    filas = []
    for s in ORDEN:
        pref = [p.split(",")[0] for _, serv, p in datos["rutas"] if serv == f"ms-{s}"]
        filas.append([f"<b>ms-{s}</b>", E(SERVICIOS[s]["titulo"].split("· ")[1]), "<br>".join(c(p) for p in pref),
                      str(sum(len(v) for v in datos["nucleo"][s].values())), E(datos["replicas"].get(s, "—"))])
    filas.append(["<b>api-gateway</b>", "la única puerta de la SPA", "los 13", "—", E(datos["gw"])])
    filas.append(["<b>service-registry</b>", "dónde está cada instancia", "—", "—", E(datos["eu"])])
    rutas = [[c(p.split(",")[0] + "/**"), E(serv), c(i)] for i, serv, p in datos["rutas"]]
    assert len(rutas) == 13, f"el gateway tiene {len(rutas)} prefijos"
    n_aud = len(datos["pub"]["auditoria.evento"])
    assert "administracion" not in datos["pub"]["auditoria.evento"], "ms-administracion no debe publicar auditoria.evento"
    rc = datos["recortes"]["compartido"]
    return f"""
<p>La plataforma tiene <b>13 piezas propias</b>: los 11 microservicios, el API Gateway y el Service Registry (Eureka).
Cada microservicio es un subdominio con su base PostgreSQL (una base y un usuario por servicio: ninguno lee la base de
otro), su parte del núcleo y sus adaptadores Spring. Se hablan de dos formas: por <b>eventos en Kafka</b> casi siempre,
y por <b>REST síncrono</b> solo en dos casos de negocio; además, el gateway y cada servicio leen la llave pública de
ms-identidad para validar el JWT y la guardan.</p>
{tabla(["Componente", "Qué es", "Prefijo en el gateway", "Clases del núcleo", "Réplicas (página 5)"], filas,
       ["c-nombre", None, None, None, None])}
<p class="suave">Las réplicas son las mínimas y máximas del diagrama de despliegue (página 5), con autoescalado al 65 % de
CPU. En el equipo corre una réplica de cada uno.</p>

<h3>Quién publica y quién consume cada tópico</h3>
<p>Son los 10 tópicos de negocio más la cola de errores de ms-notificaciones. <b>P</b> = publica, <b>C</b> = consume.
La tabla se desliza a lo ancho en el teléfono.</p>
{matriz(datos)}
<p>La misma información por tópico, con la clase que escucha:</p>
{por_topico(datos)}
<div class="aviso"><b>auditoria.evento</b> lo publican los otros {n_aud} servicios y solo lo consume ms-administracion.
ms-administracion <b>no</b> lo publica: sus propias acciones las escribe directo en su bitácora
(<code>AdministracionService</code>). ms-notificaciones es el que más escucha (seis tópicos) y no publica nada de negocio.</div>

<h3>Los 13 prefijos del gateway</h3>
<p>Salen de <code>servicios/api-gateway/src/main/resources/application.yml</code>. Cada ruta va a <code>lb://ms-…</code>:
el gateway pregunta a Eureka por las instancias vivas y reparte entre ellas. ms-identidad y ms-cotizaciones tienen dos
prefijos cada uno; por eso son 13 para 11 servicios.</p>
{tabla(["Prefijo", "Servicio", "Id de la ruta"], rutas, ["c-nombre", None, None])}

<h3>Las llamadas síncronas</h3>
{tabla(["Desde", "Hacia", "Para qué", "Si el destino no responde"], [
    ["api-gateway", "los 11 servicios (<code>lb://</code>)", "Enrutar cada petición de la SPA", "503 a la SPA; las demás rutas siguen"],
    ["api-gateway y cada servicio", "ms-identidad, <code>GET /api/auth/jwks</code>", "La llave pública para validar el JWT", "Siguen con la llave que ya tienen"],
    ["ms-cotizaciones", "ms-integracion, <code>GET /interno/precios/{id}</code>", "El precio de referencia del Ministerio (V13)", "La cotización queda PENDIENTE DE PRECIO y se reenvía después"],
    ["ms-ordenes", "ms-cotizaciones, <code>GET /api/cotizaciones/{numero}</code>", "Leer la cotización OPCIONADA (V21)", "503 «No se pudo leer la cotización…»; se reintenta"],
], ["c-nombre", None, None, None])}
<p>Las dos llamadas de negocio entre servicios van por Eureka y el balanceador del cliente (no por el gateway), con el JWT de quien
hace la operación y su <code>X-Correlation-Id</code>; en producción, además, mTLS. Las salidas a sistemas que no son
servicios: el Ministerio (SOAP, ms-integracion), el servidor de correo (ms-notificaciones), la API de redes sociales
(ms-analitica), Elasticsearch (ms-administracion y los logs de todos), Redis y el almacén de objetos.</p>

<h3>Cómo está armado cada servicio</h3>
<div class="flujo">
<div class="paso"><b>Adaptador de entrada</b>REST, @KafkaListener o tarea programada (Spring)</div><span class="flecha">→</span>
<div class="paso"><b>Puerto de entrada</b>El caso de uso (núcleo)</div><span class="flecha">→</span>
<div class="paso"><b>Servicio de aplicación y dominio</b>Las reglas (núcleo, Java puro)</div><span class="flecha">→</span>
<div class="paso"><b>Puerto de salida</b>Interfaz del núcleo</div><span class="flecha">→</span>
<div class="paso"><b>Adaptador de salida</b>JPA, Kafka, S3, SOAP, SMTP… (Spring)</div>
</div>
<p>Todos usan la librería <b>compartido</b>, que trae lo que es igual en los 11: <code>EventoDominio</code> y
<code>EventPublisherPort</code>; <code>KafkaEventPublisher</code>, que no publica directo sino que guarda el evento en la
tabla <code>outbox</code> en la misma transacción que el cambio (<code>OutboxEventos</code>); <code>ReleOutbox</code>, que
los publica en Kafka en orden y los borra; <code>EventosProcesados</code>, que marca cada evento recibido en
<code>evento_procesado</code> para no procesarlo dos veces; la seguridad del servicio (<code>SeguridadServicio</code>,
<code>@PreAuthorize</code>), la correlación del recorrido y el envío de errores a Elasticsearch. Por eso los 11 tienen
tabla <code>outbox</code>, y los que consumen eventos tienen además <code>evento_procesado</code> (ms-identidad, ms-menu,
ms-ordenes y ms-integracion no consumen nada).</p>
{visor_recorte("compartido", rc, "Librería compartido")}
"""


def seccion_gateway(datos):
    rec = datos["recortes"]["gateway"]
    d29, d30 = DATOS_P["imagenes"]["d29_plataforma"], DATOS_P["imagenes"]["d30_eureka"]
    return f"""
<h3>API Gateway (Spring Cloud Gateway) · la única puerta</h3>
<p>Nginx sirve la SPA y pasa todo <code>/api/**</code> al gateway. El gateway es la <b>Facade</b> del sistema: la SPA
conoce una sola dirección y no sabe cuántos servicios hay detrás. En cada petición:</p>
<ol>
<li><b>Valida el JWT</b> (firma RS256 con la llave pública de ms-identidad y vigencia); solo <code>/api/auth/**</code> pasa
sin token. Para el WebSocket de la alarma acepta el token en <code>?access_token=</code>, porque el navegador no deja
poner la cabecera.</li>
<li><b>Limita la tasa</b> (<code>RequestRateLimiter</code>, DEC-104): un balde por usuario del JWT y, sin sesión, uno por
la IP real (<code>X-Real-IP</code>, que pone Nginx). 100 peticiones por segundo con ráfagas de 200; los baldes viven en
Redis, así que el límite vale igual con varias réplicas. Pasado el límite, 429.</li>
<li><b>Pone el <code>X-Correlation-Id</code></b> si no viene (<code>CorrelacionGateway</code>): con él se sigue un recorrido
en la auditoría, los logs y la traza de correos.</li>
<li><b>Enruta</b> por prefijo a <code>lb://ms-…</code>, a una instancia viva que le da Eureka.</li>
</ol>
<p>Clases: <code>SeguridadGateway</code>, <code>CorrelacionGateway</code> y <code>LimiteTasaGateway</code>, en
<code>servicios/api-gateway/</code>. Cada servicio vuelve a validar el token y el rol: el gateway no es la única barrera.</p>
<div class="caida"><b>Si se cae:</b> no entra ninguna petición de la SPA, aunque los servicios sigan vivos y los eventos
sigan corriendo por Kafka. Por eso el despliegue tiene {E(datos['gw'])} réplicas, una por zona, detrás de un balanceador
activo-activo.</div>
<h3>Service Registry (Eureka) · dónde está cada uno</h3>
<p>Cada servicio se registra al arrancar y manda un latido cada 30 segundos; el gateway y los clientes REST
(<code>LoadBalancerClient</code> en ms-cotizaciones y ms-ordenes) le preguntan dónde hay una instancia viva. Así una réplica
que arranca recibe tráfico sin cambiar ninguna configuración. Tarda un poco en propagarse: al encender la plataforma conviene
esperar unos 75 segundos antes de la primera cotización.</p>
<div class="caida"><b>Si se cae:</b> los clientes siguen con la última lista que descargaron y enrutan a las instancias
que ya conocían; lo que no ven son las réplicas que arranquen después. El despliegue tiene {E(datos['eu'])} réplicas en zonas distintas.</div>
<div class="par">
<figure class="vent"><a href="img/plataforma/d29_plataforma.png" target="_blank" rel="noopener"><img src="img/plataforma/d29_plataforma.png"
 alt="Página /plataforma/ con los 11 servicios" width="{d29['ancho']}" height="{d29['alto']}" loading="lazy"></a>
<figcaption><code>/plataforma/</code>: los 11 servicios con su prefijo y su estado en Eureka (el círculo es el del paso de la demo en la guía de la plataforma)</figcaption></figure>
<figure class="vent"><a href="img/plataforma/d30_eureka.png" target="_blank" rel="noopener"><img src="img/plataforma/d30_eureka.png"
 alt="Consola de Eureka" width="{d30['ancho']}" height="{d30['alto']}" loading="lazy"></a>
<figcaption>La consola de Eureka (<code>localhost:8761</code>): las instancias registradas</figcaption></figure>
</div>
<h3>En el diagrama de contenedores</h3>
{visor_recorte("gateway", rec, "Servidor web, SPA, gateway y Eureka en la página 3a")}
"""


def seccion_caidas(datos):
    filas = [[f"<b>ms-{s}</b>", SERVICIOS[s]["caida"]] for s in ORDEN]
    return f"""
<p>La regla general: <b>si un servicio cae, los demás siguen y sus eventos esperan en Kafka</b>. Lo que se pierde es la
ventana de ese servicio, no el trabajo de los otros. Los eventos que el servicio caído debía publicar no se pierden
porque ya estaban en su tabla <code>outbox</code>; los que debía recibir esperan en el broker y, al volver, los procesa una
sola vez (<code>evento_procesado</code>).</p>
{tabla(["Servicio", "Qué deja de funcionar y qué sigue"], filas, ["c-nombre", None])}
<div class="aviso">Solo hay dos dependencias síncronas de negocio entre servicios y las dos tienen salida (la llave pública
de ms-identidad ya está guardada en cada uno): sin ms-integracion la
cotización queda PENDIENTE DE PRECIO y se reenvía; sin ms-cotizaciones, ms-ordenes responde 503 y la directora
reintenta. Si se cae Kafka, las escrituras se confirman igual y los eventos esperan en la outbox hasta que vuelva.</div>
"""


def seccion_guion():
    return """
<p>Para explicar cualquier servicio en uno o dos minutos, en este orden. Cada paso responde una pregunta que el jurado
suele hacer, así que si interrumpen ya se tiene la respuesta en la mano.</p>
<div class="flujo">
<div class="paso"><b>1. Qué hace</b>La frase del servicio y su subdominio</div><span class="flecha">→</span>
<div class="paso"><b>2. Quién lo usa</b>Las ventanas y el rol</div><span class="flecha">→</span>
<div class="paso"><b>3. Cómo entra</b>Prefijo del gateway, endpoints y tópicos que escucha</div><span class="flecha">→</span>
<div class="paso"><b>4. Qué guarda</b>Su base y sus tablas</div><span class="flecha">→</span>
<div class="paso"><b>5. A quién avisa</b>Lo que publica y quién lo consume</div><span class="flecha">→</span>
<div class="paso"><b>6. Si se cae</b>Qué se detiene y qué sigue</div>
</div>
<p>Y se cierra con el patrón que tiene, señalando la clase en el diagrama de clases (el número de la guía de los diagramas).</p>
<div class="guion"><h3>🎤 Ejemplo completo: ms-cotizaciones (unos 2 minutos)</h3>
<p>«ms-cotizaciones es el corazón de compras: arma la solicitud de cotización, la envía al proveedor y la valida.</p>
<p>Lo usan dos roles. El auxiliar de compras genera la cotización en V13, desde una o varias solicitudes, y gestiona los
proveedores; la directora de compras registra los valores que le responde el proveedor en V18 y la valida en V19.</p>
<p>Entra por dos prefijos del gateway, <code>/api/cotizaciones</code> y <code>/api/proveedores</code>, y escucha un solo
tópico: <code>orden.generada</code>, para pasar la cotización a CON ORDEN.</p>
<p>Tiene su propia base, <code>ms_cotizaciones</code>, con los proveedores, las cotizaciones, sus ítems y la relación con
las solicitudes; ningún otro servicio la lee.</p>
<p>Al generar, publica <code>cotizacion.enviada</code>: ms-notificaciones manda el correo al proveedor con el PDF y
ms-solicitudes pasa las solicitudes a EN TRÁMITE. Al validar, publica <code>cotizacion.validada</code>, que alimenta el
tablero de ms-analitica. Y todo lo que hace queda en la auditoría.</p>
<p>Es uno de los dos servicios con una llamada síncrona: pide el precio de referencia a ms-integracion, que lo consulta
al Ministerio por SOAP. Si no responde, la cotización no se pierde: queda PENDIENTE DE PRECIO y el auxiliar la reenvía
después. Si se cae ms-cotizaciones, no se cotiza, pero los pedidos, el inventario y las solicitudes siguen.</p>
<p>En el diagrama de clases, la página 4c, se ven sus dos patrones: <code>GenerarCotizacionFacade</code>, una fachada
que coordina en un solo clic el precio, el PDF, el guardado y el evento, y la cadena de reglas
<code>ReglaRechazada</code> → <code>ReglaSospechosa</code> → <code>ReglaOpcionada</code>, Chain of Responsibility, donde
cada regla decide o le pasa a la siguiente.»</p></div>
<div class="guion"><h3>🎤 Si piden los once en un minuto</h3>
<p>«Son once servicios, uno por subdominio, cada uno con su base. Seis siguen el ciclo del abastecimiento: ms-menu tiene
la carta; ms-pedidos, lo que piden las mesas; ms-inventario descuenta cada noche y avisa cuando un insumo baja del
25 %; ms-solicitudes lleva lo que pide la cocina; ms-cotizaciones cotiza y valida; ms-ordenes genera la compra. Cinco
son transversales: ms-identidad emite el token y el menú de cada rol; ms-notificaciones manda los correos
certificados y la alarma; ms-integracion habla SOAP con el Ministerio; ms-administracion guarda los parámetros y la
auditoría; y ms-analitica arma el tablero y las promociones. Delante están el gateway, que es la única puerta, y
Eureka, que sabe dónde está cada instancia. Entre ellos se hablan por diez tópicos de Kafka, y solo hay dos llamadas
directas de negocio; además, cada servicio lee la llave pública de ms-identidad para validar el token.»</p></div>
"""


PREGUNTAS = [
    ("¿Por qué once servicios y no menos?",
     "Porque cada uno es un subdominio con su propio ciclo de datos y su propia carga. ms-pedidos escala de 4 a 20 réplicas "
     "en hora pico y ms-menu se queda en 2 a 4; si fueran uno solo habría que escalar todo junto. Los seis del ciclo siguen "
     "los datos de un rol al siguiente; los cinco transversales (identidad, notificaciones, integración, administración y "
     "analítica) sirven a todos."),
    ("¿Por qué una base por servicio? ¿No es duplicar datos?",
     "Para que ningún servicio dependa del esquema de otro: cada uno cambia su base sin romper a nadie. Lo que se duplica "
     "son copias pequeñas y de solo lectura: el catálogo en ms-pedidos, las recetas en ms-inventario y los hechos en "
     "ms-analitica llegan por eventos; los nombres de productos de ms-cotizaciones los manda la SPA al generar la cotización, "
     "y ms-ordenes los guarda al leerla por REST. En PostgreSQL cada servicio tiene "
     "su usuario y no puede conectarse a la base de otro."),
    ("¿Por qué hay llamadas síncronas si la arquitectura es de eventos?",
     "Solo dos de negocio, y en las dos el usuario necesita la respuesta en ese momento: el precio de referencia para armar la cotización "
     "y los datos de la cotización para armar la orden. Las dos tienen salida si el otro no responde (PENDIENTE DE PRECIO y el "
     "503 con reintento). Todo lo que puede pasar después, como el correo, la auditoría o el inventario, va por eventos. "
     "Aparte está la llave pública de ms-identidad: el gateway y cada servicio la leen por REST para validar el JWT y la "
     "guardan, así que no es una llamada por cada petición."),
    ("¿Qué pasa si Kafka entrega un evento dos veces?",
     "No se procesa dos veces. Cada consumidor guarda el id del evento en su tabla evento_procesado, en la misma transacción "
     "que su cambio, y descarta el que ya vio: un pedido.registrado repetido no se descuenta dos veces del inventario."),
    ("¿Y si el servicio se cae justo después de guardar y antes de publicar?",
     "Por eso está la outbox: el evento se guarda en la tabla outbox en la misma transacción que el cambio. Si la transacción "
     "se confirma, el evento está; el relé lo publica cuando puede. Si no se confirma, no hay ni cambio ni evento."),
    ("¿Por qué ms-administracion no publica auditoria.evento?",
     "Porque es el que lo consume: publicarse un evento a sí mismo solo agregaría un viaje por Kafka. Sus acciones van directo "
     "a su bitácora en la misma transacción."),
    ("¿Por qué ms-integracion tiene base si no guarda datos de negocio?",
     "Para la outbox de sus eventos de auditoría: como todos, publica a través de su tabla outbox para no perder eventos. Los "
     "precios no van en la base sino en la caché de Redis, que comparten sus réplicas."),
    ("¿Cómo sabe el gateway a qué réplica mandar?",
     "Le pregunta a Eureka. Cada réplica se registra al arrancar y manda un latido; el gateway enruta a lb://ms-… y el "
     "balanceador escoge una instancia viva. Una réplica que arranca recibe tráfico sin tocar la configuración."),
    ("Si el gateway valida el token, ¿para qué lo valida otra vez cada servicio?",
     "Defensa en profundidad: dentro de la red alguien podría llamar a un servicio directamente. Cada servicio comprueba la "
     "firma y el rol con @PreAuthorize, y toma el usuario y la sucursal del token, nunca del cuerpo de la petición."),
    ("¿Cómo llega la alarma al jefe si hay varias réplicas de ms-notificaciones?",
     "Por Redis (PD-01): la réplica que procesa inventario.stock-bajo publica en un canal de Redis y todas las réplicas lo "
     "escuchan; la que tiene abierto el WebSocket del jefe se la entrega. Si la conexión no está, V05 relee las alertas de "
     "ms-inventario al abrir."),
    ("¿Qué pasa si el correo falla?",
     "ms-notificaciones reintenta; si sigue fallando, deja el envío en la cola de errores notificaciones.dlq y la vuelve a "
     "procesar cada 5 minutos, hasta tres veces. La traza de cada intento queda en su tabla envio y V14 la muestra."),
    ("¿Dónde están los patrones de diseño?",
     "Facade y Chain of Responsibility en ms-cotizaciones; Factory Method y Composite en ms-identidad; Factory Method y "
     "Decorator en ms-notificaciones; Adapter y Proxy en ms-integracion; Singleton en ms-administracion; Strategy en "
     "ms-analitica; y Publish-Subscribe en todos, a través de compartido."),
]


def seccion_preguntas():
    return '<dl class="preguntas">' + "".join(f"<dt>{E(q)}</dt><dd>{E(r)}</dd>" for q, r in PREGUNTAS) + "</dl>"


CSS_LOCAL = """<style>
.frase{font-size:1.06rem;font-style:italic;border-left:4px solid var(--acento);padding:2px 0 2px 12px;margin:6px 0 10px}
dl.ficha{display:grid;grid-template-columns:max-content 1fr;gap:6px 14px;margin:12px 0;font-size:.95rem}
dl.ficha dt{color:var(--suave);font-weight:600}
dl.ficha dd{margin:0;overflow-wrap:anywhere}
.suave{color:var(--suave)}
p.suave{font-size:.88rem}
.caida{background:#fdecec;border-left:5px solid #c0392b;border-radius:8px;padding:10px 14px;margin:14px 0}
@media (prefers-color-scheme:dark){:root:not([data-theme="light"]) .caida{background:#3a2222}}
.vchips{display:flex;flex-wrap:wrap;gap:10px;margin:8px 0}
.vchip{display:flex;flex-direction:column;width:170px;text-decoration:none;color:var(--texto);border:1px solid var(--borde);
border-radius:8px;overflow:hidden;background:var(--papel);font-size:.82rem}
.vchip img{width:100%;height:96px;object-fit:cover;object-position:top left;display:block;border-bottom:1px solid var(--borde)}
.vchip span{padding:5px 7px}
ul.clases{margin:4px 0 0;padding-left:16px;font-size:.88rem}
ul.clases li{margin:3px 0;overflow-wrap:anywhere}
.capas p{font-size:.88rem;overflow-wrap:anywhere}
table.entradas th:nth-child(1){width:84px}table.entradas th:nth-child(2){width:32%}table.entradas th:nth-child(4){width:22%}
.desliza{overflow-x:auto;margin:10px 0;border:1px solid var(--borde);border-radius:8px}
table.matriz{min-width:780px;font-size:.85rem}
table.matriz th span{display:block;font-size:.75rem;line-height:1.2}
table.matriz th:first-child{width:128px}table.matriz th span{overflow-wrap:anywhere}
table.matriz td{text-align:center;font-weight:700}
table.matriz td.p{background:#fde3cf!important}table.matriz td.c{background:#dcebdc!important}
table.matriz td.pc{background:linear-gradient(135deg,#fde3cf 50%,#dcebdc 50%)!important}
@media (prefers-color-scheme:dark){:root:not([data-theme="light"]) table.matriz td.p{background:#4a3020!important}
:root:not([data-theme="light"]) table.matriz td.c{background:#21362a!important}
:root:not([data-theme="light"]) table.matriz td.pc{background:linear-gradient(135deg,#4a3020 50%,#21362a 50%)!important}}
table.matriz tbody th{text-align:left;white-space:nowrap;background:var(--papel)}
table.pines{margin:8px 0 14px}
figure.vent{margin:0}
figure.vent img{display:block;width:100%;height:auto;border:1px solid var(--borde);border-radius:6px}
figure.vent figcaption{font-size:.82rem;color:var(--suave);margin-top:4px}
.par{display:grid;grid-template-columns:1fr 1fr;gap:14px;margin:12px 0}
@media (max-width:700px){.par{grid-template-columns:1fr}dl.ficha{grid-template-columns:1fr;gap:2px}
dl.ficha dd{margin-bottom:8px}.vchip{width:calc(50% - 5px)}table.entradas thead{display:none}table.entradas tr{display:block;border-bottom:1px solid var(--borde);padding:6px 0}
table.entradas td{display:block;border:0;padding:1px 4px;background:none!important}
table.entradas td:nth-child(1){display:inline-block;font-weight:700;color:var(--suave);width:auto}
table.entradas td:nth-child(2){display:inline}table.entradas td:nth-child(4){font-size:.8rem}}
pre{white-space:pre-wrap;overflow-wrap:anywhere}
p code,li code,dd code,figcaption code{overflow-wrap:anywhere;word-break:break-word}
</style>"""


def main():
    nucleo = clases_nucleo()
    vent, n_vent = ventanas()
    assert n_vent == 33, f"la guía de la plataforma tiene {n_vent} ventanas"
    rep, gw, eu = replicas()
    datos = dict(nucleo=nucleo, tablas={s: tablas_flyway(s) for s in ORDEN}, entradas={s: entradas(s) for s in ORDEN},
                 cons=consumidores(), pub=publicadores(), spring={s: adaptadores_spring(s) for s in ORDEN},
                 rutas=rutas_gateway(), replicas=rep, gw=f"{gw}", eu=f"{eu}", vent=vent, recortes=recortes())
    for s in ORDEN:
        assert "outbox" in datos["tablas"][s], f"ms-{s} sin outbox"
        consume = any(x == s for v in datos["cons"].values() for x, _ in v)
        assert ("evento_procesado" in datos["tablas"][s]) == consume, f"ms-{s}: evento_procesado y consumo no cuadran"
    assert set(datos["cons"]) <= set(TOPICOS) and set(datos["pub"]) <= set(TOPICOS), "tópico desconocido"
    clases_total = sum(len(v) for m in nucleo.values() for v in m.values())

    s = [("general", "Vista general: 11 servicios, 10 tópicos (más la cola de errores) y 13 prefijos", seccion_general(datos)),
         ("gateway", "El API Gateway y el Service Registry", seccion_gateway(datos))]
    for i, srv in enumerate(ORDEN):
        s.append((f"ms-{srv}", SERVICIOS[srv]["titulo"], seccion_servicio(i + 3, srv, datos)))
    s.append(("caidas", "Qué pasa si se cae cada uno", seccion_caidas(datos)))
    s.append(("guion", "Guion sugerido para explicar un servicio", seccion_guion()))
    s.append(("preguntas", "Preguntas probables", seccion_preguntas()))

    indice = '<nav class="indice"><strong>Contenido</strong><ol>' + "".join(
        f'<li><a href="#{i}">{E(t)}</a></li>' for i, t, _ in s) + "</ol></nav>"
    uso = ('<div class="aviso"><strong>Cómo usar esta guía.</strong> Primero la vista general (la matriz de tópicos y los '
           'prefijos del gateway); después cada servicio con la misma ficha: qué hace, qué publica y consume, su base, '
           'sus ventanas, lo que entra, sus clases, qué pasa si se cae y su paquete del diagrama de clases con los mismos '
           'números de la guía de los diagramas. Las miniaturas de las ventanas se abren en tamaño real; cada ventana se '
           'explica en la guía de la plataforma. Al final, el guion para explicar un servicio y las preguntas probables.</div>')
    cuerpo = CSS_LOCAL + uso + indice + "".join(
        f'<section class="bloque" id="{i}"><h2>{n}. {E(t)}</h2>{h}</section>' for n, (i, t, h) in enumerate(s, 1))
    sub = ("Los 11 microservicios uno por uno, más el API Gateway y Eureka: responsabilidad, rutas, eventos, base, "
           "ventanas, clases, patrones y qué pasa si se cae. Los datos salen del código de <code>KonradGourmet/</code> "
           "(rama <code>stack-real</code>) al generar la guía.")
    (GUIAS / "guia_microservicios.html").write_text(pagina("Guía de los microservicios · Konrad Gourmet", sub, cuerpo, True),
                                                    encoding="utf-8")
    n_end = sum(1 for v in datos["entradas"].values() for t, _, _ in v if t == "REST")
    print(f"guia_microservicios.html: {len(s)} secciones, {clases_total} clases del núcleo, {n_end} endpoints REST, "
          f"{len(datos['rutas'])} prefijos, {sum(len(v) for v in datos['pub'].values())} publicaciones y "
          f"{sum(len(v) for v in datos['cons'].values())} suscripciones")
    for t in TOPICOS:
        print(f"  {t}: {datos['pub'].get(t, [])} → {[x for x, _ in datos['cons'].get(t, [])]}")


if __name__ == "__main__":
    main()
