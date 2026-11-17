import { createContext, useCallback, useContext, useEffect, useRef, useState, type ReactNode } from 'react';
import { platos } from '../api/servicios';
import { Icono } from './Icono';
import { EMOJI, ESTADOS } from './formato';

// Piezas de las ventanas, con el mismo marcado de app.js para que valgan los estilos de Codex.

export function Kpi({ icono, valor, etiqueta, tono = '' }: { icono: string; valor: ReactNode; etiqueta: string; tono?: string }) {
  return (
    <div className={`kpi ${tono}`}>
      <span className="kpi-ico"><Icono nombre={icono} /></span>
      <div><strong>{valor}</strong><span>{etiqueta}</span></div>
    </div>
  );
}

export function Tabla({ cabeceras, vacio = 'Aún no hay registros.', children }: { cabeceras: string[]; vacio?: string; children: ReactNode[] }) {
  return (
    <div className="table-wrap">
      <table>
        <thead><tr>{cabeceras.map((h, k) => <th key={k}>{h}</th>)}</tr></thead>
        <tbody>{children.length ? children : <tr><td colSpan={cabeceras.length} className="empty">{vacio}</td></tr>}</tbody>
      </table>
    </div>
  );
}

/** El estado con su color; mayus y punto son las opciones up y dot de badge() en app.js. */
export function Insignia({ estado, mayus = false, punto = false }: { estado: string; mayus?: boolean; punto?: boolean }) {
  const [t, c] = ESTADOS[estado] ?? [estado, 'gris'];
  return <span className={`badge ${c}${mayus ? ' up' : ''}${punto ? ' dot' : ''}`}>{mayus ? t.toUpperCase() : t}</span>;
}

/** Paginación de las tablas, como paginar() de app.js: las filas de la página y el pie «Mostrando…». */
export function usePagina<T>(filas: T[], porPagina = 5) {
  const [pagina, setPagina] = useState(1);
  const total = filas.length;
  const paginas = Math.max(1, Math.ceil(total / porPagina));
  const p = Math.min(pagina, paginas);
  const desde = (p - 1) * porPagina;
  const pie = (
    <div className="pager"><span>Mostrando {total ? desde + 1 : 0} - {Math.min(desde + porPagina, total)} de {total}</span>
      <div>
        <button type="button" className="pg" disabled={p === 1} onClick={() => setPagina(p - 1)} aria-label="Anterior"><Icono nombre="chevL" /></button>
        {Array.from({ length: paginas }, (_, k) => (
          <button type="button" key={k} className={`pg ${k + 1 === p ? 'on' : ''}`} onClick={() => setPagina(k + 1)}>{k + 1}</button>
        ))}
        <button type="button" className="pg" disabled={p === paginas} onClick={() => setPagina(p + 1)} aria-label="Siguiente"><Icono nombre="chevR" /></button>
      </div>
    </div>
  );
  return { filas: filas.slice(desde, desde + porPagina), pie, irA: setPagina };
}

export function BotonSuave({ texto, alHacer }: { texto: string; alHacer: () => void }) {
  return <button type="button" className="btn soft sm" onClick={alHacer}>{texto} <Icono nombre="arrowR" /></button>;
}

export function VacioDetalle({ texto }: { texto: string }) {
  return <div className="empty-detail"><Icono nombre="file" /><p>{texto}</p></div>;
}

export function Medidor({ p }: { p: number }) {
  return <span className="meter"><i style={{ width: `${Math.min(100, Math.max(3, p))}%` }} className={p < 20 ? 'rojo' : 'naranja'}></i></span>;
}

// ── Imagen de un plato: la pide a ms-menu con el token (un <img> no manda la cabecera) ───────────
const cacheImagenes = new Map<number, Promise<string | null>>();

/** Olvida la imagen guardada del plato (después de subir una nueva). */
export function olvidarImagen(id: number) {
  const vieja = cacheImagenes.get(id);
  cacheImagenes.delete(id);
  void vieja?.then((u) => u && URL.revokeObjectURL(u));
}

