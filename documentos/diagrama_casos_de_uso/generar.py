"""Dibuja el diagrama UML de casos de uso de Konrad Gourmet (documentos/casos_de_uso.png).

Los casos, los actores y las asociaciones se leen de «Casos de uso — Konrad Gourmet.md»
(tabla «Actores» y «Catálogo de casos de uso»), así que el diagrama no puede decir otra cosa
que el texto. Solo la posición de cada figura está en este archivo.

Uso, desde la carpeta FinalProyecto:
    mockups_nuevos/.venv/bin/python documentos/diagrama_casos_de_uso/generar.py
"""
import re
import unicodedata
from pathlib import Path

from playwright.sync_api import sync_playwright

AQUI = Path(__file__).resolve().parent
DOCS = AQUI.parent
MD = DOCS / "Casos de uso — Konrad Gourmet.md"
HTML = AQUI / "casos_de_uso.html"
PNG = DOCS / "casos_de_uso.png"
FUENTES = Path("../mockups_admin/assets/fonts")

ROLES = ["Mesero", "Jefe de cocina", "Auxiliar de compras", "Director de compras",
         "Director comercial", "Administrador"]
EXTERNOS = ["Proveedor", "Ministerio de Comercio", "Servidor de correo certificado",
            "Redes sociales", "Sistemas externos"]


def clave(texto):
    t = unicodedata.normalize("NFKD", texto).encode("ascii", "ignore").decode().lower()
    return re.sub(r"\s+", " ", t).strip()


def filas_tabla(lineas, titulo):
    """Filas (listas de celdas) de la primera tabla después del encabezado `titulo`."""
    i = next(n for n, l in enumerate(lineas) if l.strip() == titulo)
    filas, dentro = [], False
    for l in lineas[i + 1:]:
        if l.startswith("|"):
            dentro = True
            celdas = [c.strip() for c in l.strip().strip("|").split("|")]
            if not set("".join(celdas)) <= set("- :"):
                filas.append(celdas)
        elif dentro:
            break
    return filas[1:]  # sin la fila de encabezados


def leer_md():
    lineas = MD.read_text(encoding="utf-8").splitlines()
    actores = filas_tabla(lineas, "## Actores")
    roles = [a[0] for a in actores if a[1] == "Rol principal"]
    externos = [a[0] for a in actores if a[1].startswith("Externo")]
    assert roles == ROLES, roles
    assert externos == EXTERNOS, externos
    tipo = {a[0]: ("persona" if "persona" in a[1] else "sistema") for a in actores
            if a[1].startswith("Externo")}

    nombres = {clave(n): n for n in ROLES + EXTERNOS}
    casos = []
    for c in filas_tabla(lineas, "## Catálogo de casos de uso"):
        cu, nombre, principal, secundarios = c[0], c[1], c[2], c[3]
        if principal == "Los seis roles":
            principales = ["Usuario"]
        else:
            principales = [nombres[clave(principal)]]
        sec = [] if secundarios == "—" else [nombres[clave(s)] for s in re.split(r",\s*", secundarios)]
        casos.append({"id": cu, "nombre": nombre, "principal": principales, "secundarios": sec})
    assert [c["id"] for c in casos] == [f"CU-{n:02d}" for n in range(1, 20)]
    return casos, tipo


# ---------- geometría ----------
ANCHO, CX, RX, RY = 1400, 710, 240, 25
Y0, PASO = 290, 66
CAJA = (450, 230, 970, 1556)
X_IZQ, X_DER = 150, 1240
TRONCO_IZQ, TRONCO_DER, Y_GEN = 40, 1378, 92
USUARIO = (CX, 60)


def y_caso(i):
    return Y0 + i * PASO


def figura(x, ytop, etiqueta, cursiva=False):
    estilo = ' font-style="italic"' if cursiva else ""
    s = [f'<circle cx="{x}" cy="{ytop + 11}" r="11" class="trazo"/>',
         f'<line x1="{x}" y1="{ytop + 22}" x2="{x}" y2="{ytop + 55}" class="trazo"/>',
         f'<line x1="{x - 20}" y1="{ytop + 32}" x2="{x + 20}" y2="{ytop + 32}" class="trazo"/>',
         f'<line x1="{x}" y1="{ytop + 55}" x2="{x - 15}" y2="{ytop + 80}" class="trazo"/>',
         f'<line x1="{x}" y1="{ytop + 55}" x2="{x + 15}" y2="{ytop + 80}" class="trazo"/>']
    for n, linea in enumerate(etiqueta.split("\n")):
        s.append(f'<text x="{x}" y="{ytop + 102 + n * 20}" class="actor"{estilo}>{linea}</text>')
    return s


