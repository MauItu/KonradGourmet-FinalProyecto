import { useEffect, useMemo, useState, type ReactNode } from 'react';
import { cotizaciones, notificaciones, type Cotizacion, type Proveedor, type Solicitud } from '../api/servicios';
import { ESTADOS, fecha, hoyISO, money, num, pct } from './formato';
import { Icono } from './Icono';
import { Cerrar, Insignia, Modal, Tabla, VacioDetalle } from './Piezas';

// Piezas de las cotizaciones (ms-cotizaciones), con el marcado de app.js para que valgan los estilos de Codex.

/** ENVIADA sin todos los precios del proveedor: falta registrar valores (V16 «por registrar»). */
export const sinPrecios = (c: Cotizacion) => c.items.some((i) => i.precioProveedor == null);
export const porRegistrar = (l: Cotizacion[]) => l.filter((c) => c.estado === 'ENVIADA' && sinPrecios(c));
export const porValidar = (l: Cotizacion[]) => l.filter((c) => c.estado === 'ENVIADA' && !sinPrecios(c));

/** Abre el PDF de una cotización (o lo descarga) pidiéndolo con el token: un enlace normal no manda la cabecera. */
export function abrirPdf(numero: string, descargar: boolean, avisar: (m: string, error?: boolean) => void) {
  return abrirArchivo(() => cotizaciones.pdf(numero, descargar), numero, `La cotización ${numero} no tiene PDF.`, descargar, avisar);
}

/** Abre (o descarga) un PDF que la API entrega con el token: el de una cotización o el de una orden (DEC-75). */
export async function abrirArchivo(obtener: () => Promise<Blob | null>, numero: string, sinArchivo: string, descargar: boolean,
                                   avisar: (m: string, error?: boolean) => void) {
  // la pestaña se abre ya, dentro del clic, para que el navegador no la bloquee
  const pestana = descargar ? null : window.open('', '_blank');
  try {
    const pdf = await obtener();
    if (!pdf) throw new Error(sinArchivo);
    const url = URL.createObjectURL(pdf);
    if (pestana) {
      pestana.location.href = url;
    } else {
      const a = document.createElement('a');
      a.href = url; a.download = `${numero}.pdf`;
      document.body.appendChild(a); a.click(); a.remove();
    }
    window.setTimeout(() => URL.revokeObjectURL(url), 60_000);
  } catch (e) {
    pestana?.close();
    avisar(e instanceof Error ? e.message : 'No se pudo abrir el PDF.', true);
  }
}

export function Pares({ pares, clase = 'dl' }: { pares: [string, ReactNode][]; clase?: string }) {
  return <dl className={clase}>{pares.map(([k, v]) => [<dt key={`t${k}`}>{k}</dt>, <dd key={`d${k}`}>{v}</dd>])}</dl>;
}

export function OkIcono({ tono = 'verde' }: { tono?: string }) {
  return <div className={`ok-icon ${tono}`}><span><Icono nombre="check" /></span></div>;
}

export function Marca({ centro = false }: { centro?: boolean }) {
  return (
    <div className={`ticket-brand${centro ? ' center' : ''}`}>
      <img src="/assets/logo/logo.png" alt="" />
      <div><b>Konrad <span>Gourmet</span></b><small>Sistema de Abastecimiento</small></div>
    </div>
  );
}

interface Agregado { productoId: number; producto: string; cantidad: number; unidad: string }

/**
 * V13 · Generar solicitud de cotización: una o varias solicitudes (HU-09, DEC-05), los productos sumados de
 * las escogidas (se puede quitar alguno), el proveedor (solo los activos) y la fecha máxima. El nombre de
 * cada producto viaja con la cotización: ms-cotizaciones lo guarda para el PDF y el director (DEC-74).
 */
