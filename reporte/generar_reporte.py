#!/usr/bin/env python3
"""Genera el reporte de arquitectura de Konrad Gourmet en .docx y PDF (DEC-54, DEC-55).

Formato (PLAN_REPORTE.md, sección 2): Times New Roman de 12 pt en todo el documento,
texto en negro, títulos en negrilla del mismo tamaño, tablas con líneas finas negras y filas alternas en gris claro (DEC-56),
figuras numeradas con nota y número de página abajo a la derecha.

Uso:
    python generar_reporte.py contenido Reporte_Arquitectura_Konrad_Gourmet_v2
    python generar_reporte.py prueba.md    reporte/pagina_prueba

El primer argumento es el texto fuente: un archivo .md o una carpeta, cuyos .md se leen
en orden alfabético (contenido/ tiene un archivo por sección); el segundo, el nombre de salida (sin extensión),
relativo a la carpeta documentos/. Necesita python-docx y LibreOffice.
Si Times New Roman no está instalada, LibreOffice usa Liberation Serif, que tiene las
mismas métricas; para que el PDF la incruste, se puede apuntar FONTCONFIG_FILE a una
configuración que incluya la carpeta con Times.TTF.

Marcas del texto fuente (una por línea):
    # 1. Título              título de primer nivel (centrado, entra al índice)
    ## 1.1 Título            título de segundo nivel (a la izquierda)
    !portada                 portada (datos en PORTADA)
    !indice                  índice, con las páginas calculadas en dos pasadas
    !salto                   salto de página
    !horizontal / !vertical  orientación de las páginas de texto que siguen
    \\*                       asterisco literal (por ejemplo, /api/pedidos/\\*\\*)
    !tabla N | Título | anchos relativos   seguida de una tabla con barras |
    !nota Texto              nota bajo una tabla o figura («Nota.» en cursiva)
    !figura N | archivo | Título | Nota | horizontal   figura (horizontal: página apaisada)
    !cita Texto              cita en bloque
    !referencias             desde aquí, los párrafos llevan sangría francesa
    !sinsangria Texto        párrafo sin sangría de primera línea
Dentro del texto, **negrilla** y *cursiva*. Las líneas seguidas forman un párrafo;
una línea en blanco lo cierra.
"""

import copy
import os
import re
import subprocess
import sys
import tempfile
from pathlib import Path

from docx import Document
from docx.enum.section import WD_ORIENT, WD_SECTION
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.enum.text import WD_ALIGN_PARAGRAPH, WD_BREAK, WD_LINE_SPACING, WD_TAB_ALIGNMENT, WD_TAB_LEADER
from docx.image.image import Image as ImagenDocx
from docx.oxml import OxmlElement
from docx.oxml.ns import qn
from docx.shared import Cm, Pt, RGBColor

FUENTE = "Times New Roman"
TAMANO = Pt(12)
NEGRO = RGBColor(0, 0, 0)
MARGEN = Cm(2.54)
A4 = (Cm(21.0), Cm(29.7))
SANGRIA = Cm(1.27)
AQUI = Path(__file__).resolve().parent
# DEC-56: filas alternas blanco / gris claro (Mauricio cambió el naranja claro por gris el 2026-11-09).
FONDO_ALTERNO = "F2F2F2"

PORTADA = [
    ("Sistema de Abastecimiento Konrad Gourmet", True),
    ("Documento de arquitectura: microservicios, arquitectura orientada a eventos y arquitectura hexagonal", True),
    ("", False),
    ("Mauricio Jesús Iturriza Medina", False),
    ("Carlos Franco", False),
    ("Adrián David Corredor Camero", False),
    ("Isabela Castro", False),
    ("", False),
    ("Arquitectura de Software, proyecto 1", False),
    ("Profesor Carlos Andrés López", False),
    ("Programa de Ingeniería de Sistemas", False),
    ("Fundación Universitaria Konrad Lorenz", False),
    ("Bogotá D. C., noviembre de 2026", False),
]


# ---------------------------------------------------------------- estilos

def _fuentes(rpr):
    """Fija Times New Roman en las cuatro ranuras y quita las fuentes del tema."""
    rfonts = rpr.find(qn("w:rFonts"))
    if rfonts is None:
        rfonts = OxmlElement("w:rFonts")
        rpr.insert(0, rfonts)
    for atributo in list(rfonts.attrib):
        if atributo.endswith("Theme"):
            del rfonts.attrib[atributo]
    for ranura in ("w:ascii", "w:hAnsi", "w:eastAsia", "w:cs"):
        rfonts.set(qn(ranura), FUENTE)


