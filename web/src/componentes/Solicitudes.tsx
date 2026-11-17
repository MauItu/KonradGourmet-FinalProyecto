import { useMemo, useState } from 'react';
import { solicitudes, type LineaSolicitud, type Producto, type Solicitud } from '../api/servicios';
import type { Usuario } from '../sesion/token';
import { fecha, num } from './formato';
import { Icono } from './Icono';
import { Cerrar, Insignia, Miniatura, Modal, Tabla } from './Piezas';

// Piezas de las solicitudes de alimentos (ms-solicitudes), con el marcado de app.js.

/** El nombre y la categoría de cada producto salen de ms-menu (GET /api/platos/productos). */
export function useProductos(productos: Producto[] | null) {
  return useMemo(() => {
    const porId = new Map((productos ?? []).map((p) => [p.id, p]));
    return {
      nombre: (id: number) => porId.get(id)?.nombre ?? `Producto ${id}`,
      categoria: (id: number) => porId.get(id)?.categoria ?? '—',
    };
  }, [productos]);
}

/** La sucursal del token si es la del usuario; las demás, por su número (ms-solicitudes solo guarda el id). */
export const nombreSucursal = (usuario: Usuario, id: number) => (id === usuario.sucursalId ? usuario.sucursal : `Sucursal ${id}`);

/** Detalle de una solicitud (V10 del jefe y V12 del auxiliar): detalleSolicitud() de app.js. */
export function DetalleSolicitud({ s, solicitante, sucursal, nombre, categoria }: {
  s: Solicitud; solicitante: string; sucursal: string; nombre: (id: number) => string; categoria: (id: number) => string;
}) {
  return (
    <>
      <div className="detail-head"><h3>Solicitud #{s.numero}</h3><Insignia estado={s.estado} mayus /></div>
      <div className="facts">
        <div><Icono nombre="calendar" /><small>Fecha:</small><b>{fecha(s.fecha)}</b></div>
        <div><Icono nombre="user" /><small>Solicitante:</small><b>{solicitante}</b></div>
        <div><Icono nombre="store" /><small>Sucursal:</small><b>{sucursal}</b></div>
        <div><Icono nombre="tag" /><small>Estado:</small><Insignia estado={s.estado} mayus /></div>
      </div>
      <div className="sub-head"><h4 className="sub-title"><Icono nombre="box" /> Productos solicitados</h4><small>Total de productos: {s.items.length}</small></div>
      <Tabla cabeceras={['Producto', 'Categoría', 'Cantidad', 'Marca', 'Unidad']}>
        {s.items.map((i, k) => (
          <tr key={k}>
            <td><span className="cell-thumb"><Miniatura nombre={nombre(i.productoId)} clase="xs" /><b>{nombre(i.productoId)}</b></span></td>
            <td>{categoria(i.productoId)}</td><td>{num(i.cantidad)}</td><td>{i.marca || '—'}</td><td>{i.unidad}</td>
          </tr>
        ))}
      </Tabla>
    </>
  );
}

/**
 * V08 · Nueva solicitud de alimentos. La sucursal y el solicitante son los del token (no editables); el
 * servicio los vuelve a tomar del JWT. La primera solicitud trae la de la demo (res 20 kg «Marca X» y
 * arroz 15 kg), como la interfaz de Codex; si se abre desde una alerta, trae preseleccionado ese producto.
 */
