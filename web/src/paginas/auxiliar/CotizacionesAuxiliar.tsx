import { useCallback, useState } from 'react';
import { useLocation, useNavigate, useSearchParams } from 'react-router-dom';
import { cotizaciones, platos, proveedores, solicitudes, type Cotizacion } from '../../api/servicios';
import { abrirPdf, CotizacionGenerada, Pares, VentanaCotizacion } from '../../componentes/Cotizaciones';
import { Encabezado } from '../../componentes/Encabezado';
import { asc, desc, enRango, ESTADOS, fecha, num, plural } from '../../componentes/formato';
import { Icono } from '../../componentes/Icono';
import { ErrorCarga, Insignia, Tabla, useAviso, usePagina, useDatos, VacioDetalle } from '../../componentes/Piezas';
import { useProductos } from '../../componentes/Solicitudes';
import { Tarjeta } from '../../componentes/Tarjeta';

const SIN_FILTRO = { proveedor: '', fecha: '', estado: '' };

/**
 * V15 · Cotizaciones enviadas del auxiliar de compras (GET /api/cotizaciones, ms-cotizaciones): filtros de
 * proveedor, fecha y estado, la lista y el detalle con el PDF. /cotizaciones/nueva abre V13 encima
 * (?solicitud= la trae marcada, desde V12); al generar, V14 muestra el tiquete. Una cotización que quedó
 * PENDIENTE_PRECIO se reenvía desde el detalle.
 */