def _negro(rpr):
    color = rpr.find(qn("w:color"))
    if color is None:
        color = OxmlElement("w:color")
        rpr.append(color)
    for atributo in list(color.attrib):
        del color.attrib[atributo]
    color.set(qn("w:val"), "000000")


def preparar_estilos(doc):
    estilos = doc.styles.element
    # Valores por defecto del documento: fuente, tamaño, color e idioma.
    rpr_def = estilos.find(qn("w:docDefaults")).find(qn("w:rPrDefault")).find(qn("w:rPr"))
    _fuentes(rpr_def)
    _negro(rpr_def)
    for etiqueta, valor in (("w:sz", "24"), ("w:szCs", "24")):
        el = rpr_def.find(qn(etiqueta))
        if el is None:
            el = OxmlElement(etiqueta)
            rpr_def.append(el)
        el.set(qn("w:val"), valor)
    lang = rpr_def.find(qn("w:lang"))
    if lang is None:
        lang = OxmlElement("w:lang")
        rpr_def.append(lang)
    lang.set(qn("w:val"), "es-CO")
    lang.set(qn("w:eastAsia"), "es-CO")
    lang.set(qn("w:bidi"), "ar-SA")
    # Todos los estilos: sin fuentes del tema y sin color.
    for rpr in estilos.iter(qn("w:rPr")):
        if rpr.find(qn("w:rFonts")) is not None:
            _fuentes(rpr)
        if rpr.find(qn("w:color")) is not None:
            _negro(rpr)

    normal = doc.styles["Normal"]
    normal.font.name = FUENTE
    normal.font.size = TAMANO
    normal.font.color.rgb = NEGRO
    pf = normal.paragraph_format
    pf.line_spacing_rule = WD_LINE_SPACING.ONE_POINT_FIVE
    pf.space_before = Pt(0)
    pf.space_after = Pt(0)
    pf.first_line_indent = SANGRIA
    pf.alignment = WD_ALIGN_PARAGRAPH.LEFT
    pf.widow_control = True

    for nombre, alineacion in (("Heading 1", WD_ALIGN_PARAGRAPH.CENTER), ("Heading 2", WD_ALIGN_PARAGRAPH.LEFT)):
        est = doc.styles[nombre]
        est.font.name = FUENTE
        est.font.size = TAMANO
        est.font.bold = True
        est.font.italic = False
        est.font.color.rgb = NEGRO
        est.font.all_caps = False
        p = est.paragraph_format
        p.alignment = alineacion
        p.first_line_indent = Cm(0)
        p.left_indent = Cm(0)
        p.space_before = Pt(12)
        p.space_after = Pt(6)
        p.line_spacing_rule = WD_LINE_SPACING.ONE_POINT_FIVE
        p.keep_with_next = True
        rpr = est.element.get_or_add_rPr()
        _fuentes(rpr)
        _negro(rpr)


def _formato_parrafo(p, sangria=True, espaciado=WD_LINE_SPACING.ONE_POINT_FIVE, alineacion=WD_ALIGN_PARAGRAPH.LEFT):
    pf = p.paragraph_format
    pf.first_line_indent = SANGRIA if sangria else Cm(0)
    pf.line_spacing_rule = espaciado
    pf.alignment = alineacion
    return pf


INLINE = re.compile(r"(\*\*[^*]+\*\*|\*[^*]+\*)")


def texto(p, contenido, negrilla=False, cursiva=False):
    """Agrega el texto con **negrilla** y *cursiva*. El espacio antes de % no se parte."""
    contenido = contenido.replace(" %", "\u00a0%").replace("\\*", "\x00")
    for trozo in INLINE.split(contenido):
        if not trozo:
            continue
        b, i, t = negrilla, cursiva, trozo
        if trozo.startswith("**") and trozo.endswith("**"):
            b, t = True, trozo[2:-2]
        elif trozo.startswith("*") and trozo.endswith("*") and len(trozo) > 1:
            i, t = True, trozo[1:-1]
        t = t.replace("\x00", "*")
        # «ms-pedidos» y similares no se parten al final de la línea: unión de palabras (U+2060) tras el guion.
        run = p.add_run(re.sub(r"\bms-", "ms-\u2060", t))
        run.bold = b or None
        run.italic = i or None
    return p


