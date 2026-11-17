// Las rutas de la tabla 5 que usan los cortes 3 y 4a, a través del gateway. El usuario y la sucursal los pone
// cada servicio desde el JWT; la SPA no los manda.
import { api, apiArchivo, subirArchivo } from './cliente';

// ── ms-pedidos (V02–V04) ────────────────────────────────────────────────────────────────────────
export interface PlatoCarta { platoId: number; nombre: string; categoria: string | null; precio: number }
export interface Mesa { numero: number; capacidad: number }
export interface DetallePedido { platoId: number; plato: string; cantidad: number; nota: string | null; precioUnitario: number; subtotal: number }
export interface Pedido {
  numero: string; mesa: number; meseroId: number; fecha: string; estado: string; total: number; platos: number; detalles: DetallePedido[];
}
export interface LineaNueva { platoId: number; cantidad: number; nota: string; precioUnitario: number }

export const pedidos = {
  delTurno: () => api<Pedido[]>('/api/pedidos?turno=actual'),
  catalogo: () => api<PlatoCarta[]>('/api/pedidos/catalogo'),
  mesas: () => api<Mesa[]>('/api/pedidos/mesas'),
  registrar: (mesa: number, detalles: LineaNueva[]) => api<Pedido>('/api/pedidos', { cuerpo: { mesa, detalles } }),
};

// ── ms-menu (V06) ───────────────────────────────────────────────────────────────────────────────
export interface Ingrediente { productoId: number; producto: string; categoria: string | null; cantidad: number; unidad: string; kg: number }
export interface Plato {
  id: number; sucursalId: number; nombre: string; categoria: string; descripcion: string; precioVenta: number;
  activo: boolean; imagen: boolean; ingredientes: Ingrediente[];
}
export interface Producto { id: number; nombre: string; categoriaId: number; categoria: string }
export interface DatosPlato {
  nombre: string; categoria: string; descripcion: string; precioVenta: number;
  ingredientes: { productoId: number; cantidad: number; unidad: string }[];
}

export const platos = {
  consultar: () => api<Plato[]>('/api/platos'),
  productos: () => api<Producto[]>('/api/platos/productos'),
  crear: (d: DatosPlato) => api<Plato>('/api/platos', { cuerpo: d }),
  editar: (id: number, d: DatosPlato) => api<Plato>(`/api/platos/${id}`, { metodo: 'PUT', cuerpo: d }),
  desactivar: (id: number) => api<void>(`/api/platos/${id}/desactivar`, { metodo: 'PATCH' }),
  reactivar: (id: number) => api<void>(`/api/platos/${id}/reactivar`, { metodo: 'PATCH' }),
  subirImagen: (id: number, archivo: File) => subirArchivo(`/api/platos/${id}/imagen`, archivo),
  imagen: (id: number) => apiArchivo(`/api/platos/${id}/imagen`),
};

// ── ms-inventario (V05 y V07) ───────────────────────────────────────────────────────────────────
export interface Insumo {
  productoId: number; nombre: string; stockActual: number; stockReferencia: number; unidad: string;
  porcentaje: number; minimo: number; bajoUmbral: boolean; alertaAtendida: boolean;
}

export const inventario = {
  insumos: () => api<Insumo[]>('/api/inventario/insumos'),
  alertas: () => api<Insumo[]>('/api/inventario/alertas'),
  /** Adelanta el cierre de las 23:59 (solo en la demostración, DEC-68). */
  cierre: () => api<void>('/api/inventario/cierre', { metodo: 'POST' }),
};

// ── ms-solicitudes (V08–V12) ────────────────────────────────────────────────────────────────────
export type EstadoSolicitud = 'PENDIENTE' | 'EN_TRAMITE' | 'COMPLETADA';
export interface ItemSolicitud { productoId: number; cantidad: number; marca: string | null; unidad: string; cotizado: boolean; precioSugerido: number | null }
export interface Solicitud {
  numero: string; sucursalId: number; solicitanteId: number; correoSolicitante: string; fecha: string;
  estado: EstadoSolicitud; valorSugeridoTotal: number; items: ItemSolicitud[];
}
export interface LineaSolicitud { productoId: number; cantidad: number; marca: string; unidad: string }
/** Los criterios de HU-07 (V11); uno vacío no filtra. */
export interface Criterios { desde?: string; hasta?: string; producto?: string; marca?: string; estado?: string }

const consulta = (c: Criterios) => {
  const q = new URLSearchParams();
  Object.entries(c).forEach(([k, v]) => { if (v) q.set(k, v); });
  return q.toString();
};

export const solicitudes = {
  registrar: (items: LineaSolicitud[]) => api<Solicitud>('/api/solicitudes', { cuerpo: { items } }),
  /** V10: las del jefe que inició sesión (el servicio lo saca del token). */
  mias: (c: Criterios = {}) => api<Solicitud[]>(`/api/solicitudes?${consulta({ ...c, solicitante: 'yo' } as Criterios)}`),
  /** V11–V12: las de todas las sucursales (auxiliar de compras). */
  consultar: (c: Criterios = {}) => api<Solicitud[]>(`/api/solicitudes${consulta(c) ? '?' + consulta(c) : ''}`),
  detalle: (numero: string) => api<Solicitud>(`/api/solicitudes/${encodeURIComponent(numero)}`),
};
