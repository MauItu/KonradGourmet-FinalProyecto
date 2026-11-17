// Las rutas de la tabla 5 que usan los cortes 3 a 5, a través del gateway. El usuario y la sucursal los pone
// cada servicio desde el JWT; la SPA no los manda.
import { api, apiArchivo, apiConCorrelacion, subirArchivo } from './cliente';

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

// ── ms-cotizaciones: proveedores (HU-15) ────────────────────────────────────────────────────────
export interface Proveedor {
  id: number; razonSocial: string; nit: string; correo: string; telefono: string | null; direccion: string | null;
  categorias: string[]; activo: boolean;
}
export type DatosProveedor = Omit<Proveedor, 'id' | 'activo'>;

export const proveedores = {
  /** activos = true: el desplegable de V13 (los inactivos no reciben cotizaciones). */
  consultar: (activos = false) => api<Proveedor[]>(`/api/proveedores${activos ? '?activos=true' : ''}`),
  registrar: (d: DatosProveedor) => api<Proveedor>('/api/proveedores', { cuerpo: d }),
  editar: (id: number, d: DatosProveedor) => api<Proveedor>(`/api/proveedores/${id}`, { metodo: 'PUT', cuerpo: d }),
  inactivar: (id: number) => api<void>(`/api/proveedores/${id}/inactivar`, { metodo: 'PATCH' }),
};

// ── ms-cotizaciones: cotizaciones (V13–V19) ─────────────────────────────────────────────────────
export type EstadoCotizacion = 'PENDIENTE_PRECIO' | 'ENVIADA' | 'RECHAZADA' | 'SOSPECHOSA' | 'OPCIONADA' | 'CON_ORDEN';
export interface ItemCotizacion {
  productoId: number; producto: string; cantidad: number; unidad: string; precioEstandar: number | null; precioProveedor: number | null;
}
/** estandar, cotizado y diferencia (en %) los calcula el servicio; van en null mientras falten precios. */
export interface Cotizacion {
  numero: string; solicitudes: string[]; proveedorId: number; proveedor: string; correoProveedor: string;
  fechaEnvio: string; fechaLimite: string; estado: EstadoCotizacion; pdf: boolean; items: ItemCotizacion[];
  estandar: number | null; cotizado: number | null; diferencia: number | null;
}
export interface NuevaCotizacion {
  solicitudes: string[]; proveedorId: number; fechaLimite: string;
  items: { productoId: number; producto: string; cantidad: number; unidad: string }[];
}

const ruta = (numero: string) => `/api/cotizaciones/${encodeURIComponent(numero)}`;

export const cotizaciones = {
  /** V13: genera y envía (o la deja PENDIENTE_PRECIO si el Ministerio no respondió); con la correlación, para V14. */
  generar: (d: NuevaCotizacion) => apiConCorrelacion<Cotizacion>('/api/cotizaciones', { cuerpo: d }),
  consultar: (estado?: EstadoCotizacion) => api<Cotizacion[]>(`/api/cotizaciones${estado ? `?estado=${estado}` : ''}`),
  /** V20: solo las OPCIONADA (las CON_ORDEN ya salieron de la lista). */
  opcionadas: () => api<Cotizacion[]>('/api/cotizaciones?estado=OPCIONADA'),
  /** V16: las enviadas que esperan los valores del proveedor o la validación (director). */
  pendientes: () => api<Cotizacion[]>('/api/cotizaciones/pendientes'),
  detalle: (numero: string) => api<Cotizacion>(ruta(numero)),
  /** V18: el precio por unidad del proveedor de cada producto. */
  registrarValores: (numero: string, precios: Record<number, number>) => api<Cotizacion>(`${ruta(numero)}/valores`, { metodo: 'PUT', cuerpo: { precios } }),
  /** V19: la cadena de reglas decide OPCIONADA, RECHAZADA o SOSPECHOSA. */
  validar: (numero: string) => api<Cotizacion>(`${ruta(numero)}/validacion`, { metodo: 'POST' }),
  reenviar: (numero: string) => api<Cotizacion>(`${ruta(numero)}/reenvio`, { metodo: 'POST' }),
  /** El PDF del almacén de objetos, pedido con el token. */
  pdf: (numero: string, descargar = false) => apiArchivo(`${ruta(numero)}/pdf${descargar ? '?descargar=true' : ''}`),
};

// ── ms-ordenes (V21–V23) ────────────────────────────────────────────────────────────────────────
export type EstadoOrden = 'EN_PROCESO' | 'FINALIZADA';
/** precio es por unidad; valor = cantidad × precio. */
export interface ItemOrden { productoId: number; producto: string; cantidad: number; unidad: string; precio: number; valor: number }
export interface OrdenCompra {
  numero: string; fecha: string; cotizacion: string; solicitudes: string[]; proveedor: string; correoProveedor: string;
  estado: EstadoOrden; pdf: boolean; items: ItemOrden[]; total: number;
}

const rutaOrden = (numero: string) => `/api/ordenes-compra/${encodeURIComponent(numero)}`;

export const ordenes = {
  /** V21: la orden de la cotización OPCIONADA escogida en V20 (OC-0012 en adelante). */
  generar: (cotizacionNumero: string) => api<OrdenCompra>('/api/ordenes-compra', { cuerpo: { cotizacionNumero } }),
  consultar: (estado?: EstadoOrden) => api<OrdenCompra[]>(`/api/ordenes-compra${estado ? `?estado=${estado}` : ''}`),
  /** «Ver PDF» de V23 (DEC-75), pedido con el token. */
  pdf: (numero: string, descargar = false) => apiArchivo(`${rutaOrden(numero)}/pdf${descargar ? '?descargar=true' : ''}`),
};

// ── ms-notificaciones ───────────────────────────────────────────────────────────────────────────
export interface Envio { destino: string; tipo: string; fechaHora: string; exito: boolean; error: string | null }

export const notificaciones = {
  /** Los envíos de un recorrido (DEC-76): V14 sabe así si el correo al proveedor ya salió. */
  envios: (correlacion: string) => api<Envio[]>(`/api/notificaciones/envios?correlacion=${encodeURIComponent(correlacion)}`),
};
