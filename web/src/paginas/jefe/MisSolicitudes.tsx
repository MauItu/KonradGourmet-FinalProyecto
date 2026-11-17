import { useCallback, useMemo, useState } from 'react';
import { useLocation, useNavigate, useSearchParams } from 'react-router-dom';
import { inventario, platos, solicitudes, type Solicitud } from '../../api/servicios';
import { Encabezado } from '../../componentes/Encabezado';
import { desc, enRango, ESTADOS, fecha } from '../../componentes/formato';
import { Icono } from '../../componentes/Icono';
import { ErrorCarga, Insignia, Tabla, usePagina, useDatos, VacioDetalle } from '../../componentes/Piezas';
import { DetalleSolicitud, SolicitudRegistrada, useProductos, VentanaSolicitud } from '../../componentes/Solicitudes';
import { Tarjeta } from '../../componentes/Tarjeta';
import { useSesion } from '../../sesion/Sesion';

/**
 * V10 · Mis solicitudes (GET /api/solicitudes?solicitante=yo, ms-solicitudes): la lista con los filtros de
 * fecha y estado, y el detalle de la seleccionada. /solicitudes/nueva abre V08 encima (?producto= la
 * preselecciona, desde una alerta de V05 o V07); al registrar, V09 muestra el número.
 */
export function MisSolicitudes() {
  const { usuario } = useSesion();
  const navegar = useNavigate();
  const { pathname } = useLocation();
  const [params, setParams] = useSearchParams();
  const lista = useDatos(() => solicitudes.mias());
  const productos = useDatos(platos.productos);
  const insumos = useDatos(inventario.insumos);
  const [filtroFecha, setFiltroFecha] = useState('');
  const [filtroEstado, setFiltroEstado] = useState('');
  const [registrada, setRegistrada] = useState<Solicitud | null>(null);
  const { nombre, categoria } = useProductos(productos.datos);
  const unidades = useMemo(() => new Map((insumos.datos ?? []).map((i) => [i.productoId, i.unidad])), [insumos.datos]);
  const nueva = pathname === '/solicitudes/nueva';
  const todas = desc(lista.datos ?? []);
  const visibles = todas.filter((s) => (!filtroEstado || s.estado === filtroEstado) && enRango(s.fecha, filtroFecha));
  const sel = todas.find((s) => s.numero === params.get('sel')) ?? visibles[0];
  const { filas, pie } = usePagina(visibles, 5);

  const cerrar = useCallback(() => navegar('/solicitudes', { replace: true }), [navegar]);
  const registrar = useCallback((s: Solicitud) => {
    setRegistrada(s);
    lista.recargar();
    navegar(`/solicitudes?sel=${s.numero}`, { replace: true });
  }, [lista, navegar]);

  if (!usuario) return null;
  const listos = productos.datos && insumos.datos && lista.datos;
  return (
    <>
      <Encabezado titulo="Mis solicitudes" sub="Consulta el estado de las solicitudes registradas" icono="file" />
      <div className="page">
        <ErrorCarga mensaje={lista.error || productos.error || insumos.error} />
        <div className="filters-line">
          <label className="fsel"><Icono nombre="calendar" />
            <select value={filtroFecha} onChange={(e) => setFiltroFecha(e.target.value)} aria-label="Fecha">
              <option value="">Fecha</option><option value="hoy">Hoy</option><option value="7">Últimos 7 días</option>
            </select></label>
          <label className="fsel"><Icono nombre="filter" />
            <select value={filtroEstado} onChange={(e) => setFiltroEstado(e.target.value)} aria-label="Estado">
              <option value="">Estado</option>
              {(['PENDIENTE', 'EN_TRAMITE', 'COMPLETADA'] as const).map((e) => <option key={e} value={e}>{ESTADOS[e][0]}</option>)}
            </select></label>
          <button type="button" className="btn primary push" onClick={() => navegar('/solicitudes/nueva')}><Icono nombre="plus" /> Nueva solicitud</button>
        </div>
        <div className="grid halves">
          <Tarjeta titulo="Lista de solicitudes" icono="file" sub="Revisa y selecciona una solicitud para ver sus detalles">
            <Tabla cabeceras={['Solicitud', 'Fecha', 'Productos', 'Estado', 'Acción']}
              vacio={todas.length ? 'No hay solicitudes con esos filtros.' : 'Aún no hay solicitudes registradas.'}>
              {filas.map((s) => (
                <tr key={s.numero} className={`clickable ${sel && s.numero === sel.numero ? 'selected' : ''}`} onClick={() => setParams({ sel: s.numero })}>
                  <td><b>{s.numero}</b></td><td>{fecha(s.fecha)}</td><td>{s.items.length}</td><td><Insignia estado={s.estado} mayus /></td>
                  <td className="act"><Icono nombre="chevR" /></td>
                </tr>
              ))}
            </Tabla>
            {pie}
          </Tarjeta>
          <Tarjeta titulo="Detalle de solicitud" icono="file" sub="Información completa de la solicitud seleccionada">
            {sel
              ? <DetalleSolicitud s={sel} solicitante={usuario.nombre} sucursal={usuario.sucursal} nombre={nombre} categoria={categoria} />
              : <VacioDetalle texto="Aún no hay solicitudes registradas." />}
          </Tarjeta>
        </div>
      </div>
      {nueva && listos && (
        <VentanaSolicitud usuario={usuario} productos={productos.datos ?? []} unidades={unidades}
          productoInicial={params.get('producto') ? Number(params.get('producto')) : null}
          precargar={!params.get('producto') && !(lista.datos ?? []).length}
          alCerrar={cerrar} alRegistrar={registrar} />
      )}
      {registrada && (
        <SolicitudRegistrada s={registrada} alCerrar={() => setRegistrada(null)}
          alVer={() => { setRegistrada(null); navegar(`/solicitudes?sel=${registrada.numero}`); }}
          alInicio={() => navegar('/inicio')} />
      )}
    </>
  );
}
