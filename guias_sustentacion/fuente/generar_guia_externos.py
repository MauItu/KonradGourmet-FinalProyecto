"""Genera guias_sustentacion/guia_externos.html: lo que está fuera de la plataforma (DEC-108, DEC-109, DEC-113).

Lo que en el equipo reemplaza a un sistema externo (Mailpit por el servidor de correo certificado, WireMock por el
Ministerio de Comercio y por la API de las redes sociales), nuestro servicio SOAP y las consolas de apoyo (almacén de
objetos, Elasticsearch, Kafka, Redis, PostgreSQL y Eureka): qué reemplaza cada uno, dónde está en los diagramas, quién
lo usa, cómo se abre, qué mostrar y qué decir.

Las salidas de la terminal y las imágenes salen de img/externos/, que deja imagenes_externos.py en la instancia kg-demo;
lo demás se lee al generar de la configuración (docker-compose.yml, los mappings de WireMock, los application.yml) y del
código (los decoradores del correo, el Proxy de precios, los adaptadores del almacén y de Elasticsearch). Se niega a
generar si algo no cuadra. Los recortes de las páginas 2 y 5 necesitan Pillow. Uso, desde FinalProyecto:
    mockups_nuevos/.venv/bin/python documentos/guias_sustentacion/fuente/generar_guia_externos.py
"""
import json
import re
import sys
from pathlib import Path

AQUI = Path(__file__).resolve().parent
sys.path.insert(0, str(AQUI))
from estilo import E, pagina, visor              # noqa: E402
import contenido_diagramas as CD                 # noqa: E402
import generar_guia_plataforma as GP             # noqa: E402
import generar_guia_microservicios as GM         # noqa: E402

GUIAS = AQUI.parent
KG = GUIAS.parent.parent / "KonradGourmet"
PLAT = KG / "plataforma"
SERV = KG / "servicios"
NUCLEO = KG / "src/co/edu/konradlorenz"
IMG_X = GUIAS / "img" / "externos"
IMG_D = GUIAS / "img" / "diagramas"
DX = json.loads((IMG_X / "datos.json").read_text(encoding="utf-8"))
DP = GP.D
SAL, HE = DX["salidas"], DX["hechos"]
HIS = HE["historia"]
COT1, COT2 = HIS["cotizaciones"]
SOL, OC = HIS["solicitud"], HIS["orden"]
COMPOSE_TXT = (PLAT / "docker-compose.yml").read_text(encoding="utf-8")
ATAJOS = ("kg() { COMPOSE_PROJECT_NAME=kg-demo docker compose -f KonradGourmet/plataforma/docker-compose.yml \"$@\"; }\n"
          "kgs3() { kg run --rm --no-deps --entrypoint aws s3-bucket --endpoint-url http://s3:8333 \"$@\"; }")
fallas = []


def exige(cond, texto):
    if not cond:
        fallas.append(texto)


def leer(ruta):
    return Path(ruta).read_text(encoding="utf-8")


def c(t):
    return f"<code>{E(t)}</code>"


def et(*ts):
    return "".join(f'<span class="etiqueta">{t}</span>' for t in ts)


def tabla(cabeza, filas, clases=None, extra=""):
    th = "".join(f'<th{f" class={clases[i]}" if clases and clases[i] else ""}>{E(x)}</th>' for i, x in enumerate(cabeza))
    cuerpo = "".join("<tr>" + "".join(f"<td>{x}</td>" for x in f) + "</tr>" for f in filas)
    return f'<table{extra}><thead><tr>{th}</tr></thead><tbody>{cuerpo}</tbody></table>'


def pesos(n):
    return "$" + f"{n:,.0f}".replace(",", ".")


# ───────────────────────── salidas de la terminal ─────────────────────────
def terminal(clave, recorte=None, nota=""):
    """El comando, tal como se escribe, y lo que respondió en kg-demo (recortado si es largo)."""
    s = SAL[clave]
    salida = s["salida"]
    if recorte and len(salida.split("\n")) > recorte:
        lineas = salida.split("\n")
        salida = "\n".join(lineas[:recorte]) + f"\n… ({len(lineas) - recorte} líneas más)"
    return (f'<div class="term"><div class="cmd"><span>$</span> {E(s["comando"])}</div>'
            f'<pre class="sal">{E(salida) if salida else "(no imprime nada)"}</pre>'
            f'{f"<p class=nota>{nota}</p>" if nota else ""}</div>')


# ───────────────────────── imágenes ─────────────────────────
def figura(carpeta, datos, nombre, alt, pie=""):
    i = datos["imagenes"][nombre]
    return (f'<figure class="vent"><a href="img/{carpeta}/{nombre}.png" target="_blank" rel="noopener">'
            f'<img src="img/{carpeta}/{nombre}.png" alt="{E(alt)}" width="{i["ancho"]}" height="{i["alto"]}" loading="lazy"></a>'
            f'{f"<figcaption>{pie}</figcaption>" if pie else ""}</figure>')


def con_pines(carpeta, datos, nombre, alt, textos):
    i = datos["imagenes"][nombre]
    pines = i["pines"]
    exige(len(pines) == len(textos), f"{nombre}: {len(pines)} círculos y {len(textos)} textos")
    vid = "v-" + nombre
    filas = "".join(
        f'<tr id="{vid}-n{n}"><td class="num"><button class="pin" data-visor="{vid}" data-n="{n}">{n}</button></td>'
        f"<td>{t}</td></tr>" for (n, _, _), t in zip(pines, textos))
    return (visor(vid, f"img/{carpeta}/{nombre}.png", i["ancho"], i["alto"], [tuple(p) for p in pines], i["diametro"], alt)
            + f'<table class="pines"><thead><tr><th class="c-num">N.º</th><th>Qué señalar</th></tr></thead><tbody>{filas}</tbody></table>')


# ───────────────────────── los diagramas (números de la guía 1) ─────────────────────────
def num(clave, cid):
    for n, el in enumerate(CD.DIAGRAMAS[clave]["elementos"], 1):
        if el[0] == cid:
            return n
    fallas.append(f"la página {clave} no tiene {cid} en la guía de los diagramas")
    return 0


def donde(*pares):
    """«p. 2 · n.º 8; p. 3a · n.º 17» a partir de (página, celda)."""
    return "; ".join(f"p. {p} · n.º {num(p, cid)}" for p, cid in pares)


PAGINAS = {   # nombre: (página, recorte (x0, y0, x1, y1) en píxeles de la imagen o None, celdas que se explican)
    "pagina2": ("2", (80, 490, 2070, 1040), ["sys", "v14", "v15", "v16", "v17", "v22"]),
    "pagina3a": ("3a", None, ["c3a_x_sistemas", "c3a_x_ministerio", "c3a_x_correo", "c3a_x_redes", "c3a_registry",
                              "c3a_elk", "c3a_db_objetos", "c3a_ms_integracion", "c3a_db_integracion", "c3a_bus"]),
    "pagina5": ("5", (400, 150, 1880, 1120), ["v13", "v19", "v37", "v24", "v25", "v26"]),
}


def recortes():
    from PIL import Image
    diag = GM._diagramas()
    res = {}
    for nombre, (clave, caja, celdas) in PAGINAS.items():
        ax, ay, absoluta, d, cent = GM._ancla(diag, clave)
        with Image.open(IMG_D / f"{clave}.png") as im:
            x0, y0, x1, y1 = caja or (0, 0, im.size[0], im.size[1])
            if caja:
                im.crop(caja).save(IMG_X / f"{nombre}.png", optimize=True)
        quiero = {num(clave, cid) for cid in celdas}
        pines = [(n, round(cx - ax - x0), round(cy - ay - y0), nom) for n, nom, cx, cy in cent if n in quiero]
        for n, x, y, nom in pines:
            exige(0 <= x <= x1 - x0 and 0 <= y <= y1 - y0, f"{nombre}: el número {n} queda fuera del recorte")
        exige(len(pines) == len(celdas), f"{nombre}: faltan números")
        ruta = f"img/externos/{nombre}.png" if caja else f"img/diagramas/{clave}.png"
        res[nombre] = dict(clave=clave, ruta=ruta, ancho=x1 - x0, alto=y1 - y0, d=d, pines=pines)
    return res