# ---------------------------------------------------------------- tablas

def _borde(padre, lado, valor="single", tamano="6"):
    el = OxmlElement(f"w:{lado}")
    el.set(qn("w:val"), valor)
    el.set(qn("w:sz"), tamano)
    el.set(qn("w:space"), "0")
    el.set(qn("w:color"), "000000")
    padre.append(el)


def tabla(doc, filas, anchos, ancho_total):
    n = len(filas[0])
    if not anchos:
        anchos = [1] * n
    suma = sum(anchos)
    columnas = [int(ancho_total * a / suma) for a in anchos]
    t = doc.add_table(rows=len(filas), cols=n)
    t.alignment = WD_TABLE_ALIGNMENT.LEFT
    t.autofit = False
    tblpr = t._tbl.tblPr
    estilo = tblpr.find(qn("w:tblStyle"))
    if estilo is not None:
        tblpr.remove(estilo)
    bordes = OxmlElement("w:tblBorders")
    _borde(bordes, "top")
    _borde(bordes, "left", "nil")
    _borde(bordes, "bottom")
    _borde(bordes, "right", "nil")
    _borde(bordes, "insideH", "nil")
    _borde(bordes, "insideV", "nil")
    tblpr.append(bordes)
    margenes = OxmlElement("w:tblCellMar")
    for lado, valor in (("left", "80"), ("right", "80"), ("top", "20"), ("bottom", "20")):
        m = OxmlElement(f"w:{lado}")
        m.set(qn("w:w"), valor)
        m.set(qn("w:type"), "dxa")
        margenes.append(m)
    tblpr.append(margenes)
    for i, col in enumerate(t.columns):
        col.width = columnas[i]
    for r, fila in enumerate(filas):
        row = t.rows[r]
        trpr = row._tr.get_or_add_trPr()
        if r == 0:
            cab = OxmlElement("w:tblHeader")
            trpr.append(cab)
        no_partir = OxmlElement("w:cantSplit")
        trpr.append(no_partir)
        for c, valor in enumerate(fila):
            celda = row.cells[c]
            celda.width = columnas[c]
            if r == 0:
                tcpr = celda._tc.get_or_add_tcPr()
                tcb = OxmlElement("w:tcBorders")
                _borde(tcb, "bottom")
                tcpr.append(tcb)
            if r > 0 and r % 2 == 0:
                tcpr = celda._tc.get_or_add_tcPr()
                sombra = OxmlElement("w:shd")
                sombra.set(qn("w:val"), "clear")
                sombra.set(qn("w:color"), "auto")
                sombra.set(qn("w:fill"), FONDO_ALTERNO)
                tcpr.append(sombra)
            p = celda.paragraphs[0]
            _formato_parrafo(p, sangria=False, espaciado=WD_LINE_SPACING.SINGLE)
            p.paragraph_format.space_before = Pt(2)
            p.paragraph_format.space_after = Pt(2)
            # «Clase.método(» puede partirse en el punto: espacio de ancho cero (U+200B) tras él.
            valor = re.sub(r"\b([A-Z][A-Za-z0-9]+)\.([a-z][A-Za-z0-9]*\()", "\\1.\u200b\\2", valor)
            texto(p, valor.strip(), negrilla=(r == 0))
    return t


def leer_tabla(lineas):
    filas = []
    for linea in lineas:
        celdas = [c for c in re.split(r"(?<!\\)\|", linea.strip().strip("|"))]
        celdas = [c.replace("\\|", "|") for c in celdas]
        if all(re.fullmatch(r"\s*:?-{2,}:?\s*", c) for c in celdas):
            continue
        filas.append(celdas)
    return filas


# ---------------------------------------------------------------- página y secciones