def caja_sistema(x, cy, etiqueta):
    s = [f'<text x="{x}" y="{cy - 30}" class="palabra">«sistema»</text>',
         f'<rect x="{x - 26}" y="{cy - 20}" width="52" height="40" rx="3" class="trazo"/>']
    for n, linea in enumerate(etiqueta.split("\n")):
        s.append(f'<text x="{x}" y="{cy + 42 + n * 20}" class="actor">{linea}</text>')
    return s


def dibujar(casos, tipo):
    y = {c["id"]: y_caso(i) for i, c in enumerate(casos)}

    def centro(ids):
        return sum(y[i] for i in ids) / len(ids)

    def de(actor):
        return [c["id"] for c in casos if actor in c["principal"] + c["secundarios"]]

    # Dónde va cada actor: (lado, centro vertical, etiqueta)
    pos = {
        "Mesero": ("izq", centro(de("Mesero")), "Mesero"),
        "Jefe de cocina": ("izq", centro(de("Jefe de cocina")), "Jefe de\ncocina"),
        "Auxiliar de compras": ("izq", centro(de("Auxiliar de compras")), "Auxiliar de\ncompras"),
        "Director de compras": ("izq", centro(de("Director de compras")), "Director de\ncompras"),
        "Redes sociales": ("der", 460, "Redes\nsociales"),
        "Servidor de correo certificado": ("der", 960, "Servidor de correo\ncertificado"),
        "Ministerio de Comercio": ("der", 620, "Ministerio de\nComercio"),
        "Proveedor": ("der", 790, "Proveedor"),
        "Director comercial": ("der", centro(de("Director comercial")), "Director\ncomercial"),
        "Administrador": ("der", 1300, "Administrador"),
        "Sistemas externos": ("der", 1462, "Sistemas\nexternos"),
    }
    assert set(pos) == set(ROLES + EXTERNOS)

    fondo, frente = [], []

    # Usuario (abstracto) y generalizaciones
    ux, uy = USUARIO
    frente += figura(ux, uy, "Usuario", cursiva=True)
    fondo.append(f'<line x1="{ux}" y1="{uy + 112}" x2="{CX}" y2="{y["CU-01"] - RY}" class="asoc"/>')
    ramas = {"izq": [], "der": []}
    for rol in ROLES:
        lado, cy, _ = pos[rol]
        ramas[lado].append(cy - 8)
    for lado, tronco, punta in (("izq", TRONCO_IZQ, ux - 22), ("der", TRONCO_DER, ux + 22)):
        x_act = X_IZQ if lado == "izq" else X_DER
        sentido = 1 if lado == "izq" else -1
        for yr in ramas[lado]:
            fondo.append(f'<line x1="{x_act - sentido * 22}" y1="{yr}" x2="{tronco}" y2="{yr}" class="gen"/>')
        fondo.append(f'<line x1="{tronco}" y1="{Y_GEN}" x2="{tronco}" y2="{max(ramas[lado])}" class="gen"/>')
        base = punta - sentido * 16
        fondo.append(f'<line x1="{tronco}" y1="{Y_GEN}" x2="{base}" y2="{Y_GEN}" class="gen"/>')
        frente.append(f'<polygon points="{punta},{Y_GEN} {base},{Y_GEN - 9} {base},{Y_GEN + 9}" class="triangulo"/>')

    # Actores y asociaciones
    for actor, (lado, cy, etiqueta) in pos.items():
        x = X_IZQ if lado == "izq" else X_DER
        if actor in ROLES or tipo.get(actor) == "persona":
            frente += figura(x, cy - 40, etiqueta)
            ax, ay = (x + 26, cy - 8) if lado == "izq" else (x - 26, cy - 8)
        else:
            frente += caja_sistema(x, cy, etiqueta)
            ax, ay = x - 30, cy
        for cu in de(actor):
            ex = CX - RX if lado == "izq" else CX + RX  # extremo de la elipse que da al actor
            fondo.append(f'<line x1="{ax}" y1="{ay}" x2="{ex}" y2="{y[cu]}" class="asoc"/>')

    # Sistema y casos
    x1, y1, x2, y2 = CAJA
    marco = [f'<rect x="{x1}" y="{y1}" width="{x2 - x1}" height="{y2 - y1}" rx="10" class="sistema"/>',
             f'<text x="{CX}" y="{y2 - 18}" class="titulo-sistema">Sistema de Abastecimiento Konrad Gourmet</text>']
    sistema = []
    for c in casos:
        sistema.append(f'<ellipse cx="{CX}" cy="{y[c["id"]]}" rx="{RX}" ry="{RY}" class="caso"/>')
        sistema.append(f'<text x="{CX}" y="{y[c["id"]] + 6}" class="cu">{c["id"]} {c["nombre"]}</text>')

    alto = y2 + 110
    leyenda = [
        f'<text x="40" y="{y2 + 50}" class="nota">Figura de persona: rol o persona externa. Caja con «sistema»: sistema externo. '
        'Línea simple: asociación. Flecha de punta hueca: generalización.</text>',
        f'<text x="40" y="{y2 + 75}" class="nota">Los seis roles son usuarios del sistema: heredan de <tspan font-style="italic">Usuario</tspan>, '
        'actor abstracto, y por eso todos inician sesión (CU-01).</text>',
    ]
    svg = "\n".join(marco + fondo + sistema + frente + leyenda)
    return f"""<!doctype html>
<html lang="es"><head><meta charset="utf-8"><title>Casos de uso · Konrad Gourmet</title>
<style>
@font-face {{ font-family: Poppins; font-weight: 400; src: url('{FUENTES}/Poppins-Regular.ttf'); }}
@font-face {{ font-family: Poppins; font-weight: 500; src: url('{FUENTES}/Poppins-Medium.ttf'); }}
@font-face {{ font-family: Poppins; font-weight: 600; src: url('{FUENTES}/Poppins-SemiBold.ttf'); }}
html, body {{ margin: 0; background: #fff; }}
svg {{ display: block; font-family: Poppins, sans-serif; }}
.trazo {{ fill: #fff; stroke: #2E2A26; stroke-width: 1.8; }}
.asoc {{ stroke: #8C8279; stroke-width: 1.4; }}
.gen {{ stroke: #5F5953; stroke-width: 1.4; fill: none; }}
.triangulo {{ fill: #fff; stroke: #5F5953; stroke-width: 1.4; }}
.sistema {{ fill: #FAF7F3; stroke: #D9CFC4; stroke-width: 1.5; }}
.caso {{ fill: #FFF4E8; stroke: #C9B8A6; stroke-width: 1.5; }}
.cu {{ font-size: 16px; font-weight: 500; fill: #2E2A26; text-anchor: middle; }}
.actor {{ font-size: 15px; fill: #2E2A26; text-anchor: middle; }}
.palabra {{ font-size: 12px; fill: #5F5953; text-anchor: middle; }}
.titulo-sistema {{ font-size: 15px; fill: #5F5953; text-anchor: middle; font-weight: 500; }}
.titulo {{ font-size: 22px; font-weight: 600; fill: #2E2A26; }}
.nota {{ font-size: 14px; fill: #5F5953; }}
</style></head><body>
<svg xmlns="http://www.w3.org/2000/svg" width="{ANCHO}" height="{alto}" viewBox="0 0 {ANCHO} {alto}">
<text x="40" y="38" class="titulo">Casos de uso · 19 casos, 6 roles y 5 actores externos</text>
{svg}
</svg></body></html>"""


def main():
    casos, tipo = leer_md()
    HTML.write_text(dibujar(casos, tipo), encoding="utf-8")
    with sync_playwright() as p:
        nav = p.chromium.launch(executable_path="/usr/bin/chromium")
        pag = nav.new_page(viewport={"width": ANCHO, "height": 800}, device_scale_factor=2)
        pag.goto(HTML.as_uri())
        pag.evaluate("document.fonts.ready")
        assert pag.evaluate("document.fonts.check('16px Poppins')"), "Poppins no cargó"
        pag.locator("svg").screenshot(path=str(PNG))
        nav.close()
    # Resumen para verificar contra el texto
    for c in casos:
        print(c["id"], c["nombre"], "|", ", ".join(c["principal"]), "|", ", ".join(c["secundarios"]) or "—")
    print("PNG:", PNG)


if __name__ == "__main__":
    main()