export function VentanaCotizacion({ cotizables, nombre, proveedores, inicial, alCerrar, alGenerar }: {
  cotizables: Solicitud[]; nombre: (id: number) => string; proveedores: Proveedor[]; inicial: string | null;
  alCerrar: () => void; alGenerar: (c: Cotizacion, correlacion: string | null) => void;
}) {
  const [sols, setSols] = useState<string[]>(() => {
    if (inicial && cotizables.some((s) => s.numero === inicial)) return [inicial];
    return cotizables.length === 1 ? [cotizables[0].numero] : [];
  });
  const [excl, setExcl] = useState<number[]>([]);
  const [buscar, setBuscar] = useState('');
  const [proveedor, setProveedor] = useState<number | ''>(proveedores[0]?.id ?? '');
  const [limite, setLimite] = useState(hoyISO(7));
  const [error, setError] = useState('');
  const [enviando, setEnviando] = useState(false);

  const productos = useMemo(() => {
    const suma = new Map<number, Agregado>();
    cotizables.filter((s) => sols.includes(s.numero)).forEach((s) => s.items.forEach((i) => {
      const a = suma.get(i.productoId) ?? { productoId: i.productoId, producto: nombre(i.productoId), cantidad: 0, unidad: i.unidad };
      a.cantidad += i.cantidad;
      suma.set(i.productoId, a);
    }));
    return [...suma.values()].sort((a, b) => a.productoId - b.productoId);
  }, [cotizables, sols, nombre]);
  const incluidos = productos.filter((p) => !excl.includes(p.productoId));
  const valido = sols.length > 0 && incluidos.length > 0 && proveedor !== '' && !!limite;

  const marcar = (numero: string, si: boolean) => setSols((x) => (si ? [...x, numero] : x.filter((n) => n !== numero)));
  const enviar = async () => {
    setEnviando(true);
    try {
      const { datos, correlacion } = await cotizaciones.generar({
        solicitudes: sols, proveedorId: Number(proveedor), fechaLimite: limite,
        items: incluidos.map((p) => ({ productoId: p.productoId, producto: p.producto, cantidad: Math.round(p.cantidad * 1000) / 1000, unidad: p.unidad })),
      });
      alGenerar(datos, correlacion);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'No se pudo generar la cotización.');
      setEnviando(false);
    }
  };

  return (
    <Modal clase="modal md" alCerrar={alCerrar}>
      <header className="m-head"><span className="m-ico"><Icono nombre="file" /></span><h2>Generar solicitud de cotización</h2><Cerrar alCerrar={alCerrar} /></header>
      <section className="m-sec"><h3>Solicitudes a incluir</h3>
        <div className="check-list">
          {cotizables.map((s) => (
            <label key={s.numero} className="check-row">
              <input type="checkbox" checked={sols.includes(s.numero)} onChange={(e) => marcar(s.numero, e.target.checked)} aria-label={`Incluir ${s.numero}`} />
              <b>{s.numero}</b><span>{s.items.map((i) => nombre(i.productoId)).join(', ')}</span><span>{fecha(s.fecha)}</span><Insignia estado={s.estado} punto />
            </label>
          ))}
          {!cotizables.length && <p className="muted">No hay solicitudes por cotizar.</p>}
        </div>
        {!sols.length && <p className="form-error">Debe seleccionar al menos una solicitud</p>}
      </section>
      <section className="m-sec"><h3>Productos a cotizar</h3>
        <label className="search sm"><Icono nombre="search" /><input value={buscar} onChange={(e) => setBuscar(e.target.value)} placeholder="Buscar producto..." aria-label="Buscar producto" /></label>
        <div className="scroll-table">
          <Tabla cabeceras={['', 'Producto', 'Cantidad', 'Unidad']} vacio="Marca una solicitud para ver sus productos.">
            {productos.filter((p) => p.producto.toLowerCase().includes(buscar.trim().toLowerCase())).map((p) => (
              <tr key={p.productoId}>
                <td><input type="checkbox" checked={!excl.includes(p.productoId)} aria-label={`Cotizar ${p.producto}`}
                  onChange={(e) => setExcl((x) => (e.target.checked ? x.filter((id) => id !== p.productoId) : [...x, p.productoId]))} /></td>
                <td>{p.producto}</td><td>{num(p.cantidad)}</td><td>{p.unidad}</td>
              </tr>
            ))}
          </Tabla>
        </div>
      </section>
      <div className="frow two">
        <label className="fl">Proveedor
          <select value={proveedor} onChange={(e) => setProveedor(e.target.value ? Number(e.target.value) : '')} aria-label="Proveedor">
            {proveedores.map((p) => <option key={p.id} value={p.id}>{p.razonSocial}</option>)}
            {!proveedores.length && <option value="">No hay proveedores activos</option>}
          </select></label>
        <label className="fl">Fecha máxima para cotización
          <input type="date" value={limite} min={hoyISO()} onChange={(e) => setLimite(e.target.value)} aria-label="Fecha máxima para cotización" /></label>
      </div>
      <section className="m-sec"><h3>Resumen</h3>
        <Tabla cabeceras={['Producto', 'Cantidad', 'Unidad']} vacio="Sin productos seleccionados.">
          {incluidos.map((p) => <tr key={p.productoId}><td>{p.producto}</td><td>{num(p.cantidad)}</td><td>{p.unidad}</td></tr>)}
        </Tabla>
      </section>
      {error && <p className="form-error" role="alert">{error}</p>}
      <footer className="m-foot">
        <button type="button" className="btn ghost" onClick={alCerrar}>Cancelar</button>
        <button type="button" className="btn primary" disabled={!valido || enviando} onClick={() => void enviar()}>GENERAR Y ENVIAR COTIZACIÓN</button>
      </footer>
    </Modal>
  );
}