function urlImagen(id: number): Promise<string | null> {
  let p = cacheImagenes.get(id);
  if (!p) {
    p = platos.imagen(id).then((b) => (b ? URL.createObjectURL(b) : null)).catch(() => null);
    cacheImagenes.set(id, p);
  }
  return p;
}

/**
 * La miniatura de un plato o insumo: la foto si el plato tiene (DEC-67) y si no el emoji, como en la
 * interfaz de Codex. Con platoId sin «tiene», pregunta al servicio (el mesero no sabe cuál tiene foto).
 */
export function Miniatura({ nombre, clase = '', platoId, tiene, version = 0 }: { nombre: string; clase?: string; platoId?: number; tiene?: boolean; version?: number }) {
  const [url, setUrl] = useState<string | null>(null);
  useEffect(() => {
    let vivo = true;
    setUrl(null);
    if (platoId != null && tiene !== false) void urlImagen(platoId).then((u) => { if (vivo) setUrl(u); });
    return () => { vivo = false; };
  }, [platoId, tiene, version]);
  return url
    ? <span className={`thumb ${clase}`}><img src={url} alt="" /></span>
    : <span className={`thumb ${clase}`} aria-hidden="true">{EMOJI[nombre] ?? '🍽️'}</span>;
}

// ── Ventana emergente ──────────────────────────────────────────────────────────────────────────
export function Modal({ alCerrar, clase, children }: { alCerrar: () => void; clase: string; children: ReactNode }) {
  useEffect(() => {
    document.body.classList.add('modal-open');
    const tecla = (e: KeyboardEvent) => { if (e.key === 'Escape') alCerrar(); };
    window.addEventListener('keydown', tecla);
    return () => { document.body.classList.remove('modal-open'); window.removeEventListener('keydown', tecla); };
  }, [alCerrar]);
  return (
    <div className="overlay" onMouseDown={(e) => { if (e.target === e.currentTarget) alCerrar(); }}>
      <div className={clase} role="dialog" aria-modal="true">{children}</div>
    </div>
  );
}

export function Cerrar({ alCerrar }: { alCerrar: () => void }) {
  return <button type="button" className="icon-btn m-close" onClick={alCerrar} aria-label="Cerrar"><Icono nombre="x" /></button>;
}

// ── Aviso de abajo (toast) ─────────────────────────────────────────────────────────────────────
type Avisar = (mensaje: string, error?: boolean) => void;
const ContextoAviso = createContext<Avisar>(() => undefined);

export function ProveedorAviso({ children }: { children: ReactNode }) {
  const [aviso, setAviso] = useState<{ mensaje: string; error: boolean } | null>(null);
  const reloj = useRef<number>();
  const avisar = useCallback<Avisar>((mensaje, error = false) => {
    setAviso({ mensaje, error });
    window.clearTimeout(reloj.current);
    reloj.current = window.setTimeout(() => setAviso(null), 4200);
  }, []);
  return (
    <ContextoAviso.Provider value={avisar}>
      {children}
      <div id="toast" role="status" aria-live="polite" className={aviso ? `show${aviso.error ? ' error' : ''}` : ''}>
        {aviso && <><Icono nombre={aviso.error ? 'alert' : 'circleCheck'} /><span>{aviso.mensaje}</span></>}
      </div>
    </ContextoAviso.Provider>
  );
}

export const useAviso = () => useContext(ContextoAviso);

// ── Datos de la API con su estado de carga ─────────────────────────────────────────────────────
export function useDatos<T>(cargar: () => Promise<T>, deps: unknown[] = []) {
  const [datos, setDatos] = useState<T | null>(null);
  const [error, setError] = useState('');
  const [vuelta, setVuelta] = useState(0);
  useEffect(() => {
    let vivo = true;
    cargar().then((d) => { if (vivo) { setDatos(d); setError(''); } })
      .catch((e: unknown) => { if (vivo) setError(e instanceof Error ? e.message : 'No se pudo cargar.'); });
    return () => { vivo = false; };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [vuelta, ...deps]);
  const recargar = useCallback(() => setVuelta((v) => v + 1), []);
  return { datos, error, recargar };
}

export function ErrorCarga({ mensaje }: { mensaje: string }) {
  return mensaje ? <p className="form-error" role="alert">{mensaje}</p> : null;
}
