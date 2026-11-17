// Formatos de la interfaz en memoria de Codex (app.js), para que las cifras se vean igual.
const NUM0 = new Intl.NumberFormat('es-CO', { maximumFractionDigits: 0 });
const NUM2 = new Intl.NumberFormat('es-CO', { maximumFractionDigits: 2 });

export const money = (v: number | null | undefined) => (v == null ? '—' : '$' + NUM0.format(v));
export const num = (v: number | null | undefined) => (v == null ? '—' : NUM2.format(v));
export const pad2 = (n: number) => String(n).padStart(2, '0');
export const plural = (n: number, uno: string, varios: string) => `${n} ${n === 1 ? uno : varios}`;
export const mesaTxt = (n: number) => `Mesa ${pad2(n)}`;

/** Las fechas llegan como LocalDateTime de Bogotá («2026-11-17T01:32:57.16»), sin zona. */
export const aFecha = (s: string | null | undefined) => (s ? new Date(s.slice(0, 23)) : null);
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
