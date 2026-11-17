import { useNavigate } from 'react-router-dom';
import { pedidos, type Pedido } from '../../api/servicios';
import { Encabezado } from '../../componentes/Encabezado';
import { aFecha, ESTADOS, hora, mesaTxt, plural } from '../../componentes/formato';
import { Icono } from '../../componentes/Icono';
import { BotonSuave, ErrorCarga, Insignia, Kpi, Miniatura, Tabla, useDatos } from '../../componentes/Piezas';
import { Tarjeta } from '../../componentes/Tarjeta';
import type { Usuario } from '../../sesion/token';
import { NOMBRE_ROL } from '../../sesion/token';

/** V02 · Inicio del mesero: GET /api/pedidos?turno=actual (ms-pedidos). */
export function InicioMesero({ usuario }: { usuario: Usuario }) {
  const navegar = useNavigate();
  const { datos, error } = useDatos(pedidos.delTurno);
  const lista: Pedido[] = datos ?? [];
  const activos = lista.filter((p) => p.estado !== 'SERVIDO' && p.estado !== 'ENTREGADO');
  const mesas = [...new Map(activos.map((p) => [p.mesa, p])).values()];
  const ver = (n: string) => navegar(`/pedidos?sel=${n}`);
  return (
    <>
      <Encabezado saludoA={usuario.nombre} sub={`${NOMBRE_ROL[usuario.rol]} · ${usuario.sucursal}`} />
      <div className="page">
        <ErrorCarga mensaje={error} />
        <div className="kpi-cta">
          <Kpi icono="clipboard" valor={lista.length} etiqueta="Pedidos de hoy" />
          <Kpi icono="table" valor={new Set(lista.map((p) => p.mesa)).size} etiqueta="Mesas atendidas" />
          <button type="button" className="cta" onClick={() => navegar('/pedidos/nuevo')}><Icono nombre="plus" /> Registrar nuevo pedido</button>
        </div>
        <div className="grid main-side">
          <Tarjeta titulo="Pedidos recientes" clase="underline" extra={<BotonSuave texto="Ver todos los pedidos" alHacer={() => navegar('/pedidos')} />}>
            <Tabla cabeceras={['Nº pedido', 'Mesa', 'Platos', 'Hora', 'Estado', '']} vacio="Todavía no hay pedidos en el turno. Usa «Registrar nuevo pedido».">
              {lista.slice(0, 5).map((p) => (
                <tr key={p.numero} className="clickable" onClick={() => ver(p.numero)}>
                  <td><b>{p.numero}</b></td><td>Mesa {p.mesa}</td><td>{plural(p.platos, 'plato', 'platos')}</td>
                  <td>{hora(p.fecha)}</td><td><Insignia estado={p.estado} /></td><td className="act"><Icono nombre="chevR" /></td>
                </tr>
              ))}
            </Tabla>
          </Tarjeta>
          <div className="stack">
            <Tarjeta titulo="Mesas en servicio" extra={<span className="pill-count">{plural(mesas.length, 'mesa activa', 'mesas activas')}</span>}>
              <div className="mesas">
                {mesas.length ? mesas.map((p) => (
                  <button type="button" key={p.mesa} className={`mesa-row tono-${(ESTADOS[p.estado] ?? [])[1]}`} onClick={() => ver(p.numero)}>
                    <span className="bar"></span>
                    <div><b>{mesaTxt(p.mesa)}</b> <small>{plural(p.platos, 'plato', 'platos')}</small><span>Siguiente: {p.detalles[0]?.plato ?? '—'}</span></div>
                    <Insignia estado={p.estado} /><Icono nombre="chevR" clase="chev" />
                  </button>
                )) : <p className="muted pad">No hay mesas con pedidos en curso.</p>}
              </div>
            </Tarjeta>
            <Tarjeta titulo="Próximas entregas" extra={<BotonSuave texto="Ver todos" alHacer={() => navegar('/pedidos')} />}>
              <div className="entregas">
                {activos.filter((p) => p.estado === 'EN_PREPARACION').slice(0, 3).map((p) => {
                  const min = Math.max(0, Math.round((Date.now() - (aFecha(p.fecha)?.getTime() ?? Date.now())) / 60000));
                  return (
                    <div key={p.numero} className="entrega">
                      <span className="when"><i></i>{min} min</span>
                      <Miniatura nombre={p.detalles[0]?.plato ?? ''} clase="sm" platoId={p.detalles[0]?.platoId} />
                      <div><b>{mesaTxt(p.mesa)}</b><small>{p.detalles[0]?.plato} ({plural(p.platos, 'plato', 'platos')})</small></div>
                    </div>
                  );
                })}
                {!activos.some((p) => p.estado === 'EN_PREPARACION') && <p className="muted pad">Sin entregas pendientes.</p>}
              </div>
            </Tarjeta>
          </div>
        </div>
      </div>
    </>
  );
}
