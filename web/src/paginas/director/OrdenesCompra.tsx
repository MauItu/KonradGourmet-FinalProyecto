import { useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { ordenes, proveedores } from '../../api/servicios';
import { Encabezado } from '../../componentes/Encabezado';
import { desc, enRango, ESTADOS, fecha, money } from '../../componentes/formato';
import { Icono } from '../../componentes/Icono';
import { abrirPdfOrden, DetalleOrden } from '../../componentes/Ordenes';
import { ErrorCarga, Insignia, Tabla, useAviso, useDatos, usePagina } from '../../componentes/Piezas';
import { Tarjeta } from '../../componentes/Tarjeta';

/**
 * V23 · Órdenes de compra + detalle (HU-14): GET /api/ordenes-compra (ms-ordenes), con los filtros de fecha,
 * proveedor y estado, la lista y el detalle de la escogida (?sel=OC-…), y su PDF del almacén de objetos
 * («Ver PDF» y «Descargar», DEC-75).
 */
export function OrdenesCompra() {
  const avisar = useAviso();
  const [params, setParams] = useSearchParams();
  const lista = useDatos(() => ordenes.consultar());
  const provs = useDatos(() => proveedores.consultar());
  const [filtro, setFiltro] = useState({ fecha: '', proveedor: '', estado: '' });

  const todas = desc(lista.datos ?? []);
  const visibles = todas.filter((o) => (!filtro.proveedor || o.proveedor === filtro.proveedor) && (!filtro.estado || o.estado === filtro.estado)
    && enRango(o.fecha, filtro.fecha));
  const sel = todas.find((o) => o.numero === params.get('sel')) ?? visibles[0] ?? null;
  const { filas, pie, irA } = usePagina(visibles, 6);
  const campo = (k: 'fecha' | 'proveedor' | 'estado') => (e: { target: { value: string } }) => { setFiltro((f) => ({ ...f, [k]: e.target.value })); irA(1); };

  return (
    <>
      <Encabezado titulo="Órdenes de compra" icono="receipt" sub="Consulta y revisa las órdenes de compra generadas." />
      <div className="page">
        <ErrorCarga mensaje={lista.error || provs.error} />
        <div className="grid main-side wide-side">
          <div className="stack">
            <section className="card filters compact">
              <label>Fecha<select value={filtro.fecha} onChange={campo('fecha')}>
                <option value="">Todas las fechas</option><option value="hoy">Hoy</option><option value="7">Últimos 7 días</option></select></label>
              <label>Proveedor<select value={filtro.proveedor} onChange={campo('proveedor')}>
                <option value="">Todos los proveedores</option>{(provs.datos ?? []).map((p) => <option key={p.id}>{p.razonSocial}</option>)}</select></label>
              <label>Estado<select value={filtro.estado} onChange={campo('estado')}>
                <option value="">Todos los estados</option>{['EN_PROCESO', 'FINALIZADA'].map((e) => <option key={e} value={e}>{ESTADOS[e]?.[0] ?? e}</option>)}</select></label>
            </section>
            <Tarjeta titulo="Listado de órdenes de compra" icono="file" clase="codigos">
              <Tabla cabeceras={['Orden', 'Fecha', 'Proveedor', 'Cotización', 'Total', 'Estado', 'Ver']}
                vacio="Aún no hay órdenes. Se generan desde Cotizaciones → Opcionadas.">
                {filas.map((o) => (
                  <tr key={o.numero} className={`clickable ${sel && o.numero === sel.numero ? 'selected' : ''}`} onClick={() => setParams({ sel: o.numero })}>
                    <td><b>{o.numero}</b></td><td>{fecha(o.fecha)}</td><td>{o.proveedor}</td><td>{o.cotizacion}</td><td>{money(o.total)}</td>
                    <td><Insignia estado={o.estado} mayus /></td><td><span className="pill-btn naranja">Ver <Icono nombre="chevR" /></span></td>
                  </tr>
                ))}
              </Tabla>
              {pie}
            </Tarjeta>
          </div>
          <Tarjeta titulo="Detalle de orden de compra" icono="receipt">
            <DetalleOrden o={sel} alVerPdf={() => sel && void abrirPdfOrden(sel.numero, false, avisar)}
              alDescargar={() => sel && void abrirPdfOrden(sel.numero, true, avisar)} />
          </Tarjeta>
        </div>
      </div>
    </>
  );
}
