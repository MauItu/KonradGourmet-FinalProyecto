import { useEffect, useState } from 'react';
import { administracion, type ErrorRegistrado } from '../../api/servicios';
import { Encabezado } from '../../componentes/Encabezado';
import { Icono } from '../../componentes/Icono';
import { Tabla, usePagina } from '../../componentes/Piezas';
import { Tarjeta } from '../../componentes/Tarjeta';
import { fecha, plural, SERVICIOS } from '../../componentes/formato';

/**
 * Logs de errores (HU-22, sin mockup): los errores que los servicios envían a Elasticsearch (DEC-84), con servicio,
 * mensaje, fecha y hora, los más recientes primero (GET /api/admin/logs, ms-administracion). El servicio es
 * opcional: sin él salen todos. Es de solo lectura y solo la ve el administrador.
 */
export function Logs() {
  const [servicio, setServicio] = useState('');
  const [errores, setErrores] = useState<ErrorRegistrado[] | null>(null);
  const [error, setError] = useState('');
  const [buscando, setBuscando] = useState(false);
  const { filas, pie, irA } = usePagina(errores ?? [], 10);

  const buscar = async (s = servicio) => {
    setBuscando(true);
    try {
      setErrores(await administracion.logs(s));
      setError('');
      irA(1);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'No se pudieron consultar los logs de errores.');
    } finally {
      setBuscando(false);
    }
  };
  useEffect(() => { void buscar(''); }, []); // eslint-disable-line react-hooks/exhaustive-deps

  return (
    <>
      <Encabezado titulo="Logs de errores" sub="Errores registrados por los servicios del sistema" icono="alert" />
      <div className="page">
        <section className="card filters compact">
          <label>Servicio<select value={servicio} onChange={(e) => { setServicio(e.target.value); void buscar(e.target.value); }} aria-label="Servicio">
            <option value="">Todos</option>{SERVICIOS.map((s) => <option key={s} value={s}>{s}</option>)}</select></label>
          <div className="filter-btns">
            <button type="button" className="btn outline sm" disabled={buscando} onClick={() => void buscar()}><Icono nombre="refresh" /> Actualizar</button>
          </div>
          {error && <p className="form-error" role="alert">{error}</p>}
        </section>
        <Tarjeta titulo="Errores registrados" icono="alert"
          sub={errores ? `${plural(errores.length, 'error', 'errores')} · ${servicio || 'todos los servicios'} · los más recientes primero` : 'Cargando…'}>
          <Tabla cabeceras={['Fecha', 'Hora', 'Servicio', 'Mensaje']} vacio="No hay errores registrados para el servicio seleccionado.">
            {filas.map((e, k) => (
              <tr key={`${e.fechaHora}${k}`}>
                <td>{fecha(e.fechaHora)}</td><td className="nowrap">{e.fechaHora.slice(11, 19)}</td>
                <td><span className="badge rojo">{e.servicio}</span></td><td className="mensaje-error">{e.mensaje}</td>
              </tr>
            ))}
          </Tabla>
          {pie}
        </Tarjeta>
      </div>
    </>
  );
}
