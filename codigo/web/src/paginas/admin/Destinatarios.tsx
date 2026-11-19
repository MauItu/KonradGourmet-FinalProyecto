import { useState } from 'react';
import { notificaciones } from '../../api/servicios';
import { Encabezado } from '../../componentes/Encabezado';
import { Icono } from '../../componentes/Icono';
import { ErrorCarga, Tabla, useAviso, useDatos } from '../../componentes/Piezas';
import { Tarjeta } from '../../componentes/Tarjeta';
import { SUCURSALES } from '../../componentes/formato';

/**
 * Destinatarios de alertas (HU-20 (2), DEC-13, sin mockup): los correos de los jefes de cocina de cada sucursal, que
 * reciben la alarma y el correo de stock bajo (y las promociones). Se consultan por sucursal y se agregan (GET y
 * POST /api/notificaciones/destinatarios, ms-notificaciones); cada registro queda en la auditoría.
 */
export function Destinatarios() {
  const avisar = useAviso();
  const [sucursalId, setSucursalId] = useState(1);
  const lista = useDatos(() => notificaciones.destinatarios(sucursalId), [sucursalId]);
  const [correo, setCorreo] = useState('');
  const [error, setError] = useState('');
  const [ocupado, setOcupado] = useState(false);
  const sucursal = SUCURSALES.find((s) => s.id === sucursalId)?.nombre ?? '';

  const agregar = async () => {
    setOcupado(true);
    try {
      const c = correo.trim().toLowerCase();
      await notificaciones.registrarDestinatario(sucursalId, c);
      lista.recargar();
      setCorreo('');
      setError('');
      avisar(`${c} recibirá las alertas de stock bajo de la ${sucursal}.`);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'No se pudo registrar el destinatario.');
    } finally {
      setOcupado(false);
    }
  };

  return (
    <>
      <Encabezado titulo="Destinatarios de alertas" sub="Correos de los jefes de cocina que reciben las alertas de stock bajo" icono="bell" />
      <div className="page">
        <section className="card filters compact">
          <label>Sucursal<select value={sucursalId} onChange={(e) => { setSucursalId(Number(e.target.value)); setError(''); }} aria-label="Sucursal">
            {SUCURSALES.map((s) => <option key={s.id} value={s.id}>{s.nombre}</option>)}</select></label>
        </section>
        <div className="grid halves">
          <Tarjeta titulo={`Destinatarios de la ${sucursal}`} icono="bell"
            sub={lista.datos ? `${lista.datos.length} ${lista.datos.length === 1 ? 'correo' : 'correos'}` : 'Cargando…'}>
            <ErrorCarga mensaje={lista.error} />
            <Tabla cabeceras={['Correo', 'Recibe']} vacio="Esta sucursal todavía no tiene destinatarios: nadie recibiría sus alertas.">
              {(lista.datos ?? []).map((c) => (
                <tr key={c}><td><b>{c}</b></td><td>Alarma y correo de stock bajo</td></tr>
              ))}
            </Tabla>
          </Tarjeta>
          <Tarjeta titulo="Agregar destinatario" icono="plusCircle" sub={`Se agrega a la ${sucursal}`}>
            <div className="form-grid">
              <label className="fl span2">Correo del jefe de cocina *<input type="email" value={correo} onChange={(e) => setCorreo(e.target.value)} maxLength={160}
                placeholder="jefe.cocina@konradgourmet.com" aria-label="Correo del destinatario" /></label>
            </div>
            {error && <p className="form-error" role="alert">{error}</p>}
            <div className="m-actions">
              <button type="button" className="btn primary" disabled={ocupado} onClick={() => void agregar()}><Icono nombre="check" /> Agregar destinatario</button>
            </div>
          </Tarjeta>
        </div>
      </div>
    </>
  );
}
