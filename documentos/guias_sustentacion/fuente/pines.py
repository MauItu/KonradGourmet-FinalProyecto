"""Exporta páginas del .drawio con círculos numerados encima de los elementos que explica la guía.

Los círculos se agregan a una copia temporal del .drawio (el archivo del proyecto no cambia) y la copia
se exporta con la aplicación drawio a escala 1. Junto con cada página se agrega un ancla invisible en la
esquina superior izquierda, para saber en qué píxel de la imagen queda cada círculo; al final se comprueba
con Pillow que el centro de cada círculo tenga su color.
"""
import html
import re
import subprocess
import tempfile
import xml.etree.ElementTree as ET
from pathlib import Path

COLOR = "#D81B60"          # el color de los círculos: no lo usa ningún elemento del .drawio
ORDEN = ["1", "2", "3a", "3b", "4a", "4b", "4c", "4d", "4e", "5", "6"]   # -p de drawio, desde 1


def _geometrias(diagrama):
    celdas = {}
    for c in diagrama.iter("mxCell"):
        g = c.find("mxGeometry")
        geo, puntos = None, []
        if g is not None:
            if g.get("width") is not None:
                geo = (float(g.get("x") or 0), float(g.get("y") or 0), float(g.get("width")), float(g.get("height")))
            puntos = [(float(p.get("x") or 0), float(p.get("y") or 0)) for p in g.iter("mxPoint")]
        celdas[c.get("id")] = (c.get("parent"), geo, puntos, c.get("vertex") == "1")

    def absoluta(i):
        padre, geo, _, _ = celdas[i]
        x, y, w, h = geo
        while padre in celdas and celdas[padre][1] is not None:
            x += celdas[padre][1][0]
            y += celdas[padre][1][1]
            padre = celdas[padre][0]
        return x, y, w, h

    return celdas, absoluta


def diametro(ancho):
    return int(min(76, max(34, ancho / 70)))


def exportar(drawio, salida, pines):
    """pines: {clave de página: [(id de celda, posición), ...]} en el orden de los números.

    posición: 'si' (esquina superior izquierda, por omisión), 'sd', 'ii', 'id', 'c' (centro), o 'fi' y 'fd' (fuera, a la
    izquierda o a la derecha, a media altura: para no tapar el texto del elemento).
    Devuelve {clave: {"ancho": w, "alto": h, "pines": [(n, x, y), ...]}} en píxeles de la imagen."""
    texto = Path(drawio).read_text(encoding="utf-8")
    raiz = ET.fromstring(texto)
    diagramas = {d.get("name").split(" ")[0]: d for d in raiz.findall("diagram")}
    lineas = texto.split("\n")
    resultado, extra, clave = {}, {}, None
    for clave_p, lista in pines.items():
        celdas, absoluta = _geometrias(diagramas[clave_p])
        xs, ys = [], []
        for i, (_, geo, puntos, vertice) in celdas.items():
            if geo is not None and (vertice or celdas[i][0] not in (None, "0")):
                x, y, w, h = absoluta(i) if vertice else (geo[0], geo[1], 0, 0)
                xs.append(x); ys.append(y)
            for px, py in puntos:
                xs.append(px); ys.append(py)
        ancho_aprox = max(xs) - min(xs)
        d = diametro(ancho_aprox)
        cent = []
        for n, (cid, pos) in enumerate(lista, 1):
            x, y, w, h = absoluta(cid)
            cx, cy = {"si": (x, y), "sd": (x + w, y), "ii": (x, y + h), "id": (x + w, y + h),
                      "c": (x + w / 2, y + h / 2), "fi": (x - d / 2 - 6, y + h / 2),
                      "fd": (x + w + d / 2 + 6, y + h / 2)}[pos or "si"]
            cent.append((n, cx, cy))
        ax = min(min(xs), min(c[1] for c in cent) - d / 2) - 40
        ay = min(min(ys), min(c[2] for c in cent) - d / 2) - 40
        estilo = ("ellipse;whiteSpace=wrap;html=1;aspect=fixed;fillColor=" + COLOR + ";strokeColor=#FFFFFF;"
                  f"strokeWidth={max(3, d // 12)};fontColor=#FFFFFF;fontStyle=1;fontSize={round(d * 0.5)};"
                  "align=center;verticalAlign=middle;spacing=0;resizable=0;")
        nuevas = [f'<mxCell id="guia_ancla" value="" style="rounded=0;fillColor=none;strokeColor=none;" vertex="1" '
                  f'parent="1"><mxGeometry x="{ax:.0f}" y="{ay:.0f}" width="2" height="2" as="geometry"/></mxCell>']
        for n, cx, cy in cent:
            nuevas.append(f'<mxCell id="guia_pin_{n}" value="{n}" style="{estilo}" vertex="1" parent="1">'
                          f'<mxGeometry x="{cx - d / 2:.0f}" y="{cy - d / 2:.0f}" width="{d}" height="{d}" '
                          f'as="geometry"/></mxCell>')
        extra[clave_p] = nuevas
        resultado[clave_p] = {"d": d, "pines": [(n, round(cx - round(ax)), round(cy - round(ay))) for n, cx, cy in cent]}
    nuevo = []
    for ln in lineas:
        m = re.match(r'<diagram name="([^"]+)"', ln)
        if m:
            clave = m.group(1).split(" ")[0]
        if ln.startswith("</root></mxGraphModel></diagram>") and clave in extra:
            nuevo.extend(extra[clave])
        nuevo.append(ln)
    salida = Path(salida)
    salida.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory() as tmp:
        copia = Path(tmp) / "con_pines.drawio"
        copia.write_text("\n".join(nuevo), encoding="utf-8")
        for clave_p in pines:
            png = salida / f"{clave_p}.png"
            subprocess.run(["drawio", "-x", "-f", "png", "-s", "1", "-p", str(ORDEN.index(clave_p) + 1),
                            "-o", str(png), str(copia)], check=True, capture_output=True)
    return resultado


def comprobar(salida, resultado):
    """Cada círculo debe tener su color en el píxel calculado; devuelve los que fallan."""
    from PIL import Image
    rgb = tuple(int(COLOR[i:i + 2], 16) for i in (1, 3, 5))
    fallas = []
    for clave, datos in resultado.items():
        im = Image.open(Path(salida) / f"{clave}.png").convert("RGB")
        datos["ancho"], datos["alto"] = im.size
        r = int(datos["d"] * 0.3)
        for n, x, y in datos["pines"]:
            # el número blanco ocupa el centro: se mira un punto de color debajo de él, dentro del círculo
            px = im.getpixel((x, min(im.size[1] - 1, y + r)))
            if max(abs(a - b) for a, b in zip(px, rgb)) > 40:
                fallas.append((clave, n, x, y, px))
    return fallas