def visor_pagina(r, textos):
    vid = "v-" + r["ruta"].split("/")[-1].split(".")[0]
    filas = "".join(
        f'<tr id="{vid}-n{n}"><td class="num"><button class="pin" data-visor="{vid}" data-n="{n}">{n}</button></td>'
        f"<td><b>{E(nom)}</b>. {textos[n]}</td></tr>" for n, _, _, nom in r["pines"])
    exige(set(textos) == {p[0] for p in r["pines"]}, f"página {r['clave']}: los textos no cuadran con los números")
    return (visor(vid, r["ruta"], r["ancho"], r["alto"], [(n, x, y) for n, x, y, _ in r["pines"]], r["d"],
                  f"Página {r['clave']} del .drawio") +
            f'<table class="pines"><thead><tr><th class="c-num">N.º</th><th>Qué es y qué lo hace en el equipo</th></tr>'
            f"</thead><tbody>{filas}</tbody></table>")


# ───────────────────────── lo que se lee de la configuración y del código ─────────────────────────
def configuracion():
    cfg = {}
    # Puertos publicados: solo la aplicación, Mailpit y Eureka.
    puertos = re.findall(r'^\s+- "(\d+:\d+)"', COMPOSE_TXT, re.M)
    cfg["puertos"] = puertos
    exige(sorted(puertos) == ["8025:8025", "8080:80", "8761:8761"], f"el compose publica {puertos}")
    imagen = lambda s: re.search(rf"^  {s}:\n(?:    .*\n)*?    image: (\S+)", COMPOSE_TXT, re.M).group(1)
    cfg["imagenes"] = {s: imagen(s) for s in ("correo", "ministerio", "redes", "s3", "elasticsearch", "kafka", "redis", "postgres")}
    cfg["tendencias_local"] = re.search(r'TENDENCIAS: "([^"]+)"', COMPOSE_TXT).group(1)
    cfg["redes_url"] = re.search(r"REDES_URL: (\S+)", COMPOSE_TXT).group(1)
    cfg["redes_token"] = re.search(r"REDES_TOKEN: (\S+)", COMPOSE_TXT).group(1)
    cfg["espera_cola"] = re.search(r"ESPERA_COLA_ERRORES: (\S+)", COMPOSE_TXT).group(1)
    # Ministerio (WireMock): un mapping por producto, con su demora, y el SOAP Fault para los demás.
    precios = []
    for f in sorted((PLAT / "infra/ministerio/mappings").glob("precio-*.json")):
        m = json.loads(leer(f))
        prod = re.search(r"text\(\)\)='(\d+)'", m["request"]["bodyPatterns"][0]["matchesXPath"]).group(1)
        nombre = re.search(r": (\w+) \(producto", m["name"]).group(1)
        valor = float(re.search(r"<precio>([\d.]+)</precio>", m["response"]["body"]).group(1))
        precios.append((int(prod), nombre, valor, m["response"]["fixedDelayMilliseconds"]))
    cfg["precios"] = precios
    fault = json.loads(leer(PLAT / "infra/ministerio/mappings/sin-precio.json"))
    cfg["fault"] = (fault["response"]["status"], fault["response"]["fixedDelayMilliseconds"])
    exige({p[3] for p in precios} == {300} and cfg["fault"] == (500, 300), "las demoras del Ministerio no son 300 ms")
    exige(precios[0][:3] == (1, "Res", 20000.0), "el precio de la res no es 20000")
    # Redes (WireMock): con el token y la consulta de RedesSocialesAdapter, las tendencias; sin token, 401.
    t = json.loads(leer(PLAT / "infra/redes/mappings/tendencias.json"))
    st = json.loads(leer(PLAT / "infra/redes/mappings/sin-token.json"))
    cfg["redes"] = dict(token=t["request"]["headers"]["Authorization"]["equalTo"],
                        consulta={k: v["equalTo"] for k, v in t["request"]["queryParameters"].items()},
                        tendencias=[(x["producto"], x["menciones"]) for x in t["response"]["jsonBody"]["tendencias"]],
                        demora=t["response"]["fixedDelayMilliseconds"], sin_token=st["response"]["status"],
                        ruta=t["request"]["urlPath"])
    exige(cfg["redes"]["token"] == f"Bearer {cfg['redes_token']}", "el token de las redes no es el del compose")
    exige(cfg["redes_url"].endswith(cfg["redes"]["ruta"]), "REDES_URL no apunta a la ruta del mapping")
    adaptador = leer(NUCLEO / "analitica/adaptadores/salida/RedesSocialesAdapter.java")
    exige("?pais=CO&categoria=gastronomia&periodo=semana" in adaptador and cfg["redes"]["consulta"] ==
          {"pais": "CO", "categoria": "gastronomia", "periodo": "semana"}, "la consulta de RedesSocialesAdapter cambió")
    # Correo: la cadena de decoradores, las cabeceras, el remitente, los reintentos y la cola de errores.
    conf = leer(SERV / "ms-notificaciones/src/main/java/co/edu/konradlorenz/notificaciones/configuracion/ConfiguracionNotificaciones.java")
    exige("new CorreoCertificadoDecorator(new EstampaCronologicaDecorator(smtp))" in conf, "la cadena de decoradores cambió")
    sal = NUCLEO / "notificaciones/adaptadores/salida"
    exige('"X-Certificado", "SHA-256:"' in leer(sal / "CorreoCertificadoDecorator.java") and "formatHex(hash, 0, 8)" in
          leer(sal / "CorreoCertificadoDecorator.java"), "X-Certificado ya no es la huella SHA-256 de 8 bytes")
    exige('"X-Sello-Tiempo"' in leer(sal / "EstampaCronologicaDecorator.java"), "X-Sello-Tiempo cambió")
    yml = leer(SERV / "ms-notificaciones/src/main/resources/application.yml")
    cfg["remitente"] = re.search(r"remitente: (.+)", yml).group(1)
    cfg["reintentos"] = int(re.search(r"reintentos: (\d+)", yml).group(1))
    cfg["espera_reintento"] = int(re.search(r"espera-reintento-ms: (\d+)", yml).group(1))
    cfg["espera_cola_defecto"] = re.search(r"ESPERA_COLA_ERRORES:(\w+)", yml).group(1)
    cfg["canal"] = re.search(r"canal-alarmas: (\S+)", yml).group(1)
    exige(cfg["canal"] == "alarmas:stock-bajo", "el canal de alarmas cambió")
    # Redis: el prefijo y la vigencia del Proxy.
    mapa = leer(SERV / "ms-integracion/src/main/java/co/edu/konradlorenz/integracion/configuracion/MapaPreciosRedis.java")
    exige('PREFIJO = "precio-referencia:"' in mapa and "Duration.ofDays(1)" in mapa, "la caché de precios cambió")
    # Elasticsearch: el mismo índice en quien escribe y en quien lee.
    exige('INDICE = "logs-errores"' in leer(SERV / "compartido/src/main/java/co/edu/konradlorenz/compartido/infraestructura/AppenderElasticsearch.java")
          and 'INDICE = "logs-errores"' in leer(SERV / "ms-administracion/src/main/java/co/edu/konradlorenz/administracion/adaptadores/salida/ElasticsearchLogsAdapter.java"),
          "el índice de los logs cambió")
    # Almacén: los dos buckets.
    exige("for b in documentos-pdf imagenes-platos" in leer(PLAT / "infra/s3/crear-bucket.sh"), "los buckets cambiaron")
    # Cómo se cambia al sistema real: una variable de entorno por adaptador.
    cfg["variables"] = {
        "MINISTERIO_URL": re.search(r"\$\{MINISTERIO_URL:([^}]+)\}", leer(SERV / "ms-integracion/src/main/resources/application.yml")).group(1),
        "SMTP_HOST": re.search(r"\$\{SMTP_HOST:([^}]+)\}", yml).group(1),
        "REDES_URL": re.search(r"\$\{REDES_URL:([^}]+)\}", leer(SERV / "ms-analitica/src/main/resources/application.yml")).group(1),
        "S3_URL": re.search(r"\$\{S3_URL:([^}]+)\}", leer(SERV / "ms-cotizaciones/src/main/resources/application.yml")).group(1),
    }
    # El asunto de cada correo y el evento que lo dispara: cada texto tiene que estar en la rama de su evento.
    serv = leer(NUCLEO / "notificaciones/aplicacion/NotificacionService.java")
    ramas = dict(re.findall(r"instanceof (\w+) e\) \{(.*?)\n        \}", serv, re.S))
    for _, topico, clase, fragmento in ASUNTOS:
        exige(fragmento in ramas.get(clase, ""), f"NotificacionService ya no arma «{fragmento}» con {clase} ({topico})")
    exige(len(ramas) == len(ASUNTOS), f"NotificacionService atiende {len(ramas)} eventos")
    # Los atajos que usa la guía son los mismos con que imagenes_externos.py corrió cada comando.
    fuente = leer(AQUI / "imagenes_externos.py")
    exige("COMPOSE_PROJECT_NAME=kg-demo docker compose -f {COMPOSE}" in fuente and
          'kgs3() { kg run --rm --no-deps --entrypoint aws s3-bucket --endpoint-url http://s3:8333' in fuente,
          "los atajos de imagenes_externos.py no son los de la guía")
    return cfg


