"""Genera guias_sustentacion/guia_diagramas.html y sus imágenes en img/diagramas/.

Uso, desde la carpeta FinalProyecto (necesita la aplicación drawio y Pillow):
    mockups_nuevos/.venv/bin/python documentos/guias_sustentacion/fuente/generar_guia_diagramas.py
"""
import shutil
import subprocess
import sys
from pathlib import Path

AQUI = Path(__file__).resolve().parent
sys.path.insert(0, str(AQUI))
import pines as P                                   # noqa: E402
from contenido_diagramas import DIAGRAMAS, INTRO_ORDEN   # noqa: E402
from estilo import E, pagina, visor                 # noqa: E402

GUIAS = AQUI.parent
DOCS = GUIAS.parent
DRAWIO = DOCS / "Arquitectura_Konrad_Gourmet.drawio"
IMG = GUIAS / "img" / "diagramas"


def exportar_imagenes():
    IMG.mkdir(parents=True, exist_ok=True)
    con_pines = {k: [(cid, pos) for cid, pos, _, _ in d["elementos"]]
                 for k, d in DIAGRAMAS.items() if not d.get("sin_pines")}
    res = P.exportar(DRAWIO, IMG, con_pines)
    tapados = []
    for k, datos in res.items():
        ps = datos["pines"]
        for i in range(len(ps)):
            for j in range(i + 1, len(ps)):
                if (ps[i][1] - ps[j][1]) ** 2 + (ps[i][2] - ps[j][2]) ** 2 < (datos["d"] * 1.1) ** 2:
                    tapados.append((k, ps[i][0], ps[j][0]))
    if tapados:
        sys.exit(f"Números que se tapan (página, a, b): {tapados}")
    fallas = P.comprobar(IMG, res)
    if fallas:
        sys.exit(f"Hay números fuera de su lugar: {fallas}")
    # la página 6 va sin números: se exporta tal cual
    subprocess.run(["drawio", "-x", "-f", "png", "-s", "1", "-p", str(P.ORDEN.index("6") + 1),
                    "-o", str(IMG / "6.png"), str(DRAWIO)], check=True, capture_output=True)
    shutil.copyfile(DOCS / "casos_de_uso.png", IMG / "casos_de_uso.png")
    from PIL import Image
    for clave in ("6", "casos_de_uso"):
        with Image.open(IMG / f"{clave}.png") as im:
            res[clave] = {"d": 0, "pines": [], "ancho": im.size[0], "alto": im.size[1]}
    return res


def seccion(clave, d, datos, n_orden, minutos):
    id_v = f"v-{clave}"
    ruta = f"img/diagramas/{clave}.png"
    partes = [f'<section class="bloque" id="d-{clave}">',
              f'<h2>{n_orden}. {E(d["titulo"])}</h2>',
              f'<div class="meta"><span>📄 {E(d["origen"])}</span><span>📘 Reporte: {E(d["reporte"])}</span>'
              f'<span>⏱ {E(minutos)}</span></div>',
              f'<p>{E(d["que_es"])}</p>',
              visor(id_v, ruta, datos["ancho"], datos["alto"], datos["pines"], datos["d"], d["titulo"])]
    if d.get("sin_pines"):
        partes.append(f'<h3>{E(d["tabla_titulo"])}</h3><table><thead><tr><th class="c-nombre">Elemento</th><th>Qué es</th></tr>'
                      '</thead><tbody>')
        partes += [f'<tr><td><strong>{E(a)}</strong></td><td>{E(b)}</td></tr>' for a, b in d["tabla"]]
        partes.append('</tbody></table>')
    else:
        partes.append('<h3>Qué es cada parte</h3><table><thead><tr><th class="c-num">#</th><th class="c-nombre">Elemento</th><th>Qué es</th></tr>'
                      '</thead><tbody>')
        for n, (_, _, nombre, texto) in enumerate(d["elementos"], 1):
            partes.append(f'<tr id="{id_v}-n{n}"><td class="num"><button class="pin" data-visor="{id_v}" '
                          f'data-n="{n}" title="Ver el {n} en el diagrama">{n}</button></td>'
                          f'<td><strong>{E(nombre)}</strong></td><td>{E(texto)}</td></tr>')
        partes.append('</tbody></table>')
    partes.append('<div class="guion"><h3>🎤 Guion sugerido para sustentarlo en vivo</h3>')
    partes += [f'<p>{E(p)}</p>' for p in d["guion"]]
    partes.append('</div>')
    if d.get("preguntas"):
        partes.append('<h3>Si preguntan…</h3><dl class="preguntas">')
        partes += [f'<dt>{E(q)}</dt><dd>{E(r)}</dd>' for q, r in d["preguntas"]]
        partes.append('</dl>')
    partes.append('</section>')
    return "\n".join(partes)


def main():
    res = exportar_imagenes()
    indice = ['<nav class="indice"><strong>Orden sugerido para la sustentación</strong><ol>']
    for clave, nombre, minutos in INTRO_ORDEN:
        indice.append(f'<li><a href="#d-{clave}">{E(nombre)}</a> <span class="min">· {E(minutos)}</span></li>')
    indice.append('</ol></nav>')
    uso = ('<div class="aviso"><strong>Cómo usar esta guía.</strong> Cada diagrama tiene su imagen con círculos '
           'rosados numerados. En la tabla de abajo, el mismo número dice qué es esa parte: toca el número y el '
           'visor se acerca a ese punto del diagrama; toca un círculo en la imagen y la página baja a su fila. Usa '
           '<em>+</em>, <em>−</em> y <em>Tamaño real</em> para leer los textos pequeños, y arrastra para moverte. '
           'Al final de cada diagrama hay un guion sugerido para decirlo en voz alta, y algunas preguntas que '
           'pueden salir. El recorrido completo toma unos 20 a 25 minutos; si hay menos tiempo, prioriza el '
           'contexto, los contenedores, el mapa integrado y el recorrido de la compra.</div>')
    secciones = [seccion(clave, DIAGRAMAS[clave], res[clave], i, minutos)
                 for i, (clave, _, minutos) in enumerate(INTRO_ORDEN, 1)]
    sub = ("Los doce diagramas del proyecto, en el orden en que conviene sustentarlos: qué es cada parte y qué "
           "decir. Salen del archivo <code>Arquitectura_Konrad_Gourmet.drawio</code> (11 páginas) y del diagrama "
           "de casos de uso.")
    html = pagina("Guía de los diagramas · Konrad Gourmet", sub, "\n".join([uso] + indice + secciones), True)
    (GUIAS / "guia_diagramas.html").write_text(html, encoding="utf-8")
    total = sum(len(d.get("elementos", [])) for d in DIAGRAMAS.values())
    print(f"guia_diagramas.html: {len(secciones)} diagramas, {total} elementos numerados")


if __name__ == "__main__":
    main()