def _numero_pagina(seccion):
    pie = seccion.footer
    pie.is_linked_to_previous = False
    p = pie.paragraphs[0]
    p.text = ""
    _formato_parrafo(p, sangria=False, espaciado=WD_LINE_SPACING.SINGLE, alineacion=WD_ALIGN_PARAGRAPH.RIGHT)
    run = p.add_run()
    for tipo, contenido in (("begin", None), (None, " PAGE "), ("end", None)):
        if tipo:
            fld = OxmlElement("w:fldChar")
            fld.set(qn("w:fldCharType"), tipo)
            run._r.append(fld)
        else:
            instr = OxmlElement("w:instrText")
            instr.set(qn("xml:space"), "preserve")
            instr.text = contenido
            run._r.append(instr)


def _pagina(seccion, horizontal=False):
    ancho, alto = A4
    if horizontal:
        seccion.orientation = WD_ORIENT.LANDSCAPE
        seccion.page_width, seccion.page_height = alto, ancho
    else:
        seccion.orientation = WD_ORIENT.PORTRAIT
        seccion.page_width, seccion.page_height = ancho, alto
    for lado in ("top_margin", "bottom_margin", "left_margin", "right_margin"):
        setattr(seccion, lado, MARGEN)
    seccion.header_distance = Cm(1.25)
    seccion.footer_distance = Cm(1.25)


def ancho_util(seccion):
    return seccion.page_width - seccion.left_margin - seccion.right_margin


def alto_util(seccion):
    return seccion.page_height - seccion.top_margin - seccion.bottom_margin


# ---------------------------------------------------------------- generador

