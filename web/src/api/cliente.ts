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
  const r = await pedir(ruta, {
    metodo: opciones.metodo ?? (opciones.cuerpo !== undefined ? 'POST' : 'GET'),
    tipo: opciones.cuerpo !== undefined ? 'application/json' : undefined,
    cuerpo: opciones.cuerpo !== undefined ? JSON.stringify(opciones.cuerpo) : undefined,
    publica: opciones.publica,
  });
  const texto = await r.text();
  let datos: unknown = null;
  try { datos = texto ? JSON.parse(texto) : null; } catch { datos = null; }

  if (!r.ok) {
    if (r.status === 401 && !opciones.publica) avisarVencida();
    const mensaje = (datos as { mensaje?: string } | null)?.mensaje
      ?? (r.status === 403 ? 'Tu rol no tiene permiso para esta acción.'
        : r.status === 503 ? 'El servicio todavía no está disponible.'
        : r.status === 401 ? 'Tu sesión venció. Vuelve a iniciar sesión.'
        : 'No se pudo completar la operación.');
    throw new ErrorApi(r.status, mensaje);
  }
  return datos as T;
}

/** Un archivo binario (la imagen de un plato o el PDF de una cotización); null si el servicio responde 404. */
export async function apiArchivo(ruta: string): Promise<Blob | null> {
  const r = await pedir(ruta, { metodo: 'GET' });
  if (r.status === 404) return null;
  if (!r.ok) {
    if (r.status === 401) avisarVencida();
    throw new ErrorApi(r.status, 'No se pudo leer el archivo.');
  }
  return r.blob();
}

/** Sube un archivo tal cual, con su tipo (PUT /api/platos/{id}/imagen). */
export async function subirArchivo(ruta: string, archivo: Blob): Promise<void> {
  const r = await pedir(ruta, { metodo: 'PUT', tipo: archivo.type, cuerpo: archivo });
  if (!r.ok) {
    if (r.status === 401) avisarVencida();
    let mensaje = 'No se pudo subir el archivo.';
    try { mensaje = (JSON.parse(await r.text()) as { mensaje?: string }).mensaje ?? mensaje; } catch { /* sin cuerpo */ }
    throw new ErrorApi(r.status, mensaje);
  }
}

async function pedir(ruta: string, o: { metodo: string; tipo?: string; cuerpo?: BodyInit; publica?: boolean }): Promise<Response> {
  const cabeceras: Record<string, string> = { Accept: 'application/json, image/*, application/pdf' };
  if (o.tipo) cabeceras['Content-Type'] = o.tipo;
  const token = leerToken();
  if (token && !o.publica) cabeceras.Authorization = `Bearer ${token}`;
  try {
    return await fetch(ruta, { method: o.metodo, headers: cabeceras, body: o.cuerpo, cache: 'no-store' });
  } catch {
    throw new ErrorApi(0, 'No hay conexión con el servidor. Revisa que la plataforma esté arriba.');
  }
}

function avisarVencida() {
  cerrarSesionLocal();
  window.dispatchEvent(new Event(SESION_VENCIDA));
}
