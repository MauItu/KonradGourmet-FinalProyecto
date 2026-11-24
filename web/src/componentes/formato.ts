// Formatos de la interfaz en memoria (app.js), para que las cifras se vean igual.
const NUM0 = new Intl.NumberFormat('es-CO', { maximumFractionDigits: 0 });
const NUM2 = new Intl.NumberFormat('es-CO', { maximumFractionDigits: 2 });

const PCT = new Intl.NumberFormat('es-CO', { minimumFractionDigits: 2, maximumFractionDigits: 2 });

/** La diferencia contra el Ministerio: «+4,81 %», «−61,54 %» (pct() de app.js). */
export const pct = (v: number | null | undefined) => (v == null ? '—' : (v > 0 ? '+' : v < 0 ? '−' : '') + PCT.format(Math.abs(v)) + ' %');
export const money = (v: number | null | undefined) => (v == null ? '—' : '$' + NUM0.format(v));
export const num = (v: number | null | undefined) => (v == null ? '—' : NUM2.format(v));
export const pad2 = (n: number) => String(n).padStart(2, '0');
export const plural = (n: number, uno: string, varios: string) => `${n} ${n === 1 ? uno : varios}`;
export const mesaTxt = (n: number) => `Mesa ${pad2(n)}`;

/**
 * Las fechas llegan como LocalDateTime de Bogotá («2026-11-17T01:32:57.16») o LocalDate («2026-11-24», las
 * de las cotizaciones), sin zona: se leen como hora local (un «AAAA-MM-DD» solo, el navegador lo tomaría en UTC).
 */
export const aFecha = (s: string | null | undefined) => (s ? new Date(s.length === 10 ? `${s}T00:00` : s.slice(0, 23)) : null);
export const fecha = (s: string | null | undefined) => {
  const d = aFecha(s);
  return d ? `${pad2(d.getDate())}/${pad2(d.getMonth() + 1)}/${d.getFullYear()}` : '—';
};
export const hora = (s: string | null | undefined) => {
  const d = aFecha(s);
  return d ? d.toLocaleTimeString('es-CO', { hour: 'numeric', minute: '2-digit' }) : '—';
};

// Estados con su texto y color (los mismos de app.js).
export const ESTADOS: Record<string, [string, string]> = {
  EN_PREPARACION: ['En preparación', 'naranja'], LISTO: ['Listo', 'verde'], SERVIDO: ['Servido', 'verde'], ENTREGADO: ['Entregado', 'verde'],
  PENDIENTE: ['Pendiente', 'ambar'], EN_TRAMITE: ['En trámite', 'azul'], COMPLETADA: ['Completada', 'verde'],
  PENDIENTE_PRECIO: ['Pendiente de precio', 'gris'], ENVIADA: ['Enviada', 'naranja'], RECHAZADA: ['Rechazada', 'rojo'],
  SOSPECHOSA: ['Sospechosa', 'ambar'], OPCIONADA: ['Opcionada', 'azul'], CON_ORDEN: ['Con orden', 'verde'],
  EN_PROCESO: ['En proceso', 'naranja'], FINALIZADA: ['Finalizada', 'verde'],
  ACTIVO: ['Activo', 'verde'], INACTIVO: ['Inactivo', 'gris'],
  SUGERIDA: ['Sugerida', 'ambar'], ACEPTADA: ['Aceptada', 'verde'], DESCARTADA: ['Descartada', 'gris'],
};

/** Más antiguo primero (asc() de app.js). */
export const asc = <T extends { numero: string }>(a: T[]) => [...a].sort((x, y) => x.numero.localeCompare(y.numero));

/** Hoy (o dentro de n días) como AAAA-MM-DD, en la hora del navegador (hoyISO() de app.js). */
export const hoyISO = (dias = 0) => {
  const d = new Date(); d.setDate(d.getDate() + dias);
  return `${d.getFullYear()}-${pad2(d.getMonth() + 1)}-${pad2(d.getDate())}`;
};

/** Más reciente primero (por número, como desc() de app.js). */
export const desc = <T extends { numero: string }>(a: T[]) => [...a].sort((x, y) => y.numero.localeCompare(x.numero));

/** El filtro de fecha de V10: «hoy» o los últimos 7 días (enRango() de app.js). */
export const enRango = (s: string, filtro: string) => {
  if (!filtro) return true;
  const d = aFecha(s);
  if (!d) return false;
  const hoy = new Date(); hoy.setHours(0, 0, 0, 0);
  if (filtro === 'hoy') return d >= hoy;
  const hace7 = new Date(hoy); hace7.setDate(hoy.getDate() - 7);
  return d >= hace7;
};

export const EMOJI: Record<string, string> = {
  'Bandeja paisa': '🍛', Ajiaco: '🍲', 'Punta de anca': '🥩', 'Pechuga a la plancha': '🍗', 'Ensalada césar': '🥗',
  'Arepa con queso': '🫓', 'Limonada natural': '🍋', 'Jugo natural': '🧃', Res: '🥩', Arroz: '🍚', Papa: '🥔', Pollo: '🍗',
};

/** Las categorías de la carta (V03 y V06, DEC-66). */
export const CATEGORIAS_CARTA = ['Fuertes', 'Entradas', 'Sopas', 'Bebidas', 'Postres'];

/** Las sucursales de la empresa (las mismas de ms-identidad). */
export const SUCURSALES = [
  { id: 1, nombre: 'Sucursal Centro' },
  { id: 2, nombre: 'Sucursal Norte' },
  { id: 3, nombre: 'Sucursal Chapinero' },
];

/** Los 11 microservicios que envían sus errores a Elasticsearch (DEC-84), para el filtro de HU-22. */
export const SERVICIOS = ['ms-identidad', 'ms-pedidos', 'ms-menu', 'ms-inventario', 'ms-solicitudes', 'ms-cotizaciones',
  'ms-ordenes', 'ms-notificaciones', 'ms-integracion', 'ms-analitica', 'ms-administracion'];
