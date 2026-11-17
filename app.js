/* Konrad Gourmet — interfaz web de la demo (mockups V01 a V23).
 * Cada rol tiene su panel izquierdo, su inicio y sus ventanas, como en DISEÑO UXUI.pdf.
 * Los datos vienen de ServidorWeb.java, que usa las clases del Ensamble en memoria. */
'use strict';

// ----------------------------------------------------------------------------- roles

const ROLES = {
  mesero: {
    nombre: 'Isabella Torres', saludo: 'Isabella Torres', cargo: 'Mesero', sede: 'Sucursal Centro',
    correo: 'isabella.torres@konradgourmet.com', codigo: 'MESERO', iniciales: 'IT',
    menu: [
      { id: 'inicio', t: 'Inicio', i: 'home' },
      { id: 'pedidos', t: 'Pedidos', i: 'clipboard' },
      { id: 'nuevo-pedido', t: 'Nuevo pedido', i: 'plusCircle' },
    ],
  },
  jefe: {
    nombre: 'Carlos Mendoza', saludo: 'Carlos Mendoza', cargo: 'Jefe de Cocina', sede: 'Sucursal Centro',
    correo: 'carlos.mendoza@konradgourmet.com', codigo: 'JEFE_COCINA', iniciales: 'CM',
    menu: [
      { id: 'inicio', t: 'Inicio', i: 'home' },
      { id: 'platos', t: 'Platos', i: 'utensils' },
      { id: 'inventario', t: 'Inventario', i: 'box' },
      { id: 'solicitudes', t: 'Solicitudes', i: 'file' },
      { id: 'alertas', t: 'Alertas', i: 'bell' },
    ],
  },
  auxiliar: {
    nombre: 'Isabella Castro', saludo: 'Isabella', cargo: 'Auxiliar Área de Compras', sede: 'Sede Centro',
    correo: 'isabella.castro@konradgourmet.com', codigo: 'AUXILIAR_COMPRAS', iniciales: 'IC',
    menu: [
      { id: 'inicio', t: 'Inicio', i: 'home' },
      { t: 'Solicitudes', i: 'file', hijos: [{ id: 'solicitudes', t: 'Todas las solicitudes' }] },
      { t: 'Cotizaciones', i: 'file', hijos: [{ id: 'cotizaciones', t: 'Cotizaciones enviadas' }] },
    ],
  },
  director: {
    nombre: 'Laura Gómez', saludo: 'Laura', cargo: 'Directora Área de Compras', sede: 'Sede Centro',
    correo: 'laura.gomez@konradgourmet.com', codigo: 'DIRECTOR_COMPRAS', iniciales: 'LG',
    // se reemplaza por el árbol que arma DirectorCompras.construirMenu() (Composite + Factory Method)
    menu: [
      { id: 'inicio', t: 'Inicio', i: 'home' },
      { t: 'Cotizaciones', i: 'file', hijos: [
        { id: 'cotizaciones', t: 'Todas' }, { id: 'pendientes', t: 'Pendientes de validar' }, { id: 'opcionadas', t: 'Opcionadas' }] },
      { id: 'ordenes', t: 'Órdenes de compra', i: 'receipt' },
    ],
  },
};

const RUTAS_DIRECTOR = {
  '/inicio': { id: 'inicio', i: 'home' }, '/cotizaciones': { i: 'file' },
  '/cotizaciones/todas': { id: 'cotizaciones' }, '/cotizaciones/pendientes': { id: 'pendientes' },
  '/cotizaciones/opcionadas': { id: 'opcionadas' }, '/ordenes-compra': { id: 'ordenes', i: 'receipt' },
};

// ----------------------------------------------------------------------------- estado

let S = null;          // estado que devuelve /api/state
let rol = null;
let vista = 'inicio';
const ui = {
  sel: {}, pag: {}, abiertos: {},
  cat: 'Todos', mesa: 5, carrito: null, cantidadTarjeta: {},
  invBuscar: '', invCat: '',
  filtroSol: { fecha: '', estado: '' },
  hu07: { desde: '', hasta: '', producto: '', marca: '', estado: '' }, hu07Res: null, hu07Error: '',
  filtroCot: { proveedor: '', fecha: '', estado: '' }, filtroCotBorrador: { proveedor: '', fecha: '', estado: '' },
  filtroDir: { proveedor: '', estado: '', fecha: '' }, filtroOrd: { fecha: '', proveedor: '', estado: '' },
  compararSol: '', cal: null,
  modal: null, m: {},
};

// ----------------------------------------------------------------------------- utilidades

