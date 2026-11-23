import { useNavigate } from 'react-router-dom';
import { inventario, platos, solicitudes } from '../../api/servicios';
import { useAlarmaRecibida } from '../../componentes/Alarmas';
import { Encabezado } from '../../componentes/Encabezado';
import { desc, fecha, num } from '../../componentes/formato';
import { Icono } from '../../componentes/Icono';
import { BotonSuave, ErrorCarga, Insignia, Kpi, Medidor, Miniatura, Tabla, useDatos } from '../../componentes/Piezas';
import { Tarjeta } from '../../componentes/Tarjeta';
import { NOMBRE_ROL, type Usuario } from '../../sesion/token';

/**
 * V05 · Inicio del jefe de cocina: alertas de stock bajo (GET /api/inventario/alertas, ms-inventario),
 * platos registrados (ms-menu) y sus solicitudes (GET /api/solicitudes?solicitante=yo, ms-solicitudes).
 * Una alerta deja de salir cuando ms-inventario recibe solicitud.registrada con ese producto (HU-16). Cuando
 * llega una alarma en vivo (ms-notificaciones, PD-01), vuelve a leer las alertas.
 */
export function InicioJefe({ usuario }: { usuario: Usuario }) {
  const navegar = useNavigate();
  const alertas = useDatos(inventario.alertas);
  useAlarmaRecibida(alertas.recargar);
  const carta = useDatos(platos.consultar);
  const mias = useDatos(() => solicitudes.mias());
  const lista = alertas.datos ?? [];
  const sols = desc(mias.datos ?? []);
  const cuenta = (estado: string) => (mias.datos ? sols.filter((s) => s.estado === estado).length : '—');
  return (
    <>
      <Encabezado saludoA={usuario.nombre} sub={`${NOMBRE_ROL[usuario.rol]} · ${usuario.sucursal}`} />
      <div className="page">
        <ErrorCarga mensaje={alertas.error || carta.error || mias.error} />
        <div className="kpis four">
          <Kpi icono="bowl" valor={carta.datos ? carta.datos.filter((p) => p.activo).length : '—'} etiqueta="Platos registrados" />
          <Kpi icono="alert" valor={alertas.datos ? lista.length : '—'} etiqueta="Stock bajo" tono="danger" />
          <Kpi icono="file" valor={cuenta('PENDIENTE')} etiqueta="Solicitudes pendientes" />
          <Kpi icono="refresh" valor={cuenta('EN_TRAMITE')} etiqueta="Solicitudes en trámite" />
        </div>
        <div className="grid halves">
          <Tarjeta titulo="Alertas de inventario" icono="bell" extra={<BotonSuave texto="Ver inventario" alHacer={() => navegar('/inventario')} />}>
            <div className="alert-list">
              {lista.map((i) => (
                <div key={i.productoId} className="alert-row">
                  <Miniatura nombre={i.nombre} clase="sm" />
                  <b>{i.nombre}</b>
                  <span className="queda" title="Cantidad restante">{num(i.stockActual)} {i.unidad}</span>
                  <span className={`pct ${i.porcentaje < 20 ? 'rojo' : 'naranja'}`}>{num(i.porcentaje)}%</span>
                  <Medidor p={i.porcentaje} />
                  <button type="button" className="round-btn" onClick={() => navegar(`/solicitudes/nueva?producto=${i.productoId}`)} aria-label={`Solicitar ${i.nombre}`}><Icono nombre="chevR" /></button>
                </div>
              ))}
              {alertas.datos && !lista.length && <p className="muted pad">No hay alertas activas. El cierre diario (23:59) actualiza el inventario.</p>}
            </div>
          </Tarjeta>
          <Tarjeta titulo="Solicitudes recientes" icono="file" extra={<BotonSuave texto="Ver todas" alHacer={() => navegar('/solicitudes')} />}>
            <Tabla cabeceras={['Nº solicitud', 'Fecha', 'Productos', 'Estado', 'Acción']} vacio="Aún no hay solicitudes.">
              {sols.slice(0, 5).map((s) => (
                <tr key={s.numero} className="clickable" onClick={() => navegar(`/solicitudes?sel=${s.numero}`)}>
                  <td><b>{s.numero}</b></td><td>{fecha(s.fecha)}</td><td>{s.items.length}</td><td><Insignia estado={s.estado} /></td>
                  <td className="act"><span className="round-btn"><Icono nombre="chevR" /></span></td>
                </tr>
              ))}
            </Tabla>
          </Tarjeta>
        </div>
      </div>
    </>
  );
}
