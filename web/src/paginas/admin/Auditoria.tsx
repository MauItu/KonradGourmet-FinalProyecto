import { useEffect, useState } from 'react';
import { administracion, type Movimiento } from '../../api/servicios';
import { Encabezado } from '../../componentes/Encabezado';
import { Icono } from '../../componentes/Icono';
import { Tabla, usePagina } from '../../componentes/Piezas';
import { Tarjeta } from '../../componentes/Tarjeta';
import { fecha, hoyISO, plural } from '../../componentes/formato';

/**
 * Auditoría (HU-21, sin mockup): los movimientos de todos los servicios con acción, usuario, fecha y hora, en un
 * rango de fechas obligatorio (PD-07; GET /api/admin/auditoria, ms-administracion). Abre con los últimos 7 días.
 * Es de solo lectura y solo la ve el administrador.
 */
export function Auditoria() {
  const [desde, setDesde] = useState(hoyISO(-7));
  const [hasta, setHasta] = useState(hoyISO());
  const [movimientos, setMovimientos] = useState<Movimiento[] | null>(null);
  const [periodo, setPeriodo] = useState('');
  const [error, setError] = useState('');
  const [buscando, setBuscando] = useState(false);
  const { filas, pie, irA } = usePagina(movimientos ?? [], 10);

  const buscar = async (d = desde, h = hasta) => {
    setBuscando(true);
    try {
      setMovimientos(await administracion.auditoria(d, h));
      setPeriodo(d === h ? `del ${fecha(d)}` : `del ${fecha(d)} al ${fecha(h)}`);
      setError('');
      irA(1);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'No se pudo consultar la auditoría.');
    } finally {
      setBuscando(false);
    }
  };
  useEffect(() => { void buscar(); }, []); // eslint-disable-line react-hooks/exhaustive-deps
  const hoy = () => { setDesde(hoyISO()); setHasta(hoyISO()); void buscar(hoyISO(), hoyISO()); };

  return (
    <>
      <Encabezado titulo="Auditoría" sub="Quién hizo cada acción en el sistema y cuándo" icono="clipboard" />
      <div className="page">
        <section className="card filters compact">
          <label>Desde *<input type="date" value={desde} onChange={(e) => setDesde(e.target.value)} aria-label="Desde" /></label>
          <label>Hasta *<input type="date" value={hasta} onChange={(e) => setHasta(e.target.value)} aria-label="Hasta" /></label>
          <div className="filter-btns">
            <button type="button" className="btn outline sm" disabled={buscando} onClick={() => void buscar()}><Icono nombre="search" /> Consultar</button>
            <button type="button" className="btn ghost sm" onClick={hoy}><Icono nombre="calendar" /> Hoy</button>
          </div>
          {error && <p className="form-error" role="alert">{error}</p>}
        </section>
        <Tarjeta titulo="Movimientos de auditoría" icono="clipboard"
          sub={movimientos ? `${plural(movimientos.length, 'movimiento', 'movimientos')} ${periodo}` : 'Cargando…'}>
          <Tabla cabeceras={['Fecha', 'Hora', 'Usuario', 'Acción']} vacio="No hay movimientos de auditoría en el periodo seleccionado.">
            {filas.map((m, k) => (
              <tr key={`${m.fecha}${m.hora}${k}`}>
                <td>{fecha(m.fecha)}</td><td className="nowrap">{m.hora}</td><td><b>{m.usuario}</b></td><td>{m.accion}</td>
              </tr>
            ))}
          </Tabla>
          {pie}
        </Tarjeta>
      </div>
    </>
  );
}
