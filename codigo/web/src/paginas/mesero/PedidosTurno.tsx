import { useNavigate, useSearchParams } from 'react-router-dom';
import { pedidos } from '../../api/servicios';
import { Encabezado } from '../../componentes/Encabezado';
import { fecha, hora, money, pad2, plural } from '../../componentes/formato';
import { Icono } from '../../componentes/Icono';
import { ErrorCarga, Insignia, Miniatura, Tabla, useDatos, VacioDetalle } from '../../componentes/Piezas';
import { Tarjeta } from '../../componentes/Tarjeta';

/** Pedidos del turno (V02 «Ver todos los pedidos»): lista y detalle, GET /api/pedidos?turno=actual. */
export function PedidosTurno() {
  const navegar = useNavigate();
  const [params, setParams] = useSearchParams();
  const { datos, error } = useDatos(pedidos.delTurno);
  const lista = datos ?? [];
  const sel = lista.find((p) => p.numero === params.get('sel')) ?? lista[0];
  return (
    <>
      <Encabezado icono="clipboard" titulo="Pedidos" sub="Consulta los pedidos registrados en el turno"
        accion={<button type="button" className="btn primary" onClick={() => navegar('/pedidos/nuevo')}><Icono nombre="plus" /> Nuevo pedido</button>} />
      <div className="page">
        <ErrorCarga mensaje={error} />
        <div className="grid main-side">
          <Tarjeta titulo="Pedidos del turno" icono="clipboard">
            <Tabla cabeceras={['Nº pedido', 'Mesa', 'Platos', 'Hora', 'Total', 'Estado']} vacio="Todavía no hay pedidos en el turno.">
              {lista.map((p) => (
                <tr key={p.numero} className={`clickable ${sel && p.numero === sel.numero ? 'selected' : ''}`} onClick={() => setParams({ sel: p.numero })}>
                  <td><b>{p.numero}</b></td><td>Mesa {p.mesa}</td><td>{plural(p.platos, 'plato', 'platos')}</td>
                  <td>{hora(p.fecha)}</td><td>{money(p.total)}</td><td><Insignia estado={p.estado} /></td>
                </tr>
              ))}
            </Tabla>
          </Tarjeta>
          <Tarjeta titulo="Detalle del pedido" icono="file">
            {sel ? (
              <>
                <div className="detail-head"><h3>Pedido {sel.numero}</h3><Insignia estado={sel.estado} /></div>
                <div className="facts four">
                  <div><Icono nombre="table" /><small>Mesa</small><b>{pad2(sel.mesa)}</b></div>
                  <div><Icono nombre="utensils" /><small>Platos</small><b>{sel.platos}</b></div>
                  <div><Icono nombre="calendar" /><small>Fecha</small><b>{fecha(sel.fecha)}</b></div>
                  <div><Icono nombre="clock" /><small>Hora</small><b>{hora(sel.fecha)}</b></div>
                </div>
                <div className="lines">
                  {sel.detalles.map((d, k) => (
                    <div key={k} className="line">
                      <Miniatura nombre={d.plato} clase="sm" platoId={d.platoId} />
                      <div><b>{d.plato}</b><small>{d.cantidad} × {money(d.precioUnitario)}{d.nota ? ` · ${d.nota}` : ''}</small></div>
                      <b>{money(d.subtotal)}</b>
                    </div>
                  ))}
                </div>
                <div className="total-row"><span>Total</span><b>{money(sel.total)}</b></div>
              </>
            ) : <VacioDetalle texto="Selecciona un pedido para ver su detalle." />}
          </Tarjeta>
        </div>
      </div>
    </>
  );
}
