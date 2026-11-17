// Cliente de la API: todas las llamadas pasan por Nginx → API Gateway con el JWT en la cabecera.
import { leerToken, cerrarSesionLocal } from '../sesion/almacen';

export class ErrorApi extends Error {
  constructor(public estado: number, mensaje: string) {
    super(mensaje);
  }
}

/** Avisa a la aplicación que el token venció o no es válido (401), para volver al login. */
export const SESION_VENCIDA = 'kg:sesion-vencida';

export async function api<T>(ruta: string, opciones: { metodo?: string; cuerpo?: unknown; publica?: boolean } = {}): Promise<T> {
  const cabeceras: Record<string, string> = { Accept: 'application/json' };
  if (opciones.cuerpo !== undefined) cabeceras['Content-Type'] = 'application/json';
  const token = leerToken();
  if (token && !opciones.publica) cabeceras.Authorization = `Bearer ${token}`;

  let r: Response;
  try {
    r = await fetch(ruta, {
      method: opciones.metodo ?? (opciones.cuerpo !== undefined ? 'POST' : 'GET'),
      headers: cabeceras,
      body: opciones.cuerpo !== undefined ? JSON.stringify(opciones.cuerpo) : undefined,
      cache: 'no-store',
    });
  } catch {
    throw new ErrorApi(0, 'No hay conexión con el servidor. Revisa que la plataforma esté arriba.');
  }

  const texto = await r.text();
  let datos: unknown = null;
  try { datos = texto ? JSON.parse(texto) : null; } catch { datos = null; }

  if (!r.ok) {
    if (r.status === 401 && !opciones.publica) {
      cerrarSesionLocal();
      window.dispatchEvent(new Event(SESION_VENCIDA));
    }
    const mensaje = (datos as { mensaje?: string } | null)?.mensaje
      ?? (r.status === 403 ? 'Tu rol no tiene permiso para esta acción.'
        : r.status === 503 ? 'El servicio todavía no está disponible.'
        : r.status === 401 ? 'Tu sesión venció. Vuelve a iniciar sesión.'
        : 'No se pudo completar la operación.');
    throw new ErrorApi(r.status, mensaje);
  }
  return datos as T;
}
