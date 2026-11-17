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
};

export const EMOJI: Record<string, string> = {
  'Bandeja paisa': '🍛', Ajiaco: '🍲', 'Punta de anca': '🥩', 'Pechuga a la plancha': '🍗', 'Ensalada césar': '🥗',
  'Arepa con queso': '🫓', 'Limonada natural': '🍋', 'Jugo natural': '🧃', Res: '🥩', Arroz: '🍚', Papa: '🥔', Pollo: '🍗',
};

/** Las categorías de la carta (V03 y V06, DEC-66). */
export const CATEGORIAS_CARTA = ['Fuertes', 'Entradas', 'Sopas', 'Bebidas', 'Postres'];
