"""Estilo y piezas comunes de las guías de sustentación (HTML autocontenido; las imágenes van en img/)."""
import html

E = html.escape

CSS = """
:root{--fondo:#f6f4f1;--papel:#ffffff;--texto:#1f2328;--suave:#5b6470;--borde:#e2ddd6;--acento:#F26B21;
--acento-suave:#fdefe1;--pin:#D81B60;--guion:#eef6ee;--guion-borde:#5a9a5a;--codigo:#f1eee9;--enlace:#b4470f}
@media (prefers-color-scheme:dark){:root:not([data-theme="light"]){--fondo:#16181c;--papel:#1f2227;--texto:#e7e9ec;
--suave:#a3abb5;--borde:#343a42;--acento:#ff8a4c;--acento-suave:#3a2a20;--guion:#1d2a1f;--guion-borde:#6fb46f;
--codigo:#2a2e35;--enlace:#ffab7a}}
*{box-sizing:border-box}
html{scroll-behavior:smooth}
body{margin:0;background:var(--fondo);color:var(--texto);font:16px/1.6 system-ui,-apple-system,"Segoe UI",Roboto,
"Helvetica Neue",Arial,sans-serif}
a{color:var(--enlace)}
code{background:var(--codigo);padding:.1em .35em;border-radius:4px;font-size:.92em}
header.portada{background:var(--papel);border-bottom:4px solid var(--acento);padding:28px 16px 20px}
header.portada>div,main{max-width:1180px;margin:0 auto}
header.portada h1{margin:0 0 4px;font-size:1.9rem;line-height:1.2}
header.portada p{margin:4px 0;color:var(--suave)}
main{padding:8px 16px 80px}
nav.indice{background:var(--papel);border:1px solid var(--borde);border-radius:10px;padding:14px 18px;margin:20px 0}
nav.indice ol{margin:6px 0 0;padding-left:22px;columns:2;column-gap:32px}
nav.indice li{break-inside:avoid;margin:2px 0}
nav.indice .min{color:var(--suave);font-size:.88em}
section.bloque{background:var(--papel);border:1px solid var(--borde);border-radius:12px;padding:20px 22px;margin:28px 0}
section.bloque>h2{margin:0 0 4px;font-size:1.45rem}
.meta{color:var(--suave);font-size:.9rem;margin-bottom:12px}
.meta span{display:inline-block;margin-right:14px}
h3{font-size:1.08rem;margin:20px 0 8px}
table{border-collapse:collapse;width:100%;font-size:.95rem}
table{table-layout:fixed}
th,td{border-bottom:1px solid var(--borde);padding:7px 8px;text-align:left;vertical-align:top;overflow-wrap:anywhere}
th.c-num{width:52px}th.c-nombre{width:30%}
@media (max-width:700px){th.c-num{width:40px}th.c-nombre{width:34%}th,td{padding:6px 5px;font-size:.9rem}}
th{background:var(--acento-suave)}
tbody tr:nth-child(even) td{background:color-mix(in srgb,var(--acento-suave) 35%,transparent)}
td.num{width:44px;text-align:center}
button.pin{all:unset;cursor:pointer;display:inline-flex;align-items:center;justify-content:center;min-width:26px;height:26px;
border-radius:50%;background:var(--pin);color:#fff;font-weight:700;font-size:.85rem;padding:0 4px}
button.pin:focus-visible{outline:3px solid var(--acento);outline-offset:2px}
.guion{background:var(--guion);border-left:5px solid var(--guion-borde);border-radius:8px;padding:12px 16px;margin-top:18px}
.guion h3{margin-top:0}
.guion p{margin:8px 0}
.preguntas dt{font-weight:600;margin-top:10px}
.preguntas dd{margin:2px 0 0 0;color:var(--texto)}
.visor{border:1px solid var(--borde);border-radius:10px;overflow:hidden;background:#fff}
.visor .barra{display:flex;flex-wrap:wrap;gap:6px;align-items:center;padding:6px 8px;background:var(--codigo);
border-bottom:1px solid var(--borde);font-size:.85rem;color:var(--suave)}
.visor .barra button{font:inherit;color:var(--texto);background:var(--papel);border:1px solid var(--borde);border-radius:6px;
padding:3px 10px;cursor:pointer}
.visor .barra a{margin-left:auto}
.visor .marco{overflow:auto;max-height:78vh;cursor:grab;touch-action:pan-x pan-y}
.visor .marco.arrastrando{cursor:grabbing}
.visor .lienzo{position:relative}
.visor .lienzo img{display:block;width:100%;height:auto;user-select:none;-webkit-user-drag:none}
.visor .zona{position:absolute;transform:translate(-50%,-50%);border-radius:50%;cursor:pointer}
.visor .zona.destello{box-shadow:0 0 0 6px rgba(242,107,33,.9),0 0 0 14px rgba(242,107,33,.35)}
.aviso{background:var(--acento-suave);border-radius:8px;padding:10px 14px;margin:12px 0}
.pie{color:var(--suave);font-size:.85rem;text-align:center;margin-top:40px}
@media (max-width:700px){nav.indice ol{columns:1}section.bloque{padding:16px 14px}header.portada h1{font-size:1.5rem}
td.num{width:34px}}
@media print{.visor .marco{max-height:none;overflow:visible}.visor .barra{display:none}section.bloque{break-inside:avoid-page}}
"""