def comprobar_datos(cfg):
    g3 = DP["historia"]
    for k in ("pedido", "solicitud", "cotizaciones", "orden", "orden_total", "alarma", "correos"):
        exige(HIS[k] == g3[k], f"la historia de la guía 5 no coincide con la de la guía 3 en {k}")
    atrasos = [l.split()[-1] for l in SAL["k_atraso"]["salida"].split("\n")[1:] if l.strip()]
    exige(set(atrasos) <= {"0", "-"}, "ms-notificaciones tiene eventos pendientes")
    exige(len([l for l in SAL["k_evento"]["salida"].split("\n") if l.startswith("{")]) == 1, "orden.generada no tiene un solo evento")
    exige(HE["ministerio_llamadas"] == 1, "el Ministerio no recibió exactamente una llamada")
    exige(HE["redes_llamadas"][1] > HE["redes_llamadas"][0], "la cuenta de las redes no subió")
    exige(HE["dlq_despues"] == HE["dlq_antes"] + 1, "la cola de errores no recibió el evento")
    exige(HE["correos_al_final"] == len(HIS["correos"]) + 1, "hay correos repetidos")
    cc = HE["cotizacion_correo"]
    exige(cc["adjuntos"] == [f"{COT1['numero']}.pdf"], "el correo de la cotización no lleva su PDF")
    exige(SAL["c_huella"]["salida"] == cc["certificado"].split(":")[1], "la huella no es la de la cabecera")
    for k in ("m_cuenta", "m_wsdl", "m_precio", "m_fault", "r_llaves", "r_get", "r_ttl", "r_alarma", "r_limite", "n_cuenta",
              "n_cuenta2", "n_tendencias", "n_401", "s_401", "s_wsdl", "s_precio", "o_buckets", "o_pdf", "o_img", "k_topicos",
              "k_atraso", "k_evento", "k_dlq", "k_dlq2", "p_bases", "p_orden", "p_outbox", "p_ajena", "e_indices0", "e_error",
              "e_indices1", "c_huella", "x_pausa", "x_reanuda"):
        exige(k in SAL, f"falta la salida {k}")
    for c_ in HIS["correos"]:
        evento_de(c_["asunto"])
    for n in ("e01_cabeceras", "e02_promociones", "e03_logs_error", "e04_mailpit_reenvio"):
        exige((IMG_X / f"{n}.png").exists(), f"falta la imagen {n}")


ASUNTOS = [   # (asunto, tópico, clase del evento, texto que lo arma en NotificacionService)
    (r"Stock bajo: .+", "inventario.stock-bajo", "StockBajoEvent", '"Stock bajo: "'),
    (r"Solicitud SOL-\d+ registrada", "solicitud.registrada", "SolicitudRegistradaEvent", '" registrada\\nSu solicitud "'),
    (r"Solicitud SOL-\d+ en trámite", "solicitud.en-tramite", "SolicitudEnTramiteEvent", '" en trámite\\nSu solicitud "'),
    (r"Solicitud de cotización COT-\d+", "cotizacion.enviada", "CotizacionEnviadaEvent", '"Solicitud de cotización "'),
    (r"Orden de compra OC-\d+", "orden.generada", "OrdenGeneradaEvent", '"Orden de compra "'),
    (r"Promociones sugeridas", "tendencia.detectada", "TendenciaDetectadaEvent", '"Promociones sugeridas\\n'),
]


def evento_de(asunto):
    for patron, topico, _, _ in ASUNTOS:
        if re.fullmatch(patron, asunto):
            return topico
    fallas.append(f"no se sabe de qué evento sale «{asunto}»")
    return "?"


# ───────────────────────── secciones ─────────────────────────
QUIEN = {   # (página, celda) → qué lo hace en el equipo
    ("2", "sys"): "Todo lo que corre en <code>kg-demo</code>: la SPA, el gateway, Eureka y los once servicios con sus datos.",
    ("2", "v14"): "El contenedor <code>ministerio</code> (WireMock) responde el servicio web SOAP con el mismo WSDL. Solo lo llama ms-integracion.",
    ("2", "v15"): "El contenedor <code>correo</code> (Mailpit) recibe por SMTP lo que envía ms-notificaciones y lo muestra en <code>localhost:8025</code>.",
    ("2", "v16"): "El contenedor <code>redes</code> (WireMock) responde la API de tendencias con token. Solo la llama ms-analitica.",
    ("2", "v17"): "No se reemplaza: nuestro servicio SOAP corre de verdad en ms-integracion y se alcanza por el gateway en <code>localhost:8080/api/integracion/abastecimiento</code>, con token.",
    ("2", "v22"): "Es la dirección de correo del proveedor: su correo llega a la bandeja de Mailpit (<code>ventas@proveedorabc.com</code>).",
    ("3a", "c3a_x_sistemas"): "Se prueba con <code>curl</code> contra el WSDL por el gateway (sección 6).",
    ("3a", "c3a_x_ministerio"): "WireMock <code>ministerio</code> (sección 4).",
    ("3a", "c3a_x_correo"): "Mailpit (sección 3).",
    ("3a", "c3a_x_redes"): "WireMock <code>redes</code> (sección 5).",
    ("3a", "c3a_registry"): "Eureka, publicado en <code>localhost:8761</code> (sección 12).",
    ("3a", "c3a_elk"): "Elasticsearch, solo dentro de la red: la ventana «Logs de errores» o <code>kg exec elasticsearch curl</code> (sección 8).",
    ("3a", "c3a_db_objetos"): "SeaweedFS, solo dentro de la red: el PDF de V14 y V23 o <code>kgs3 s3 ls</code> (sección 7).",
    ("3a", "c3a_ms_integracion"): "La frontera: el único que habla SOAP hacia afuera y hacia adentro.",
    ("3a", "c3a_db_integracion"): "Redis, solo dentro de la red: <code>kg exec redis redis-cli</code> (sección 10).",
    ("3a", "c3a_bus"): "Kafka, solo dentro de la red: <code>kg exec kafka …</code> (sección 9).",
    ("5", "v13"): "Una sola instancia de Eureka en <code>localhost:8761</code>.",
    ("5", "v19"): "De la observabilidad, en el equipo está Elasticsearch con el índice <code>logs-errores</code>.",
    ("5", "v37"): "Por aquí salen las llamadas al Ministerio, al correo y a las redes: en el equipo van a los contenedores <code>ministerio</code>, <code>correo</code> y <code>redes</code>.",
    ("5", "v24"): "Un PostgreSQL con once bases separadas, una por servicio (sección 11).",
    ("5", "v25"): "Un Redis con la caché de precios, el canal de alarmas y el límite de tasa.",
    ("5", "v26"): "SeaweedFS con los buckets <code>documentos-pdf</code> e <code>imagenes-platos</code>.",
}


