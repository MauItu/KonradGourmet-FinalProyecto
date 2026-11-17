import { Encabezado } from '../componentes/Encabezado';
import { Tarjeta } from '../componentes/Tarjeta';
import { useSesion } from '../sesion/Sesion';
import { NOMBRE_ROL } from '../sesion/token';

export function Perfil() {
  const { usuario } = useSesion();
  if (!usuario) return null;
  const vence = new Date(usuario.vence).toLocaleTimeString('es-CO', { hour: 'numeric', minute: '2-digit' });
  return (
    <>
      <Encabezado titulo="Mi perfil" sub="Tus datos en el sistema" icono="user" />
      <div className="page">
        <Tarjeta titulo={usuario.nombre} sub={NOMBRE_ROL[usuario.rol]} icono="user">
          <dl className="dl">
            <dt>Correo</dt><dd>{usuario.correo}</dd>
            <dt>Rol</dt><dd>{NOMBRE_ROL[usuario.rol]}</dd>
            <dt>Sucursal</dt><dd>{usuario.sucursal}</dd>
            <dt>Sesión</dt><dd>Activa hasta las {vence}</dd>
          </dl>
        </Tarjeta>
      </div>
    </>
  );
}
