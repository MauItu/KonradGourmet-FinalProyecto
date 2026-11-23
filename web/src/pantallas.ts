// Título, subtítulo e ícono de cada ruta del menú (las rutas las arma ms-identidad en construirMenu(); cada una
// tiene su página en App.tsx).
export interface Pantalla {
  titulo: string;
  sub: string;
  icono: string;
}

export const PANTALLAS: Record<string, Pantalla> = {
  '/inicio': { titulo: 'Inicio', sub: 'Resumen del día', icono: 'home' },
  '/pedidos': { titulo: 'Pedidos del turno', sub: 'Pedidos registrados en el turno', icono: 'clipboard' },
  '/pedidos/nuevo': { titulo: 'Nuevo pedido', sub: 'Registra el pedido de una mesa', icono: 'plusCircle' },
  '/platos': { titulo: 'Platos', sub: 'Platos de la sucursal y sus ingredientes', icono: 'utensils' },
  '/platos/nuevo': { titulo: 'Nuevo plato', sub: 'Registra un plato con su receta', icono: 'utensils' },
  '/inventario': { titulo: 'Inventario', sub: 'Existencias y alertas de stock bajo', icono: 'box' },
  '/solicitudes': { titulo: 'Solicitudes', sub: 'Solicitudes de alimentos', icono: 'file' },
  '/solicitudes/nueva': { titulo: 'Nueva solicitud', sub: 'Pide alimentos al área de compras', icono: 'file' },
  '/promociones': { titulo: 'Promociones sugeridas', sub: 'Sugerencias del análisis semanal (HU-19)', icono: 'tag' },
  '/cotizaciones': { titulo: 'Cotizaciones', sub: 'Cotizaciones enviadas a proveedores', icono: 'file' },
  '/cotizaciones/nueva': { titulo: 'Generar cotización', sub: 'Arma y envía la solicitud de cotización', icono: 'send' },
  '/cotizaciones/todas': { titulo: 'Todas las cotizaciones', sub: 'Cotizaciones de todos los proveedores', icono: 'file' },
  '/cotizaciones/pendientes': { titulo: 'Pendientes de validar', sub: 'Registra los valores del proveedor y valida', icono: 'scale' },
  '/cotizaciones/opcionadas': { titulo: 'Cotizaciones opcionadas', sub: 'Listas para generar la orden de compra', icono: 'star' },
  '/proveedores': { titulo: 'Proveedores', sub: 'Registro y mantenimiento de proveedores (HU-15)', icono: 'store' },
  '/ordenes-compra': { titulo: 'Órdenes de compra', sub: 'Órdenes generadas y su seguimiento', icono: 'receipt' },
  '/bam': { titulo: 'Tablero BAM', sub: 'Indicadores del negocio (HU-18)', icono: 'chart' },
  '/admin': { titulo: 'Administración', sub: 'Parámetros, imagen corporativa, auditoría y logs', icono: 'settings' },
  '/admin/parametros': { titulo: 'Parámetros y tema', sub: 'Imagen corporativa y parámetros generales (HU-20)', icono: 'settings' },
  '/admin/auditoria': { titulo: 'Auditoría', sub: 'Quién hizo cada acción y cuándo (HU-21)', icono: 'clipboard' },
  '/admin/logs': { titulo: 'Logs de errores', sub: 'Errores registrados por los servicios (HU-22)', icono: 'alert' },
  '/usuarios': { titulo: 'Usuarios', sub: 'Alta de usuarios con su rol y sucursal', icono: 'users' },
  '/destinatarios': { titulo: 'Destinatarios de alertas', sub: 'Correos de los jefes de cocina por sucursal', icono: 'bell' },
  '/perfil': { titulo: 'Mi perfil', sub: 'Tus datos en el sistema', icono: 'user' },
};

export const pantalla = (ruta: string): Pantalla =>
  PANTALLAS[ruta] ?? { titulo: 'Konrad Gourmet', sub: '', icono: 'home' };