def seccion_mapa(cfg, rec):
    i = cfg["imagenes"]
    filas = [
        ["Mailpit · <code>correo</code>", "Servidor de correo certificado (SMTP/TLS)", donde(("2", "v15"), ("3a", "c3a_x_correo"), ("5", "v37")),
         "ms-notificaciones: <code>CorreoSmtpAdapter</code> envuelto por los decoradores", "Navegador: <code>localhost:8025</code>"],
        ["WireMock · <code>ministerio</code>", "Ministerio de Comercio, servicio web SOAP de precios", donde(("2", "v14"), ("3a", "c3a_x_ministerio")),
         "ms-integracion: <code>MinisterioComercioAdapter</code> y <code>MinisterioSoapClient</code>, detrás de <code>PrecioReferenciaCacheProxy</code>",
         "<code>kg exec ministerio curl …</code>"],
        ["WireMock · <code>redes</code>", "Redes sociales, API de tendencias (HTTPS/REST)", donde(("2", "v16"), ("3a", "c3a_x_redes")),
         "ms-analitica: <code>TendenciasJob</code> con <code>RedesSocialesAdapter</code>", "<code>kg exec redes curl …</code>"],
        ["Nuestro servicio SOAP", "Lo consumen otros sistemas (no se reemplaza)", donde(("2", "v17"), ("3a", "c3a_x_sistemas")),
         "ms-integracion: <code>AbastecimientoSoapEndpoint</code> (Apache CXF), por el gateway", "<code>curl localhost:8080/api/integracion/abastecimiento?wsdl</code> con token"],
        ["SeaweedFS · <code>s3</code>", "Almacén de objetos S3", donde(("3a", "c3a_db_objetos"), ("5", "v26")),
         "Escriben ms-cotizaciones, ms-ordenes y ms-menu; lee ms-notificaciones para adjuntar", "PDF en V14 y V23 · <code>kgs3 s3 ls</code>"],
        ["Elasticsearch", "Logs de error (ELK)", donde(("3a", "c3a_elk"), ("5", "v19")),
         "Escriben los once (<code>AppenderElasticsearch</code>); lee ms-administracion (<code>ElasticsearchLogsAdapter</code>)",
         "Ventana «Logs de errores» · <code>kg exec elasticsearch curl …</code>"],
        ["Kafka", "Broker de eventos", donde(("3a", "c3a_bus")), "Los once publican por la outbox y consumen", "<code>kg exec kafka /opt/kafka/bin/…</code>"],
        ["Redis", "Caché, canal de alarmas y límite de tasa", donde(("3a", "c3a_db_integracion"), ("5", "v25")),
         "ms-integracion, ms-notificaciones y el gateway", "<code>kg exec redis redis-cli …</code>"],
        ["PostgreSQL", "Una base por servicio", donde(("5", "v24")), "Cada servicio, solo la suya", "<code>kg exec postgres psql …</code>"],
        ["Eureka y <code>/plataforma/</code>", "Service Registry", donde(("3a", "c3a_registry"), ("5", "v13")),
         "El gateway y los once servicios", "Navegador: <code>localhost:8761</code> y <code>localhost:8080/plataforma/</code>"],
    ]
    t = tabla(["En el equipo", "Qué es en la arquitectura", "Dónde está (guía 1)", "Quién lo usa", "Cómo se abre"], filas,
              extra=' class="mapa"')
    textos = lambda nombre: {num(rec[nombre]["clave"], cid): QUIEN[(rec[nombre]["clave"], cid)] for cid in PAGINAS[nombre][2]}
    imgs = ", ".join(f"{c(k)} {E(v)}" for k, v in cfg["imagenes"].items())
    return f"""
<p>En la arquitectura (página 2) el sistema habla con cuatro sistemas externos: el Ministerio de Comercio, el servidor
de correo certificado, las redes sociales y los sistemas que consumen nuestro servicio SOAP. En el equipo, los tres
primeros los reemplazan contenedores que responden con el mismo contrato, y el cuarto es nuestro propio endpoint. Además
están las piezas de apoyo de la página 3a (almacén de objetos, Elasticsearch, Kafka, Redis, PostgreSQL y Eureka). Los
números son los de la guía de los diagramas.</p>
{t}
<p class="suave">Imágenes de los contenedores: {imgs}.</p>
<h3>Página 2 · el contexto</h3>
{visor_pagina(rec["pagina2"], textos("pagina2"))}
<h3>Página 3a · los contenedores</h3>
<p class="suave">La página completa: los números que interesan aquí están a los dos lados (los sistemas externos) y
abajo (Kafka, Redis y el almacén). Toca un número de la tabla para ir a él.</p>
{visor_pagina(rec["pagina3a"], textos("pagina3a"))}
<h3>Página 5 · el despliegue</h3>
{visor_pagina(rec["pagina5"], textos("pagina5"))}
"""


def seccion_abrir(cfg):
    pub = {"8080:80": "la aplicación (Nginx) y, por el gateway, nuestro SOAP", "8025:8025": "Mailpit", "8761:8761": "Eureka"}
    return f"""
<p>El compose solo publica tres puertos en el equipo; todo lo demás vive dentro de la red de Docker y no se le cambia
nada para la sustentación (DEC-113).</p>
{tabla(["Puerto", "Qué es"], [[c("localhost:" + p.split(":")[0]), pub[p]] for p in cfg["puertos"]])}
<p>Para lo que no se publica hay dos caminos:</p>
<ol>
<li><b>Las ventanas de la aplicación</b>, que ya lo usan: el PDF de V14 y de V23 sale del almacén de objetos; «Logs de
errores» lee Elasticsearch; la diferencia frente al precio estándar de V18 y V19 viene del Ministerio; la traza de envíos de
V14 dice si el correo salió.</li>
<li><b>La terminal</b>, con <code>docker compose exec</code> y la herramienta que ya trae cada contenedor (<code>curl</code>
en WireMock y Elasticsearch, <code>redis-cli</code>, <code>psql</code>, los scripts de Kafka) y, para el almacén,
<code>docker compose run</code> del contenedor que crea los buckets, que trae <code>aws</code> y las credenciales.</li>
</ol>
<p>Antes de entrar, en una terminal abierta en <code>FinalProyecto/</code>, se pegan estos dos atajos (duran lo que dure
la terminal):</p>
<pre class="atajos">{E(ATAJOS)}</pre>
<p class="suave">Todos los comandos de esta guía se corrieron así en <code>kg-demo</code> después del ensayo de la guía de la
plataforma, y lo que se ve debajo de cada uno es lo que respondieron. Las horas y los identificadores cambian en cada
ensayo; los números de la historia ({c(SOL)}, {c(COT1['numero'])}, {c(COT2['numero'])}, {c(OC)}) no.</p>
"""


def seccion_correo(cfg):
    corr = HE["correos"]
    textos = GP.correos_textos()
    filas = []
    for i, m in enumerate(corr, 1):
        ev = evento_de(m["asunto"])
        filas.append([str(i), E(m["asunto"]), E(m["para"]), c(ev), E(", ".join(m["adjuntos"])) or "—"])
    cc = HE["cotizacion_correo"]
    d24 = DP["imagenes"]["d24_mailpit"]
    vid = "v-d24x"
    pines = "".join(
        f'<tr id="{vid}-n{n}"><td class="num"><button class="pin" data-visor="{vid}" data-n="{n}">{n}</button></td><td>{t}</td></tr>'
        for (n, _, _), t in zip(d24["pines"], textos))
    bandeja = (visor(vid, "img/plataforma/d24_mailpit.png", d24["ancho"], d24["alto"], [tuple(p) for p in d24["pines"]],
                     d24["diametro"], "Bandeja de Mailpit") +
               f'<table class="pines"><thead><tr><th class="c-num">N.º</th><th>Correo</th></tr></thead><tbody>{pines}</tbody></table>')
    return f"""
<dl class="ficha">
<dt>Reemplaza a</dt><dd>El servidor de correo certificado (SMTP/TLS): {donde(("2", "v15"), ("3a", "c3a_x_correo"))}.</dd>
<dt>Quién lo usa</dt><dd>Solo ms-notificaciones, que escucha seis tópicos y manda un correo por cada evento. El remitente es
{c(cfg["remitente"])}.</dd>
<dt>Cómo se abre</dt><dd>{c("http://localhost:8025")} en el navegador: la bandeja, cada correo con sus pestañas
<i>Text</i>, <i>Headers</i> y <i>Raw</i>, y los adjuntos.</dd>
<dt>Patrones</dt><dd>{et("Decorator", "Factory Method (CreadorCorreo)", "Claim check", "Eventos", "Hexagonal")}</dd>
</dl>
<h3>Qué correos llegan y de qué evento sale cada uno</h3>
<p>Después del ensayo de la guía de la plataforma la bandeja tiene estos {len(corr)} correos, del más reciente al más antiguo:</p>
{tabla(["N.º", "Asunto", "Para", "Evento (tópico)", "Adjunto"], filas, ["c-num", None, None, None, None], extra=' class="correos"')}
{bandeja}
<h3>El certificado y el sello de tiempo: la cadena de decoradores</h3>
<div class="flujo">
<div class="paso"><b>NotificacionService</b>recibe el evento y arma el mensaje</div><span class="flecha">→</span>
<div class="paso"><b>CorreoCertificadoDecorator</b>pone <code>X-Certificado</code></div><span class="flecha">→</span>
<div class="paso"><b>EstampaCronologicaDecorator</b>pone <code>X-Sello-Tiempo</code></div><span class="flecha">→</span>
<div class="paso"><b>CorreoSmtpAdapter</b>descarga el PDF del almacén, lo adjunta y envía por SMTP</div><span class="flecha">→</span>
<div class="paso"><b>Mailpit</b>lo recibe</div>
</div>
<p>Los decoradores se arman en <code>ConfiguracionNotificaciones</code>:
<code>new CorreoCertificadoDecorator(new EstampaCronologicaDecorator(smtp))</code>. Cada uno agrega su cabecera y pasa el
mensaje al siguiente; el adaptador SMTP no sabe que existen. Agregar otra garantía (por ejemplo, una firma) es agregar
otro decorador.</p>
{con_pines("externos", DX, "e01_cabeceras", f"Cabeceras del correo de {COT1['numero']} en Mailpit", [
    f"<code>X-Certificado: {E(cc['certificado'])}</code>: los primeros 8 bytes (16 caracteres) de la huella SHA-256 del "
    "destinatario y el cuerpo. Si alguien cambia el texto o el destinatario, la huella ya no coincide.",
    f"<code>X-Sello-Tiempo: {E(cc['sello'])}</code>: la fecha y hora de Bogotá en que salió, con su zona horaria.",
    f"El adjunto {E(', '.join(cc['adjuntos']))}: el PDF que ms-cotizaciones guardó en el almacén; el evento solo llevaba su dirección."])}
<p>La huella se puede comprobar en vivo con el destinatario y el cuerpo del correo (pestaña <i>Text</i>):</p>
{terminal("c_huella", nota="Da lo mismo que la cabecera <code>X-Certificado</code>.")}
<h3>El correo de la orden y su PDF</h3>
<div class="par">
<div>{con_pines("plataforma", DP, "d25_mailpit_orden", "Correo de la orden en Mailpit", [f"El asunto, con {OC}.", f"El adjunto {OC}.pdf."])}</div>
<div>{figura("plataforma", DP, "d21_pdf_orden", "PDF de la orden", f"El PDF de {OC}: el mismo que abre V23 y que va adjunto.")}</div>
</div>
<p class="suave">El cuerpo del correo de la orden dice «total {HIS['orden_total']}» sin formato de pesos (observado, sin
corregir).</p>
<div class="dir"><b>Qué mostrar:</b> la bandeja, el correo de {c(COT1['numero'])} con la pestaña <i>Headers</i> (las dos
cabeceras) y su adjunto; si hay tiempo, la huella con <code>sha256sum</code>.</div>
"""