class Reporte:
    def __init__(self, paginas_indice=None):
        self.doc = Document()
        preparar_estilos(self.doc)
        s = self.doc.sections[0]
        _pagina(s)
        s.different_first_page_header_footer = True
        _numero_pagina(s)
        self.indice = []                 # títulos de primer nivel, en orden
        self.paginas_indice = paginas_indice or {}
        self.referencias = False
        self.figuras = []
        self.tablas = []
        self.tras_tabla = False          # el párrafo siguiente a una tabla lleva aire arriba
        self.orientacion = "v"           # orientación de la sección actual
        self.orientacion_texto = "v"     # orientación pedida para el texto (!horizontal / !vertical)
        self.seccion_nueva = False       # la sección actual todavía no tiene contenido
        self.tras_figura_h = False       # la página actual la ocupa una figura apaisada

    @property
    def seccion(self):
        return self.doc.sections[-1]

    def asegurar(self, orientacion):
        """Abre una sección nueva solo si cambia la orientación (evita páginas en blanco)."""
        if orientacion != self.orientacion:
            nueva = self.doc.add_section(WD_SECTION.NEW_PAGE)
            _pagina(nueva, horizontal=(orientacion == "h"))
            nueva.different_first_page_header_footer = False
            self.orientacion = orientacion
            self.seccion_nueva = True
            return True
        return False

    def contenido(self):
        abrio = self.asegurar(self.orientacion_texto)
        if self.tras_figura_h and not abrio:
            self.salto()                 # el texto no sigue en la página de una figura apaisada
        self.tras_figura_h = False
        self.seccion_nueva = False

    def portada(self):
        doc = self.doc
        for _ in range(5):
            _formato_parrafo(doc.add_paragraph(), sangria=False)
        for linea, negrilla in PORTADA:
            p = doc.add_paragraph()
            _formato_parrafo(p, sangria=False, alineacion=WD_ALIGN_PARAGRAPH.CENTER)
            if linea:
                texto(p, linea, negrilla=negrilla)
        self.salto()

    def salto(self):
        p = self.doc.add_paragraph()
        _formato_parrafo(p, sangria=False, espaciado=WD_LINE_SPACING.SINGLE)
        p.add_run().add_break(WD_BREAK.PAGE)

    def titulo(self, nivel, contenido):
        self.contenido()
        h = self.doc.add_heading(level=nivel)
        texto(h, contenido)
        if nivel == 1:
            self.indice.append(contenido)
            if contenido.startswith("Anexo"):
                h.paragraph_format.page_break_before = True

    def parrafo(self, contenido, sangria=True):
        self.contenido()
        p = self.doc.add_paragraph()
        _formato_parrafo(p, sangria=sangria and not self.referencias)
        if self.tras_tabla:
            p.paragraph_format.space_before = Pt(12)
            self.tras_tabla = False
        if self.referencias:
            p.paragraph_format.left_indent = SANGRIA
            p.paragraph_format.first_line_indent = -SANGRIA
        texto(p, contenido)

    def cita(self, contenido):
        self.contenido()
        p = self.doc.add_paragraph()
        pf = _formato_parrafo(p, sangria=False)
        pf.left_indent = SANGRIA
        texto(p, contenido)

    def rotulo(self, tipo, numero, titulo, pagina_nueva=False):
        p = self.doc.add_paragraph()
        pf = _formato_parrafo(p, sangria=False, espaciado=WD_LINE_SPACING.ONE_POINT_FIVE)
        pf.space_before = Pt(12)
        pf.page_break_before = pagina_nueva
        pf.keep_with_next = True
        texto(p, f"{tipo} {numero}", negrilla=True)
        p = self.doc.add_paragraph()
        pf = _formato_parrafo(p, sangria=False, espaciado=WD_LINE_SPACING.ONE_POINT_FIVE)
        pf.space_after = Pt(6)
        pf.keep_with_next = True
        texto(p, titulo, cursiva=True)

    def nota(self, contenido):
        self.tras_tabla = False
        p = self.doc.add_paragraph()
        pf = _formato_parrafo(p, sangria=False, espaciado=WD_LINE_SPACING.SINGLE)
        pf.space_before = Pt(6)
        pf.space_after = Pt(12)
        texto(p, "*Nota.* " + contenido)

    def tabla(self, numero, titulo, anchos, lineas):
        self.tablas.append(numero)
        self.contenido()
        self.rotulo("Tabla", numero, titulo)
        tabla(self.doc, leer_tabla(lineas), anchos, ancho_util(self.seccion))
        self.tras_tabla = True

    def figura(self, numero, archivo, titulo, nota, horizontal):
        self.figuras.append(numero)
        pagina_nueva = False
        if horizontal:
            abrio = self.asegurar("h")
            # Una figura apaisada ocupa su propia página.
            pagina_nueva = not abrio and not self.seccion_nueva
            self.seccion_nueva = False
        else:
            self.contenido()
        self.rotulo("Figura", numero, titulo, pagina_nueva)
        ruta = (AQUI / archivo).resolve()
        imagen = ImagenDocx.from_file(str(ruta))
        ancho_px, alto_px = imagen.px_width, imagen.px_height
        ancho = ancho_util(self.seccion)
        # Deja lugar para el rótulo (dos líneas) y la nota (hasta tres líneas).
        alto_max = alto_util(self.seccion) - Cm(4.2)
        if ancho * alto_px / ancho_px > alto_max:
            ancho = int(alto_max * ancho_px / alto_px)
        p = self.doc.add_paragraph()
        pf = _formato_parrafo(p, sangria=False, espaciado=WD_LINE_SPACING.SINGLE, alineacion=WD_ALIGN_PARAGRAPH.CENTER)
        pf.keep_with_next = True
        p.add_run().add_picture(str(ruta), width=ancho)
        if nota:
            self.nota(nota)
        self.tras_figura_h = horizontal

    def indice_contenido(self, titulos):
        p = self.doc.add_paragraph()
        _formato_parrafo(p, sangria=False, alineacion=WD_ALIGN_PARAGRAPH.CENTER)
        texto(p, "Contenido", negrilla=True)
        tope = ancho_util(self.seccion)
        for t in titulos:
            p = self.doc.add_paragraph()
            pf = _formato_parrafo(p, sangria=False)
            pf.tab_stops.add_tab_stop(tope, WD_TAB_ALIGNMENT.RIGHT, WD_TAB_LEADER.DOTS)
            texto(p, f"{t}\t{self.paginas_indice.get(t, '00')}")
        self.salto()

    def construir(self, lineas, titulos_indice):
        i = 0
        bloque = []

        def cerrar():
            if bloque:
                self.parrafo(" ".join(x.strip() for x in bloque))
                bloque.clear()

        while i < len(lineas):
            linea = lineas[i].rstrip("\n")
            s = linea.strip()
            if s.startswith("<!--"):
                while "-->" not in lineas[i]:
                    i += 1
                i += 1
                continue
            if not s:
                cerrar()
            elif s.startswith("## "):
                cerrar(); self.titulo(2, s[3:].strip())
            elif s.startswith("# "):
                cerrar(); self.titulo(1, s[2:].strip())
            elif s == "!portada":
                cerrar(); self.portada()
            elif s == "!indice":
                cerrar(); self.indice_contenido(titulos_indice)
            elif s == "!horizontal":
                cerrar(); self.orientacion_texto = "h"
            elif s == "!vertical":
                cerrar(); self.orientacion_texto = "v"
            elif s == "!salto":
                cerrar(); self.salto()
            elif s == "!referencias":
                cerrar(); self.referencias = True
            elif s.startswith("!nota "):
                cerrar(); self.nota(s[6:])
            elif s.startswith("!cita "):
                cerrar(); self.cita(s[6:])
            elif s.startswith("!sinsangria "):
                cerrar(); self.parrafo(s[12:], sangria=False)
            elif s.startswith("!figura "):
                cerrar()
                partes = [x.strip() for x in s[8:].split("|")]
                numero, archivo, titulo = partes[0], partes[1], partes[2]
                nota = partes[3] if len(partes) > 3 else ""
                horizontal = len(partes) > 4 and partes[4] == "horizontal"
                self.figura(numero, archivo, titulo, nota, horizontal)
            elif s.startswith("!tabla "):
                cerrar()
                partes = [x.strip() for x in s[7:].split("|")]
                anchos = [float(a) for a in partes[2].split(",")] if len(partes) > 2 and partes[2] else None
                filas = []
                i += 1
                while i < len(lineas) and lineas[i].strip().startswith("|"):
                    filas.append(lineas[i])
                    i += 1
                self.tabla(partes[0], partes[1], anchos, filas)
                continue
            else:
                bloque.append(s)
            i += 1
        cerrar()

    def guardar(self, ruta):
        self.doc.core_properties.author = "Equipo Konrad Gourmet"
        self.doc.core_properties.title = "Sistema de Abastecimiento Konrad Gourmet: documento de arquitectura"
        self.doc.core_properties.language = "es-CO"
        self.doc.save(ruta)


