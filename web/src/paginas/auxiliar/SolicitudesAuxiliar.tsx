import { useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { platos, solicitudes, type Criterios, type Solicitud } from '../../api/servicios';
import { Encabezado } from '../../componentes/Encabezado';
import { desc, ESTADOS, fecha } from '../../componentes/formato';
import { Icono } from '../../componentes/Icono';
import { ErrorCarga, Insignia, Tabla, usePagina, useDatos, VacioDetalle } from '../../componentes/Piezas';
import { DetalleSolicitud, nombreSucursal, useProductos } from '../../componentes/Solicitudes';
import { Tarjeta } from '../../componentes/Tarjeta';
import { useSesion } from '../../sesion/Sesion';

const VACIOS: Criterios = { desde: '', hasta: '', producto: '', marca: '', estado: '' };

/**
 * V12 · Todas las solicitudes del auxiliar de compras: los cinco filtros de HU-07 (GET /api/solicitudes
 * con desde, hasta, producto, marca y estado, que aplica ms-solicitudes en su base), la lista y el
 * detalle (GET /api/solicitudes/{id}). «Generar cotización» lleva a V13, que llega en el corte 4b.
 */
export function SolicitudesAuxiliar() {
  const { usuario } = useSesion();
  const navegar = useNavigate();
  const [params, setParams] = useSearchParams();
  const todas = useDatos(() => solicitudes.consultar());
  const productos = useDatos(platos.productos);
  const [filtro, setFiltro] = useState<Criterios>(VACIOS);
  const [resultado, setResultado] = useState<Solicitud[] | null>(null);
  const [errorFiltro, setErrorFiltro] = useState('');
  const [buscando, setBuscando] = useState(false);
  const elegido = params.get('sel');
  const detalle = useDatos(() => (elegido ? solicitudes.detalle(elegido) : Promise.resolve(null)), [elegido]);
  const { nombre, categoria } = useProductos(productos.datos);
  const lista = desc(resultado ?? todas.datos ?? []);
  const sel = (elegido && detalle.datos?.numero === elegido ? detalle.datos : null) ?? (elegido ? null : lista[0] ?? null);
  const { filas, pie, irA } = usePagina(lista, 6);
  const marcas = [...new Set((todas.datos ?? []).flatMap((s) => s.items.map((i) => i.marca)).filter((m): m is string => !!m))];
  const campo = (k: keyof Criterios) => (e: { target: { value: string } }) => setFiltro((f) => ({ ...f, [k]: e.target.value }));

  const buscar = async () => {
    setBuscando(true);
    try {
      setResultado(await solicitudes.consultar(filtro));
      setErrorFiltro('');
      irA(1);
    } catch (e) {
      setErrorFiltro(e instanceof Error ? e.message : 'No se pudo buscar.');
    } finally {
      setBuscando(false);
    }
  };
  const limpiar = () => { setFiltro(VACIOS); setResultado(null); setErrorFiltro(''); irA(1); };
  const cotizar = (numero?: string) => navegar(numero ? `/cotizaciones/nueva?solicitud=${numero}` : '/cotizaciones/nueva');

  if (!usuario) return null;
  return (
    <>
      <Encabezado titulo="Solicitudes" sub="Revisa y gestiona las solicitudes de alimentos de las sucursales" icono="file" />
      <div className="page">
        <ErrorCarga mensaje={todas.error || productos.error} />
        <section className="card filters">
          <label>Desde<input type="date" value={filtro.desde} onChange={campo('desde')} /></label>
          <label>Hasta<input type="date" value={filtro.hasta} onChange={campo('hasta')} /></label>
          <label>Producto<select value={filtro.producto} onChange={campo('producto')}>
            <option value="">Todos</option>{(productos.datos ?? []).map((p) => <option key={p.id} value={p.id}>{p.nombre}</option>)}</select></label>
          <label>Marca<input list="kg-marcas-filtro" value={filtro.marca} onChange={campo('marca')} placeholder="Todas" />
            <datalist id="kg-marcas-filtro">{marcas.map((m) => <option key={m} value={m} />)}</datalist></label>
          <label>Estado<select value={filtro.estado} onChange={campo('estado')}>
            <option value="">Todos</option>
            {(['PENDIENTE', 'EN_TRAMITE', 'COMPLETADA'] as const).map((e) => <option key={e} value={e}>{ESTADOS[e][0]}</option>)}</select></label>
          <div className="filter-btns">
            <button type="button" className="btn outline sm" disabled={buscando} onClick={() => void buscar()}><Icono nombre="search" /> Buscar</button>
            <button type="button" className="btn ghost sm" onClick={limpiar}><Icono nombre="refresh" /> Limpiar</button>
          </div>
          {errorFiltro && <p className="form-error" role="alert">{errorFiltro}</p>}
        </section>
        <div className="grid halves">
          <Tarjeta titulo="Solicitudes de alimentos" icono="file" sub="Revisa y gestiona las últimas solicitudes de alimentos"
            extra={<button type="button" className="btn soft sm" onClick={() => cotizar()}><Icono nombre="plusCircle" /> Generar cotización</button>}>
            <Tabla cabeceras={['Nº solicitud', 'Sucursal', 'Fecha', 'Productos', 'Estado', 'Acción']}
              vacio={resultado ? 'No se encontraron solicitudes con los criterios indicados' : 'Aún no hay solicitudes de alimentos.'}>
              {filas.map((s) => (
                <tr key={s.numero} className={`clickable ${sel && s.numero === sel.numero ? 'selected' : ''}`} onClick={() => setParams({ sel: s.numero })}>
                  <td><b>{s.numero}</b></td><td>{nombreSucursal(usuario, s.sucursalId)}</td><td>{fecha(s.fecha)}</td><td>{s.items.length}</td>
                  <td><Insignia estado={s.estado} punto /></td><td><span className="link-btn">Ver <Icono nombre="chevR" /></span></td>
                </tr>
              ))}
            </Tabla>
            {pie}
          </Tarjeta>
          <Tarjeta titulo="Detalle de solicitud" icono="file">
            <ErrorCarga mensaje={detalle.error} />
            {sel ? (
              <>
                <DetalleSolicitud s={sel} solicitante={sel.correoSolicitante} sucursal={nombreSucursal(usuario, sel.sucursalId)}
                  nombre={nombre} categoria={categoria} />
                {sel.estado !== 'COMPLETADA' && (
                  <button type="button" className="btn primary w mt" onClick={() => cotizar(sel.numero)}>
                    <Icono nombre="send" /> Generar cotización con {sel.numero}</button>
                )}
              </>
            ) : <VacioDetalle texto={elegido && !detalle.error ? 'Cargando la solicitud…' : 'Selecciona una solicitud.'} />}
          </Tarjeta>
        </div>
      </div>
    </>
  );
}
