import { useNavigate } from 'react-router-dom';
import { inventario, platos } from '../../api/servicios';
import { Encabezado } from '../../componentes/Encabezado';
import { num } from '../../componentes/formato';
import { Icono } from '../../componentes/Icono';
import { BotonSuave, ErrorCarga, Kpi, Medidor, Miniatura, Tabla, useDatos } from '../../componentes/Piezas';
import { Tarjeta } from '../../componentes/Tarjeta';
import { NOMBRE_ROL, type Usuario } from '../../sesion/token';

/**
 * V05 · Inicio del jefe de cocina: alertas de stock bajo (GET /api/inventario/alertas, ms-inventario) y
 * platos registrados (ms-menu). Las solicitudes las atiende ms-solicitudes, que llega en el corte 4.
 */
export function InicioJefe({ usuario }: { usuario: Usuario }) {
  const navegar = useNavigate();
  const alertas = useDatos(inventario.alertas);
  const carta = useDatos(platos.consultar);
  const lista = alertas.datos ?? [];
  return (
    <>
      <Encabezado saludoA={usuario.nombre} sub={`${NOMBRE_ROL[usuario.rol]} · ${usuario.sucursal}`} />
      <div className="page">
        <ErrorCarga mensaje={alertas.error || carta.error} />
        <div className="kpis four">
          <Kpi icono="bowl" valor={carta.datos ? carta.datos.filter((p) => p.activo).length : '—'} etiqueta="Platos registrados" />
          <Kpi icono="alert" valor={alertas.datos ? lista.length : '—'} etiqueta="Stock bajo" tono="danger" />
          <Kpi icono="file" valor="—" etiqueta="Solicitudes pendientes" />
          <Kpi icono="refresh" valor="—" etiqueta="Solicitudes en trámite" />
        </div>
        <div className="grid halves">
          <Tarjeta titulo="Alertas de inventario" icono="bell" extra={<BotonSuave texto="Ver inventario" alHacer={() => navegar('/inventario')} />}>
            <div className="alert-list">
              {lista.map((i) => (
                <div key={i.productoId} className="alert-row">
                  <Miniatura nombre={i.nombre} clase="sm" />
                  <b>{i.nombre}</b>
                  <span className={`pct ${i.porcentaje < 20 ? 'rojo' : 'naranja'}`}>{num(i.porcentaje)}%</span>
                  <Medidor p={i.porcentaje} />
                  <button type="button" className="round-btn" onClick={() => navegar('/solicitudes/nueva')} aria-label={`Solicitar ${i.nombre}`}><Icono nombre="chevR" /></button>
                </div>
              ))}
              {alertas.datos && !lista.length && <p className="muted pad">No hay alertas activas. El cierre diario (23:59) actualiza el inventario.</p>}
            </div>
          </Tarjeta>
          <Tarjeta titulo="Solicitudes recientes" icono="file" extra={<BotonSuave texto="Ver todas" alHacer={() => navegar('/solicitudes')} />}>
            <Tabla cabeceras={['Nº solicitud', 'Fecha', 'Productos', 'Estado', 'Acción']}
              vacio="Las solicitudes de alimentos las atiende ms-solicitudes, que llega en el corte 4.">{[]}</Tabla>
          </Tarjeta>
        </div>
      </div>
    </>
  );
}
