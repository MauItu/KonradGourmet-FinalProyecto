// Qué es cada ruta del menú (las rutas las arma ms-identidad en construirMenu()): título, ícono,
// ventanas de los mockups y el servicio que la atiende. Las que todavía no están construidas dicen
// en qué corte llegan (las del corte 3 ya tienen su página en App.tsx).
export interface Pantalla {
  titulo: string;
  sub: string;
  icono: string;
  ventanas: string;
  servicio: string;
  corte?: number;
}

export const PANTALLAS: Record<string, Pantalla> = {
  '/inicio': { titulo: 'Inicio', sub: 'Resumen del día', icono: 'home', ventanas: '', servicio: '' },
  '/pedidos': { titulo: 'Pedidos del turno', sub: 'Pedidos registrados en el turno', icono: 'clipboard', ventanas: 'V02', servicio: 'ms-pedidos' },
  '/pedidos/nuevo': { titulo: 'Nuevo pedido', sub: 'Registra el pedido de una mesa', icono: 'plusCircle', ventanas: 'V03 y V04', servicio: 'ms-pedidos' },
  '/platos': { titulo: 'Platos', sub: 'Platos de la sucursal y sus ingredientes', icono: 'utensils', ventanas: 'V06', servicio: 'ms-menu' },
  '/platos/nuevo': { titulo: 'Nuevo plato', sub: 'Registra un plato con su receta', icono: 'utensils', ventanas: 'V06', servicio: 'ms-menu' },
  '/inventario': { titulo: 'Inventario', sub: 'Existencias y alertas de stock bajo', icono: 'box', ventanas: 'V05 y V07', servicio: 'ms-inventario' },
  '/solicitudes': { titulo: 'Solicitudes', sub: 'Solicitudes de alimentos', icono: 'file', ventanas: 'V10 a V12', servicio: 'ms-solicitudes', corte: 4 },
  '/solicitudes/nueva': { titulo: 'Nueva solicitud', sub: 'Pide alimentos al área de compras', icono: 'file', ventanas: 'V08 y V09', servicio: 'ms-solicitudes', corte: 4 },
  '/promociones': { titulo: 'Promociones sugeridas', sub: 'Sugerencias del análisis semanal (HU-19)', icono: 'tag', ventanas: 'sin mockup', servicio: 'ms-analitica', corte: 6 },
  '/cotizaciones': { titulo: 'Cotizaciones', sub: 'Cotizaciones enviadas a proveedores', icono: 'file', ventanas: 'V15 y V17', servicio: 'ms-cotizaciones', corte: 4 },
  '/cotizaciones/nueva': { titulo: 'Generar cotización', sub: 'Arma y envía la solicitud de cotización', icono: 'send', ventanas: 'V13 y V14', servicio: 'ms-cotizaciones', corte: 4 },
  '/cotizaciones/todas': { titulo: 'Todas las cotizaciones', sub: 'Cotizaciones de todos los proveedores', icono: 'file', ventanas: 'V17', servicio: 'ms-cotizaciones', corte: 4 },
  '/cotizaciones/pendientes': { titulo: 'Pendientes de validar', sub: 'Registra los valores del proveedor y valida', icono: 'scale', ventanas: 'V18 y V19', servicio: 'ms-cotizaciones', corte: 4 },
  '/cotizaciones/opcionadas': { titulo: 'Cotizaciones opcionadas', sub: 'Listas para generar la orden de compra', icono: 'star', ventanas: 'V20', servicio: 'ms-cotizaciones', corte: 5 },
  '/proveedores': { titulo: 'Proveedores', sub: 'Registro y mantenimiento de proveedores (HU-15)', icono: 'store', ventanas: 'sin mockup', servicio: 'ms-cotizaciones', corte: 4 },
  '/ordenes-compra': { titulo: 'Órdenes de compra', sub: 'Órdenes generadas y su seguimiento', icono: 'receipt', ventanas: 'V21 a V23', servicio: 'ms-ordenes', corte: 5 },
  '/bam': { titulo: 'Tablero BAM', sub: 'Indicadores del negocio (HU-18)', icono: 'chart', ventanas: 'sin mockup', servicio: 'ms-analitica', corte: 6 },
  '/admin': { titulo: 'Administración', sub: 'Parámetros, imagen corporativa, auditoría y logs', icono: 'settings', ventanas: 'sin mockup', servicio: 'ms-administracion', corte: 6 },
  '/usuarios': { titulo: 'Usuarios', sub: 'Alta de usuarios con su rol y sucursal', icono: 'users', ventanas: 'sin mockup', servicio: 'ms-identidad', corte: 6 },
  '/destinatarios': { titulo: 'Destinatarios de alertas', sub: 'Correos de los jefes de cocina por sucursal', icono: 'bell', ventanas: 'sin mockup', servicio: 'ms-notificaciones', corte: 6 },
  '/perfil': { titulo: 'Mi perfil', sub: 'Tus datos en el sistema', icono: 'user', ventanas: '', servicio: 'ms-identidad' },
};

export const pantalla = (ruta: string): Pantalla =>
  PANTALLAS[ruta] ?? { titulo: 'Konrad Gourmet', sub: '', icono: 'home', ventanas: '', servicio: '' };
