import { useNavigate } from 'react-router-dom';
import { cotizaciones, ordenes } from '../../api/servicios';
import { porRegistrar, porValidar } from '../../componentes/Cotizaciones';
import { Encabezado } from '../../componentes/Encabezado';
import { asc } from '../../componentes/formato';
import { ErrorCarga, Kpi, Tabla, useDatos } from '../../componentes/Piezas';
import { Tarjeta } from '../../componentes/Tarjeta';
import { NOMBRE_ROL, type Usuario } from '../../sesion/token';
import { AccionPendiente } from './CotizacionesDirector';

/**
 * V16 · Inicio del director de compras: las cotizaciones por registrar y por validar (GET
 * /api/cotizaciones/pendientes, ms-cotizaciones), el resumen por estado (GET /api/cotizaciones) y las
 * órdenes de compra (GET /api/ordenes-compra, ms-ordenes). Cada acción abre V18 en «Pendientes de validar».
 */
export function InicioDirector({ usuario }: { usuario: Usuario }) {
  const navegar = useNavigate();
  const pendientes = useDatos(cotizaciones.pendientes);
  const todas = useDatos(() => cotizaciones.consultar());
  const compras = useDatos(() => ordenes.consultar());
  const pend = asc(pendientes.datos ?? []);
  const lista = todas.datos ?? [];
  const conteo = (e: string) => lista.filter((c) => c.estado === e).length;
  const valores = (numero: string) => navegar(`/cotizaciones/pendientes?sel=${numero}&valores=${numero}`);
  return (
    <>
      <Encabezado saludoA={usuario.nombre.split(' ')[0]} sub={`${NOMBRE_ROL[usuario.rol]} · ${usuario.sucursal}`} />
      <div className="page">
        <ErrorCarga mensaje={pendientes.error || todas.error || compras.error} />
        <div className="kpis four">
          <Kpi icono="file" valor={pendientes.datos ? porRegistrar(pend).length : '—'} etiqueta="Cotizaciones por registrar" />
          <Kpi icono="clock" valor={pendientes.datos ? porValidar(pend).length : '—'} etiqueta="Pendientes de validar" />
          <Kpi icono="star" valor={todas.datos ? conteo('OPCIONADA') : '—'} etiqueta="Cotizaciones opcionadas" />
          <Kpi icono="cart" valor={compras.datos ? compras.datos.length : '—'} etiqueta="Órdenes de compra" />
        </div>
        <div className="grid main-side">
          <Tarjeta titulo="Pendientes de atención" icono="file" sub="Cotizaciones recibidas listas para registro y validación.">
            <Tabla cabeceras={['Cotización', 'Proveedor', 'Solicitud', 'Acción']}
              vacio="No hay cotizaciones pendientes. El auxiliar de compras las genera y envía a los proveedores.">
              {pend.slice(0, 6).map((c) => (
                <tr key={c.numero}><td><b>{c.numero}</b></td><td>{c.proveedor}</td><td>{c.solicitudes.join(', ')}</td>
                  <td><AccionPendiente c={c} alHacer={valores} /></td></tr>
              ))}
            </Tabla>
          </Tarjeta>
          <div className="stack">
            <Tarjeta titulo="Pendientes hoy" icono="clock" sub="Cotizaciones que requieren tu revisión." extra={<span className="pill-count">{pend.length}</span>}>
              <Tabla cabeceras={['Cotización', 'Proveedor', 'Solicitud']} vacio="Nada pendiente para hoy.">
                {pend.slice(0, 4).map((c) => (
                  <tr key={c.numero} className="clickable" onClick={() => navegar(`/cotizaciones/pendientes?sel=${c.numero}`)}>
                    <td>{c.numero}</td><td>{c.proveedor}</td><td>{c.solicitudes.join(', ')}</td></tr>
                ))}
              </Tabla>
            </Tarjeta>
            <Tarjeta titulo="Estado de cotizaciones" icono="chart" sub="Resumen general del estado actual.">
              <ul className="estado-list">
                <li><i className="naranja"></i>Enviada<b className="n naranja">{conteo('ENVIADA')}</b></li>
                <li><i className="azul"></i>Opcionada<b className="n azul">{conteo('OPCIONADA') + conteo('CON_ORDEN')}</b></li>
                <li><i className="rojo"></i>Rechazada<b className="n rojo">{conteo('RECHAZADA')}</b></li>
                <li><i className="ambar"></i>Sospechosa<b className="n ambar">{conteo('SOSPECHOSA')}</b></li>
              </ul>
            </Tarjeta>
          </div>
        </div>
      </div>
    </>
  );
}