JS_VISOR = r"""
(function(){
  document.querySelectorAll('.visor').forEach(function(v){
    var marco=v.querySelector('.marco'), lienzo=v.querySelector('.lienzo'), w=+v.dataset.w, escala=0;
    function ajustar(){ return marco.clientWidth / w; }
    function poner(e, cx, cy){
      var a=ajustar(); e=Math.max(a, Math.min(1.2, e));
      var px = cx===undefined ? (marco.scrollLeft+marco.clientWidth/2)/(w*escala) : cx;
      var py = cy===undefined ? (marco.scrollTop+marco.clientHeight/2)/(lienzo.offsetHeight||1) : cy;
      escala=e; lienzo.style.width=(w*e)+'px';
      marco.scrollLeft=px*w*e-marco.clientWidth/2;
      marco.scrollTop=py*lienzo.offsetHeight-marco.clientHeight/2;
    }
    function encajar(){ escala=ajustar(); lienzo.style.width='100%'; marco.scrollLeft=0; marco.scrollTop=0; }
    v.querySelector('[data-a=ajustar]').onclick=encajar;
    v.querySelector('[data-a=mas]').onclick=function(){ poner((escala||ajustar())*1.5); };
    v.querySelector('[data-a=menos]').onclick=function(){ var e=(escala||ajustar())/1.5; if(e<=ajustar()) encajar(); else poner(e); };
    v.querySelector('[data-a=real]').onclick=function(){ poner(1); };
    v.irA=function(n){
      var z=v.querySelector('.zona[data-n="'+n+'"]'); if(!z) return;
      var cx=parseFloat(z.style.left)/100, cy=parseFloat(z.style.top)/100;
      poner(Math.max(ajustar(), 0.8), cx, cy);
      v.scrollIntoView({behavior:'smooth', block:'nearest'});
      z.classList.add('destello'); setTimeout(function(){ z.classList.remove('destello'); }, 1600);
    };
    v.querySelectorAll('.zona').forEach(function(z){
      z.onclick=function(ev){ ev.stopPropagation(); var f=document.getElementById(v.id+'-n'+z.dataset.n);
        if(f){ f.scrollIntoView({behavior:'smooth', block:'center'}); f.classList.add('marcada');
        setTimeout(function(){ f.classList.remove('marcada'); }, 1600);} };
    });
    var x0, y0, sl, st, mov=false;
    marco.addEventListener('pointerdown', function(e){ if(e.pointerType!=='mouse') return; mov=true; x0=e.clientX; y0=e.clientY;
      sl=marco.scrollLeft; st=marco.scrollTop; marco.classList.add('arrastrando'); });
    window.addEventListener('pointermove', function(e){ if(!mov) return; marco.scrollLeft=sl-(e.clientX-x0); marco.scrollTop=st-(e.clientY-y0); });
    window.addEventListener('pointerup', function(){ mov=false; marco.classList.remove('arrastrando'); });
    window.addEventListener('resize', function(){ if(!lienzo.style.width || lienzo.style.width==='100%') escala=ajustar(); });
    escala=ajustar();
  });
  document.querySelectorAll('button.pin[data-visor]').forEach(function(b){
    b.onclick=function(){ var v=document.getElementById(b.dataset.visor); if(v&&v.irA) v.irA(b.dataset.n); };
  });
})();
"""

CSS += "tr.marcada td{background:var(--acento-suave)!important;transition:background .3s}\n"


def pagina(titulo, subtitulo, cuerpo, con_visor=False):
    return f"""<!doctype html>
<html lang="es">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>{E(titulo)}</title>
<style>{CSS}</style>
</head>
<body>
<header class="portada"><div>
<h1>{E(titulo)}</h1>
<p>{subtitulo}</p>
</div></header>
<main>
{cuerpo}
<p class="pie">Konrad Gourmet · Sistema de Abastecimiento · Arquitectura de Software, Fundación Universitaria Konrad Lorenz, 2026-2</p>
</main>
{"<script>" + JS_VISOR + "</script>" if con_visor else ""}
</body>
</html>
"""


def visor(id_, ruta, ancho, alto, pines, d, texto_alt):
    zonas = "".join(
        f'<span class="zona" data-n="{n}" title="{n}" style="left:{x / ancho * 100:.3f}%;top:{y / alto * 100:.3f}%;'
        f'width:{d / ancho * 100:.3f}%;aspect-ratio:1"></span>' for n, x, y in pines)
    return (f'<div class="visor" id="{id_}" data-w="{ancho}" data-h="{alto}">'
            f'<div class="barra"><button data-a="ajustar">Ajustar</button><button data-a="menos">−</button>'
            f'<button data-a="mas">+</button><button data-a="real">Tamaño real</button>'
            f'<span>Arrastra para moverte; toca un número para ir a su fila.</span>'
            f'<a href="{E(ruta)}" target="_blank" rel="noopener">Abrir la imagen</a></div>'
            f'<div class="marco"><div class="lienzo"><img src="{E(ruta)}" alt="{E(texto_alt)}" width="{ancho}" '
            f'height="{alto}" loading="lazy">{zonas}</div></div></div>')