export function VentanaSolicitud({ usuario, productos, unidades, productoInicial, precargar, alCerrar, alRegistrar }: {
  usuario: Usuario; productos: Producto[]; unidades: Map<number, string>; productoInicial: number | null; precargar: boolean;
  alCerrar: () => void; alRegistrar: (s: Solicitud) => void;
}) {
  const base = productos.find((p) => p.id === productoInicial) ?? productos[0];
  const [cat, setCat] = useState(base?.categoria ?? '');
  const [prod, setProd] = useState<number | null>(base?.id ?? null);
  const [cant, setCant] = useState('');
  const [marca, setMarca] = useState('');
  const [filas, setFilas] = useState<LineaSolicitud[]>(() => (precargar && productos.some((p) => p.id === 1) && productos.some((p) => p.id === 2)
    ? [{ productoId: 1, cantidad: 20, marca: 'Marca X', unidad: unidades.get(1) ?? 'kg' }, { productoId: 2, cantidad: 15, marca: '', unidad: unidades.get(2) ?? 'kg' }]
    : []));
  const [error, setError] = useState('');
  const [enviando, setEnviando] = useState(false);
  const cats = [...new Set(productos.map((p) => p.categoria))];
  const deCategoria = productos.filter((p) => p.categoria === cat);
  const actual = productos.find((p) => p.id === prod) ?? deCategoria[0];
  const unidad = actual ? unidades.get(actual.id) ?? 'kg' : 'kg';
  const nombre = (id: number) => productos.find((p) => p.id === id)?.nombre ?? `Producto ${id}`;

  const cambiarCategoria = (c: string) => { setCat(c); setProd(productos.find((p) => p.categoria === c)?.id ?? null); };
  const agregar = () => {
    const cantidad = Number(cant.replace(',', '.'));
    if (!actual || !(cantidad > 0)) { setError('Indica un producto y una cantidad mayor que cero.'); return; }
    const m = marca.trim();
    setFilas((f) => {
      const previo = f.findIndex((x) => x.productoId === actual.id && x.marca === m);
      if (previo < 0) return [...f, { productoId: actual.id, cantidad, marca: m, unidad }];
      return f.map((x, k) => (k === previo ? { ...x, cantidad: x.cantidad + cantidad } : x));
    });
    setError(''); setCant(''); setMarca('');
  };
  const enviar = async () => {
    setEnviando(true);
    try {
      alRegistrar(await solicitudes.registrar(filas));
    } catch (e) {
      setError(e instanceof Error ? e.message : 'No se pudo registrar la solicitud.');
      setEnviando(false);
    }
  };

  return (
    <Modal clase="modal md" alCerrar={alCerrar}>
      <header className="m-head"><span className="m-ico"><Icono nombre="cart" /></span><h2>Nueva solicitud de alimentos</h2><Cerrar alCerrar={alCerrar} /></header>
      <section className="m-sec"><h3>Información automática</h3>
        <div className="auto">
          <div><Icono nombre="store" /><span><small>Sucursal:</small><b>Konrad Gourmet – {usuario.sucursal}</b></span></div>
          <div><Icono nombre="user" /><span><small>Solicitante:</small><b>{usuario.nombre}</b></span></div>
        </div>
      </section>
      <section className="m-sec"><h3>Producto</h3>
        <div className="frow five">
          <label>Categoría<select value={cat} onChange={(e) => cambiarCategoria(e.target.value)}>{cats.map((c) => <option key={c}>{c}</option>)}</select></label>
          <label>Producto<select value={actual?.id ?? ''} onChange={(e) => setProd(Number(e.target.value))} aria-label="Producto">
            {deCategoria.map((p) => <option key={p.id} value={p.id}>{p.nombre}</option>)}</select></label>
          <label>Cantidad<input type="number" min={0} step="any" value={cant} onChange={(e) => setCant(e.target.value)} placeholder="0" aria-label="Cantidad" /></label>
          <label>Marca (opcional)<input list="kg-marcas" value={marca} onChange={(e) => setMarca(e.target.value)} placeholder="—" maxLength={80} aria-label="Marca" />
            <datalist id="kg-marcas"><option value="Marca X" /><option value="Marca Y" /></datalist></label>
          <label>Unidad<select value={unidad} onChange={() => undefined} aria-label="Unidad"><option>{unidad}</option></select></label>
        </div>
        <button type="button" className="btn primary sm" onClick={agregar}><Icono nombre="plus" /> Agregar</button>
        {error && <p className="form-error" role="alert">{error}</p>}
      </section>
      <section className="m-sec"><h3>Productos solicitados</h3>
        <Tabla cabeceras={['Producto', 'Cantidad', 'Marca', 'Unidad', 'Acción']} vacio="Agrega al menos un producto.">
          {filas.map((f, k) => (
            <tr key={k}><td>{nombre(f.productoId)}</td><td>{num(f.cantidad)}</td><td>{f.marca || '—'}</td><td>{f.unidad}</td>
              <td><button type="button" className="icon-btn danger" aria-label={`Quitar ${nombre(f.productoId)}`} onClick={() => setFilas((x) => x.filter((_, j) => j !== k))}><Icono nombre="trash" /></button></td></tr>
          ))}
        </Tabla>
      </section>
      <footer className="m-foot">
        <button type="button" className="btn ghost" onClick={alCerrar}>Cancelar</button>
        <button type="button" className="btn primary" disabled={!filas.length || enviando} onClick={() => void enviar()}>Enviar solicitud</button>
      </footer>
    </Modal>
  );
}

/** V09 · Solicitud registrada. El correo de confirmación lo envía ms-notificaciones (corte 5). */
export function SolicitudRegistrada({ s, alVer, alInicio, alCerrar }: { s: Solicitud; alVer: () => void; alInicio: () => void; alCerrar: () => void }) {
  return (
    <Modal clase="modal sm center" alCerrar={alCerrar}>
      <Cerrar alCerrar={alCerrar} />
      <div className="burst"><div className="ok-icon naranja"><span><Icono nombre="check" /></span></div></div>
      <h2>Solicitud registrada</h2>
      <div className="code-big">{s.numero}</div>
      <div className="estado-pill">Estado: <b>{(s.estado === 'PENDIENTE' ? 'Pendiente' : s.estado).toUpperCase()}</b></div>
      <p className="muted">La confirmación se enviará al correo registrado.</p>
      <div className="m-actions">
        <button type="button" className="btn primary" onClick={alVer}><Icono nombre="file" /> VER SOLICITUD</button>
        <button type="button" className="btn outline" onClick={alInicio}><Icono nombre="home" /> VOLVER AL INICIO</button>
      </div>
    </Modal>
  );
}