def seccion_ministerio(cfg):
    filas = [[str(p), E(n), pesos(v), f"{d} ms"] for p, n, v, d in cfg["precios"]]
    filas.append(["cualquier otro", "—", f"SOAP Fault ({cfg['fault'][0]})", f"{cfg['fault'][1]} ms"])
    return f"""
<dl class="ficha">
<dt>Reemplaza a</dt><dd>El servicio web SOAP del Ministerio de Comercio, que publica el precio estándar de cada producto:
{donde(("2", "v14"), ("3a", "c3a_x_ministerio"))}.</dd>
<dt>Quién lo usa</dt><dd>Solo ms-integracion. ms-cotizaciones le pide el precio de referencia al generar la cotización
(V13) y con él calcula la diferencia que ve la directora en V18 y V19.</dd>
<dt>Cómo se abre</dt><dd>No se publica: {c("kg exec ministerio curl …")} contra {c("localhost:8080")} dentro del contenedor.</dd>
<dt>Patrones</dt><dd>{et("Adapter", "Proxy (caché en Redis)", "SOA / SOAP", "Hexagonal")}</dd>
</dl>
<div class="flujo">
<div class="paso"><b>V13</b>el auxiliar genera la cotización</div><span class="flecha">→</span>
<div class="paso"><b>ms-cotizaciones</b>REST interno, por Eureka</div><span class="flecha">→</span>
<div class="paso"><b>PrecioReferenciaCacheProxy</b>¿está en Redis y tiene menos de un día?</div><span class="flecha">→</span>
<div class="paso"><b>MinisterioComercioAdapter</b>traduce al puerto</div><span class="flecha">→</span>
<div class="paso"><b>MinisterioSoapClient</b>SOAP</div><span class="flecha">→</span>
<div class="paso"><b>ministerio</b>WireMock</div>
</div>
<h3>Qué responde y con qué demora</h3>
<p>Un mapping por producto de la demo (<code>infra/ministerio/mappings/</code>), con {cfg["precios"][0][3]} ms de demora para
que se note cuándo responde la caché:</p>
{tabla(["Producto", "Nombre", "Precio por kilo", "Demora"], filas)}
{terminal("m_wsdl", recorte=12, nota="El contrato que sigue <code>MinisterioSoapClient</code>: <code>consultarPrecioEstandar(codigoProducto)</code> → <code>precio</code>. Su comentario dice que lo sirve el contenedor del entorno local: si se muestra en vivo, basta con la respuesta de la res.")}
{terminal("m_precio", nota="La res: 20000.0, en unos 300 ms.")}
{terminal("m_fault", nota="Un producto sin precio: SOAP Fault con el código 500. ms-integracion lo convierte en un error del puerto y la cotización queda PENDIENTE DE PRECIO.")}
<h3>La cuenta de llamadas: la caché de Redis</h3>
<p>En el ensayo se generaron dos cotizaciones de res ({c(COT1['numero'])} y {c(COT2['numero'])}). WireMock lleva la cuenta
de lo que recibe, y antes de las consultas a mano de arriba tenía una sola llamada: la primera cotización le preguntó al
Ministerio y la segunda la respondió el Proxy desde Redis.</p>
{terminal("m_cuenta", nota="Ojo: las consultas a mano con <code>curl</code> también cuentan; para mostrar la caché, contar antes de probar.")}
{terminal("r_get")}
{terminal("r_ttl", nota="Segundos que le quedan: la llave vence en un día (supuesto S3), el mismo plazo con que el Proxy da por vencido un precio.")}
<h3>Si el Ministerio no responde</h3>
<div class="par">
<div>{figura("plataforma", DP, "reenviar", "Cotización pendiente de precio", "«Reenviar una cotización»: la cotización quedó PENDIENTE DE PRECIO y se reenvía cuando el Ministerio vuelve.")}</div>
<div><p>Con {c("kg stop ministerio")} y una cotización de un producto que no esté en la caché, la cotización se guarda
PENDIENTE DE PRECIO y no se envía al proveedor. Con {c("kg start ministerio")} el auxiliar la reenvía desde «Cotizaciones
enviadas». Los productos que ya estaban en la caché siguen saliendo con su precio aunque el Ministerio esté caído.</p>
<p class="suave">No hace falta mostrarlo en vivo: la imagen basta.</p></div>
</div>
<div class="dir"><b>Qué mostrar:</b> la cuenta (1), la llave de Redis con su vencimiento y, si preguntan por el contrato,
el WSDL y la respuesta de la res.</div>
"""


def seccion_redes(cfg):
    r = cfg["redes"]
    tend = tabla(["Producto", "Menciones"], [[E(p), f"{m:,}".replace(",", ".")] for p, m in r["tendencias"]])
    return f"""
<dl class="ficha">
<dt>Reemplaza a</dt><dd>La API pública de tendencias de una red social (la red concreta no la fija el enunciado, supuesto S11):
{donde(("2", "v16"), ("3a", "c3a_x_redes"))}.</dd>
<dt>Quién lo usa</dt><dd>Solo ms-analitica: <code>TendenciasJob</code> (con ShedLock) llama a <code>RedesSocialesAdapter</code>,
cruza las tendencias con la carta y publica <code>tendencia.detectada</code>; ms-notificaciones manda el correo
«Promociones sugeridas» a los jefes de cocina, que las aceptan o descartan en «Promociones sugeridas».</dd>
<dt>Cuándo</dt><dd>Cada semana, el lunes a las 6:00; en el equipo, {c(cfg["tendencias_local"])} (cada 30 s), para verlo trabajar.
Se publica a lo sumo un evento por día.</dd>
<dt>Cómo se abre</dt><dd>No se publica: {c("kg exec redes curl …")}.</dd>
<dt>Patrones</dt><dd>{et("Adapter", "Eventos", "Hexagonal")}</dd>
</dl>
<h3>Qué responde</h3>
<p>Con el token {c(r["token"])} y la consulta {c("?" + "&".join(f"{k}={v}" for k, v in r["consulta"].items()))}, los productos
en tendencia de la semana, de más a menos menciones, con {r["demora"]} ms de demora; sin token, {r["sin_token"]}.</p>
{tend}
{terminal("n_tendencias")}
{terminal("n_401")}
<h3>La cuenta sube sola</h3>
{terminal("n_cuenta")}
{terminal("n_cuenta2", nota=f"31 segundos después: {HE['redes_llamadas'][0]} → {HE['redes_llamadas'][1]}. Es <code>TendenciasJob</code>, no nosotros.")}
{con_pines("externos", DX, "e02_promociones", "Correo de promociones en Mailpit", [
    "El correo que sale de <code>tendencia.detectada</code>: una línea por plato de la carta que está en tendencia."])}
<div class="dir"><b>Qué mostrar:</b> la respuesta con el token, el 401 sin él y la cuenta antes y después de 30 segundos;
luego el correo de promociones en Mailpit.</div>
"""


