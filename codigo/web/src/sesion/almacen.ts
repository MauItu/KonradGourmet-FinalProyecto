// Dónde vive el token: localStorage si el usuario marcó «Recordarme», sessionStorage si no.
// El almacenamiento puede no estar disponible (modo privado); en ese caso la sesión vive en memoria.
const CLAVE = 'kg-token';
let enMemoria: string | null = null;

function seguro<T>(f: () => T, porDefecto: T): T {
  try { return f(); } catch { return porDefecto; }
}

export function guardarToken(token: string, recordar: boolean) {
  enMemoria = token;
  seguro(() => {
    (recordar ? localStorage : sessionStorage).setItem(CLAVE, token);
    (recordar ? sessionStorage : localStorage).removeItem(CLAVE);
  }, undefined);
}

export function leerToken(): string | null {
  return enMemoria ?? seguro(() => sessionStorage.getItem(CLAVE) ?? localStorage.getItem(CLAVE), null);
}

export function cerrarSesionLocal() {
  enMemoria = null;
  seguro(() => { sessionStorage.removeItem(CLAVE); localStorage.removeItem(CLAVE); }, undefined);
}