def comprobar_numeracion(numeros, tipo):
    simples = [n for n in numeros if n.isdigit()]
    esperado = [str(k) for k in range(1, len(simples) + 1)]
    if simples != esperado:
        raise SystemExit(f"La numeración de {tipo} no es correlativa: {simples}")


def a_pdf(docx, carpeta):
    subprocess.run(["soffice", "--headless", "--convert-to", "pdf", "--outdir", str(carpeta), str(docx)],
                   check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
    return carpeta / (docx.stem + ".pdf")


def paginas_de_titulos(pdf, titulos):
    """Busca en qué página empieza cada título de primer nivel (se salta portada e índice)."""
    salida = subprocess.run(["pdftotext", "-layout", str(pdf), "-"], capture_output=True, text=True, check=True).stdout
    paginas = salida.split("\f")
    resultado = {}
    desde = 0
    for t in titulos:
        for n in range(desde, len(paginas)):
            lineas = [l.strip() for l in paginas[n].splitlines()]
            if any(l == t for l in lineas) and n >= 2:
                resultado[t] = str(n + 1)
                desde = n
                break
    return resultado


def main():
    fuente = AQUI / sys.argv[1]
    salida = AQUI.parent / sys.argv[2]
    destino, nombre = salida.parent, salida.name
    if fuente.is_dir():
        partes = sorted(fuente.glob("*.md"))
        lineas = []
        for parte in partes:
            lineas += parte.read_text(encoding="utf-8").splitlines() + [""]
    else:
        lineas = fuente.read_text(encoding="utf-8").splitlines()
    titulos = [l.strip()[2:].strip() for l in lineas if l.startswith("# ")]
    paginas = {}
    for pasada in (1, 2, 3):
        rep = Reporte(paginas)
        rep.construir(lineas, titulos)
        comprobar_numeracion(rep.figuras, "figuras")
        comprobar_numeracion(rep.tablas, "tablas")
        docx = destino / f"{nombre}.docx"
        rep.guardar(docx)
        subprocess.run(["zip", "-qd", str(docx), "*/"], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
        pdf = a_pdf(docx, destino)
        if "!indice" not in (l.strip() for l in lineas):
            break
        nuevas = paginas_de_titulos(pdf, titulos)
        if nuevas == paginas:
            break
        faltan = [t for t in titulos if t not in nuevas]
        if faltan:
            print("Títulos que no se encontraron en el PDF:", faltan, file=sys.stderr)
        paginas = nuevas
    print(docx)
    print(pdf)


if __name__ == "__main__":
    main()
