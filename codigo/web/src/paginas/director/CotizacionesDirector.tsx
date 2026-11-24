import { useCallback, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { cotizaciones, proveedores, type Cotizacion } from '../../api/servicios';
import { abrirPdf, sinPrecios, TicketCotizacion, Validacion, VentanaValores } from '../../componentes/Cotizaciones';
import { Encabezado } from '../../componentes/Encabezado';
import { asc, enRango, ESTADOS, fecha } from '../../componentes/formato';
import { Icono } from '../../componentes/Icono';
import { ErrorCarga, Insignia, Tabla, useAviso, usePagina, useDatos } from '../../componentes/Piezas';
import { Tarjeta } from '../../componentes/Tarjeta';

/** El botón de una cotización enviada: «Registrar valores» si faltan precios, «Validar» si ya están (accionPendiente() de app.js). */
export function AccionPendiente({ c, alHacer }: { c: Cotizacion; alHacer: (numero: string) => void }) {
  return sinPrecios(c)
    ? <button type="button" className="pill-btn naranja" onClick={(e) => { e.stopPropagation(); alHacer(c.numero); }}>Registrar valores <Icono nombre="chevR" /></button>
    : <button type="button" className="pill-btn azul" onClick={(e) => { e.stopPropagation(); alHacer(c.numero); }}>Validar <Icono nombre="chevR" /></button>;
}

/**
 * V17 · Todas las cotizaciones del director de compras (GET /api/cotizaciones) y su submenú «Pendientes de
 * validar» (GET /api/cotizaciones/pendientes): indicadores, filtros, la lista y el tiquete de la escogida.
 * ?valores=COT-… abre V18 encima; al validar, V19 muestra el estado que dio la cadena de reglas.
 */
export function CotizacionesDirector({ soloPendientes }: { soloPendientes: boolean }) {
  const navegar = useNavigate();
  const avisar = useAviso();
  const [params, setParams] = useSearchParams();
  const todas = useDatos(() => cotizaciones.consultar());
  const pendientes = useDatos(() => (soloPendientes ? cotizaciones.pendientes() : Promise.resolve([] as Cotizacion[])), [soloPendientes]);
  const provs = useDatos(() => proveedores.consultar());
  const [filtro, setFiltro] = useState({ proveedor: '', estado: '', fecha: '' });
  const [validada, setValidada] = useState<Cotizacion | null>(null);

  const base = (soloPendientes ? pendientes.datos : todas.datos) ?? [];
  const lista = asc(base).filter((c) => (!filtro.proveedor || c.proveedor === filtro.proveedor) && (soloPendientes || !filtro.estado || c.estado === filtro.estado)
    && enRango(c.fechaEnvio, filtro.fecha));
  const sel = lista.find((c) => c.numero === params.get('sel')) ?? base.find((c) => c.numero === params.get('sel')) ?? lista[0] ?? null;
  const { filas, pie, irA } = usePagina(lista, 6);
  const todasLista = todas.datos ?? [];
  const conteo = (e: string) => (todas.datos ? todasLista.filter((c) => c.estado === e).length : '—');
  const estados = [...new Set(todasLista.map((c) => c.estado))];
  const objetivo = (sel && sel.estado === 'ENVIADA' ? sel : null) ?? asc(todasLista).find((c) => c.estado === 'ENVIADA') ?? null;
  const enValores = todasLista.find((c) => c.numero === params.get('valores')) ?? null;

  const campo = (k: 'proveedor' | 'estado' | 'fecha') => (e: { target: { value: string } }) => { setFiltro((f) => ({ ...f, [k]: e.target.value })); irA(1); };
  const abrirValores = useCallback((numero: string) => setParams({ sel: numero, valores: numero }), [setParams]);
  const cerrarValores = useCallback(() => setParams(sel ? { sel: sel.numero } : {}), [setParams, sel]);
  const recargar = () => { todas.recargar(); pendientes.recargar(); };

  return (
    <>
      <Encabezado titulo={soloPendientes ? 'Pendientes de validar' : 'Todas las cotizaciones'} icono="file"
        sub={soloPendientes ? 'Cotizaciones enviadas que esperan el registro de valores o la validación.' : 'Consulta y gestiona el historial de cotizaciones.'} />
      <div className="page">
        <ErrorCarga mensaje={todas.error || pendientes.error || provs.error} />
        <div className="grid main-side wide-side">
          <div className="stack">
            <div className="kpis three soft">
              <div className="kpi peach"><span className="kpi-ico"><Icono nombre="send" /></span><div><span>Enviadas</span><strong>{todas.datos ? todasLista.length : '—'}</strong></div></div>
              <div className="kpi blue"><span className="kpi-ico"><Icono nombre="file" /></span><div><span>Opcionadas</span><strong>{conteo('OPCIONADA')}</strong></div></div>
              <div className="kpi rose"><span className="kpi-ico"><Icono nombre="circleX" /></span><div><span>Rechazadas</span><strong>{conteo('RECHAZADA')}</strong></div></div>
            </div>
            <section className="card filters compact">
              <label>Proveedor<select value={filtro.proveedor} onChange={campo('proveedor')}>
                <option value="">Todos los proveedores</option>{(provs.datos ?? []).map((p) => <option key={p.id}>{p.razonSocial}</option>)}</select></label>
              {!soloPendientes && (
                <label>Estado<select value={filtro.estado} onChange={campo('estado')}>
                  <option value="">Todos los estados</option>{estados.map((e) => <option key={e} value={e}>{ESTADOS[e]?.[0] ?? e}</option>)}</select></label>
              )}
              <label>Fecha<select value={filtro.fecha} onChange={campo('fecha')}>
                <option value="">Todas las fechas</option><option value="hoy">Hoy</option><option value="7">Últimos 7 días</option></select></label>
              <button type="button" className="btn primary sm push" disabled={!objetivo} onClick={() => objetivo && abrirValores(objetivo.numero)}>
                <Icono nombre="plus" /> Registrar valor de cotización</button>
            </section>
            <Tarjeta titulo={soloPendientes ? 'Pendientes de validar' : 'Todas las cotizaciones'} icono="file">
              <Tabla cabeceras={['Nº', 'Proveedor', 'Solicitud', 'Fecha', 'Estado', soloPendientes ? 'Acción' : 'Ver']}
                vacio={soloPendientes ? 'No hay cotizaciones pendientes de registrar o validar.' : 'Aún no hay cotizaciones. El auxiliar de compras las genera desde Solicitudes.'}>
                {filas.map((c) => (
                  <tr key={c.numero} className={`clickable ${sel && c.numero === sel.numero ? 'selected' : ''}`} onClick={() => setParams({ sel: c.numero })}>
                    <td><b>{c.numero}</b></td><td>{c.proveedor}</td><td>{c.solicitudes.join(', ')}</td><td>{fecha(c.fechaEnvio)}</td>
                    <td><Insignia estado={c.estado} mayus punto /></td>
                    <td>{soloPendientes ? <AccionPendiente c={c} alHacer={abrirValores} /> : <span className="pill-btn naranja">Ver <Icono nombre="chevR" /></span>}</td>
                  </tr>
                ))}
              </Tabla>
              {pie}
            </Tarjeta>
          </div>
          <TicketCotizacion c={sel} acciones={sel && (
            <div className="ticket-acciones">
              {sel.pdf && <button type="button" className="btn ghost sm w" onClick={() => void abrirPdf(sel.numero, false, avisar)}><Icono nombre="file" /> Ver PDF</button>}
              {sel.estado === 'ENVIADA' && <button type="button" className="btn primary sm w" onClick={() => abrirValores(sel.numero)}>
                <Icono nombre="scale" /> {sinPrecios(sel) ? 'Registrar valores' : 'Validar'}</button>}
            </div>
          )} />
        </div>
      </div>
      {enValores && enValores.estado === 'ENVIADA' && (
        <VentanaValores c={enValores} alCerrar={cerrarValores}
          alGuardar={(c) => { recargar(); setParams({ sel: c.numero }); avisar(`Valores de ${c.numero} guardados: la cotización queda pendiente de validar.`); }}
          alValidar={(c) => { recargar(); setParams({ sel: c.numero }); setValidada(c); }} />
      )}
      {validada && (
        <Validacion c={validada} alCerrar={() => setValidada(null)}
          alVer={() => { setValidada(null); navegar(`/cotizaciones/todas?sel=${validada.numero}`); }}
          alOpcionadas={() => { setValidada(null); navegar('/cotizaciones/opcionadas'); }} />
      )}
    </>
  );
}