type EstadoCorreo = 'esperando' | 'enviado' | 'fallo' | 'sin-respuesta';

/**
 * El correo al proveedor lo envía ms-notificaciones después, al recibir cotizacion.enviada: V14 pregunta por
 * los envíos del recorrido (DEC-76) cada segundo, hasta 15 s, y se queda con el primero de ese tipo.
 */
function useCorreoDelRecorrido(correlacion: string | null, activo: boolean): EstadoCorreo {
  const [estado, setEstado] = useState<EstadoCorreo>('esperando');
  useEffect(() => {
    if (!activo || !correlacion) { setEstado('sin-respuesta'); return undefined; }
    let vivo = true;
    let vueltas = 0;
    let reloj = 0;
    const mirar = async () => {
      vueltas += 1;
      try {
        const envios = (await notificaciones.envios(correlacion)).filter((e) => e.tipo === 'cotizacion.enviada');
        if (!vivo) return;
        if (envios.some((e) => e.exito)) { setEstado('enviado'); return; }
        if (envios.length) { setEstado('fallo'); return; }
      } catch { /* se vuelve a intentar */ }
      if (!vivo) return;
      if (vueltas >= 15) setEstado('sin-respuesta'); else reloj = window.setTimeout(() => void mirar(), 1000);
    };
    reloj = window.setTimeout(() => void mirar(), 600);
    return () => { vivo = false; window.clearTimeout(reloj); };
  }, [correlacion, activo]);
  return estado;
}

/** El texto del fallo es el de PD-10 (CU-08 / HU-12, «Falla el correo»). */
const textoCorreo = (estado: EstadoCorreo, numero: string) => ({
  esperando: 'Enviando el correo certificado al proveedor…',
  enviado: 'El PDF fue generado y enviado al proveedor.',
  fallo: `No se pudo enviar el correo al proveedor. La cotización ${numero} está guardada; puede reintentar el envío.`,
  'sin-respuesta': 'El correo al proveedor lo envía ms-notificaciones al recibir cotizacion.enviada.',
})[estado];

/**
 * V14 · Cotización generada: el tiquete con el número, el PDF que quedó en el almacén de objetos y el
 * correo al proveedor, que envía ms-notificaciones al recibir cotizacion.enviada: la marca sale cuando ya
 * salió (DEC-76).
 */
export function CotizacionGenerada({ c, correlacion, alVerPdf, alCerrar }: {
  c: Cotizacion; correlacion: string | null; alVerPdf: () => void; alCerrar: () => void;
}) {
  const enviada = c.estado === 'ENVIADA';
  const correo = useCorreoDelRecorrido(correlacion, enviada);
  return (
    <Modal clase="ticket-modal" alCerrar={alCerrar}>
      <Cerrar alCerrar={alCerrar} />
      <div className="ticket-slot"></div>
      <div className="ticket">
        <OkIcono tono={enviada ? 'verde' : 'naranja'} />
        <h2>Solicitud de cotización<br />{enviada ? 'enviada' : 'guardada'}</h2>
        <div className="code-big">{c.numero}</div>
        <div className="estado-pill">Estado: <Insignia estado={c.estado} mayus punto /></div>
        <p className="muted">{enviada ? textoCorreo(correo, c.numero)
          : 'Quedó pendiente del precio estándar del Ministerio: reenvíala desde Cotizaciones enviadas.'}</p>
        <Pares clase="dl dotted" pares={[[c.solicitudes.length > 1 ? 'Solicitudes origen:' : 'Solicitud origen:', c.solicitudes.join(', ')],
          ['Proveedor:', c.proveedor], ['Fecha envío:', fecha(c.fechaEnvio)], ['Fecha límite:', fecha(c.fechaLimite)]]} />
        <h4 className="sub-title">Productos solicitados</h4>
        <ul className="ticket-items">{c.items.map((i) => <li key={i.productoId}><span>{i.producto}</span><b>{num(i.cantidad)} {i.unidad}</b></li>)}</ul>
        <div className="ticket-checks">
          <span><Icono nombre="file" /> PDF generado {c.pdf && <i className="ok"><Icono nombre="check" /></i>}</span>
          <span title={textoCorreo(correo, c.numero)} className={`correo-${correo}`}>
            <Icono nombre={correo === 'fallo' ? 'alert' : 'send'} /> {correo === 'fallo' ? 'Correo no enviado' : 'Correo enviado'}
            {correo === 'enviado' && <i className="visto"><Icono nombre="check" /></i>}
          </span>
        </div>
        {c.pdf && <button type="button" className="btn outline sm mt" onClick={alVerPdf}><Icono nombre="file" /> Ver PDF</button>}
        <p className="ticket-foot"><b>KONRAD GOURMET</b><br />{enviada ? '¡Cotización enviada con éxito!' : 'Cotización guardada sin enviar'}</p>
      </div>
    </Modal>
  );
}