def seccion_soap():
    return f"""
<dl class="ficha">
<dt>Qué es</dt><dd>Los «sistemas externos» que consumen los servicios del restaurante ({donde(("2", "v17"), ("3a", "c3a_x_sistemas"))}).
No se reemplazan: nuestro endpoint SOAP corre de verdad en ms-integracion (<code>AbastecimientoSoapEndpoint</code>, con
Apache CXF) y entra por el gateway, que pide un token válido como a cualquier petición (DEC-72).</dd>
<dt>Cómo se abre</dt><dd>Desde el equipo, por {c("localhost:8080")}: es lo único de esta guía que pasa por el gateway.</dd>
<dt>Patrones</dt><dd>{et("SOA / SOAP", "Facade (gateway)", "OAuth 2.0 / JWT")}</dd>
</dl>
{terminal("s_401", nota="Sin token, el gateway no deja pasar.")}
{terminal("s_wsdl", recorte=14)}
{terminal("s_precio", nota="El precio sugerido de la res. Sale del mismo Proxy: estaba en Redis, así que el Ministerio no recibió otra llamada.")}
<div class="dir"><b>Qué mostrar:</b> el 401, el WSDL y la respuesta. Sirve para cerrar la integración: consumimos SOAP del
Ministerio y exponemos SOAP a otros sistemas, los dos en el mismo servicio de frontera.</div>
"""


def seccion_almacen():
    return f"""
<dl class="ficha">
<dt>Qué es</dt><dd>El almacén de objetos S3 de la página 3a y la 5 ({donde(("3a", "c3a_db_objetos"), ("5", "v26"))}). En el
equipo es SeaweedFS, que habla la misma API S3 (DEC-59).</dd>
<dt>Quién lo usa</dt><dd>ms-cotizaciones guarda el PDF de la cotización ({c("cotizaciones/")}) y ms-ordenes el de la orden
({c("ordenes/")}), en {c("documentos-pdf")}; ms-menu guarda las imágenes de los platos en {c("imagenes-platos")};
ms-notificaciones descarga el PDF para adjuntarlo.</dd>
<dt>Cómo se abre</dt><dd>Por la aplicación: «Ver PDF» en V14 y en V23. En la terminal: {c("kgs3 s3 ls …")}.</dd>
<dt>Patrones</dt><dd>{et("Claim check", "Adapter (AlmacenObjetosAdapter)", "Hexagonal")}</dd>
</dl>
{terminal("o_buckets")}
{terminal("o_pdf", nota=f"{COT1['numero']}, {COT2['numero']} y {OC} son los del ensayo; los anteriores vienen con los datos de la demo.")}
{terminal("o_img", recorte=10, nota="Vacío en la demo: los platos de la carta inicial no tienen foto (V03 muestra su ícono). El bucket se llena cuando el jefe de cocina sube la foto de un plato en V06.")}
<div class="par">
<div>{figura("plataforma", DP, "d12_pdf_cotizacion", "PDF de la cotización", f"El PDF de {COT1['numero']} (V14): sale de {c('documentos-pdf/cotizaciones/')}.")}</div>
<div>{figura("plataforma", DP, "d21_pdf_orden", "PDF de la orden", f"El PDF de {OC} (V23): sale de {c('documentos-pdf/ordenes/')}.")}</div>
</div>
<p><b>Claim check:</b> Kafka no transporta archivos. El evento lleva solo la dirección del PDF; quien lo necesita lo pide al
almacén. Se ve en el evento {c("orden.generada")} (sección 9).</p>
"""


def seccion_elastic():
    return f"""
<dl class="ficha">
<dt>Qué es</dt><dd>Los logs de error de la página 3a ({donde(("3a", "c3a_elk"), ("5", "v19"))}): el índice {c("logs-errores")}.</dd>
<dt>Quién lo usa</dt><dd>Escriben los once servicios: <code>AppenderElasticsearch</code> (de <code>compartido</code>) manda
cada error con el servicio, el mensaje, la hora, la correlación y el usuario, por lotes y sin hacer esperar al servicio.
Lee solo ms-administracion (<code>ElasticsearchLogsAdapter</code>) para la ventana «Logs de errores» del administrador.</dd>
<dt>Cómo se abre</dt><dd>Por la aplicación: «Logs de errores». En la terminal: {c("kg exec elasticsearch curl …")}.</dd>
</dl>
{terminal("e_indices0", nota="Con la demo recién hecha no hay índice: ningún servicio ha fallado. El índice se crea con el primer error.")}
{figura("plataforma", DP, "d28_logs", "Logs de errores vacío", "«Logs de errores» sin errores, después del ensayo.")}
<p>Para ver un error de verdad, el paso opcional de la sección 13 detiene el correo un momento: el error aparece aquí con
su servicio, su mensaje y la correlación del recorrido.</p>
"""


def seccion_kafka():
    return f"""
<dl class="ficha">
<dt>Qué es</dt><dd>El broker de eventos ({donde(("3a", "c3a_bus"))}): los 10 tópicos y la cola de errores de ms-notificaciones,
con la creación automática de tópicos apagada.</dd>
<dt>Cómo se abre</dt><dd>{c("kg exec kafka /opt/kafka/bin/…")}, con los scripts que trae la imagen.</dd>
</dl>
{terminal("k_topicos", nota="Los 10 tópicos de la sección 6 del reporte y <code>notificaciones.dlq</code>; los que empiezan por <code>__</code> son internos de Kafka.")}
{terminal("k_atraso", nota="Tópico, partición y atraso (LAG) del grupo de ms-notificaciones: 0 es que ya procesó todo; «-», una partición que todavía no ha recibido eventos (cada tópico tiene tres).")}
{terminal("k_evento", nota=f"El evento <code>orden.generada</code> de {OC} lleva <code>pdfUrl</code> (la dirección en el almacén), el correo de quien lo hizo y la correlación del recorrido.")}
"""


def seccion_redis(cfg):
    return f"""
<dl class="ficha">
<dt>Qué es</dt><dd>{donde(("3a", "c3a_db_integracion"), ("5", "v25"))}. Tres usos separados: la caché de precios del
Ministerio (llaves {c("precio-referencia:{producto}")}), el canal {c(cfg["canal"])} de las alarmas en vivo y los baldes del
límite de tasa del gateway.</dd>
<dt>Cómo se abre</dt><dd>{c("kg exec redis redis-cli …")}.</dd>
</dl>
{terminal("r_llaves")}
<h3>El canal de las alarmas</h3>
<p>Con una terminal suscrita al canal antes del cierre del día (paso 2 de la guía de la plataforma), la alarma de la res
pasa por aquí: <code>AlarmaPushAdapter</code> la publica y cada réplica de ms-notificaciones la entrega por WebSocket a los
jefes conectados (PD-01). Se corta con Ctrl+C.</p>
{terminal("r_alarma")}
<h3>El límite de tasa</h3>
{terminal("r_limite", nota="Un balde por usuario con sesión (<code>usuario:2</code> es el jefe de cocina) y otro por IP para el inicio de sesión. Duran unos segundos, así que hay que consultarlos justo después de usar la aplicación.")}
"""


def seccion_postgres():
    return f"""
<dl class="ficha">
<dt>Qué es</dt><dd>{donde(("5", "v24"))}: una base por servicio, cada una con su usuario, que no puede entrar a las demás.</dd>
<dt>Cómo se abre</dt><dd>{c("kg exec postgres psql -U postgres …")}.</dd>
</dl>
{terminal("p_bases")}
{terminal("p_orden", nota=f"{OC} sobre {COT1['numero']}, con la dirección de su PDF; FINALIZADA porque el ensayo la marcó como recibida.")}
{terminal("p_outbox", nota="La outbox de ms-ordenes está vacía: el relé ya publicó y borró todos sus eventos.")}
{terminal("p_ajena", nota="El usuario de ms-pedidos no puede conectarse a la base de ms-ordenes: los servicios se hablan por REST o por eventos, nunca por la base del otro.")}
"""


def seccion_eureka():
    d29, d30 = DP["imagenes"]["d29_plataforma"], DP["imagenes"]["d30_eureka"]
    exige(len(d29["pines"]) == 1 and len(d30["pines"]) == 1, "d29 o d30 cambiaron de círculos")
    return f"""
<p>Las dos consolas publicadas que no son correo: {c("localhost:8761")} (Eureka, {donde(("3a", "c3a_registry"), ("5", "v13"))})
y {c("localhost:8080/plataforma/")}, una página que muestra los once servicios activos en Eureka con su prefijo en el gateway.</p>
<div class="par">
<div>{con_pines("plataforma", DP, "d29_plataforma", "Servicios activos", ["Los 11 servicios, activos, con su prefijo en el gateway."])}</div>
<div>{con_pines("plataforma", DP, "d30_eureka", "Eureka", ["Las instancias registradas: el gateway y los once servicios."])}</div>
</div>
<p>Si al empezar un clic responde 503, es que Eureka todavía no propagó: abrir <code>/plataforma/</code> y esperar a que
estén los once.</p>
"""


