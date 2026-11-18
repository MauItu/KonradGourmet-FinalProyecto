import { useState } from 'react';
import { usuarios } from '../../api/servicios';
import { Encabezado } from '../../componentes/Encabezado';
import { Icono } from '../../componentes/Icono';
import { Tabla, useAviso } from '../../componentes/Piezas';
import { Tarjeta } from '../../componentes/Tarjeta';
import { SUCURSALES } from '../../componentes/formato';
import { NOMBRE_ROL, type Rol } from '../../sesion/token';

const VACIO = { nombre: '', correo: '', rol: 'MESERO' as Rol, sucursalId: 1, clave: '' };
type Registrado = { id: number; nombre: string; correo: string; rol: Rol; sucursalId: number };

/**
 * Usuarios (CU-19, sin mockup): el administrador registra un usuario con un rol y una sucursal (POST /api/usuarios,
 * ms-identidad). El servicio crea la subclase del rol, aplica la política de contraseña, guarda solo el hash y deja
 * la auditoría. Los seis roles son fijos (PD-06). Al lado, los usuarios registrados en esta visita.
 */
export function Usuarios() {
  const avisar = useAviso();
  const [f, setF] = useState(VACIO);
  const [ver, setVer] = useState(false);
  const [error, setError] = useState('');
  const [ocupado, setOcupado] = useState(false);
  const [registrados, setRegistrados] = useState<Registrado[]>([]);
  const campo = (k: keyof typeof VACIO) => (e: { target: { value: string } }) =>
    setF((x) => ({ ...x, [k]: k === 'sucursalId' ? Number(e.target.value) : e.target.value }));

  const registrar = async () => {
    setOcupado(true);
    try {
      const { id } = await usuarios.registrar({ ...f, nombre: f.nombre.trim(), correo: f.correo.trim() });
      setRegistrados((r) => [{ id, nombre: f.nombre.trim(), correo: f.correo.trim().toLowerCase(), rol: f.rol, sucursalId: f.sucursalId }, ...r]);
      avisar(`Usuario ${f.correo.trim().toLowerCase()} registrado como ${NOMBRE_ROL[f.rol]}.`);
      setF(VACIO);
      setError('');
    } catch (e) {
      setError(e instanceof Error ? e.message : 'No se pudo registrar el usuario.');
    } finally {
      setOcupado(false);
    }
  };

  return (
    <>
      <Encabezado titulo="Usuarios" sub="Alta de usuarios con su rol y su sucursal" icono="users" />
      <div className="page">
        <div className="grid halves">
          <Tarjeta titulo="Nuevo usuario" icono="plusCircle" sub="Los campos con * son obligatorios">
            <div className="form-grid">
              <label className="fl span2">Nombre completo *<input value={f.nombre} onChange={campo('nombre')} maxLength={120} aria-label="Nombre completo" /></label>
              <label className="fl span2">Correo *<input type="email" value={f.correo} onChange={campo('correo')} maxLength={160}
                placeholder="nombre.apellido@konradgourmet.com" aria-label="Correo" /></label>
              <label className="fl">Rol *<select value={f.rol} onChange={campo('rol')} aria-label="Rol">
                {(Object.keys(NOMBRE_ROL) as Rol[]).map((r) => <option key={r} value={r}>{NOMBRE_ROL[r]}</option>)}</select></label>
              <label className="fl">Sucursal *<select value={f.sucursalId} onChange={campo('sucursalId')} aria-label="Sucursal">
                {SUCURSALES.map((s) => <option key={s.id} value={s.id}>{s.nombre}</option>)}</select></label>
              <label className="fl span2">Contraseña inicial *
                <span className="tema-color">
                  <input type={ver ? 'text' : 'password'} value={f.clave} onChange={campo('clave')} maxLength={72} autoComplete="new-password" aria-label="Contraseña inicial" />
                  <button type="button" className="btn ghost sm" onClick={() => setVer((v) => !v)}>{ver ? 'Ocultar' : 'Mostrar'}</button>
                </span>
                <small className="unit">Al menos 8 caracteres, con una mayúscula, una minúscula y un número.</small>
              </label>
            </div>
            {error && <p className="form-error" role="alert">{error}</p>}
            <div className="m-actions">
              <button type="button" className="btn primary" disabled={ocupado} onClick={() => void registrar()}><Icono nombre="check" /> Registrar usuario</button>
            </div>
          </Tarjeta>
          <Tarjeta titulo="Registrados ahora" icono="users" sub="Los usuarios que registraste en esta visita; ya pueden iniciar sesión">
            <Tabla cabeceras={['Nombre', 'Rol', 'Sucursal']} vacio="Todavía no has registrado usuarios en esta visita.">
              {registrados.map((u) => (
                <tr key={u.id}>
                  <td><b>{u.nombre}</b><small className="unit">{u.correo}</small></td><td>{NOMBRE_ROL[u.rol]}</td>
                  <td>{SUCURSALES.find((s) => s.id === u.sucursalId)?.nombre}</td>
                </tr>
              ))}
            </Tabla>
          </Tarjeta>
        </div>
      </div>
    </>
  );
}