/** El tiquete del lado derecho de V17 (ticketCotizacion() de app.js). */
export function TicketCotizacion({ c, acciones }: { c: Cotizacion | null; acciones?: ReactNode }) {
  if (!c) return <div className="ticket side"><VacioDetalle texto="Selecciona una cotización." /></div>;
  return (
    <div className="ticket side">
      <Marca />
      <p className="ticket-label">Detalle de cotización</p><h3 className="ticket-code">{c.numero}</h3>
      <Pares pares={[['Proveedor:', <span key="p" className="t-naranja">{c.proveedor}</span>], ['Solicitud origen:', c.solicitudes.join(', ')],
        ['Fecha:', fecha(c.fechaEnvio)], ['Estado:', <Insignia key="e" estado={c.estado} mayus punto />]]} />
      <h4 className="sub-title">Productos cotizados</h4>
      <Tabla cabeceras={['Producto', 'Cantidad']}>
        {c.items.map((i) => <tr key={i.productoId}><td>{i.producto}</td><td>{num(i.cantidad)} {i.unidad}</td></tr>)}
      </Tabla>
      {c.cotizado != null && <div className="total-row sm"><span>Valor cotizado</span><b>{money(c.cotizado)}</b></div>}
      {acciones}
      <div className="barcode"></div><small className="barcode-num">{c.numero}</small>
    </div>
  );
}

/**
 * V18 · Registrar valores de cotización: el precio por unidad del proveedor de cada producto, junto al
 * estándar del Ministerio; el valor de la línea y el total cotizado se recalculan mientras se escribe.
 * «Guardar» solo registra; «Validar cotización» registra y valida (V19).
 */