def seccion_error(cfg):
    duracion = lambda t: re.sub(r"(\d+)s$", r"\1 s", re.sub(r"(\d+)m$", r"\1 min", t))
    cola = duracion(cfg["espera_cola"])
    return f"""
<div class="aviso"><b>Opcional y al final.</b> Usa <code>pause</code>, no <code>stop</code>: Mailpit guarda los correos en
un archivo temporal que borra al detenerse, así que con <code>stop</code> la bandeja queda vacía. Con <code>pause</code> el contenedor se congela y conserva todo.</div>
<ol class="pasos">
<li><b>Pausar el correo.</b>{terminal("x_pausa")}</li>
<li><b>El jefe de cocina registra una solicitud</b> en «Solicitudes» → «Nueva solicitud» (en el ensayo, {c(HE["error_solicitud"])}:
5 kg de arroz). ms-notificaciones intenta el correo: el servidor no contesta, lo reintenta {cfg["reintentos"]} veces cada
{cfg["espera_reintento"] // 1000} s y lo pasa a la cola de errores. En el ensayo tardó unos {HE["error_segundos"]} s.</li>
<li><b>El error en Elasticsearch</b>, con su correlación:{terminal("e_error")}{terminal("e_indices1")}
{con_pines("externos", DX, "e03_logs_error", "Logs de errores con el error", [
    "El servicio que falló: ms-notificaciones.",
    "El mensaje: qué evento no se pudo notificar, después de cuántos intentos, que pasó a la cola de errores y por qué (el servidor no respondió)."])}
{terminal("k_dlq2", nota=f"La cola de errores tiene un evento más ({HE['dlq_antes']} → {HE['dlq_despues']}).")}</li>
<li><b>Reanudar el correo.</b>{terminal("x_reanuda")} <code>ReprocesoColaErrores</code> vuelve a procesar la cola cada
{E(cola)} en el equipo ({E(duracion(cfg["espera_cola_defecto"]))} por defecto); en el ensayo el correo llegó a los {HE["reenvio_segundos"]} s.
{con_pines("externos", DX, "e04_mailpit_reenvio", "Mailpit con el correo reenviado", [
    f"«Solicitud {E(HE['error_solicitud'])} registrada»: llegó una sola vez, sin repetir los anteriores."])}</li>
</ol>
<div class="dir"><b>Qué decir:</b> «Si el servidor de correo no responde, la solicitud igual queda registrada; el correo se
reintenta, el error queda en los logs con la correlación para rastrearlo y, cuando el servidor vuelve, la cola de errores lo
entrega sola.»</div>
"""


GUION = [
    ("Introducción · 30 s",
     "Además de la aplicación, podemos mostrar lo que la rodea. En el diagrama de contexto el sistema habla con cuatro "
     "sistemas externos: el Ministerio de Comercio, el servidor de correo certificado, las redes sociales y los sistemas "
     "que consumen nuestro servicio SOAP. En el entorno local cada uno tiene su equivalente con el mismo contrato: Mailpit "
     "recibe los correos y dos servicios WireMock responden como el Ministerio y como la API de tendencias. Así vemos qué "
     "sale del sistema y qué le responden."),
    ("El correo certificado · 1½ min",
     f"Esta es la bandeja del servidor de correo. Cada correo sale de un evento: la alerta de stock bajo, la solicitud "
     f"registrada y en trámite, las dos cotizaciones con su PDF y la orden {OC}. Si abrimos las cabeceras de la cotización "
     f"{COT1['numero']} vemos dos que pone el sistema: X-Certificado, la huella SHA-256 del destinatario y el contenido, y "
     "X-Sello-Tiempo, la hora de envío. Las ponen dos decoradores apilados sobre el adaptador SMTP, que no sabe que existen. "
     "El PDF adjunto no viajó por Kafka: el evento llevaba su dirección y ms-notificaciones lo descargó del almacén."),
    ("El Ministerio y la caché · 1½ min",
     f"El precio estándar lo da el servicio SOAP del Ministerio, y solo ms-integracion habla con él. En la demo generamos dos "
     f"cotizaciones de res, {COT1['numero']} y {COT2['numero']}, pero el Ministerio recibió una sola llamada: la segunda la "
     "respondió el Proxy desde Redis, donde el precio vale un día. Si el Ministerio no responde, la cotización queda "
     "pendiente de precio y se reenvía cuando vuelve."),
    ("Las redes sociales · 1 min",
     "ms-analitica consulta cada semana la API de tendencias con su token; sin token responde 401. Aquí lo corre cada 30 "
     "segundos, y la cuenta de llamadas sube sola. Cuando un plato de la carta está en tendencia, publica un evento y el "
     "jefe de cocina recibe el correo de promociones sugeridas."),
    ("Los datos detrás · 1½ min",
     f"El PDF de la orden está en el almacén de objetos, en el bucket documentos-pdf. En Kafka están los diez tópicos y la "
     f"cola de errores, y el evento orden.generada de {OC} lleva solo la dirección del PDF. Cada servicio tiene su base: el "
     "usuario de pedidos no puede entrar a la base de órdenes. Y nuestro propio servicio SOAP responde por el gateway, con "
     "token, el mismo precio que ya estaba en la caché."),
    ("Opcional: el error real · 1½ min",
     "Pausamos el servidor de correo y el jefe de cocina registra una solicitud. La solicitud queda registrada igual; el "
     "correo se reintenta, pasa a la cola de errores y el error aparece en «Logs de errores» con el servicio y la "
     "correlación. Reanudamos el correo y, sin hacer nada más, la cola lo vuelve a procesar y el correo llega."),
    ("Cierre · 30 s",
     "Por último, Eureka: los once servicios y el gateway registrados. El gateway solo enruta a instancias vivas, y si una "
     "réplica se cae, deja de aparecer aquí."),
]

PREGUNTAS = [
    ("¿Por qué no se conectan al Ministerio de verdad?",
     "El servicio del Ministerio no está disponible para el proyecto. WireMock responde con el mismo WSDL que sigue "
     "MinisterioSoapClient; conectarse al real es cambiar la variable MINISTERIO_URL, sin tocar el adaptador."),
    ("¿Qué cambia en producción para cada sistema externo?",
     "Solo la configuración: MINISTERIO_URL, el servidor SMTP (con TLS) en SMTP_HOST, REDES_URL y REDES_TOKEN, y S3_URL para "
     "el almacén. Los puertos de la arquitectura hexagonal no cambian; por eso se pudieron reemplazar en el entorno local."),
    ("¿Qué garantiza el certificado del correo?",
     "Que el contenido y el destinatario no cambiaron: la cabecera lleva la huella SHA-256 de los dos. Se publican los "
     "primeros 8 bytes porque bastan para comparar en la cabecera. El sello de tiempo dice cuándo salió, con zona horaria."),
    ("¿Por qué decoradores y no un método que lo haga todo?",
     "Cada garantía es una responsabilidad separada que se apila sobre el envío; agregar una (una firma, un acuse) es "
     "agregar un decorador, sin tocar el adaptador SMTP ni los demás."),
    ("¿Cuánto dura la caché de precios y quién la comparte?",
     "Un día (supuesto S3). Vive en Redis, así que la comparten todas las réplicas de ms-integracion; la llave vence sola."),
    ("¿Qué pasa si el correo falla?",
     "Se reintenta dos veces, el error queda en Elasticsearch con su correlación, el evento pasa a notificaciones.dlq y "
     "ReprocesoColaErrores lo vuelve a procesar más tarde. La operación del usuario no se pierde ni se bloquea."),
    ("¿Por qué el PDF no va dentro del evento?",
     "Es el patrón claim check: Kafka está hecho para mensajes pequeños; el archivo va al almacén de objetos y el evento "
     "lleva su dirección. Así el PDF se guarda una vez, crece por separado (requisito del 200 %) y se respalda con el almacén."),
    ("¿Quién puede ver Elasticsearch, Kafka o las bases?",
     "Ningún usuario directamente: no se publican fuera de la red. Los logs los ve el administrador en su ventana, a través de "
     "ms-administracion; cada base solo la abre su servicio."),
    ("¿Cómo se protege la API de redes y nuestro SOAP?",
     "La API de redes pide un token Bearer (sin él responde 401) e irá por HTTPS. Nuestro SOAP entra por el gateway, que "
     "valida el JWT; los sistemas externos lo obtienen con OAuth 2.0 client credentials."),
    ("¿Por qué SeaweedFS y no MinIO?",
     "La imagen de MinIO ya no se puede descargar y los dos hablan la misma API S3, así que el adaptador no cambia (DEC-59). "
     "En producción es un almacén de objetos administrado, versionado y multizona."),
    ("¿Cada cuánto se buscan tendencias?",
     "Cada semana, el lunes a las 6:00, con ShedLock para que corra en una sola réplica; en el entorno local cada 30 segundos "
     "para poder verlo. Se publica a lo sumo un evento por día."),
]


