import { useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { cotizaciones, type Cotizacion, type OrdenCompra } from '../../api/servicios';
import { Pares } from '../../componentes/Cotizaciones';
import { Encabezado } from '../../componentes/Encabezado';
import { fecha, money, num, pct } from '../../componentes/formato';
import { Icono } from '../../componentes/Icono';
import { OrdenGenerada, VentanaOrden } from '../../componentes/Ordenes';
import { ErrorCarga, Insignia, Tabla, useAviso, useDatos, VacioDetalle } from '../../componentes/Piezas';
import { Tarjeta } from '../../componentes/Tarjeta';

const ORDEN: Record<string, { texto: string; comparar: (a: Cotizacion, b: Cotizacion) => number }> = {
  diferencia: { texto: 'Menor diferencia', comparar: (a, b) => (a.diferencia ?? 0) - (b.diferencia ?? 0) },
  total: { texto: 'Menor valor total', comparar: (a, b) => (a.cotizado ?? 0) - (b.cotizado ?? 0) },
  numero: { texto: 'Número de cotización', comparar: (a, b) => a.numero.localeCompare(b.numero) },
};

/**
 * V20 · Cotizaciones opcionadas (HU-17): GET /api/cotizaciones?estado=OPCIONADA (ms-cotizaciones). Las de una
 * misma solicitud lado a lado, ordenadas por diferencia o por valor total, con la comparación económica de la
 * escogida y «Seleccionar para compra», que abre V21; al confirmar, V22. Una cotización con orden sale de la
 * lista cuando ms-cotizaciones recibe orden.generada (CON_ORDEN).
 */
export function Opcionadas() {
  const navegar = useNavigate();
  const avisar = useAviso();
  const [params, setParams] = useSearchParams();
  const lista = useDatos(cotizaciones.opcionadas);
  const [solicitud, setSolicitud] = useState('');
  const [orden, setOrden] = useState('diferencia');
  const [comprando, setComprando] = useState<Cotizacion | null>(null);
  const [generada, setGenerada] = useState<OrdenCompra | null>(null);
  // la que ya tiene orden se oculta enseguida; la lista de ms-cotizaciones la quita al recibir orden.generada
  const [conOrden, setConOrden] = useState<string[]>([]);

  const todas = (lista.datos ?? []).filter((c) => !conOrden.includes(c.numero));
  const solicitudes = [...new Set(todas.flatMap((c) => c.solicitudes))].sort();
  const pedida = params.get('sel') ? todas.find((c) => c.numero === params.get('sel')) : undefined;
  const solSel = solicitudes.includes(solicitud) ? solicitud : pedida?.solicitudes[0] ?? solicitudes[0] ?? '';
  const filas = todas.filter((c) => c.solicitudes.includes(solSel)).sort(ORDEN[orden].comparar);
  const sel = filas.find((c) => c.numero === params.get('sel')) ?? filas[0] ?? null;

  const alGenerar = (o: OrdenCompra) => {
    setComprando(null);
    setGenerada(o);
    setConOrden((x) => [...x, o.cotizacion]);
    setParams({});
    window.setTimeout(lista.recargar, 1500);
  };

  return (
    <>
      <Encabezado titulo="Cotizaciones opcionadas" icono="file" sub="Compara las cotizaciones que cumplen con los criterios y selecciona la mejor opción." />
      <div className="page">
        <ErrorCarga mensaje={lista.error} />
        <div className="grid main-side wide-side">
          <div className="stack">
            <section className="card filters compact">
              <label>Solicitud<select value={solSel} onChange={(e) => { setSolicitud(e.target.value); setParams({}); }} aria-label="Solicitud">
                {solicitudes.map((s) => <option key={s}>{s}</option>)}{!solicitudes.length && <option value="">—</option>}</select></label>
              <label className="orden-por">Ordenar por<select value={orden} onChange={(e) => setOrden(e.target.value)} aria-label="Ordenar por">
                {Object.entries(ORDEN).map(([k, v]) => <option key={k} value={k}>{v.texto}</option>)}</select></label>
            </section>
            <Tarjeta titulo="Comparación de cotizaciones" icono="scale" clase="codigos">
              <Tabla cabeceras={['Nº', 'Proveedor', 'Fecha', 'Valor estándar', 'Cotización', 'Diferencia', 'Estado', 'Ver']}
                vacio="No hay cotizaciones opcionadas disponibles">
                {filas.map((c) => (
                  <tr key={c.numero} className={`clickable ${sel && c.numero === sel.numero ? 'selected' : ''}`} onClick={() => setParams({ sel: c.numero })}>
                    <td><b>{c.numero}</b></td><td><b>{c.proveedor}</b></td><td>{fecha(c.fechaEnvio)}</td><td>{money(c.estandar)}</td><td>{money(c.cotizado)}</td>
                    <td><span className={`diff ${(c.diferencia ?? 0) > 0 ? 'up' : 'down'}`}>{pct(c.diferencia)}</span></td>
                    <td><Insignia estado={c.estado} mayus punto /></td>
                    <td><span className="pill-btn naranja">Ver <Icono nombre="chevR" /></span></td>
                  </tr>
                ))}
              </Tabla>
            </Tarjeta>
          </div>
          <Tarjeta titulo="Detalle de cotización opcionada" icono="file">
            {sel ? (
              <>
                <Pares pares={[['Cotización', <b key="n">{sel.numero}</b>], ['Proveedor', <b key="p">{sel.proveedor}</b>],
                  [sel.solicitudes.length > 1 ? 'Solicitudes' : 'Solicitud', sel.solicitudes.join(', ')], ['Estado', <Insignia key="e" estado={sel.estado} mayus punto />]]} />
                <h4 className="sub-title"><Icono nombre="chart" /> Comparación económica</h4>
                <div className="econ">
                  <div><span>Ministerio</span><b>{money(sel.estandar)}</b></div>
                  <div><span>Proveedor</span><b>{money(sel.cotizado)}</b></div>
                  <div><span>Diferencia</span><b className={(sel.diferencia ?? 0) > 0 ? 't-rojo' : 't-verde'}>{pct(sel.diferencia)}</b></div>
                </div>
                <h4 className="sub-title"><Icono nombre="box" /> Productos</h4>
                <Tabla cabeceras={['Producto', 'Cantidad', 'Valor']}>
                  {sel.items.map((i) => <tr key={i.productoId}><td>{i.producto}</td><td>{num(i.cantidad)} {i.unidad}</td>
                    <td>{money(i.precioProveedor != null ? i.precioProveedor * i.cantidad : null)}</td></tr>)}
                </Tabla>
                <div className="ticket-acciones">
                  <button type="button" className="btn primary w" onClick={() => setComprando(sel)}><Icono nombre="cart" /> Seleccionar para compra</button>
                  <button type="button" className="btn outline w" onClick={() => navegar(`/cotizaciones/todas?sel=${sel.numero}`)}><Icono nombre="file" /> Ver cotización</button>
                </div>
              </>
            ) : <VacioDetalle texto={lista.datos ? 'No hay cotizaciones opcionadas disponibles' : 'Cargando…'} />}
          </Tarjeta>
        </div>
      </div>
      {comprando && <VentanaOrden c={comprando} alCerrar={() => setComprando(null)} alGenerar={alGenerar} />}
      {generada && (
        <OrdenGenerada o={generada} alCerrar={() => { setGenerada(null); avisar(`${generada.numero} quedó en Órdenes de compra.`); }}
          alVer={() => navegar(`/ordenes-compra?sel=${generada.numero}`)} alInicio={() => navegar('/inicio')} />
      )}
    </>
  );
}