export function VentanaValores({ c, alCerrar, alGuardar, alValidar }: {
  c: Cotizacion; alCerrar: () => void; alGuardar: (c: Cotizacion) => void; alValidar: (c: Cotizacion) => void;
}) {
  const [precios, setPrecios] = useState<Record<number, string>>(() =>
    Object.fromEntries(c.items.map((i) => [i.productoId, i.precioProveedor != null ? String(i.precioProveedor) : ''])));
  const [error, setError] = useState('');
  const [ocupado, setOcupado] = useState(false);
  const valor = (id: number) => Number((precios[id] ?? '').replace(',', '.'));
  const completo = c.items.every((i) => valor(i.productoId) > 0);
  const total = c.items.reduce((t, i) => t + (valor(i.productoId) > 0 ? valor(i.productoId) * i.cantidad : 0), 0);

  const enviar = async (validar: boolean) => {
    if (!completo) { setError('Ingresa el precio del proveedor de todos los productos.'); return; }
    setOcupado(true);
    try {
      const guardada = await cotizaciones.registrarValores(c.numero, Object.fromEntries(c.items.map((i) => [i.productoId, valor(i.productoId)])));
      if (validar) alValidar(await cotizaciones.validar(c.numero)); else alGuardar(guardada);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'No se pudieron registrar los valores.');
      setOcupado(false);
    }
  };

  return (
    <Modal clase="modal md" alCerrar={alCerrar}>
      <header className="m-head"><span className="m-ico"><Icono nombre="file" /></span><h2>Registrar valores de cotización</h2><Cerrar alCerrar={alCerrar} /></header>
      <div className="info-box three">
        <div><small>Cotización</small><b>{c.numero}</b></div><div><small>Proveedor:</small><b>{c.proveedor}</b></div>
        <div><small>{c.solicitudes.length > 1 ? 'Solicitudes:' : 'Solicitud:'}</small><b>{c.solicitudes.join(', ')}</b></div>
      </div>
      <div className="table-wrap boxed">
        <table>
          <thead><tr><th>Producto</th><th>Cantidad</th><th>Estándar Ministerio</th><th>Precio proveedor</th></tr></thead>
          <tbody>
            {c.items.map((i) => (
              <tr key={i.productoId}>
                <td><b>{i.producto}</b></td><td>{num(i.cantidad)} {i.unidad}</td>
                <td>{money(i.precioEstandar != null ? i.precioEstandar * i.cantidad : null)}<small className="unit">{money(i.precioEstandar)}/{i.unidad}</small></td>
                <td>
                  <label className="money-in"><span>$</span>
                    <input type="number" min={0} step="any" value={precios[i.productoId] ?? ''} aria-label={`Precio del proveedor por ${i.unidad} de ${i.producto}`}
                      onChange={(e) => { setPrecios((p) => ({ ...p, [i.productoId]: e.target.value })); setError(''); }} />
                    <em>/{i.unidad}</em></label>
                  <small className="unit">{valor(i.productoId) > 0 ? `= ${money(valor(i.productoId) * i.cantidad)}` : ''}</small>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <div className="info-box two totals">
        <div><small>Valor estándar total</small><b>{money(c.estandar)}</b></div>
        <div><small>Valor cotizado total</small><b>{completo ? money(total) : '—'}</b></div>
      </div>
      {error && <p className="form-error" role="alert">{error}</p>}
      <footer className="m-foot center">
        <button type="button" className="btn outline wide" disabled={ocupado} onClick={() => void enviar(false)}>Guardar</button>
        <button type="button" className="btn primary wide" disabled={ocupado} onClick={() => void enviar(true)}>Validar cotización</button>
      </footer>
    </Modal>
  );
}

/** V19 · Validación completada: el estado que calculó la cadena de reglas y los otros posibles. */
export function Validacion({ c, alVer, alOpcionadas, alCerrar }: { c: Cotizacion; alVer: () => void; alOpcionadas: () => void; alCerrar: () => void }) {
  const tono = ({ OPCIONADA: 'verde', RECHAZADA: 'rojo', SOSPECHOSA: 'ambar' } as Record<string, string>)[c.estado] ?? 'gris';
  return (
    <Modal clase="ticket-modal" alCerrar={alCerrar}>
      <Cerrar alCerrar={alCerrar} />
      <div className="ticket">
        <Marca centro />
        <OkIcono />
        <h2 className="caps">Validación completada</h2>
        <div className="cmp">
          <div><span>Valor estándar Ministerio</span><b>{money(c.estandar)}</b></div>
          <div><span>Valor proveedor</span><b>{money(c.cotizado)}</b></div>
          <div className="diff-row"><span>Diferencia</span><b className={(c.diferencia ?? 0) > 0 ? 't-naranja' : 't-verde'}>{pct(c.diferencia)}</b></div>
        </div>
        <div className={`result-pill ${tono}`}><i></i>{(ESTADOS[c.estado]?.[0] ?? c.estado).toUpperCase()}</div>
        <p className="info-line"><Icono nombre="info" /> El sistema calculó automáticamente el estado de la cotización.</p>
        <p className="muted sm">Otros posibles resultados</p>
        <div className="result-chips">
          <span className={`rojo ${c.estado === 'RECHAZADA' ? 'on' : ''}`}><i></i>RECHAZADA</span>
          <span className={`ambar ${c.estado === 'SOSPECHOSA' ? 'on' : ''}`}><i></i>SOSPECHOSA</span>
          <span className={`verde ${c.estado === 'OPCIONADA' ? 'on' : ''}`}><i></i>OPCIONADA</span>
        </div>
        <div className="m-actions">
          <button type="button" className="btn outline" onClick={alVer}>Ver cotización</button>
          <button type="button" className="btn primary" onClick={alOpcionadas}>Ver cotizaciones opcionadas</button>
        </div>
      </div>
    </Modal>
  );
}