export function CotizacionesAuxiliar() {
  const navegar = useNavigate();
  const avisar = useAviso();
  const { pathname } = useLocation();
  const [params, setParams] = useSearchParams();
  const lista = useDatos(() => cotizaciones.consultar());
  const provs = useDatos(() => proveedores.consultar());
  const sols = useDatos(() => solicitudes.consultar());
  const productos = useDatos(platos.productos);
  const { nombre } = useProductos(productos.datos);
  const [borrador, setBorrador] = useState(SIN_FILTRO);
  const [filtro, setFiltro] = useState(SIN_FILTRO);
  const [generada, setGenerada] = useState<{ c: Cotizacion; correlacion: string | null } | null>(null);
  const [reenviando, setReenviando] = useState(false);
  const nueva = pathname === '/cotizaciones/nueva';

  const todas = desc(lista.datos ?? []);
  const visibles = todas.filter((c) => (!filtro.proveedor || c.proveedor === filtro.proveedor) && (!filtro.estado || c.estado === filtro.estado)
    && enRango(c.fechaEnvio, filtro.fecha));
  const sel = todas.find((c) => c.numero === params.get('sel')) ?? visibles[0] ?? null;
  const { filas, pie, irA } = usePagina(visibles, 6);
  const estados = [...new Set(todas.map((c) => c.estado))];

  const cerrar = useCallback(() => navegar('/cotizaciones', { replace: true }), [navegar]);
  const alGenerar = useCallback((c: Cotizacion, correlacion: string | null) => {
    setGenerada({ c, correlacion });
    lista.recargar();
    sols.recargar();
    navegar(`/cotizaciones?sel=${c.numero}`, { replace: true });
  }, [lista, sols, navegar]);
  const reenviar = async (numero: string) => {
    setReenviando(true);
    try {
      const c = await cotizaciones.reenviar(numero);
      lista.recargar();
      avisar(c.estado === 'ENVIADA' ? `${numero} se completó con el precio del Ministerio y se envió al proveedor.` : `${numero} sigue pendiente: el Ministerio no respondió.`, c.estado !== 'ENVIADA');
    } catch (e) {
      avisar(e instanceof Error ? e.message : 'No se pudo reenviar.', true);
    } finally {
      setReenviando(false);
    }
  };

  const listos = sols.datos && productos.datos && provs.datos;
  return (
    <>
      <Encabezado titulo="Cotizaciones" sub="Consulta y revisa las cotizaciones enviadas a proveedores" icono="file"
        accion={<button type="button" className="btn primary sm" onClick={() => navegar('/cotizaciones/nueva')}><Icono nombre="plusCircle" /> Generar cotización</button>} />
      <div className="page">
        <ErrorCarga mensaje={lista.error || provs.error || sols.error || productos.error} />
        <section className="card filters">
          <label>Proveedor<select value={borrador.proveedor} onChange={(e) => setBorrador((f) => ({ ...f, proveedor: e.target.value }))}>
            <option value="">Todos</option>{(provs.datos ?? []).map((p) => <option key={p.id}>{p.razonSocial}</option>)}</select></label>
          <label>Fecha<select value={borrador.fecha} onChange={(e) => setBorrador((f) => ({ ...f, fecha: e.target.value }))}>
            <option value="">Todas</option><option value="hoy">Hoy</option><option value="7">Últimos 7 días</option></select></label>
          <label>Estado<select value={borrador.estado} onChange={(e) => setBorrador((f) => ({ ...f, estado: e.target.value }))}>
            <option value="">Todos</option>{estados.map((e) => <option key={e} value={e}>{ESTADOS[e]?.[0] ?? e}</option>)}</select></label>
          <div className="filter-btns">
            <button type="button" className="btn outline sm" onClick={() => { setFiltro(borrador); irA(1); }}><Icono nombre="search" /> BUSCAR</button>
            <button type="button" className="btn ghost sm" onClick={() => { setBorrador(SIN_FILTRO); setFiltro(SIN_FILTRO); irA(1); }}><Icono nombre="refresh" /> LIMPIAR</button>
          </div>
        </section>
        <div className="grid main-side">
          <Tarjeta titulo="Cotizaciones enviadas" icono="file" sub={`Se encontraron ${plural(visibles.length, 'cotización', 'cotizaciones')} en el periodo seleccionado`}>
            <Tabla cabeceras={['Cotización', 'Proveedor', 'Fecha envío', 'Fecha límite', 'Estado', 'Acción']}
              vacio={todas.length ? 'No hay cotizaciones con esos filtros.' : 'Aún no hay cotizaciones. Genéralas desde Solicitudes.'}>
              {filas.map((c) => (
                <tr key={c.numero} className={`clickable ${sel && c.numero === sel.numero ? 'selected' : ''}`} onClick={() => setParams({ sel: c.numero })}>
                  <td><b>{c.numero}</b></td><td>{c.proveedor}</td><td>{fecha(c.fechaEnvio)}</td><td>{fecha(c.fechaLimite)}</td>
                  <td><Insignia estado={c.estado} mayus punto /></td><td><span className="link-btn">Ver detalle <Icono nombre="chevR" /></span></td>
                </tr>
              ))}
            </Tabla>
            {pie}
          </Tarjeta>
          <Tarjeta titulo="Detalle de cotización" icono="file" sub={sel?.numero ?? ''}>
            {sel ? (
              <>
                <Pares pares={[['Cotización:', <b key="n">{sel.numero}</b>], [sel.solicitudes.length > 1 ? 'Solicitudes origen:' : 'Solicitud origen:', sel.solicitudes.join(', ')],
                  ['Proveedor:', sel.proveedor], ['Fecha envío:', fecha(sel.fechaEnvio)], ['Fecha límite:', fecha(sel.fechaLimite)],
                  ['Estado:', <Insignia key="e" estado={sel.estado} mayus punto />]]} />
                <h4 className="sub-title"><Icono nombre="box" /> Productos</h4>
                <Tabla cabeceras={['Producto', 'Cantidad', 'Unidad']}>
                  {sel.items.map((i) => <tr key={i.productoId}><td>{i.producto}</td><td>{num(i.cantidad)}</td><td>{i.unidad}</td></tr>)}
                </Tabla>
                {sel.pdf ? (
                  <>
                    <button type="button" className="btn primary w mt" onClick={() => void abrirPdf(sel.numero, false, avisar)}><Icono nombre="file" /> Ver PDF</button>
                    <button type="button" className="btn ghost w" onClick={() => void abrirPdf(sel.numero, true, avisar)}><Icono nombre="download" /> Descargar</button>
                  </>
                ) : sel.estado === 'PENDIENTE_PRECIO' && (
                  <button type="button" className="btn primary w mt" disabled={reenviando} onClick={() => void reenviar(sel.numero)}>
                    <Icono nombre="refresh" /> Reenviar (consultar el precio otra vez)</button>
                )}
              </>
            ) : <VacioDetalle texto={lista.datos ? 'Aún no hay cotizaciones enviadas.' : 'Cargando…'} />}
          </Tarjeta>
        </div>
      </div>
      {nueva && listos && (
        <VentanaCotizacion cotizables={asc((sols.datos ?? []).filter((s) => s.estado !== 'COMPLETADA'))} nombre={nombre}
          proveedores={(provs.datos ?? []).filter((p) => p.activo)} inicial={params.get('solicitud')} alCerrar={cerrar} alGenerar={alGenerar} />
      )}
      {generada && <CotizacionGenerada c={generada.c} correlacion={generada.correlacion}
        alVerPdf={() => void abrirPdf(generada.c.numero, false, avisar)} alCerrar={() => setGenerada(null)} />}
    </>
  );
}