def seccion_guion():
    total = "≈ 8 min (≈ 6½ sin el paso opcional)"
    return (f'<p class="suave">{total}. En primera persona del plural; cada bloque se lee en voz alta mientras se muestra lo '
            'de su sección.</p><div class="guion">' +
            "".join(f"<h3>{E(t)}</h3><p>«{E(x)}»</p>" for t, x in GUION) + "</div>")


def seccion_preguntas():
    return '<dl class="preguntas">' + "".join(f"<dt>{E(q)}</dt><dd>{E(r)}</dd>" for q, r in PREGUNTAS) + "</dl>"


def seccion_antes():
    return f"""
<ul class="lista-control">
<li>kg-demo restablecida y encendida: <code>documentos/guias_sustentacion/fuente/restablecer_demo.sh</code> y
<code>COMPOSE_PROJECT_NAME=kg-demo KonradGourmet/plataforma/levantar.sh --sin-compilar</code>; esperar ~75 s. El
restablecimiento también vacía Redis y Kafka: la primera cotización le pregunta al Ministerio y los tópicos solo tienen
los eventos de esta demo.</li>
<li>Hecha la demo de la guía de la plataforma (sin ella no están {c(SOL)}, {c(COT1['numero'])}, {c(OC)} ni los correos).</li>
<li>Una terminal en <code>FinalProyecto/</code> con los atajos <code>kg</code> y <code>kgs3</code> pegados, y la letra grande.</li>
<li>Pestañas abiertas: {c("localhost:8025")} (Mailpit), {c("localhost:8761")} (Eureka) y {c("localhost:8080/plataforma/")}.</li>
<li>Si se va a mostrar el canal de alarmas: una segunda terminal con <code>kg exec redis redis-cli SUBSCRIBE alarmas:stock-bajo</code>
abierta antes del cierre del día.</li>
<li>Contar las llamadas al Ministerio antes de probarlo con <code>curl</code>.</li>
<li>El paso del error, si se hace, al final y con <code>pause</code>.</li>
<li>Al terminar: <code>restablecer_demo.sh</code>, que la deja restablecida y detenida.</li>
</ul>
"""


CSS_LOCAL = """<style>
dl.ficha{display:grid;grid-template-columns:max-content 1fr;gap:6px 14px;margin:12px 0;font-size:.95rem}
dl.ficha dt{color:var(--suave);font-weight:600}
dl.ficha dd{margin:0;overflow-wrap:anywhere}
.suave{color:var(--suave)}
p.suave{font-size:.88rem}
.term{margin:10px 0 16px}
.term .cmd{background:#1f2328;color:#e7e9ec;border-radius:8px 8px 0 0;padding:8px 12px;
font:13px/1.5 ui-monospace,SFMono-Regular,Menlo,Consolas,monospace;white-space:pre-wrap;overflow-wrap:anywhere}
.term .cmd span{color:#7ee787;user-select:none}
.term pre.sal{margin:0;border-radius:0 0 8px 8px;border-top:0;max-height:420px}
.term p.nota{font-size:.88rem;color:var(--suave);margin:4px 2px 0}
pre{white-space:pre-wrap;overflow-wrap:anywhere}
pre.atajos{background:#1f2328;color:#e7e9ec}
table.pines{margin:8px 0 14px}
table.mapa{font-size:.88rem}
table.mapa th:nth-child(1){width:15%}table.mapa th:nth-child(3){width:16%}
figure.vent{margin:0}
figure.vent img{display:block;width:100%;height:auto;border:1px solid var(--borde);border-radius:6px}
figure.vent figcaption{font-size:.82rem;color:var(--suave);margin-top:4px}
.par{display:grid;grid-template-columns:1fr 1fr;gap:14px;margin:12px 0}
.dir{background:var(--codigo);border-radius:8px;padding:8px 12px;margin:14px 0}
ol.pasos>li{margin:10px 0}
.lista-control li{margin:4px 0}
p code,li code,dd code,td code,figcaption code{overflow-wrap:anywhere;word-break:break-word}
@media (max-width:700px){.par{grid-template-columns:1fr}dl.ficha{grid-template-columns:1fr;gap:2px}dl.ficha dd{margin-bottom:8px}
table.mapa thead,table.correos thead{display:none}table.correos tr{display:block;border-bottom:1px solid var(--borde);padding:6px 0}
table.correos td{display:block;border:0;padding:1px 4px;background:none!important}table.correos td:first-child{display:none}table.correos td:nth-child(2){font-weight:700}
table.mapa tr{display:block;border-bottom:1px solid var(--borde);padding:6px 0}
table.mapa td{display:block;border:0;padding:1px 4px;background:none!important}table.mapa td:first-child{font-weight:700}}
</style>"""


def main():
    cfg = configuracion()
    comprobar_datos(cfg)
    rec = recortes()
    s = [("mapa", "El mapa: qué reemplaza a qué y dónde está en los diagramas", seccion_mapa(cfg, rec)),
         ("abrir", "Cómo se abre lo que no se publica", seccion_abrir(cfg)),
         ("correo", "Mailpit · el servidor de correo certificado", seccion_correo(cfg)),
         ("ministerio", "WireMock · el Ministerio de Comercio", seccion_ministerio(cfg)),
         ("redes", "WireMock · las redes sociales", seccion_redes(cfg)),
         ("soap", "Nuestro servicio SOAP · los sistemas externos que lo consumen", seccion_soap()),
         ("almacen", "El almacén de objetos (SeaweedFS)", seccion_almacen()),
         ("elastic", "Elasticsearch · los logs de error", seccion_elastic()),
         ("kafka", "Kafka · el broker de eventos", seccion_kafka()),
         ("redis", "Redis · caché, alarmas y límite de tasa", seccion_redis(cfg)),
         ("postgres", "PostgreSQL · una base por servicio", seccion_postgres()),
         ("eureka", "Eureka y la página de servicios activos", seccion_eureka()),
         ("error", "Paso opcional: un error de verdad", seccion_error(cfg)),
         ("guion", "Guion sugerido", seccion_guion()),
         ("preguntas", "Preguntas probables", seccion_preguntas()),
         ("antes", "Antes de entrar", seccion_antes())]
    if fallas:
        sys.exit("No se genera la guía:\n  " + "\n  ".join(fallas))
    indice = '<nav class="indice"><strong>Contenido</strong><ol>' + "".join(
        f'<li><a href="#{i}">{E(t)}</a></li>' for i, t, _ in s) + "</ol></nav>"
    uso = ('<div class="aviso"><strong>Cómo usar esta guía.</strong> Es material del equipo para preparar la sustentación '
           '(DEC-109): dice qué pieza del entorno local reemplaza a cada sistema externo. El guion sugerido, en cambio, lo '
           'presenta como el entorno local, sin hablar de límites. Cada sección tiene la misma ficha (qué es, dónde está en '
           'los diagramas, quién lo usa y cómo se abre), las consultas con lo que respondieron en <code>kg-demo</code> después '
           'del ensayo de la guía de la plataforma, y qué mostrar.</div>')
    cuerpo = CSS_LOCAL + uso + indice + "".join(
        f'<section class="bloque" id="{i}"><h2>{n}. {E(t)}</h2>{h}</section>' for n, (i, t, h) in enumerate(s, 1))
    sub = ("Lo que en el equipo hace las veces del servidor de correo certificado, del Ministerio de Comercio y de las redes "
           "sociales, nuestro servicio SOAP y las consolas de apoyo (almacén de objetos, Elasticsearch, Kafka, Redis, "
           "PostgreSQL y Eureka): qué reemplaza cada uno, dónde está en los diagramas, quién lo usa, cómo se abre y qué mostrar.")
    (GUIAS / "guia_externos.html").write_text(pagina("Guía de lo que está fuera de la plataforma · Konrad Gourmet", sub, cuerpo, True),
                                              encoding="utf-8")
    print(f"guia_externos.html: {len(s)} secciones, {len(SAL)} consultas, {len(HE['correos'])} correos, "
          f"Ministerio {HE['ministerio_llamadas']} llamada, redes {HE['redes_llamadas']}, error en {HE['error_segundos']} s, "
          f"reenvío en {HE['reenvio_segundos']} s")


if __name__ == "__main__":
    main()