const $ = (s, r = document) => r.querySelector(s);
const $$ = (s, r = document) => [...r.querySelectorAll(s)];
const esc = (s) => String(s ?? '').replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[c]);
const NUM0 = new Intl.NumberFormat('es-CO', { maximumFractionDigits: 0 });
const NUM2 = new Intl.NumberFormat('es-CO', { maximumFractionDigits: 2 });
const PCT = new Intl.NumberFormat('es-CO', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
const money = (v) => (v == null ? '—' : '$' + NUM0.format(v));
const num = (v) => (v == null ? '—' : NUM2.format(v));
const pct = (v) => (v == null ? '—' : (v > 0 ? '+' : v < 0 ? '−' : '') + PCT.format(Math.abs(v)) + ' %');
const pad2 = (n) => String(n).padStart(2, '0');
const aFecha = (s) => {
  if (!s) return null;
  if (/^\d{4}-\d{2}-\d{2}$/.test(s)) { const [y, m, d] = s.split('-').map(Number); return new Date(y, m - 1, d); }
  return new Date(s.slice(0, 23));
};
const fecha = (s) => { const d = aFecha(s); return d ? `${pad2(d.getDate())}/${pad2(d.getMonth() + 1)}/${d.getFullYear()}` : '—'; };
const hora = (s) => { const d = aFecha(s); return d ? d.toLocaleTimeString('es-CO', { hour: 'numeric', minute: '2-digit' }) : '—'; };
const hoyISO = (masDias = 0) => { const d = new Date(); d.setDate(d.getDate() + masDias); return `${d.getFullYear()}-${pad2(d.getMonth() + 1)}-${pad2(d.getDate())}`; };
const mayus = (s) => s.charAt(0).toUpperCase() + s.slice(1);
const plural = (n, uno, varios) => `${n} ${n === 1 ? uno : varios}`;
const saludo = () => { const h = new Date().getHours(); return h < 12 ? 'Buenos días' : h < 19 ? 'Buenas tardes' : 'Buenas noches'; };
const desc = (a, k = 'numero') => [...a].sort((x, y) => String(y[k]).localeCompare(String(x[k])));
const asc = (a, k = 'numero') => [...a].sort((x, y) => String(x[k]).localeCompare(String(y[k])));
const enRango = (s, filtro) => {
  if (!filtro) return true;
  const d = aFecha(s); if (!d) return false;
  const hoy = new Date(); hoy.setHours(0, 0, 0, 0);
  if (filtro === 'hoy') return d >= hoy;
  const hace7 = new Date(hoy); hace7.setDate(hoy.getDate() - 7);
  return d >= hace7;
};

const ICONOS = {
  home: '<path d="m3 10 9-7 9 7v10a1 1 0 0 1-1 1h-5v-6H9v6H4a1 1 0 0 1-1-1z"/>',
  clipboard: '<rect x="8" y="2" width="8" height="4" rx="1"/><path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2"/><path d="M9 12h6M9 16h6"/>',
  plusCircle: '<circle cx="12" cy="12" r="9"/><path d="M12 8v8M8 12h8"/>',
  plus: '<path d="M12 5v14M5 12h14"/>',
  minus: '<path d="M5 12h14"/>',
  user: '<circle cx="12" cy="8" r="4"/><path d="M4 21a8 8 0 0 1 16 0"/>',
  logout: '<path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><path d="m16 17 5-5-5-5M21 12H9"/>',
  utensils: '<path d="M3 2v7a2 2 0 0 0 2 2h2a2 2 0 0 0 2-2V2M6 2v20M21 15V2a5 5 0 0 0-5 5v6a2 2 0 0 0 2 2h3zm0 0v7"/>',
  box: '<path d="M21 8 12 3 3 8v8l9 5 9-5z"/><path d="m3 8 9 5 9-5M12 13v8"/>',
  file: '<path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><path d="M14 2v6h6M8 13h8M8 17h6"/>',
  bell: '<path d="M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9"/><path d="M10.3 21a1.94 1.94 0 0 0 3.4 0"/>',
  calendar: '<rect x="3" y="4" width="18" height="18" rx="2"/><path d="M16 2v4M8 2v4M3 10h18"/>',
  clock: '<circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/>',
  search: '<circle cx="11" cy="11" r="7"/><path d="m21 21-4.3-4.3"/>',
  chevR: '<path d="m9 18 6-6-6-6"/>', chevL: '<path d="m15 18-6-6 6-6"/>', chevD: '<path d="m6 9 6 6 6-6"/>', chevU: '<path d="m18 15-6-6-6 6"/>',
  arrowR: '<path d="M5 12h14M13 6l6 6-6 6"/>',
  check: '<path d="M20 6 9 17l-5-5"/>',
  x: '<path d="M18 6 6 18M6 6l12 12"/>',
  trash: '<path d="M3 6h18M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6M10 11v6M14 11v6"/>',
  cart: '<circle cx="9" cy="20" r="1.4"/><circle cx="18" cy="20" r="1.4"/><path d="M2 3h3l2.6 12.2a2 2 0 0 0 2 1.6h8.2a2 2 0 0 0 2-1.5L22 7H6"/>',
  alert: '<path d="m10.3 3.9-8.2 14a2 2 0 0 0 1.7 3h16.4a2 2 0 0 0 1.7-3l-8.2-14a2 2 0 0 0-3.4 0z"/><path d="M12 9v4M12 17h.01"/>',
  refresh: '<path d="M21 12a9 9 0 0 1-15.5 6.2L3 16M3 12a9 9 0 0 1 15.5-6.2L21 8"/><path d="M21 3v5h-5M3 21v-5h5"/>',
  star: '<path d="m12 2 3.1 6.3 6.9 1-5 4.9 1.2 6.8L12 17.8 5.8 21l1.2-6.8-5-4.9 6.9-1z"/>',
  send: '<path d="m22 2-7 20-4-9-9-4z"/><path d="M22 2 11 13"/>',
  download: '<path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><path d="m7 10 5 5 5-5M12 15V3"/>',
  scale: '<path d="M12 3v18M7 21h10M4 7h16M7 7l-3 7a3 3 0 0 0 6 0zM17 7l-3 7a3 3 0 0 0 6 0z"/>',
  chart: '<path d="M3 3v18h18"/><path d="M8 17v-6M13 17V7M18 17v-4"/>',
  info: '<circle cx="12" cy="12" r="9"/><path d="M12 16v-4M12 8h.01"/>',
  image: '<rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="9" cy="9" r="2"/><path d="m21 15-5-5L5 21"/>',
  upload: '<path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><path d="m17 8-5-5-5 5M12 3v12"/>',
  table: '<path d="M3 8h18M6 8v12M18 8v12"/><path d="M8 8V5h8v3"/>',
  store: '<path d="M3 9 4.5 4h15L21 9M3 9v11h18V9M3 9h18M9 20v-6h6v6"/>',
  tag: '<path d="M20.6 13.4 13.4 20.6a2 2 0 0 1-2.8 0L3 13V3h10l7.6 7.6a2 2 0 0 1 0 2.8z"/><circle cx="7.5" cy="7.5" r="1.5"/>',
  pencil: '<path d="M17 3a2.8 2.8 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5z"/>',
  receipt: '<path d="M4 2v20l3-2 3 2 3-2 3 2 3-2 3 2V2l-3 2-3-2-3 2-3-2-3 2z"/><path d="M8 8h8M8 12h8M8 16h5"/>',
  bowl: '<path d="M3 11h18a9 9 0 0 1-18 0zM7 21h10M12 3v4M8 5l1 3M16 5l-1 3"/>',
  circleCheck: '<circle cx="12" cy="12" r="9"/><path d="m8 12 3 3 5-6"/>',
  circleX: '<circle cx="12" cy="12" r="9"/><path d="m15 9-6 6M9 9l6 6"/>',
  filter: '<path d="M3 4h18l-7 8v6l-4 2v-8z"/>',
};
const icon = (n, cls = '') => `<svg class="ic ${cls}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">${ICONOS[n] || ''}</svg>`;

const EMOJI = {
  'Bandeja paisa': '🍛', 'Ajiaco': '🍲', 'Punta de anca': '🥩', 'Pechuga a la plancha': '🍗', 'Ensalada césar': '🥗',
  'Arepa con queso': '🫓', 'Limonada natural': '🍋', 'Jugo natural': '🧃', 'Res': '🥩', 'Arroz': '🍚', 'Papa': '🥔', 'Pollo': '🍗',
};
const thumb = (nombre, cls = '', plato = null) => plato && plato.imagen
  ? `<span class="thumb ${cls}"><img src="/api/imagen/${plato.id}" alt=""></span>`
  : `<span class="thumb ${cls}" aria-hidden="true">${EMOJI[nombre] || '🍽️'}</span>`;

// estados: texto y color (V02, V05, V10, V11, V15, V17, V20, V23)
const ESTADOS = {
  EN_PREPARACION: ['En preparación', 'naranja'], LISTO: ['Listo', 'verde'], SERVIDO: ['Servido', 'verde'],
  PENDIENTE: ['Pendiente', 'ambar'], EN_TRAMITE: ['En trámite', 'azul'], COMPLETADA: ['Completada', 'verde'],
  PENDIENTE_PRECIO: ['Pendiente de precio', 'gris'], ENVIADA: ['Enviada', 'naranja'], RECHAZADA: ['Rechazada', 'rojo'],
  SOSPECHOSA: ['Sospechosa', 'ambar'], OPCIONADA: ['Opcionada', 'azul'], CON_ORDEN: ['Con orden', 'verde'],
  EN_PROCESO: ['En proceso', 'naranja'], FINALIZADA: ['Finalizada', 'verde'],
};
const badge = (estado, { up = false, dot = false } = {}) => {
  const [t, c] = ESTADOS[estado] || [estado, 'gris'];
  return `<span class="badge ${c}${up ? ' up' : ''}${dot ? ' dot' : ''}">${esc(up ? t.toUpperCase() : t)}</span>`;
};

const producto = (id) => (S.insumos.find((i) => i.id === id) || {}).nombre || `Producto ${id}`;
const cot = (n) => S.cotizaciones.find((c) => c.numero === n);
const sol = (n) => S.solicitudes.find((s) => s.numero === n);
const ord = (n) => S.ordenes.find((o) => o.numero === n);
const ped = (n) => S.pedidos.find((p) => p.numero === n);
const sinPrecios = (c) => c.items.some((i) => i.precioProveedor == null);
const porRegistrar = () => S.cotizaciones.filter((c) => c.estado === 'ENVIADA' && sinPrecios(c));
const porValidar = () => S.cotizaciones.filter((c) => c.estado === 'ENVIADA' && !sinPrecios(c));

// ----------------------------------------------------------------------------- piezas comunes

function topbar(t) {
  const titulo = t.saludo
    ? `<h1>${saludo()}, <em>${esc(t.saludo)}</em></h1><p>${esc(t.sub)}</p>`
    : `<div class="page-title">${t.icono ? `<span class="title-ico">${icon(t.icono)}</span>` : ''}<div><h1>${esc(t.titulo)}</h1><p>${esc(t.sub)}</p></div></div>`;
  return `<header class="topbar ${t.saludo ? 'is-greeting' : ''}">
    <div class="topbar-text">${titulo}</div>
    <div class="topbar-chips">
      <span class="chip">${icon('calendar')}<span class="js-fecha">${fechaLarga()}</span></span>
      <span class="chip">${icon('clock')}<span class="js-hora">${horaActual()}</span></span>
      ${t.accion || ''}
    </div>
  </header>`;
}
const fechaLarga = () => mayus(new Date().toLocaleDateString('es-CO', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' }));
const horaActual = () => new Date().toLocaleTimeString('es-CO', { hour: 'numeric', minute: '2-digit' });

const card = (titulo, cuerpo, { icono = '', sub = '', extra = '', cls = '' } = {}) => `
  <section class="card ${cls}">
    ${titulo ? `<div class="card-head"><div class="card-title">${icono ? `<span class="title-ico sm">${icon(icono)}</span>` : ''}<div><h2>${titulo}</h2>${sub ? `<p>${sub}</p>` : ''}</div></div>${extra}</div>` : ''}
    ${cuerpo}
  </section>`;
const tabla = (cabeceras, filas, vacio = 'Aún no hay registros.', cols = cabeceras.length) =>
  `<div class="table-wrap"><table><thead><tr>${cabeceras.map((h) => `<th>${h}</th>`).join('')}</tr></thead><tbody>${
    filas.length ? filas.join('') : `<tr><td colspan="${cols}" class="empty">${vacio}</td></tr>`}</tbody></table></div>`;
const btnSoft = (texto, attrs) => `<button class="btn soft sm" ${attrs}>${texto} ${icon('arrowR')}</button>`;
const kpi = (icono, valor, etiqueta, tono = '') => `<div class="kpi ${tono}"><span class="kpi-ico">${icon(icono)}</span><div><strong>${valor}</strong><span>${etiqueta}</span></div></div>`;
const dl = (pares) => `<dl class="dl">${pares.map(([k, v]) => `<dt>${k}</dt><dd>${v}</dd>`).join('')}</dl>`;
const vacioDetalle = (texto) => `<div class="empty-detail">${icon('file')}<p>${texto}</p></div>`;

function paginar(id, filas, porPagina = 5) {
  const total = filas.length;
  const paginas = Math.max(1, Math.ceil(total / porPagina));
  const p = Math.min(ui.pag[id] || 1, paginas);
  const desde = (p - 1) * porPagina;
  const pie = `<div class="pager"><span>Mostrando ${total ? desde + 1 : 0} - ${Math.min(desde + porPagina, total)} de ${total}</span><div>
    <button class="pg" data-act="pagina" data-id="${id}" data-p="${p - 1}" ${p === 1 ? 'disabled' : ''} aria-label="Anterior">${icon('chevL')}</button>
    ${Array.from({ length: paginas }, (_, k) => `<button class="pg ${k + 1 === p ? 'on' : ''}" data-act="pagina" data-id="${id}" data-p="${k + 1}">${k + 1}</button>`).join('')}
    <button class="pg" data-act="pagina" data-id="${id}" data-p="${p + 1}" ${p === paginas ? 'disabled' : ''} aria-label="Siguiente">${icon('chevR')}</button>
  </div></div>`;
  return { filas: filas.slice(desde, desde + porPagina), pie };
}

function sidebar() {
  const R = ROLES[rol];
  const item = (it) => {
    if (it.hijos) {
      const activo = it.hijos.some((h) => h.id === vista);
      const abierto = activo || ui.abiertos[it.t];
      return `<div class="nav-group ${abierto ? 'open' : ''}">
        <button class="nav-item ${activo ? 'parent-active' : ''}" data-act="grupo" data-t="${esc(it.t)}" data-go1="${it.hijos[0].id}">${icon(it.i)}<span>${esc(it.t)}</span>${icon(abierto ? 'chevD' : 'chevR', 'chev')}</button>
        <div class="nav-sub">${it.hijos.map((h) => `<button class="${h.id === vista ? 'active' : ''}" data-go="${h.id}"><i></i>${esc(h.t)}</button>`).join('')}</div>
      </div>`;
    }
    return `<button class="nav-item ${it.id === vista ? 'active' : ''}" data-go="${it.id}">${icon(it.i)}<span>${esc(it.t)}</span></button>`;
  };
  return `
    <div class="brand"><img src="assets/logo/logo.png" alt=""><div><b>Konrad <span>Gourmet</span></b><small>Sistema de Abastecimiento</small></div></div>
    <div class="profile">
      <div class="avatar"><span>${R.iniciales}</span><i class="online" title="En línea"></i></div>
      <b>${esc(R.nombre)}</b><span class="cargo">${esc(R.cargo)}</span><small>${esc(R.sede)}</small>
    </div>
    <nav class="nav" aria-label="Menú principal">${R.menu.map(item).join('')}</nav>
    <div class="nav-foot">
      <button class="nav-item ${vista === 'perfil' ? 'active' : ''}" data-go="perfil">${icon('user')}<span>Mi perfil</span></button>
      <button class="nav-item" data-act="salir">${icon('logout')}<span>Cerrar sesión</span></button>
    </div>`;
}

// ----------------------------------------------------------------------------- MESERO (V02 a V04)

const mesaTxt = (n) => `Mesa ${pad2(n)}`;
const nombrePlatos = (p) => p.detalles.map((d) => d.plato).join(', ');

const MESERO = {
  // V02 · Dashboard del mesero
  inicio() {
    const pedidos = desc(S.pedidos);
    const activos = pedidos.filter((p) => p.estado !== 'SERVIDO');
    const mesas = [...new Map(activos.map((p) => [p.mesa, p])).values()];
    const recientes = pedidos.slice(0, 5).map((p) => `
      <tr class="clickable" data-go="pedidos" data-sel="${p.numero}"><td><b>${p.numero}</b></td><td>Mesa ${p.mesa}</td><td>${plural(p.platos, 'plato', 'platos')}</td>
      <td>${hora(p.fecha)}</td><td>${badge(p.estado)}</td><td class="act">${icon('chevR')}</td></tr>`);
    const listaMesas = mesas.length ? mesas.map((p) => `
      <button class="mesa-row tono-${(ESTADOS[p.estado] || [])[1]}" data-go="pedidos" data-sel="${p.numero}">
        <span class="bar"></span><div><b>${mesaTxt(p.mesa)}</b> <small>${plural(p.platos, 'plato', 'platos')}</small><span>Siguiente: ${esc(p.detalles[0]?.plato || '—')}</span></div>
        ${badge(p.estado)}${icon('chevR', 'chev')}</button>`).join('') : `<p class="muted pad">No hay mesas con pedidos en curso.</p>`;
    const entregas = activos.filter((p) => p.estado === 'EN_PREPARACION').slice(0, 3).map((p) => {
      const min = Math.max(0, Math.round((Date.now() - aFecha(p.fecha)) / 60000));
      return `<div class="entrega"><span class="when"><i></i>${min} min</span>${thumb(p.detalles[0]?.plato, 'sm')}
        <div><b>${mesaTxt(p.mesa)}</b><small>${esc(p.detalles[0]?.plato)} (${plural(p.platos, 'plato', 'platos')})</small></div></div>`;
    }).join('') || `<p class="muted pad">Sin entregas pendientes.</p>`;
    return {
      top: { saludo: ROLES.mesero.saludo, sub: 'Mesero · Sucursal Centro' },
      html: `
        <div class="kpi-cta">
          ${kpi('clipboard', pedidos.length, 'Pedidos de hoy')}
          ${kpi('table', new Set(pedidos.map((p) => p.mesa)).size, 'Mesas atendidas')}
          <button class="cta" data-go="nuevo-pedido">${icon('plus')} Registrar nuevo pedido</button>
        </div>
        <div class="grid main-side">
          ${card('Pedidos recientes', tabla(['Nº pedido', 'Mesa', 'Platos', 'Hora', 'Estado', ''], recientes, 'Todavía no hay pedidos en el turno. Usa «Registrar nuevo pedido».'),
            { extra: btnSoft('Ver todos los pedidos', 'data-go="pedidos"'), cls: 'underline' })}
          <div class="stack">
            ${card('Mesas en servicio', `<div class="mesas">${listaMesas}</div>`, { extra: `<span class="pill-count">${plural(mesas.length, 'mesa activa', 'mesas activas')}</span>` })}
            ${card('Próximas entregas', `<div class="entregas">${entregas}</div>`, { extra: btnSoft('Ver todos', 'data-go="pedidos"') })}
          </div>
        </div>`,
    };
  },

  // Listado de pedidos del turno (V02 «Ver todos los pedidos»)
  pedidos() {
    const pedidos = desc(S.pedidos);
    const sel = ped(ui.sel.pedidos) || pedidos[0];
    const filas = pedidos.map((p) => `
      <tr class="clickable ${sel && p.numero === sel.numero ? 'selected' : ''}" data-act="sel" data-v="pedidos" data-n="${p.numero}">
        <td><b>${p.numero}</b></td><td>Mesa ${p.mesa}</td><td>${plural(p.platos, 'plato', 'platos')}</td><td>${hora(p.fecha)}</td><td>${money(p.total)}</td><td>${badge(p.estado)}</td></tr>`);
    const detalle = sel ? `
      <div class="detail-head"><h3>Pedido ${sel.numero}</h3>${badge(sel.estado)}</div>
      <div class="facts four">
        <div>${icon('table')}<small>Mesa</small><b>${pad2(sel.mesa)}</b></div><div>${icon('utensils')}<small>Platos</small><b>${sel.platos}</b></div>
        <div>${icon('calendar')}<small>Fecha</small><b>${fecha(sel.fecha)}</b></div><div>${icon('clock')}<small>Hora</small><b>${hora(sel.fecha)}</b></div>
      </div>
      <div class="lines">${sel.detalles.map((d) => `<div class="line">${thumb(d.plato, 'sm')}<div><b>${esc(d.plato)}</b><small>${d.cantidad} × ${money(d.precio)}${d.nota ? ` · ${esc(d.nota)}` : ''}</small></div><b>${money(d.subtotal)}</b></div>`).join('')}</div>
      <div class="total-row"><span>Total</span><b>${money(sel.total)}</b></div>` : vacioDetalle('Selecciona un pedido para ver su detalle.');
    return {
      top: { icono: 'clipboard', titulo: 'Pedidos', sub: 'Consulta los pedidos registrados en el turno', accion: `<button class="btn primary" data-go="nuevo-pedido">${icon('plus')} Nuevo pedido</button>` },
      html: `<div class="grid main-side">
        ${card('Pedidos del turno', tabla(['Nº pedido', 'Mesa', 'Platos', 'Hora', 'Total', 'Estado'], filas, 'Todavía no hay pedidos en el turno.'), { icono: 'clipboard' })}
        ${card('Detalle del pedido', detalle, { icono: 'file' })}
      </div>`,
    };
  },

  // V03 · Nuevo pedido: catálogo, filtros y pedido actual
  'nuevo-pedido'() {
    if (ui.carrito === null) {
      // primera vez: el pedido de la demo (2 bandejas paisas «Sin arepa» y 1 ajiaco = $82.000)
      ui.carrito = {};
      if (!S.pedidos.length) {
        const id = (n) => (S.platos.find((p) => p.nombre === n) || {}).id;
        if (id('Bandeja paisa')) ui.carrito[id('Bandeja paisa')] = { cantidad: 2, nota: 'Sin arepa' };
        if (id('Ajiaco')) ui.carrito[id('Ajiaco')] = { cantidad: 1, nota: '' };
      }
    }
    const cats = ['Todos', 'Fuertes', 'Entradas', 'Bebidas', 'Postres', 'Sopas'];
    const platos = S.platos.filter((p) => p.enCatalogo && (ui.cat === 'Todos' || p.categoria === ui.cat));
    const tarjetas = platos.map((p) => {
      const q = ui.cantidadTarjeta[p.id] || 1;
      return `<article class="dish" data-buscar="${esc((p.nombre + ' ' + (p.descripcion || '')).toLowerCase())}">
        ${thumb(p.nombre, 'dish-img', p)}
        <h4>${esc(p.nombre)}</h4><p>${esc(p.descripcion || '')}</p><b class="price">${money(p.precio)}</b>
        <div class="stepper"><button data-act="qty-tarjeta" data-id="${p.id}" data-d="-1" aria-label="Menos">${icon('minus')}</button><span>${q}</span><button data-act="qty-tarjeta" data-id="${p.id}" data-d="1" aria-label="Más">${icon('plus')}</button></div>
        <label class="note">${icon('pencil')}<input data-nota="${p.id}" placeholder="Ej. Sin cebolla..." aria-label="Nota para ${esc(p.nombre)}"></label>
        <button class="btn primary sm w" data-act="agregar" data-id="${p.id}">${icon('plus')} Agregar</button>
      </article>`;
    }).join('') || `<p class="muted pad">No hay platos en esta categoría.</p>`;
    const items = Object.entries(ui.carrito).map(([id, it]) => ({ p: S.platos.find((x) => x.id === Number(id) && x.enCatalogo), ...it })).filter((x) => x.p);
    const totalPlatos = items.reduce((a, x) => a + x.cantidad, 0);
    const totalPagar = items.reduce((a, x) => a + x.cantidad * x.p.precio, 0);
    const lineas = items.map((x) => `
      <div class="oi">${thumb(x.p.nombre, 'sm', x.p)}
        <div class="oi-main"><b>${esc(x.p.nombre)}</b><span class="price">${money(x.p.precio)}</span>
          <div class="stepper sm"><button data-act="qty-carrito" data-id="${x.p.id}" data-d="-1" aria-label="Menos">${icon('minus')}</button><span>${x.cantidad}</span><button data-act="qty-carrito" data-id="${x.p.id}" data-d="1" aria-label="Más">${icon('plus')}</button></div>
        </div>
        <b class="oi-sub">${money(x.cantidad * x.p.precio)}</b>
        <div class="oi-note">${icon('pencil')}<span>${esc(x.nota || 'Sin notas')}</span><button class="icon-btn danger" data-act="quitar" data-id="${x.p.id}" aria-label="Quitar">${icon('trash')}</button></div>
      </div>`).join('') || `<p class="muted pad">Agrega platos del catálogo.</p>`;
    return {
      top: { saludo: ROLES.mesero.saludo, sub: 'Mesero · Sucursal Centro' },
      html: `<div class="grid pos">
        <section class="card catalog">
          <div class="catalog-bar">
            <label class="search">${icon('search')}<input id="buscar-plato" data-inp="buscar-plato" placeholder="Buscar plato, bebida o entrada..." aria-label="Buscar plato"></label>
            <div class="chips">${cats.map((c) => `<button class="fchip ${ui.cat === c ? 'on' : ''}" data-act="categoria" data-c="${c}">${c}</button>`).join('')}</div>
          </div>
          <div class="dish-grid">${tarjetas}</div>
        </section>
        <aside class="card order">
          <label class="mesa-sel">${icon('table')}<span><small>Mesa</small>
            <select data-chg="mesa" aria-label="Mesa">${S.mesas.map((m) => `<option value="${m.numero}" ${m.numero === ui.mesa ? 'selected' : ''}>${mesaTxt(m.numero)}</option>`).join('')}</select></span></label>
          <h3 class="order-title">${icon('clipboard')} Pedido actual <span>(${items.length})</span></h3>
          <div class="order-items">${lineas}</div>
          <div class="order-tot">
            <div>${icon('utensils')}<span>Total de platos:</span><b>${totalPlatos}</b></div>
            <div>${icon('receipt')}<span>Total a pagar:</span><b class="big">${money(totalPagar)}</b></div>
          </div>
          <button class="btn ghost w" data-act="cancelar-pedido">Cancelar</button>
          <button class="btn primary w" data-act="registrar-pedido" ${items.length ? '' : 'disabled'}>${icon('check')} Registrar pedido</button>
        </aside>
      </div>`,
    };
  },
};

// ----------------------------------------------------------------------------- JEFE DE COCINA (V05 a V10)

const barra = (p) => `<span class="meter"><i style="width:${Math.min(100, Math.max(3, p))}%" class="${p < 20 ? 'rojo' : 'naranja'}"></i></span>`;

const JEFE = {
  // V05 · Dashboard con alertas
  inicio() {
    const alertas = S.insumos.filter((i) => i.alerta);
    const sols = desc(S.solicitudes);
    const filas = sols.slice(0, 5).map((s) => `
      <tr class="clickable" data-go="solicitudes" data-sel="${s.numero}"><td><b>${s.numero}</b></td><td>${fecha(s.fecha)}</td><td>${s.items.length}</td><td>${badge(s.estado)}</td>
      <td class="act"><span class="round-btn">${icon('chevR')}</span></td></tr>`);
    const listaAlertas = alertas.map((i) => `
      <div class="alert-row">${thumb(i.nombre, 'sm')}<b>${esc(i.nombre)}</b><span class="pct ${i.porcentaje < 20 ? 'rojo' : 'naranja'}">${num(i.porcentaje)}%</span>${barra(i.porcentaje)}
        <button class="round-btn" data-act="nueva-solicitud" data-producto="${i.id}" aria-label="Solicitar ${esc(i.nombre)}">${icon('chevR')}</button></div>`).join('')
      || `<p class="muted pad">No hay alertas activas. El cierre diario (23:59) actualiza el inventario.</p>`;
    return {
      top: { saludo: ROLES.jefe.saludo, sub: 'Jefe de Cocina · Sucursal Centro' },
      html: `
        <div class="kpis four">
          ${kpi('bowl', S.platos.filter((p) => p.activo).length, 'Platos registrados')}
          ${kpi('alert', alertas.length, 'Stock bajo', 'danger')}
          ${kpi('file', S.solicitudes.filter((s) => s.estado === 'PENDIENTE').length, 'Solicitudes pendientes')}
          ${kpi('refresh', S.solicitudes.filter((s) => s.estado === 'EN_TRAMITE').length, 'Solicitudes en trámite')}
        </div>
        <div class="grid halves">
          ${card('Alertas de inventario', `<div class="alert-list">${listaAlertas}</div>`, { icono: 'bell', extra: btnSoft('Ver inventario', 'data-go="inventario"') })}
          ${card('Solicitudes recientes', tabla(['Nº solicitud', 'Fecha', 'Productos', 'Estado', 'Acción'], filas, 'Aún no hay solicitudes.'), { icono: 'file', extra: btnSoft('Ver todas', 'data-go="solicitudes"') })}
        </div>`,
    };
  },

  // V06 · Platos: listado, detalle y «Nuevo plato»
  platos() {
    const sel = S.platos.find((p) => p.id === Number(ui.sel.platos)) || S.platos[0];
    const filas = S.platos.map((p) => `
      <tr class="clickable ${sel && p.id === sel.id ? 'selected' : ''}" data-act="sel" data-v="platos" data-n="${p.id}">
        <td><span class="cell-thumb">${thumb(p.nombre, 'sm', p)}<b>${esc(p.nombre)}</b></span></td><td>${esc(p.categoria || '—')}</td><td>${money(p.precio)}</td><td>${p.activo ? '<span class="badge verde">Activo</span>' : '<span class="badge gris">Inactivo</span>'}</td></tr>`);
    const detalle = sel ? `
      ${thumb(sel.nombre, 'hero', sel)}
      <div class="detail-head"><h3>${esc(sel.nombre)}</h3>${sel.activo ? '<span class="badge verde">Activo</span>' : '<span class="badge gris">Inactivo</span>'}</div>
      <div class="detail-actions"><button class="btn outline sm" data-act="editar-plato" data-id="${sel.id}">${icon('pencil')} Editar plato</button>
        <button class="btn ${sel.activo ? 'ghost' : 'primary'} sm" data-act="estado-plato" data-id="${sel.id}" data-activo="${sel.activo ? 0 : 1}">${sel.activo ? 'Desactivar' : 'Activar'}</button></div>
      <div class="price-line"><span>Precio de venta</span><b>${money(sel.precio)}</b></div>
      ${dl([['Categoría', esc(sel.categoria || '—')], ['Descripción', esc(sel.descripcion || '—')]])}
      <h4 class="sub-title">${icon('utensils')} Ingredientes <small>(${sel.receta.length})</small></h4>
      ${sel.receta.length ? `<ul class="ing-list">${sel.receta.map((r) => `<li>${thumb(r.insumo, 'xs')}<span>${esc(r.insumo)}</span><b>${num(r.cantidad)} ${esc(r.unidad)}</b></li>`).join('')}</ul>` : '<p class="muted">Este plato no descuenta insumos del inventario.</p>'}`
      : vacioDetalle('Selecciona un plato.');
    return {
      top: { icono: 'utensils', titulo: 'Platos', sub: 'Gestiona el menú y consulta los detalles de cada plato', accion: `<button class="btn primary" data-act="nuevo-plato">${icon('plus')} Nuevo plato</button>` },
      html: `<div class="grid main-side">
        ${card('Lista de platos', tabla(['Plato', 'Categoría', 'Precio', 'Estado'], filas), { icono: 'clipboard', sub: `${plural(S.platos.length, 'plato registrado', 'platos registrados')} en el menú` })}
        ${card('', detalle, { cls: 'plate-detail' })}
      </div>`,
    };
  },

  // V07 · Inventario + alertas
  inventario() {
    const bajos = S.insumos.filter((i) => i.porcentaje < 25);
    const cats = [...new Set(S.insumos.map((i) => i.categoria))];
    const buscar = ui.invBuscar.trim().toLowerCase();
    const filas = S.insumos.filter((i) => (!ui.invCat || i.categoria === ui.invCat) && (!buscar || (i.nombre + ' ' + i.categoria).toLowerCase().includes(buscar))).map((i) => `
      <tr data-buscar="${esc((i.nombre + ' ' + i.categoria).toLowerCase())}">
        <td>${thumb(i.nombre, 'sm')}</td><td><b>${esc(i.nombre)}</b></td><td>${esc(i.categoria)}</td><td>${num(i.stock)}</td><td>${esc(i.unidad)}</td><td>${num(i.minimo)}</td>
        <td>${i.porcentaje < 25 ? '<span class="badge rojo">Stock bajo</span>' : '<span class="badge verde">Normal</span>'}</td>
        <td><button class="btn outline sm" data-act="nueva-solicitud" data-producto="${i.id}">${icon('cart')} Solicitar</button></td></tr>`);
    return {
      top: { saludo: ROLES.jefe.saludo, sub: 'Jefe de Cocina · Sucursal Centro' },
      html: `
        <div class="kpis three">
          ${kpi('box', S.insumos.length, 'Productos en inventario')}
          ${kpi('alert', bajos.length, 'Stock bajo', 'danger')}
          ${kpi('circleCheck', S.insumos.length - bajos.length, 'Stock normal', 'ok')}
        </div>
        ${card('Inventario de insumos', `
          ${tabla(['Imagen', 'Nombre del insumo', 'Categoría', 'Stock actual', 'Unidad', 'Stock mínimo', 'Estado', 'Acción'], filas)}
          <p class="note-foot">${icon('info')} Stock mínimo: 25 % del stock de referencia. El consumo de los pedidos se descuenta en el cierre diario de las 23:59.</p>`, {
          extra: `<div class="toolbar">
            <label class="search sm">${icon('search')}<input data-inp="buscar-insumo" value="${esc(ui.invBuscar)}" placeholder="Buscar ingrediente, categoría..." aria-label="Buscar insumo"></label>
            <select data-chg="inv-cat" aria-label="Categoría"><option value="">Todas las categorías</option>${cats.map((c) => `<option ${c === ui.invCat ? 'selected' : ''}>${esc(c)}</option>`).join('')}</select>
            <button class="btn ghost sm" data-act="cierre" title="Ejecuta ConsumoDiarioJob: descuenta el consumo del día y genera las alertas">${icon('refresh')} Cierre del día (23:59)</button>
          </div>`, cls: 'underline' })}`,
    };
  },

  // V10 · Mis solicitudes: listado y detalle
  solicitudes() {
    const f = ui.filtroSol;
    const sols = desc(S.solicitudes).filter((s) => (!f.estado || s.estado === f.estado) && enRango(s.fecha, f.fecha));
    const sel = sol(ui.sel.solicitudes) || sols[0];
    const { filas, pie } = paginar('sol-jefe', sols.map((s) => `
      <tr class="clickable ${sel && s.numero === sel.numero ? 'selected' : ''}" data-act="sel" data-v="solicitudes" data-n="${s.numero}">
        <td><b>${s.numero}</b></td><td>${fecha(s.fecha)}</td><td>${s.items.length}</td><td>${badge(s.estado, { up: true })}</td><td class="act">${icon('chevR')}</td></tr>`));
    return {
      top: { titulo: 'Mis solicitudes', sub: 'Consulta el estado de las solicitudes registradas' },
      html: `
        <div class="filters-line">
          <label class="fsel">${icon('calendar')}<select data-chg="sol-fecha" aria-label="Fecha"><option value="">Fecha</option><option value="hoy" ${f.fecha === 'hoy' ? 'selected' : ''}>Hoy</option><option value="7" ${f.fecha === '7' ? 'selected' : ''}>Últimos 7 días</option></select></label>
          <label class="fsel">${icon('filter')}<select data-chg="sol-estado" aria-label="Estado"><option value="">Estado</option>${['PENDIENTE', 'EN_TRAMITE', 'COMPLETADA'].map((e) => `<option value="${e}" ${f.estado === e ? 'selected' : ''}>${ESTADOS[e][0]}</option>`).join('')}</select></label>
          <button class="btn primary push" data-act="nueva-solicitud">${icon('plus')} Nueva solicitud</button>
        </div>
        <div class="grid halves">
          ${card('Lista de solicitudes', tabla(['Solicitud', 'Fecha', 'Productos', 'Estado', 'Acción'], filas, 'No hay solicitudes con esos filtros.') + pie, { icono: 'file', sub: 'Revisa y selecciona una solicitud para ver sus detalles' })}
          ${card('Detalle de solicitud', sel ? detalleSolicitud(sel) : vacioDetalle('Aún no hay solicitudes registradas.'), { icono: 'file', sub: 'Información completa de la solicitud seleccionada' })}
        </div>`,
    };
  },

  // Alertas de inventario (menú «Alertas» de V05 y fondo de V09)
  alertas() {
    const bajos = S.insumos.filter((i) => i.porcentaje < 25).sort((a, b) => a.porcentaje - b.porcentaje);
    const activas = bajos.filter((i) => i.alerta);
    const filas = bajos.map((i) => `
      <div class="alerta-item">${thumb(i.nombre)}<div class="ai-name"><b>${esc(i.nombre)}</b><small>${esc(i.categoria)} · ${num(i.stock)} ${esc(i.unidad)} de ${num(i.referencia)} ${esc(i.unidad)}</small></div>
        <div class="ai-meter"><small>Existencia actual: <b class="${i.porcentaje < 20 ? 't-rojo' : 't-naranja'}">${num(i.porcentaje)}%</b></small>${barra(i.porcentaje)}</div>
        ${i.alerta ? `<button class="btn primary sm" data-act="nueva-solicitud" data-producto="${i.id}">${icon('cart')} Solicitar alimento</button>` : `<span class="badge verde">${icon('check')} Solicitud registrada</span>`}
      </div>`).join('') || `<p class="muted pad">Ningún insumo está por debajo del 25 %. Ejecuta el cierre del día en Inventario para descontar el consumo de los pedidos.</p>`;
    return {
      top: { titulo: 'Alertas de inventario', sub: 'Productos con existencia por debajo del 25 % del stock de referencia' },
      html: `
        <div class="kpis two">
          <div class="kpi danger wide">${'<span class="kpi-ico">' + icon('alert') + '</span>'}<div><strong>${plural(activas.length, 'alerta activa', 'alertas activas')}</strong><span>Insumos bajo el 25 % sin solicitud de alimentos</span></div></div>
          <div class="kpi warn wide">${'<span class="kpi-ico">' + icon('bell') + '</span>'}<div><strong>${plural(bajos.length, 'insumo bajo', 'insumos bajos')}</strong><span>Existencia por debajo del 25 % del stock de referencia</span></div></div>
        </div>
        ${card('', `<div class="alertas">${filas}</div>`)}`,
    };
  },
};

function detalleSolicitud(s) {
  return `
    <div class="detail-head"><h3>Solicitud #${s.numero}</h3>${badge(s.estado, { up: true })}</div>
    <div class="facts">
      <div>${icon('calendar')}<small>Fecha:</small><b>${fecha(s.fecha)}</b></div>
      <div>${icon('user')}<small>Solicitante:</small><b>${esc(s.solicitante)}</b></div>
      <div>${icon('store')}<small>Sucursal:</small><b>${esc(s.sucursal)}</b></div>
      <div>${icon('tag')}<small>Estado:</small>${badge(s.estado, { up: true })}</div>
    </div>
    <div class="sub-head"><h4 class="sub-title">${icon('box')} Productos solicitados</h4><small>Total de productos: ${s.items.length}</small></div>
    ${tabla(['Producto', 'Categoría', 'Cantidad', 'Marca', 'Unidad'], s.items.map((i) => `
      <tr><td><span class="cell-thumb">${thumb(i.producto, 'xs')}<b>${esc(i.producto)}</b></span></td><td>${esc(i.categoria)}</td><td>${num(i.cantidad)}</td><td>${esc(i.marca || '—')}</td><td>${esc(i.unidad)}</td></tr>`))}`;
}

// ----------------------------------------------------------------------------- AUXILIAR DE COMPRAS (V11 a V15)

function calendario() {
  const hoy = new Date();
  const c = ui.cal || { y: hoy.getFullYear(), m: hoy.getMonth() };
  const primero = new Date(c.y, c.m, 1);
  const inicio = (primero.getDay() + 6) % 7; // lunes primero
  const dias = new Date(c.y, c.m + 1, 0).getDate();
  const previos = new Date(c.y, c.m, 0).getDate();
  const celdas = [];
  for (let k = 0; k < 42; k++) {
    const d = k - inicio + 1;
    if (d < 1) celdas.push(`<span class="off">${previos + d}</span>`);
    else if (d > dias) celdas.push(`<span class="off">${d - dias}</span>`);
    else celdas.push(`<span class="${d === hoy.getDate() && c.m === hoy.getMonth() && c.y === hoy.getFullYear() ? 'today' : ''}">${d}</span>`);
  }
  const titulo = mayus(primero.toLocaleDateString('es-CO', { month: 'long', year: 'numeric' }).replace(' de ', ' '));
  return `<div class="cal">
    <div class="cal-head"><span class="title-ico sm">${icon('calendar')}</span><b>${titulo}</b>
      <div><button class="round-btn" data-act="mes" data-d="-1" aria-label="Mes anterior">${icon('chevL')}</button><button class="round-btn" data-act="mes" data-d="1" aria-label="Mes siguiente">${icon('chevR')}</button></div></div>
    <div class="cal-grid">${['Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb', 'Dom'].map((d) => `<em>${d}</em>`).join('')}${celdas.join('')}</div>
  </div>`;
}

function actividadesDelDia() {
  const pendientes = S.solicitudes.filter((s) => s.estado === 'PENDIENTE').length;
  const enviadas = S.cotizaciones.filter((c) => c.estado === 'ENVIADA').length;
  const tareas = [
    ['09:00', 'Revisar solicitudes nuevas', pendientes ? plural(pendientes, 'pendiente', 'pendientes') : 'Al día'],
    ['10:30', 'Solicitar cotizaciones', pendientes ? 'Hay solicitudes por cotizar' : 'Sin solicitudes por cotizar'],
    ['14:00', 'Hacer seguimiento', plural(enviadas, 'cotización enviada', 'cotizaciones enviadas')],
    ['16:00', 'Enviar solicitudes pendientes', S.solicitudes.filter((s) => s.estado === 'EN_TRAMITE').length + ' en trámite'],
  ];
  return card('Actividades del día', `<ul class="timeline">${tareas.map(([h, t, d]) => `<li><i></i><b>${h}</b><span>${t}<small>${d}</small></span></li>`).join('')}</ul>`,
    { icono: 'clipboard', extra: `<span class="pill-count">${plural(pendientes, 'pendiente', 'pendientes')}</span>` });
}

const filaSolicitudAux = (s, seleccionada) => `
  <tr class="clickable ${seleccionada ? 'selected' : ''}" data-act="sel" data-v="solicitudes" data-n="${s.numero}" data-ir="1">
    <td><b>${s.numero}</b></td><td>${esc(s.sucursal)}</td><td>${fecha(s.fecha)}</td><td>${s.items.length}</td><td>${badge(s.estado, { dot: true })}</td>
    <td><span class="link-btn">Ver ${icon('chevR')}</span></td></tr>`;

const AUXILIAR = {
  // V11 · Dashboard del auxiliar
  inicio() {
    const sols = desc(S.solicitudes);
    return {
      top: { saludo: ROLES.auxiliar.saludo, sub: 'Auxiliar Área de Compras' },
      html: `
        <section class="card kpi-strip">
          <div><strong>${S.solicitudes.filter((s) => s.estado === 'PENDIENTE').length}</strong><span>Solicitudes<br>pendientes</span></div>
          <div><strong>${S.solicitudes.filter((s) => s.estado === 'EN_TRAMITE').length}</strong><span>Solicitudes<br>en trámite</span></div>
          <div><strong>${S.cotizaciones.length}</strong><span>Cotizaciones<br>enviadas</span></div>
        </section>
        <div class="grid main-side">
          ${card('Solicitudes pendientes recientes', tabla(['Nº solicitud', 'Sucursal', 'Fecha', 'Productos', 'Estado', 'Acción'], sols.slice(0, 6).map((s) => filaSolicitudAux(s, false)), 'Aún no hay solicitudes de alimentos.'),
            { icono: 'file', sub: 'Revisa y gestiona las últimas solicitudes de alimentos', extra: btnSoft('Ver todas las solicitudes', 'data-go="solicitudes"') })}
          <div class="stack">${card('', calendario())}${actividadesDelDia()}</div>
        </div>`,
    };
  },

  // V12 · Solicitudes, con los filtros de HU-07 y «Generar cotización»
  solicitudes() {
    const f = ui.hu07;
    const numeros = ui.hu07Res;
    const sols = desc(S.solicitudes).filter((s) => !numeros || numeros.includes(s.numero));
    const sel = sol(ui.sel.solicitudes) || sols[0];
    const { filas, pie } = paginar('sol-aux', sols.map((s) => filaSolicitudAux(s, sel && s.numero === sel.numero)), 6);
    const marcas = [...new Set(S.solicitudes.flatMap((s) => s.items.map((i) => i.marca)).filter(Boolean))];
    return {
      top: { icono: 'file', titulo: 'Solicitudes', sub: 'Revisa y gestiona las solicitudes de alimentos de las sucursales' },
      html: `
        <section class="card filters">
          <label>Desde<input type="date" data-hu07="desde" value="${f.desde}"></label>
          <label>Hasta<input type="date" data-hu07="hasta" value="${f.hasta}"></label>
          <label>Producto<select data-hu07="producto"><option value="">Todos</option>${S.insumos.map((i) => `<option value="${i.id}" ${String(i.id) === f.producto ? 'selected' : ''}>${esc(i.nombre)}</option>`).join('')}</select></label>
          <label>Marca<input data-hu07="marca" list="marcas" value="${esc(f.marca)}" placeholder="Todas"><datalist id="marcas">${marcas.map((m) => `<option value="${esc(m)}">`).join('')}</datalist></label>
          <label>Estado<select data-hu07="estado"><option value="">Todos</option>${['PENDIENTE', 'EN_TRAMITE', 'COMPLETADA'].map((e) => `<option value="${e}" ${f.estado === e ? 'selected' : ''}>${ESTADOS[e][0]}</option>`).join('')}</select></label>
          <div class="filter-btns"><button class="btn outline sm" data-act="hu07-buscar">${icon('search')} Buscar</button><button class="btn ghost sm" data-act="hu07-limpiar">${icon('refresh')} Limpiar</button></div>
          ${ui.hu07Error ? `<p class="form-error">${esc(ui.hu07Error)}</p>` : ''}
        </section>
        <div class="grid main-side">
          ${card('Solicitudes de alimentos', tabla(['Nº solicitud', 'Sucursal', 'Fecha', 'Productos', 'Estado', 'Acción'], filas,
            numeros ? 'No se encontraron solicitudes con los criterios indicados' : 'Aún no hay solicitudes de alimentos.') + pie,
            { icono: 'file', sub: 'Revisa y gestiona las últimas solicitudes de alimentos', extra: `<button class="btn soft sm" data-act="generar-cotizacion">${icon('plusCircle')} Generar cotización</button>` })}
          ${card('Detalle de solicitud', sel ? detalleSolicitud(sel) + (sel.estado !== 'COMPLETADA'
            ? `<button class="btn primary w mt" data-act="generar-cotizacion" data-sol="${sel.numero}">${icon('send')} Generar cotización con ${sel.numero}</button>` : '')
            : vacioDetalle('Selecciona una solicitud.'), { icono: 'file' })}
        </div>`,
    };
  },

  // V15 · Cotizaciones enviadas y detalle
  cotizaciones() {
    const f = ui.filtroCot, b = ui.filtroCotBorrador;
    const lista = desc(S.cotizaciones).filter((c) => (!f.proveedor || c.proveedor === f.proveedor) && (!f.estado || c.estado === f.estado) && enRango(c.fechaEnvio, f.fecha));
    const sel = cot(ui.sel.cotizaciones) || lista[0];
    const { filas, pie } = paginar('cot-aux', lista.map((c) => `
      <tr class="clickable ${sel && c.numero === sel.numero ? 'selected' : ''}" data-act="sel" data-v="cotizaciones" data-n="${c.numero}">
        <td><b>${c.numero}</b></td><td>${esc(c.proveedor)}</td><td>${fecha(c.fechaEnvio)}</td><td>${fecha(c.fechaLimite)}</td><td>${badge(c.estado, { up: true, dot: true })}</td>
        <td><span class="link-btn">Ver detalle ${icon('chevR')}</span></td></tr>`));
    const estados = [...new Set(S.cotizaciones.map((c) => c.estado))];
    const detalle = sel ? `
      ${dl([['Cotización:', `<b>${sel.numero}</b>`], ['Solicitud origen:', sel.solicitudes.join(', ')], ['Proveedor:', esc(sel.proveedor)],
        ['Fecha envío:', fecha(sel.fechaEnvio)], ['Fecha límite:', fecha(sel.fechaLimite)], ['Estado:', badge(sel.estado, { up: true, dot: true })]])}
      <h4 class="sub-title">${icon('box')} Productos</h4>
      ${tabla(['Producto', 'Cantidad', 'Unidad'], sel.items.map((i) => `<tr><td>${esc(i.producto)}</td><td>${num(i.cantidad)}</td><td>${esc(i.unidad)}</td></tr>`))}
      <a class="btn primary w mt" href="/api/pdf/${sel.numero}" target="_blank" rel="noopener">${icon('file')} Ver PDF</a>
      <a class="btn ghost w" href="/api/pdf/${sel.numero}" download="${sel.numero}.txt">${icon('download')} Descargar</a>` : vacioDetalle('Aún no hay cotizaciones enviadas.');
    return {
      top: { titulo: 'Cotizaciones', sub: 'Consulta y revisa las cotizaciones enviadas a proveedores' },
      html: `
        <section class="card filters">
          <label>Proveedor<select data-fcot="proveedor"><option value="">Todos</option>${S.proveedores.map((p) => `<option ${p.nombre === b.proveedor ? 'selected' : ''}>${esc(p.nombre)}</option>`).join('')}</select></label>
          <label>Fecha<select data-fcot="fecha"><option value="">Todas</option><option value="hoy" ${b.fecha === 'hoy' ? 'selected' : ''}>Hoy</option><option value="7" ${b.fecha === '7' ? 'selected' : ''}>Últimos 7 días</option></select></label>
          <label>Estado<select data-fcot="estado"><option value="">Todos</option>${estados.map((e) => `<option value="${e}" ${b.estado === e ? 'selected' : ''}>${ESTADOS[e][0]}</option>`).join('')}</select></label>
          <div class="filter-btns"><button class="btn outline sm" data-act="fcot-buscar">${icon('search')} BUSCAR</button><button class="btn ghost sm" data-act="fcot-limpiar">${icon('refresh')} LIMPIAR</button></div>
        </section>
        <div class="grid main-side">
          ${card('Cotizaciones enviadas', tabla(['Cotización', 'Proveedor', 'Fecha envío', 'Fecha límite', 'Estado', 'Acción'], filas, 'No hay cotizaciones con esos filtros.') + pie,
            { icono: 'file', sub: `Se encontraron ${plural(lista.length, 'cotización', 'cotizaciones')} en el periodo seleccionado` })}
          ${card('Detalle de cotización', detalle, { icono: 'file', sub: sel ? sel.numero : '' })}
        </div>`,
    };
  },
};

// ----------------------------------------------------------------------------- DIRECTOR DE COMPRAS (V16 a V23)

function ticketCotizacion(c) {
  if (!c) return `<div class="ticket side">${vacioDetalle('Selecciona una cotización.')}</div>`;
  return `<div class="ticket side">
    <div class="ticket-brand"><img src="assets/logo/logo.png" alt=""><div><b>Konrad <span>Gourmet</span></b><small>Sistema de Abastecimiento</small></div></div>
    <p class="ticket-label">Detalle de cotización</p><h3 class="ticket-code">${c.numero}</h3>
    ${dl([['Proveedor:', `<span class="t-naranja">${esc(c.proveedor)}</span>`], ['Solicitud origen:', c.solicitudes.join(', ')], ['Fecha:', fecha(c.fechaEnvio)], ['Estado:', badge(c.estado, { up: true, dot: true })]])}
    <h4 class="sub-title">Productos cotizados</h4>
    ${tabla(['Producto', 'Cantidad'], c.items.map((i) => `<tr><td>${esc(i.producto)}</td><td>${num(i.cantidad)} ${esc(i.unidad)}</td></tr>`))}
    ${c.cotizado != null ? `<div class="total-row sm"><span>Valor cotizado</span><b>${money(c.cotizado)}</b></div>` : ''}
    <div class="barcode"></div><small class="barcode-num">${c.numero}</small>
  </div>`;
}

function accionPendiente(c) {
  return sinPrecios(c)
    ? `<button class="pill-btn naranja" data-act="registrar-valores" data-n="${c.numero}">Registrar valores ${icon('chevR')}</button>`
    : `<button class="pill-btn azul" data-act="registrar-valores" data-n="${c.numero}">Validar ${icon('chevR')}</button>`;
}

function vistaCotizacionesDirector(soloPendientes) {
  const f = ui.filtroDir;
  const base = soloPendientes ? S.cotizaciones.filter((c) => c.estado === 'ENVIADA') : S.cotizaciones;
  const lista = asc(base).filter((c) => (!f.proveedor || c.proveedor === f.proveedor) && (soloPendientes || !f.estado || c.estado === f.estado) && enRango(c.fechaEnvio, f.fecha));
  const id = soloPendientes ? 'pendientes' : 'cotizaciones';
  const sel = cot(ui.sel[id]) || lista[0];
  const { filas, pie } = paginar(id, lista.map((c) => `
    <tr class="clickable ${sel && c.numero === sel.numero ? 'selected' : ''}" data-act="sel" data-v="${id}" data-n="${c.numero}">
      <td><b>${c.numero}</b></td><td>${esc(c.proveedor)}</td><td>${c.solicitudes.join(', ')}</td><td>${fecha(c.fechaEnvio)}</td><td>${badge(c.estado, { up: true, dot: true })}</td>
      <td>${soloPendientes ? accionPendiente(c) : `<span class="pill-btn naranja">Ver ${icon('chevR')}</span>`}</td></tr>`), 6);
  const objetivo = (sel && sel.estado === 'ENVIADA' ? sel : null) || asc(S.cotizaciones).find((c) => c.estado === 'ENVIADA');
  const estados = [...new Set(S.cotizaciones.map((c) => c.estado))];
  return {
    top: { icono: 'file', titulo: soloPendientes ? 'Pendientes de validar' : 'Todas las cotizaciones', sub: soloPendientes ? 'Cotizaciones enviadas que esperan el registro de valores o la validación.' : 'Consulta y gestiona el historial de cotizaciones.' },
    html: `<div class="grid main-side wide-side">
      <div class="stack">
        <div class="kpis three soft">
          <div class="kpi peach"><span class="kpi-ico">${icon('send')}</span><div><span>Enviadas</span><strong>${S.cotizaciones.length}</strong></div></div>
          <div class="kpi blue"><span class="kpi-ico">${icon('file')}</span><div><span>Opcionadas</span><strong>${S.cotizaciones.filter((c) => c.estado === 'OPCIONADA').length}</strong></div></div>
          <div class="kpi rose"><span class="kpi-ico">${icon('circleX')}</span><div><span>Rechazadas</span><strong>${S.cotizaciones.filter((c) => c.estado === 'RECHAZADA').length}</strong></div></div>
        </div>
        <section class="card filters compact">
          <label>Proveedor<select data-fdir="proveedor"><option value="">Todos los proveedores</option>${S.proveedores.map((p) => `<option ${p.nombre === f.proveedor ? 'selected' : ''}>${esc(p.nombre)}</option>`).join('')}</select></label>
          ${soloPendientes ? '' : `<label>Estado<select data-fdir="estado"><option value="">Todos los estados</option>${estados.map((e) => `<option value="${e}" ${f.estado === e ? 'selected' : ''}>${ESTADOS[e][0]}</option>`).join('')}</select></label>`}
          <label>Fecha<select data-fdir="fecha"><option value="">Todas las fechas</option><option value="hoy" ${f.fecha === 'hoy' ? 'selected' : ''}>Hoy</option><option value="7" ${f.fecha === '7' ? 'selected' : ''}>Últimos 7 días</option></select></label>
          <button class="btn primary sm push" data-act="registrar-valores" data-n="${objetivo ? objetivo.numero : ''}" ${objetivo ? '' : 'disabled'}>${icon('plus')} Registrar valor de cotización</button>
        </section>
        ${card(soloPendientes ? 'Pendientes de validar' : 'Todas las cotizaciones', tabla(['Nº', 'Proveedor', 'Solicitud', 'Fecha', 'Estado', soloPendientes ? 'Acción' : 'Ver'], filas,
          soloPendientes ? 'No hay cotizaciones pendientes de registrar o validar.' : 'Aún no hay cotizaciones. El auxiliar de compras las genera desde Solicitudes.') + pie, { icono: 'file' })}
      </div>
      ${ticketCotizacion(sel)}
    </div>`,
  };
}

const DIRECTOR = {
  // V16 · Dashboard del director de compras
  inicio() {
    const pend = asc(S.cotizaciones.filter((c) => c.estado === 'ENVIADA'));
    const filas = pend.slice(0, 6).map((c) => `<tr><td><b>${c.numero}</b></td><td>${esc(c.proveedor)}</td><td>${c.solicitudes.join(', ')}</td><td>${accionPendiente(c)}</td></tr>`);
    const conteo = (e) => S.cotizaciones.filter((c) => c.estado === e).length;
    return {
      top: { saludo: ROLES.director.saludo, sub: 'Directora Área de Compras' },
      html: `
        <div class="kpis four">
          ${kpi('file', porRegistrar().length, 'Cotizaciones<br>por registrar')}
          ${kpi('clock', porValidar().length, 'Pendientes<br>de validar')}
          ${kpi('star', conteo('OPCIONADA'), 'Cotizaciones<br>opcionadas')}
          ${kpi('cart', S.ordenes.length, 'Órdenes<br>de compra')}
        </div>
        <div class="grid main-side">
          ${card('Pendientes de atención', tabla(['Cotización', 'Proveedor', 'Solicitud', 'Acción'], filas, 'No hay cotizaciones pendientes. El auxiliar de compras las genera y envía a los proveedores.'),
            { icono: 'file', sub: 'Cotizaciones recibidas listas para registro y validación.' })}
          <div class="stack">
            ${card('Pendientes hoy', tabla(['Cotización', 'Proveedor', 'Solicitud'], pend.slice(0, 4).map((c) => `<tr class="clickable" data-go="pendientes" data-sel="${c.numero}"><td>${c.numero}</td><td>${esc(c.proveedor)}</td><td>${c.solicitudes.join(', ')}</td></tr>`), 'Nada pendiente para hoy.'),
              { icono: 'clock', sub: 'Cotizaciones que requieren tu revisión.', extra: `<span class="pill-count">${pend.length}</span>` })}
            ${card('Estado de cotizaciones', `<ul class="estado-list">
                <li><i class="naranja"></i>Enviada<b class="n naranja">${conteo('ENVIADA')}</b></li>
                <li><i class="azul"></i>Opcionada<b class="n azul">${conteo('OPCIONADA') + conteo('CON_ORDEN')}</b></li>
                <li><i class="rojo"></i>Rechazada<b class="n rojo">${conteo('RECHAZADA')}</b></li>
                <li><i class="ambar"></i>Sospechosa<b class="n ambar">${conteo('SOSPECHOSA')}</b></li></ul>`,
              { icono: 'chart', sub: 'Resumen general del estado actual.' })}
          </div>
        </div>`,
    };
  },

  cotizaciones: () => vistaCotizacionesDirector(false), // V17
  pendientes: () => vistaCotizacionesDirector(true),    // V17 · submenú «Pendientes de validar»

  // V20 · Cotizaciones opcionadas: comparación y detalle
  opcionadas() {
    const candidatas = S.cotizaciones.filter((c) => c.estado === 'OPCIONADA' || c.estado === 'CON_ORDEN');
    const solicitudes = [...new Set(candidatas.flatMap((c) => c.solicitudes))].sort();
    const solSel = solicitudes.includes(ui.compararSol) ? ui.compararSol : solicitudes[0] || '';
    const lista = asc(candidatas.filter((c) => c.solicitudes.includes(solSel)));
    const sel = lista.find((c) => c.numero === ui.sel.opcionadas) || lista[0];
    const filas = lista.map((c) => `
      <tr class="clickable ${sel && c.numero === sel.numero ? 'selected' : ''}" data-act="sel" data-v="opcionadas" data-n="${c.numero}">
        <td><b>${esc(c.proveedor)}</b></td><td>${money(c.estandar)}</td><td>${money(c.cotizado)}</td>
        <td><span class="diff ${c.diferencia > 0 ? 'up' : 'down'}">${pct(c.diferencia)}</span></td><td>${badge(c.estado, { up: true, dot: true })}</td>
        <td><span class="pill-btn naranja">Ver ${icon('chevR')}</span></td></tr>`);
    const detalle = sel ? `
      ${dl([['Cotización', `<b>${sel.numero}</b>`], ['Proveedor', `<b>${esc(sel.proveedor)}</b>`], ['Solicitud', sel.solicitudes.join(', ')], ['Estado', badge(sel.estado, { up: true, dot: true })]])}
      <h4 class="sub-title">${icon('chart')} Comparación económica</h4>
      <div class="econ"><div><span>Ministerio</span><b>${money(sel.estandar)}</b></div><div><span>Proveedor</span><b>${money(sel.cotizado)}</b></div>
        <div><span>Diferencia</span><b class="${sel.diferencia > 0 ? 't-rojo' : 't-verde'}">${pct(sel.diferencia)}</b></div></div>
      <h4 class="sub-title">${icon('box')} Productos</h4>
      ${tabla(['Producto', 'Cantidad', 'Valor'], sel.items.map((i) => `<tr><td>${esc(i.producto)}</td><td>${num(i.cantidad)} ${esc(i.unidad)}</td><td>${money(i.precioProveedor * i.cantidad)}</td></tr>`))}
      ${sel.estado === 'OPCIONADA'
        ? `<button class="btn primary w mt" data-act="generar-orden" data-n="${sel.numero}">${icon('cart')} Seleccionar para compra</button>`
        : `<p class="note-foot">${icon('info')} Esta cotización ya tiene orden de compra.</p>`}
      <button class="btn outline w" data-go="cotizaciones" data-sel="${sel.numero}">${icon('file')} Ver cotización</button>` : vacioDetalle('Todavía no hay cotizaciones opcionadas. Registra y valida los valores de una cotización enviada.');
    return {
      top: { icono: 'file', titulo: 'Cotizaciones opcionadas', sub: 'Compara las cotizaciones que cumplen con los criterios y selecciona la mejor opción.' },
      html: `<div class="grid main-side wide-side">
        <div class="stack">
          <section class="card filters compact"><label>Solicitud<select data-chg="comparar-sol">${solicitudes.map((s) => `<option ${s === solSel ? 'selected' : ''}>${s}</option>`).join('') || '<option>—</option>'}</select></label></section>
          ${card('Comparación de cotizaciones', tabla(['Proveedor', 'Valor estándar', 'Cotización', 'Diferencia', 'Estado', 'Ver'], filas, 'Sin cotizaciones opcionadas para comparar.'), { icono: 'scale' })}
        </div>
        ${card('Detalle de cotización opcionada', detalle, { icono: 'file' })}
      </div>`,
    };
  },

  // V23 · Órdenes de compra + detalle
  ordenes() {
    const f = ui.filtroOrd;
    const lista = desc(S.ordenes).filter((o) => (!f.proveedor || o.proveedor === f.proveedor) && (!f.estado || o.estado === f.estado) && enRango(o.fecha, f.fecha));
    const sel = ord(ui.sel.ordenes) || lista[0];
    const { filas, pie } = paginar('ordenes', lista.map((o) => `
      <tr class="clickable ${sel && o.numero === sel.numero ? 'selected' : ''}" data-act="sel" data-v="ordenes" data-n="${o.numero}">
        <td><b>${o.numero}</b></td><td>${fecha(o.fecha)}</td><td>${esc(o.proveedor)}</td><td>${o.cotizacion}</td><td>${money(o.total)}</td><td>${badge(o.estado, { up: true })}</td>
        <td><span class="pill-btn naranja">Ver ${icon('chevR')}</span></td></tr>`));
    const detalle = sel ? `
      <div class="oc-head"><div><small>ORDEN DE COMPRA</small><h3>${sel.numero}</h3><span>Fecha: ${fecha(sel.fecha)}</span></div><div><small>Estado</small>${badge(sel.estado, { up: true })}</div></div>
      ${dl([['Proveedor', `<b>${esc(sel.proveedor)}</b>`], ['Solicitud', sel.solicitudes.join(', ')], ['Cotización', sel.cotizacion]])}
      ${sel.estado === 'EN_PROCESO' ? `<div class="next-step">${icon('alert')}<div><b>Siguiente paso</b><p>La orden está en proceso. Debe continuarse con la gestión de compra, seguimiento al proveedor y verificación de entrega.</p></div></div>` : ''}
      <h4 class="sub-title">${icon('box')} Productos</h4>
      ${tabla(['Producto', 'Cantidad', 'Unidad', 'Precio'], sel.items.map((i) => `<tr><td>${esc(i.producto)}</td><td>${num(i.cantidad)}</td><td>${esc(i.unidad)}</td><td>${money(i.precio * i.cantidad)}</td></tr>`))}
      <div class="total-row big"><span>TOTAL:</span><b>${money(sel.total)}</b></div>` : vacioDetalle('Aún no hay órdenes de compra.');
    return {
      top: { icono: 'receipt', titulo: 'Órdenes de compra', sub: 'Consulta y revisa las órdenes de compra generadas.' },
      html: `<div class="grid main-side wide-side">
        <div class="stack">
          <section class="card filters compact">
            <label>Fecha<select data-ford="fecha"><option value="">Todas las fechas</option><option value="hoy" ${f.fecha === 'hoy' ? 'selected' : ''}>Hoy</option><option value="7" ${f.fecha === '7' ? 'selected' : ''}>Últimos 7 días</option></select></label>
            <label>Proveedor<select data-ford="proveedor"><option value="">Todos los proveedores</option>${S.proveedores.map((p) => `<option ${p.nombre === f.proveedor ? 'selected' : ''}>${esc(p.nombre)}</option>`).join('')}</select></label>
            <label>Estado<select data-ford="estado"><option value="">Todos los estados</option>${['EN_PROCESO', 'FINALIZADA'].map((e) => `<option value="${e}" ${f.estado === e ? 'selected' : ''}>${ESTADOS[e][0]}</option>`).join('')}</select></label>
          </section>
          ${card('Listado de órdenes de compra', tabla(['Orden', 'Fecha', 'Proveedor', 'Cotización', 'Total', 'Estado', 'Ver'], filas, 'Aún no hay órdenes. Se generan desde Cotizaciones → Opcionadas.') + pie, { icono: 'file' })}
        </div>
        ${card('Detalle de orden de compra', detalle, { icono: 'receipt' })}
      </div>`,
    };
  },
};

// ----------------------------------------------------------------------------- Mi perfil (todos los roles)

function perfil() {
  const R = ROLES[rol];
  return {
    top: { icono: 'user', titulo: 'Mi perfil', sub: 'Datos del usuario con la sesión activa' },
    html: card('', `<div class="profile-card">
      <div class="avatar xl"><span>${R.iniciales}</span><i class="online"></i></div>
      <div><h2>${esc(R.nombre)}</h2><p class="t-naranja">${esc(R.cargo)}</p>
      ${dl([['Correo', esc(R.correo)], ['Sucursal', esc(R.sede)], ['Rol', `<code>${R.codigo}</code>`]])}
      <p class="note-foot">${icon('info')} Demo local: ms-identidad todavía no autentica; el rol sale del correo usado al iniciar sesión.</p></div>
    </div>`),
  };
}

const VISTAS = { mesero: MESERO, jefe: JEFE, auxiliar: AUXILIAR, director: DIRECTOR };

// ----------------------------------------------------------------------------- ventanas emergentes

const cerrar = `<button class="icon-btn m-close" data-act="cerrar" aria-label="Cerrar">${icon('x')}</button>`;
const mHead = (ico, titulo) => `<header class="m-head"><span class="m-ico">${icon(ico)}</span><h2>${titulo}</h2>${cerrar}</header>`;
const okIcon = (tono = 'verde') => `<div class="ok-icon ${tono}"><span>${icon('check')}</span></div>`;

const MODALES = {
  // V04 · Pedido registrado
  pedido({ numero }) {
    const p = ped(numero);
    return `<div class="modal sm center">${cerrar}${okIcon()}<h2>Pedido registrado</h2><div class="code-pill">${numero}</div>
      <div class="facts four boxed">
        <div>${icon('table')}<small>Mesa:</small><b>${pad2(p.mesa)}</b></div><div>${icon('utensils')}<small>Platos:</small><b>${p.platos}</b></div>
        <div>${icon('calendar')}<small>Fecha:</small><b>${fecha(p.fecha)}</b></div><div>${icon('clock')}<small>Hora:</small><b>${hora(p.fecha)}</b></div>
      </div>
      <div class="m-actions"><button class="btn ghost" data-go="pedidos" data-sel="${numero}">Ver pedido</button><button class="btn primary" data-act="otro-pedido">${icon('plus')} Nuevo pedido</button></div></div>`;
  },

  // V08 · Nueva solicitud de alimentos
  'nueva-solicitud'() {
    const m = ui.m;
    const cats = [...new Set(S.insumos.map((i) => i.categoria))];
    const productos = S.insumos.filter((i) => i.categoria === m.cat);
    const prod = S.insumos.find((i) => i.id === m.prod) || productos[0];
    return `<div class="modal md">${mHead('cart', 'Nueva solicitud de alimentos')}
      <section class="m-sec"><h3>Información automática</h3><div class="auto">
        <div>${icon('store')}<span><small>Sucursal:</small><b>Konrad Gourmet – Sucursal Centro</b></span></div>
        <div>${icon('user')}<span><small>Solicitante:</small><b>Carlos Mendoza</b></span></div></div></section>
      <section class="m-sec"><h3>Producto</h3>
        <div class="frow five">
          <label>Categoría<select data-chg="m-cat">${cats.map((c) => `<option ${c === m.cat ? 'selected' : ''}>${esc(c)}</option>`).join('')}</select></label>
          <label>Producto<select data-chg="m-prod" id="m-prod">${productos.map((i) => `<option value="${i.id}" ${prod && i.id === prod.id ? 'selected' : ''}>${esc(i.nombre)}</option>`).join('')}</select></label>
          <label>Cantidad<input id="m-cant" type="number" min="0" step="any" value="${m.cant ?? ''}" placeholder="0"></label>
          <label>Marca (opcional)<input id="m-marca" list="m-marcas" value="${esc(m.marca ?? '')}" placeholder="—"><datalist id="m-marcas"><option value="Marca X"><option value="Marca Y"></datalist></label>
          <label>Unidad<select id="m-unidad"><option>${esc(prod ? prod.unidad : 'kg')}</option></select></label>
        </div>
        <button class="btn primary sm" data-act="m-agregar-producto">${icon('plus')} Agregar</button>
        ${m.error ? `<p class="form-error">${esc(m.error)}</p>` : ''}
      </section>
      <section class="m-sec"><h3>Productos solicitados</h3>
        ${tabla(['Producto', 'Cantidad', 'Marca', 'Unidad', 'Acción'], m.filas.map((f, k) => `<tr><td>${esc(producto(f.producto))}</td><td>${num(f.cantidad)}</td><td>${esc(f.marca || '—')}</td><td>${esc(f.unidad)}</td>
          <td><button class="icon-btn danger" data-act="m-quitar-producto" data-k="${k}" aria-label="Quitar">${icon('trash')}</button></td></tr>`), 'Agrega al menos un producto.')}
      </section>
      <footer class="m-foot"><button class="btn ghost" data-act="cerrar">Cancelar</button><button class="btn primary" data-act="enviar-solicitud" ${m.filas.length ? '' : 'disabled'}>Enviar solicitud</button></footer></div>`;
  },

  // V09 · Solicitud registrada
  'solicitud-ok'({ numero }) {
    const s = sol(numero);
    const correo = S.correos.some((c) => c.asunto.includes(numero));
    return `<div class="modal sm center">${cerrar}<div class="burst">${okIcon('naranja')}</div><h2>Solicitud registrada</h2><div class="code-big">${numero}</div>
      <div class="estado-pill">Estado: <b>${(ESTADOS[s.estado] || [s.estado])[0].toUpperCase()}</b></div>
      <p class="muted">${correo ? 'Se envió la confirmación al correo registrado.' : 'La confirmación se enviará al correo registrado.'}</p>
      <div class="m-actions"><button class="btn primary" data-go="solicitudes" data-sel="${numero}">${icon('file')} VER SOLICITUD</button><button class="btn outline" data-go="inicio">${icon('home')} VOLVER AL INICIO</button></div></div>`;
  },

  // V06 · Nuevo plato
  'nuevo-plato'() {
    const m = ui.m;
    const cats = [...new Set(S.insumos.map((i) => i.categoria))];
    const productos = S.insumos.filter((i) => i.categoria === m.ingCat);
    return `<div class="modal lg">${mHead('utensils', m.id ? 'Editar plato' : 'Nuevo plato')}<p class="m-sub">${m.id ? 'Modifica los datos del plato; el cambio llega al catálogo del mesero y a la receta del inventario.' : 'Completa la información para crear un nuevo plato en el menú.'}</p>
      <div class="plato-top">
        <div class="stack-sm">
          <label class="fl"><span>Nombre del plato <i>*</i></span><input data-m="nombre" value="${esc(m.nombre)}" placeholder="Ej. Bandeja paisa"></label>
          <label class="fl">Descripción<textarea data-m="descripcion" maxlength="200" rows="3" placeholder="Describe brevemente el plato, sus características, preparación, etc.">${esc(m.descripcion)}</textarea><small class="counter">${m.descripcion.length}/200</small></label>
          <div class="frow two">
            <label class="fl"><span>Categoría <i>*</i></span><select data-m="categoria"><option value="">Selecciona una categoría</option>${['Fuertes', 'Entradas', 'Sopas', 'Bebidas', 'Postres'].map((c) => `<option ${c === m.categoria ? 'selected' : ''}>${c}</option>`).join('')}</select></label>
            <label class="fl"><span>Precio de venta <i>*</i></span><input data-m="precio" type="number" min="0" step="100" value="${esc(m.precio)}" placeholder="$ 0"></label>
          </div>
        </div>
        <label class="upload">${m.imagen || m.tieneImagen ? `<img src="${m.imagen || '/api/imagen/' + m.id}" alt="Vista previa">` : `${icon('image')}<b>Subir imagen del plato</b><small>JPG, PNG o WebP (máx. 1 MB)</small><span class="btn outline sm">${icon('upload')} Elegir archivo</span>`}
          <input type="file" accept="image/png,image/jpeg,image/webp" data-chg="m-imagen" hidden></label>
      </div>
      <section class="m-sec ing"><div class="sub-head"><h3>${icon('utensils')} Ingredientes</h3><small>Agrega los ingredientes que componen el plato.</small></div>
        <div class="frow four">
          <label>Categoría<select data-chg="m-ing-cat"><option value="">Selecciona</option>${cats.map((c) => `<option ${c === m.ingCat ? 'selected' : ''}>${esc(c)}</option>`).join('')}</select></label>
          <label>Producto<select id="m-ing-prod">${productos.length ? productos.map((i) => `<option value="${i.id}">${esc(i.nombre)}</option>`).join('') : '<option value="">Selecciona un producto</option>'}</select></label>
          <label>Cantidad<input id="m-ing-cant" type="number" min="0" step="any" placeholder="0.00"></label>
          <label>Unidad<select id="m-ing-unidad"><option value="kg">kg</option><option value="g">g</option></select></label>
        </div>
        <button class="btn primary sm push-r" data-act="m-agregar-ing">${icon('plus')} Agregar ingrediente</button>
        ${m.error ? `<p class="form-error">${esc(m.error)}</p>` : ''}
        <h3 class="mt">Ingredientes agregados (${m.ings.length})</h3>
        ${m.ings.length ? tabla(['Categoría', 'Producto', 'Cantidad', 'Unidad', 'Acción'], m.ings.map((g, k) => `<tr><td>${esc((S.insumos.find((i) => i.id === g.id) || {}).categoria)}</td><td>${esc(producto(g.id))}</td><td>${num(g.cantidad)}</td><td>${g.unidad}</td>
          <td><button class="icon-btn danger" data-act="m-quitar-ing" data-k="${k}" aria-label="Quitar">${icon('trash')}</button></td></tr>`))
          : `<div class="empty-box">${icon('bowl')}<b>Aún no has agregado ingredientes</b><small>Usa el formulario de arriba para añadir ingredientes al plato.</small></div>`}
      </section>
      <footer class="m-foot"><button class="btn ghost" data-act="cerrar">Cancelar</button><button class="btn primary" data-act="guardar-plato">${icon('file')} ${m.id ? 'Guardar cambios' : 'Guardar plato'}</button></footer></div>`;
  },

  // V13 · Generar solicitud de cotización (una o varias solicitudes, M-01 y DEC-05)
  'generar-cotizacion'() {
    const m = ui.m;
    const cotizables = asc(S.solicitudes.filter((s) => s.estado !== 'COMPLETADA'));
    const agregados = new Map();
    cotizables.filter((s) => m.sols.includes(s.numero)).forEach((s) => s.items.forEach((i) => {
      const a = agregados.get(i.productoId) || { id: i.productoId, producto: i.producto, cantidad: 0, unidad: i.unidad };
      a.cantidad += i.cantidad; agregados.set(i.productoId, a);
    }));
    const productos = [...agregados.values()];
    const incluidos = productos.filter((p) => !m.excl.includes(p.id));
    const valido = m.sols.length && incluidos.length;
    return `<div class="modal md">${mHead('file', 'Generar solicitud de cotización')}
      <section class="m-sec"><h3>Solicitudes a incluir</h3>
        <div class="check-list">${cotizables.map((s) => `<label class="check-row"><input type="checkbox" data-chg="m-sol" value="${s.numero}" ${m.sols.includes(s.numero) ? 'checked' : ''}>
          <b>${s.numero}</b><span>${esc(s.sucursal)}</span><span>${fecha(s.fecha)}</span>${badge(s.estado, { dot: true })}</label>`).join('') || '<p class="muted">No hay solicitudes por cotizar.</p>'}</div>
        ${m.sols.length ? '' : '<p class="form-error">Debe seleccionar al menos una solicitud</p>'}
      </section>
      <section class="m-sec"><h3>Productos a cotizar</h3>
        <label class="search sm">${icon('search')}<input data-inp="buscar-producto" placeholder="Buscar producto..." aria-label="Buscar producto"></label>
        <div class="scroll-table">${tabla(['', 'Producto', 'Cantidad', 'Unidad'], productos.map((p) => `<tr data-buscar="${esc(p.producto.toLowerCase())}"><td><input type="checkbox" data-chg="m-prod-cot" value="${p.id}" ${m.excl.includes(p.id) ? '' : 'checked'} aria-label="Cotizar ${esc(p.producto)}"></td>
          <td>${esc(p.producto)}</td><td>${num(p.cantidad)}</td><td>${esc(p.unidad)}</td></tr>`), 'Marca una solicitud para ver sus productos.')}</div>
      </section>
      <div class="frow two">
        <label class="fl">Proveedor<select data-chg="m-proveedor">${S.proveedores.map((p) => `<option value="${p.id}" ${String(p.id) === String(m.proveedor) ? 'selected' : ''}>${esc(p.nombre)}</option>`).join('')}</select></label>
        <label class="fl">Fecha máxima para cotización<input type="date" data-chg="m-fecha" value="${m.fecha}" min="${hoyISO()}"></label>
      </div>
      <section class="m-sec"><h3>Resumen</h3>
        ${tabla(['Producto', 'Cantidad', 'Unidad'], incluidos.map((p) => `<tr><td>${esc(p.producto)}</td><td>${num(p.cantidad)}</td><td>${esc(p.unidad)}</td></tr>`), 'Sin productos seleccionados.')}
      </section>
      <footer class="m-foot"><button class="btn ghost" data-act="cerrar">Cancelar</button><button class="btn primary" data-act="enviar-cotizacion" ${valido ? '' : 'disabled'}>GENERAR Y ENVIAR COTIZACIÓN</button></footer></div>`;
  },

  // V14 · Cotización generada con éxito
  'cotizacion-ok'({ numero }) {
    const c = cot(numero);
    const correo = S.correos.some((m) => m.asunto.includes(numero));
    const enviada = c.estado === 'ENVIADA';
    return `<div class="ticket-modal">${cerrar}<div class="ticket-slot"></div><div class="ticket">
      ${okIcon()}<h2>Solicitud de cotización<br>enviada</h2><div class="code-big">${c.numero}</div>
      <div class="estado-pill">Estado: ${badge(c.estado, { up: true, dot: true })}</div>
      <p class="muted">${enviada ? 'El PDF fue generado y enviado al proveedor.' : 'Quedó pendiente del precio estándar del Ministerio; se reenvía desde ms-cotizaciones.'}</p>
      <dl class="dl dotted"><dt>Solicitud origen:</dt><dd>${c.solicitudes.join(', ')}</dd><dt>Proveedor:</dt><dd>${esc(c.proveedor)}</dd><dt>Fecha envío:</dt><dd>${fecha(c.fechaEnvio)}</dd><dt>Fecha límite:</dt><dd>${fecha(c.fechaLimite)}</dd></dl>
      <h4 class="sub-title">Productos solicitados</h4>
      <ul class="ticket-items">${c.items.map((i) => `<li><span>${esc(i.producto)}</span><b>${num(i.cantidad)} ${esc(i.unidad)}</b></li>`).join('')}</ul>
      <div class="ticket-checks"><span>${icon('file')} PDF generado ${c.pdf ? `<i class="ok">${icon('check')}</i>` : ''}</span><span>${icon('send')} Correo enviado ${correo ? `<i class="ok">${icon('check')}</i>` : ''}</span></div>
      <p class="ticket-foot"><b>KONRAD GOURMET</b><br>¡Cotización enviada con éxito!</p>
    </div></div>`;
  },

  // V18 · Registrar valores de cotización
  'registrar-valores'({ numero }) {
    const c = cot(numero);
    const filas = c.items.map((i) => `<tr>
      <td><b>${esc(i.producto)}</b></td><td>${num(i.cantidad)} ${esc(i.unidad)}</td>
      <td>${money(i.precioEstandar * i.cantidad)}<small class="unit">${money(i.precioEstandar)}/${esc(i.unidad)}</small></td>
      <td><label class="money-in"><span>$</span><input type="number" min="0" step="any" data-precio="${i.productoId}" data-cant="${i.cantidad}" value="${i.precioProveedor ?? ''}" aria-label="Precio del proveedor por ${esc(i.unidad)} de ${esc(i.producto)}"><em>/${esc(i.unidad)}</em></label>
        <small class="unit js-linea-${i.productoId}">${i.precioProveedor != null ? '= ' + money(i.precioProveedor * i.cantidad) : ''}</small></td></tr>`).join('');
    return `<div class="modal md">${mHead('file', 'Registrar valores de cotización')}
      <div class="info-box three"><div><small>Cotización</small><b>${c.numero}</b></div><div><small>Proveedor:</small><b>${esc(c.proveedor)}</b></div><div><small>Solicitud:</small><b>${c.solicitudes.join(', ')}</b></div></div>
      <div class="table-wrap boxed"><table><thead><tr><th>Producto</th><th>Cantidad</th><th>Estándar Ministerio</th><th>Precio proveedor</th></tr></thead><tbody>${filas}</tbody></table></div>
      <div class="info-box two totals"><div><small>Valor estándar total</small><b>${money(c.estandar)}</b></div><div><small>Valor cotizado total</small><b class="js-cotizado">${money(c.cotizado)}</b></div></div>
      ${ui.m.error ? `<p class="form-error">${esc(ui.m.error)}</p>` : ''}
      <footer class="m-foot center"><button class="btn outline wide" data-act="guardar-valores" data-n="${c.numero}">Guardar</button><button class="btn primary wide" data-act="validar-cotizacion" data-n="${c.numero}">Validar cotización</button></footer></div>`;
  },

  // V19 · Validar resultados
  validacion({ numero }) {
    const c = cot(numero);
    const tono = { OPCIONADA: 'verde', RECHAZADA: 'rojo', SOSPECHOSA: 'ambar' }[c.estado] || 'gris';
    return `<div class="ticket-modal">${cerrar}<div class="ticket">
      <div class="ticket-brand center"><img src="assets/logo/logo.png" alt=""><div><b>Konrad <span>Gourmet</span></b><small>Sistema de Abastecimiento</small></div></div>
      ${okIcon()}<h2 class="caps">Validación completada</h2>
      <div class="cmp"><div><span>Valor estándar Ministerio</span><b>${money(c.estandar)}</b></div><div><span>Valor proveedor</span><b>${money(c.cotizado)}</b></div>
        <div class="diff-row"><span>Diferencia</span><b class="${c.diferencia > 0 ? 't-naranja' : 't-verde'}">${pct(c.diferencia)}</b></div></div>
      <div class="result-pill ${tono}"><i></i>${(ESTADOS[c.estado] || [c.estado])[0].toUpperCase()}</div>
      <p class="info-line">${icon('info')} El sistema calculó automáticamente el estado de la cotización.</p>
      <p class="muted sm">Otros posibles resultados</p>
      <div class="result-chips"><span class="rojo ${c.estado === 'RECHAZADA' ? 'on' : ''}"><i></i>RECHAZADA</span><span class="ambar ${c.estado === 'SOSPECHOSA' ? 'on' : ''}"><i></i>SOSPECHOSA</span><span class="verde ${c.estado === 'OPCIONADA' ? 'on' : ''}"><i></i>OPCIONADA</span></div>
      <div class="m-actions"><button class="btn outline" data-go="cotizaciones" data-sel="${c.numero}">Ver cotización</button><button class="btn primary" data-go="opcionadas" data-sel="${c.numero}">Ver cotizaciones opcionadas</button></div>
    </div></div>`;
  },

  // V21 · Generar orden de compra
  'generar-orden'({ numero }) {
    const c = cot(numero);
    return `<div class="modal md">${mHead('receipt', 'Generar orden de compra')}
      <div class="info-box three"><div><small>Cotización:</small><b>${c.numero}</b></div><div><small>${c.solicitudes.length > 1 ? 'Solicitudes:' : 'Solicitud:'}</small><b>${c.solicitudes.join(', ')}</b></div><div><small>Proveedor:</small><b>${esc(c.proveedor)}</b></div></div>
      <h3 class="m-h3">Productos</h3>
      ${tabla(['Producto', 'Cantidad', 'Precio'], c.items.map((i) => `<tr><td>${esc(i.producto)}</td><td>${num(i.cantidad)} ${esc(i.unidad)}</td><td>${money(i.precioProveedor * i.cantidad)}</td></tr>`))}
      <div class="total-row peach"><span>Total</span><b>${money(c.cotizado)}</b></div>
      <footer class="m-foot"><button class="btn ghost" data-act="cerrar">Cancelar</button><button class="btn primary" data-act="confirmar-orden" data-n="${c.numero}">Confirmar orden de compra</button></footer></div>`;
  },

  // V22 · Orden generada
  'orden-ok'({ numero }) {
    const o = ord(numero);
    return `<div class="ticket-modal">${cerrar}<div class="ticket">
      ${okIcon()}<h2 class="caps">Orden generada</h2><div class="code-big dark">${o.numero}</div>
      <dl class="dl"><dt>Proveedor:</dt><dd><b>${esc(o.proveedor)}</b></dd><dt>Cotización:</dt><dd><b>${o.cotizacion}</b></dd></dl>
      <div class="total-row peach"><span>Total</span><b>${money(o.total)}</b></div>
      <button class="btn primary w" data-go="ordenes" data-sel="${o.numero}">VER ORDEN</button>
      <button class="btn outline w" data-go="inicio">VOLVER AL INICIO</button>
    </div></div>`;
  },
};

function abrirModal(nombre, datos = {}, m = null) {
  ui.modal = { nombre, datos };
  if (m) ui.m = m;
  renderModal();
}
function cerrarModal() { ui.modal = null; ui.m = {}; renderModal(); }
function renderModal() {
  const root = $('#modal-root');
  if (!ui.modal) { root.innerHTML = ''; document.body.classList.remove('modal-open'); return; }
  const scroll = $('.overlay > *', root)?.scrollTop || 0;
  root.innerHTML = `<div class="overlay" data-act="fondo">${MODALES[ui.modal.nombre](ui.modal.datos)}</div>`;
  const caja = $('.overlay > *', root);
  if (caja) { caja.scrollTop = scroll; caja.setAttribute('role', 'dialog'); caja.setAttribute('aria-modal', 'true'); }
  document.body.classList.add('modal-open');
}

// ----------------------------------------------------------------------------- render y navegación

function render() {
  document.body.className = 'rol-' + rol + (ui.modal ? ' modal-open' : '');
  $('#sidebar').innerHTML = sidebar();
  const v = vista === 'perfil' ? perfil() : (VISTAS[rol][vista] || VISTAS[rol].inicio)();
  $('#content').innerHTML = topbar(v.top) + `<div class="page">${v.html}</div>`;
  renderModal();
}

function ir(v, sel) {
  vista = v;
  if (sel !== undefined) ui.sel[v] = sel;
  ui.modal = null; ui.m = {};
  if (location.hash !== `#${rol}/${v}`) history.pushState(null, '', `#${rol}/${v}`);
  render();
  window.scrollTo(0, 0);
  $('#content').focus({ preventScroll: true });
}

function toast(mensaje, error = false) {
  const t = $('#toast');
  t.innerHTML = `${icon(error ? 'alert' : 'circleCheck')}<span>${esc(mensaje)}</span>`;
  t.className = 'show' + (error ? ' error' : '');
  clearTimeout(toast.t);
  toast.t = setTimeout(() => { t.className = ''; }, 4200);
}

async function api(ruta, datos) {
  const r = await fetch('/api/' + ruta, datos ? { method: 'POST', body: new URLSearchParams(datos) } : { cache: 'no-store' });
  let d;
  try { d = await r.json(); } catch { throw new Error('La API local no respondió. ¿Está corriendo ./web.sh?'); }
  if (!r.ok) throw new Error(d.error || 'No se pudo completar la operación');
  return d;
}
async function enviar(ruta, datos) {
  S = await api(ruta, datos);
  return S.creado;
}

// ----------------------------------------------------------------------------- acciones

const ACCIONES = {
  salir() { try { sessionStorage.removeItem('kg-rol'); } catch { /* sin almacenamiento */ } location.href = 'index.html'; },
  grupo(el) {
    const t = el.dataset.t;
    const activo = ROLES[rol].menu.find((m) => m.t === t)?.hijos.some((h) => h.id === vista);
    if (activo) { ui.abiertos[t] = !ui.abiertos[t]; render(); } else { ui.abiertos[t] = true; ir(el.dataset.go1); }
  },
  sel(el) {
    ui.sel[el.dataset.v] = el.dataset.v === 'platos' ? Number(el.dataset.n) : el.dataset.n;
    if (el.dataset.ir && vista !== 'solicitudes') return ir('solicitudes', el.dataset.n);
    render();
  },
  pagina(el) { ui.pag[el.dataset.id] = Number(el.dataset.p); render(); },
  fondo(el, e) { if (e.target === el) cerrarModal(); },
  cerrar: () => cerrarModal(),
  mes(el) {
    const hoy = new Date();
    const c = ui.cal || { y: hoy.getFullYear(), m: hoy.getMonth() };
    const d = new Date(c.y, c.m + Number(el.dataset.d), 1);
    ui.cal = { y: d.getFullYear(), m: d.getMonth() };
    render();
  },

  // mesero
  categoria(el) { ui.cat = el.dataset.c; render(); },
  'qty-tarjeta'(el) { const id = el.dataset.id; ui.cantidadTarjeta[id] = Math.max(1, (ui.cantidadTarjeta[id] || 1) + Number(el.dataset.d)); render(); },
  agregar(el) {
    const id = Number(el.dataset.id);
    const nota = $(`[data-nota="${id}"]`)?.value.trim() || '';
    const actual = ui.carrito[id] || { cantidad: 0, nota: '' };
    ui.carrito[id] = { cantidad: actual.cantidad + (ui.cantidadTarjeta[id] || 1), nota: nota || actual.nota };
    ui.cantidadTarjeta[id] = 1;
    render();
  },
  'qty-carrito'(el) {
    const it = ui.carrito[el.dataset.id];
    it.cantidad += Number(el.dataset.d);
    if (it.cantidad <= 0) delete ui.carrito[el.dataset.id];
    render();
  },
  quitar(el) { delete ui.carrito[el.dataset.id]; render(); },
  'cancelar-pedido'() { ui.carrito = {}; render(); },
  async 'registrar-pedido'() {
    const datos = { mesa: ui.mesa };
    Object.entries(ui.carrito).forEach(([id, it]) => { datos['plato_' + id] = it.cantidad; if (it.nota) datos['nota_' + id] = it.nota; });
    const numero = await enviar('pedidos', datos);
    ui.carrito = {};
    render();
    abrirModal('pedido', { numero });
  },
  'otro-pedido'() { ui.carrito = {}; ir('nuevo-pedido'); },

  // jefe de cocina
  async cierre() {
    await enviar('cierre', {});
    const n = S.insumos.filter((i) => i.alerta).length;
    render();
    toast(`Cierre ejecutado: ${n ? plural(n, 'insumo quedó', 'insumos quedaron') + ' bajo el 25 %' : 'ningún insumo quedó bajo el 25 %'}.`);
  },
  'nueva-solicitud'(el) {
    const id = el.dataset.producto ? Number(el.dataset.producto) : null;
    const base = S.insumos.find((i) => i.id === id) || S.insumos[0];
    // primera solicitud del día: la de la demo (Res 20 kg Marca X y Arroz 15 kg)
    const filas = !id && !S.solicitudes.length ? [{ producto: 1, cantidad: 20, marca: 'Marca X', unidad: 'kg' }, { producto: 2, cantidad: 15, marca: '', unidad: 'kg' }] : [];
    abrirModal('nueva-solicitud', {}, { cat: base.categoria, prod: base.id, filas, cant: '', marca: '' });
  },
  'm-agregar-producto'() {
    const m = ui.m;
    const prod = Number($('#m-prod').value), cantidad = Number($('#m-cant').value), marca = $('#m-marca').value.trim();
    if (!prod || !(cantidad > 0)) { m.error = 'Indica un producto y una cantidad mayor que cero.'; m.cant = $('#m-cant').value; m.marca = marca; return renderModal(); }
    const previo = m.filas.find((f) => f.producto === prod && (f.marca || '') === marca);
    if (previo) previo.cantidad += cantidad; else m.filas.push({ producto: prod, cantidad, marca, unidad: $('#m-unidad').value });
    m.error = ''; m.cant = ''; m.marca = '';
    renderModal();
  },
  'm-quitar-producto'(el) { ui.m.filas.splice(Number(el.dataset.k), 1); renderModal(); },
  async 'enviar-solicitud'() {
    const datos = {};
    ui.m.filas.forEach((f, k) => Object.assign(datos, { ['producto_' + k]: f.producto, ['cantidad_' + k]: f.cantidad, ['marca_' + k]: f.marca, ['unidad_' + k]: f.unidad }));
    const numero = await enviar('solicitudes', datos);
    ui.sel.solicitudes = numero;
    render();
    abrirModal('solicitud-ok', { numero });
  },
  'nuevo-plato'() { abrirModal('nuevo-plato', {}, { nombre: '', descripcion: '', categoria: '', precio: '', imagen: '', ingCat: '', ings: [] }); },
  'editar-plato'(el) {
    const p = S.platos.find((x) => x.id === Number(el.dataset.id));
    if (!p) return;
    abrirModal('nuevo-plato', {}, { id: p.id, nombre: p.nombre, descripcion: p.descripcion || '', categoria: p.categoria || '', precio: String(p.precio),
      imagen: '', tieneImagen: p.imagen, ingCat: '', ings: p.receta.map((r) => ({ id: r.id, cantidad: r.cantidad, unidad: 'kg' })) });
  },
  async 'estado-plato'(el) {
    const p = S.platos.find((x) => x.id === Number(el.dataset.id));
    if (!p) return;
    try {
      await enviar('platos/estado', { id: p.id, activo: el.dataset.activo });
      render();
      toast(Number(el.dataset.activo) ? `Plato «${p.nombre}» activado: vuelve al catálogo del mesero.` : `Plato «${p.nombre}» desactivado: sale del catálogo del mesero, sus pedidos anteriores siguen intactos.`);
    } catch (e) { toast(e.message, true); }
  },
  'm-agregar-ing'() {
    const m = ui.m;
    const id = Number($('#m-ing-prod').value), cantidad = Number($('#m-ing-cant').value), unidad = $('#m-ing-unidad').value;
    if (!id || !(cantidad > 0)) { m.error = 'Elige un producto y una cantidad mayor que cero.'; return renderModal(); }
    m.error = '';
    m.ings.push({ id, cantidad, unidad });
    renderModal();
  },
  'm-quitar-ing'(el) { ui.m.ings.splice(Number(el.dataset.k), 1); renderModal(); },
  async 'guardar-plato'() {
    const m = ui.m;
    if (!m.nombre.trim() || !m.categoria || !(Number(m.precio) > 0)) { m.error = 'Completa el nombre, la categoría y el precio de venta.'; return renderModal(); }
    const datos = { nombre: m.nombre, descripcion: m.descripcion, categoria: m.categoria, precio: m.precio };
    m.ings.forEach((g) => { const kg = g.unidad === 'g' ? g.cantidad / 1000 : g.cantidad; datos['ing_' + g.id] = String((Number(datos['ing_' + g.id] || 0) + kg)); });
    if (m.imagen) datos.imagen = m.imagen;
    if (m.id) datos.id = m.id;
    const id = await enviar(m.id ? 'platos/editar' : 'platos', datos);
    ui.sel.platos = Number(id);
    cerrarModal(); render();
    toast(`Plato «${m.nombre}» guardado: ms-menu publicó menu.actualizado y ${m.id ? 'el catálogo del mesero y la receta quedaron al día.' : 'ya aparece en el catálogo del mesero.'}`);
  },

  // auxiliar de compras
  'hu07-buscar': () => buscarHu07(),
  'hu07-limpiar'() { ui.hu07 = { desde: '', hasta: '', producto: '', marca: '', estado: '' }; ui.hu07Res = null; ui.hu07Error = ''; render(); },
  'fcot-buscar'() { ui.filtroCot = { ...ui.filtroCotBorrador }; ui.pag['cot-aux'] = 1; render(); },
  'fcot-limpiar'() { ui.filtroCot = { proveedor: '', fecha: '', estado: '' }; ui.filtroCotBorrador = { ...ui.filtroCot }; render(); },
  'generar-cotizacion'(el) {
    const cotizables = S.solicitudes.filter((s) => s.estado !== 'COMPLETADA');
    const inicial = el.dataset.sol || (cotizables.length === 1 ? cotizables[0].numero : null);
    const abc = S.proveedores[0];
    abrirModal('generar-cotizacion', {}, { sols: inicial ? [inicial] : [], excl: [], proveedor: abc ? abc.id : '', fecha: hoyISO(7) });
  },
  async 'enviar-cotizacion'() {
    const m = ui.m;
    const datos = { proveedor: m.proveedor, fechaLimite: m.fecha };
    m.sols.forEach((s, k) => { datos['solicitud_' + k] = s; });
    const ids = new Set(S.solicitudes.filter((s) => m.sols.includes(s.numero)).flatMap((s) => s.items.map((i) => i.productoId)));
    ids.forEach((id) => { if (!m.excl.includes(id)) datos['producto_' + id] = 'on'; });
    const numero = await enviar('cotizaciones', datos);
    ui.sel.cotizaciones = numero;
    render();
    abrirModal('cotizacion-ok', { numero });
  },

  // director de compras
  'registrar-valores'(el) { if (el.dataset.n) abrirModal('registrar-valores', { numero: el.dataset.n }, {}); },
  'guardar-valores': (el) => enviarValores(el.dataset.n, false),
  'validar-cotizacion': (el) => enviarValores(el.dataset.n, true),
  'generar-orden'(el) { abrirModal('generar-orden', { numero: el.dataset.n }, {}); },
  async 'confirmar-orden'(el) {
    const numero = await enviar('ordenes', { cotizacion: el.dataset.n });
    ui.sel.ordenes = numero;
    render();
    abrirModal('orden-ok', { numero });
  },
};

async function enviarValores(numero, validar) {
  const datos = { cotizacion: numero, validar: validar ? '1' : '0' };
  for (const input of $$('[data-precio]')) {
    if (!(Number(input.value) > 0)) { ui.m.error = 'Ingresa el precio del proveedor de todos los productos.'; return renderModal(); }
    datos['precio_' + input.dataset.precio] = input.value;
  }
  await enviar('valores', datos);
  render();
  if (validar) { ui.sel.cotizaciones = numero; abrirModal('validacion', { numero }); }
  else { cerrarModal(); toast(`Valores de ${numero} guardados: la cotización queda pendiente de validar.`); }
}

async function buscarHu07() {
  const f = ui.hu07;
  try {
    ui.hu07Res = await api('solicitudes?' + new URLSearchParams(f));
    ui.hu07Error = '';
  } catch (e) { ui.hu07Error = e.message; }
  ui.pag['sol-aux'] = 1;
  render();
}

// cambios en selects y casillas
const CAMBIOS = {
  mesa(el) { ui.mesa = Number(el.value); },
  'inv-cat'(el) { ui.invCat = el.value; render(); },
  'sol-fecha'(el) { ui.filtroSol.fecha = el.value; ui.pag['sol-jefe'] = 1; render(); },
  'sol-estado'(el) { ui.filtroSol.estado = el.value; ui.pag['sol-jefe'] = 1; render(); },
  'comparar-sol'(el) { ui.compararSol = el.value; ui.sel.opcionadas = null; render(); },
  'm-cat'(el) { ui.m.cat = el.value; ui.m.prod = null; ui.m.cant = $('#m-cant').value; ui.m.marca = $('#m-marca').value; renderModal(); },
  'm-prod'(el) { ui.m.prod = Number(el.value); ui.m.cant = $('#m-cant').value; ui.m.marca = $('#m-marca').value; renderModal(); },
  'm-ing-cat'(el) { ui.m.ingCat = el.value; renderModal(); },
  'm-imagen'(el) {
    const f = el.files[0];
    if (!f) return;
    if (f.size > 1024 * 1024) { ui.m.error = 'La imagen supera 1 MB.'; return renderModal(); }
    const r = new FileReader();
    r.onload = () => { ui.m.imagen = r.result; ui.m.error = ''; renderModal(); };
    r.readAsDataURL(f);
  },
  'm-sol'(el) { const m = ui.m; m.sols = el.checked ? [...m.sols, el.value] : m.sols.filter((s) => s !== el.value); renderModal(); },
  'm-prod-cot'(el) { const m = ui.m, id = Number(el.value); m.excl = el.checked ? m.excl.filter((x) => x !== id) : [...m.excl, id]; renderModal(); },
  'm-proveedor'(el) { ui.m.proveedor = el.value; },
  'm-fecha'(el) { ui.m.fecha = el.value; },
};

document.addEventListener('click', async (e) => {
  const go = e.target.closest('[data-go]');
  const act = e.target.closest('[data-act]');
  try {
    // gana el elemento más interno: un botón con data-act dentro de una fila con data-go, o al revés
    if (act && (!go || go.contains(act))) {
      if (act.disabled) return;
      act.disabled = act.tagName === 'BUTTON' && /registrar-pedido|enviar|guardar|validar|confirmar|cierre/.test(act.dataset.act) ? true : act.disabled;
      await ACCIONES[act.dataset.act]?.(act, e);
    } else if (go) {
      ir(go.dataset.go, go.dataset.sel);
    }
  } catch (err) {
    toast(err.message, true);
    if (ui.modal) renderModal(); else render();
  }
});

document.addEventListener('change', (e) => {
  const el = e.target;
  if (el.dataset.chg) CAMBIOS[el.dataset.chg]?.(el);
  else if (el.dataset.m !== undefined) { ui.m[el.dataset.m] = el.value; }
  else if (el.dataset.hu07) ui.hu07[el.dataset.hu07] = el.value;
  else if (el.dataset.fcot) ui.filtroCotBorrador[el.dataset.fcot] = el.value;
  else if (el.dataset.fdir) { ui.filtroDir[el.dataset.fdir] = el.value; ui.pag.cotizaciones = ui.pag.pendientes = 1; render(); }
  else if (el.dataset.ford) { ui.filtroOrd[el.dataset.ford] = el.value; ui.pag.ordenes = 1; render(); }
});

document.addEventListener('input', (e) => {
  const el = e.target;
  const filtrar = (selector, texto) => $$(selector).forEach((n) => { n.style.display = n.dataset.buscar.includes(texto) ? '' : 'none'; });
  if (el.dataset.inp === 'buscar-plato') filtrar('.dish[data-buscar]', el.value.trim().toLowerCase());
  if (el.dataset.inp === 'buscar-insumo') { ui.invBuscar = el.value; filtrar('tr[data-buscar]', el.value.trim().toLowerCase()); }
  if (el.dataset.inp === 'buscar-producto') filtrar('.scroll-table tr[data-buscar]', el.value.trim().toLowerCase());
  if (el.dataset.m !== undefined) {
    ui.m[el.dataset.m] = el.value;
    if (el.dataset.m === 'descripcion') { const c = el.parentElement.querySelector('.counter'); if (c) c.textContent = `${el.value.length}/200`; }
  }
  if (el.dataset.hu07) ui.hu07[el.dataset.hu07] = el.value;
  if (el.dataset.precio !== undefined) {
    // V18: el valor de la línea y el total cotizado se recalculan mientras se escribe
    let total = 0, completo = true;
    $$('[data-precio]').forEach((i) => {
      const v = Number(i.value), linea = v * Number(i.dataset.cant);
      const out = $('.js-linea-' + i.dataset.precio);
      if (out) out.textContent = v > 0 ? '= ' + money(linea) : '';
      if (v > 0) total += linea; else completo = false;
    });
    const t = $('.js-cotizado');
    if (t) t.textContent = completo ? money(total) : '—';
  }
});

document.addEventListener('keydown', (e) => { if (e.key === 'Escape' && ui.modal) cerrarModal(); });
window.addEventListener('popstate', () => {
  const [r, v] = location.hash.slice(1).split('/');
  if (r === rol && v && v !== vista) { vista = v; ui.modal = null; render(); }
});

// ----------------------------------------------------------------------------- arranque

async function iniciar() {
  const [r, v] = location.hash.slice(1).split('/');
  let guardado = null;
  try { guardado = sessionStorage.getItem('kg-rol'); } catch { /* sin almacenamiento */ }
  rol = ROLES[r] ? r : ROLES[guardado] ? guardado : null;
  if (!rol) { location.replace('index.html'); return; }
  try { sessionStorage.setItem('kg-rol', rol); } catch { /* sin almacenamiento */ }
  const vistas = VISTAS[rol];
  vista = v && (vistas[v] || v === 'perfil') ? v : 'inicio';
  history.replaceState(null, '', `#${rol}/${vista}`);
  try {
    S = await api('state');
  } catch (e) {
    $('#content').innerHTML = `<div class="page"><section class="card offline">${icon('alert')}<h2>No hay conexión con la demo</h2><p>Arranca el servidor con <code>./web.sh</code> y abre la dirección que imprime.</p></section></div>`;
    return;
  }
  if (rol === 'director') {
    // el menú del director lo arma el Composite de ms-identidad (DirectorCompras.construirMenu())
    try {
      const raiz = await api('menu/director');
      const convertir = (n) => {
        const r = RUTAS_DIRECTOR[n.ruta];
        if (!r) return null;
        return n.hijos.length ? { t: n.titulo, i: r.i, hijos: n.hijos.map((h) => ({ id: RUTAS_DIRECTOR[h.ruta]?.id, t: h.titulo })).filter((h) => h.id) } : { id: r.id, t: n.titulo, i: r.i };
      };
      const menu = raiz.hijos.map(convertir).filter(Boolean);
      if (menu.length) ROLES.director.menu = menu;
    } catch { /* se queda el menú de respaldo */ }
  }
  render();
  setInterval(() => {
    $$('.js-hora').forEach((n) => { n.textContent = horaActual(); });
    $$('.js-fecha').forEach((n) => { n.textContent = fechaLarga(); });
  }, 20000);
}

iniciar();
