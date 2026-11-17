import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { solicitudes, type Solicitud } from '../../api/servicios';
import { Encabezado } from '../../componentes/Encabezado';
import { desc, fecha, plural } from '../../componentes/formato';
import { Icono } from '../../componentes/Icono';
import { BotonSuave, ErrorCarga, Insignia, Tabla, useDatos } from '../../componentes/Piezas';
import { nombreSucursal } from '../../componentes/Solicitudes';
import { Tarjeta } from '../../componentes/Tarjeta';
import { NOMBRE_ROL, type Usuario } from '../../sesion/token';

const mayus = (s: string) => s.charAt(0).toUpperCase() + s.slice(1);

/** El calendario del mes, con hoy marcado (calendario() de app.js). */
function Calendario() {
  const hoy = new Date();
  const [mes, setMes] = useState({ y: hoy.getFullYear(), m: hoy.getMonth() });
  const primero = new Date(mes.y, mes.m, 1);
  const inicio = (primero.getDay() + 6) % 7; // lunes primero
  const dias = new Date(mes.y, mes.m + 1, 0).getDate();
  const previos = new Date(mes.y, mes.m, 0).getDate();
  const mover = (d: number) => setMes(({ y, m }) => ({ y: m + d < 0 ? y - 1 : m + d > 11 ? y + 1 : y, m: (m + d + 12) % 12 }));
  const celdas = Array.from({ length: 42 }, (_, k) => {
    const d = k - inicio + 1;
    if (d < 1) return <span key={k} className="off">{previos + d}</span>;
    if (d > dias) return <span key={k} className="off">{d - dias}</span>;
    const esHoy = d === hoy.getDate() && mes.m === hoy.getMonth() && mes.y === hoy.getFullYear();
    return <span key={k} className={esHoy ? 'today' : ''}>{d}</span>;
  });
  const titulo = mayus(primero.toLocaleDateString('es-CO', { month: 'long', year: 'numeric' }).replace(' de ', ' '));
  return (
    <div className="cal">
      <div className="cal-head"><span className="title-ico sm"><Icono nombre="calendar" /></span><b>{titulo}</b>
        <div>
          <button type="button" className="round-btn" onClick={() => mover(-1)} aria-label="Mes anterior"><Icono nombre="chevL" /></button>
          <button type="button" className="round-btn" onClick={() => mover(1)} aria-label="Mes siguiente"><Icono nombre="chevR" /></button>
        </div>
      </div>
      <div className="cal-grid">{['Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb', 'Dom'].map((d) => <em key={d}>{d}</em>)}{celdas}</div>
    </div>
  );
}

/** Actividades del día del auxiliar (actividadesDelDia() de app.js). Las cotizaciones llegan en el corte 4b. */
function Actividades({ lista }: { lista: Solicitud[] }) {
  const pendientes = lista.filter((s) => s.estado === 'PENDIENTE').length;
  const tareas: [string, string, string][] = [
    ['09:00', 'Revisar solicitudes nuevas', pendientes ? plural(pendientes, 'pendiente', 'pendientes') : 'Al día'],
    ['10:30', 'Solicitar cotizaciones', pendientes ? 'Hay solicitudes por cotizar' : 'Sin solicitudes por cotizar'],
    ['14:00', 'Hacer seguimiento', 'Cotizaciones enviadas (corte 4b)'],
    ['16:00', 'Enviar solicitudes pendientes', `${lista.filter((s) => s.estado === 'EN_TRAMITE').length} en trámite`],
  ];
  return (
    <Tarjeta titulo="Actividades del día" icono="clipboard" extra={<span className="pill-count">{plural(pendientes, 'pendiente', 'pendientes')}</span>}>
      <ul className="timeline">
        {tareas.map(([h, t, d]) => <li key={h}><i></i><b>{h}</b><span>{t}<small>{d}</small></span></li>)}
      </ul>
    </Tarjeta>
  );
}

/**
 * V11 · Inicio del auxiliar de compras: las solicitudes de todas las sucursales (GET /api/solicitudes,
 * ms-solicitudes), los indicadores y las más recientes. «Cotizaciones enviadas» llega con ms-cotizaciones
 * (corte 4b).
 */
export function InicioAuxiliar({ usuario }: { usuario: Usuario }) {
  const navegar = useNavigate();
  const { datos, error } = useDatos(() => solicitudes.consultar());
  const lista = desc(datos ?? []);
  const cuenta = (estado: string) => (datos ? lista.filter((s) => s.estado === estado).length : '—');
  return (
    <>
      <Encabezado saludoA={usuario.nombre.split(' ')[0]} sub={`${NOMBRE_ROL[usuario.rol]} · ${usuario.sucursal}`} />
      <div className="page">
        <ErrorCarga mensaje={error} />
        <section className="card kpi-strip">
          <div><strong>{cuenta('PENDIENTE')}</strong><span>Solicitudes<br />pendientes</span></div>
          <div><strong>{cuenta('EN_TRAMITE')}</strong><span>Solicitudes<br />en trámite</span></div>
          <div><strong title="Llegan con ms-cotizaciones (corte 4b)">—</strong><span>Cotizaciones<br />enviadas</span></div>
        </section>
        <div className="grid main-side">
          <Tarjeta titulo="Solicitudes pendientes recientes" icono="file" sub="Revisa y gestiona las últimas solicitudes de alimentos"
            extra={<BotonSuave texto="Ver todas las solicitudes" alHacer={() => navegar('/solicitudes')} />}>
            <Tabla cabeceras={['Nº solicitud', 'Sucursal', 'Fecha', 'Productos', 'Estado', 'Acción']} vacio="Aún no hay solicitudes de alimentos.">
              {lista.slice(0, 6).map((s) => (
                <tr key={s.numero} className="clickable" onClick={() => navegar(`/solicitudes?sel=${s.numero}`)}>
                  <td><b>{s.numero}</b></td><td>{nombreSucursal(usuario, s.sucursalId)}</td><td>{fecha(s.fecha)}</td><td>{s.items.length}</td>
                  <td><Insignia estado={s.estado} punto /></td><td><span className="link-btn">Ver <Icono nombre="chevR" /></span></td>
                </tr>
              ))}
            </Tabla>
          </Tarjeta>
          <div className="stack">
            <Tarjeta><Calendario /></Tarjeta>
            <Actividades lista={lista} />
          </div>
        </div>
      </div>
    </>
  );
}
