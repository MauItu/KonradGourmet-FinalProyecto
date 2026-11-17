import { Link } from 'react-router-dom';
import { Encabezado } from '../componentes/Encabezado';
import { Icono } from '../componentes/Icono';
import { Tarjeta } from '../componentes/Tarjeta';
import { pantalla } from '../pantallas';
import { useSesion } from '../sesion/Sesion';
import { NOMBRE_ROL, type ElementoMenu } from '../sesion/token';

/** Inicio de cada rol: saludo y accesos a sus ventanas, tomados del menú de ms-identidad. */
export function Inicio() {
  const { usuario } = useSesion();
  if (!usuario) return null;
  const hojas = (m: ElementoMenu): ElementoMenu[] => (m.hijos.length ? m.hijos.flatMap(hojas) : [m]);
  const accesos = hojas(usuario.menu).filter((h) => !['/inicio', '/perfil', '/cerrar-sesion'].includes(h.ruta));
  const nombre = usuario.nombre.split(' ')[0];
  return (
    <>
      <Encabezado saludoA={usuario.rol === 'MESERO' || usuario.rol === 'JEFE_COCINA' ? usuario.nombre : nombre}
        sub={`${NOMBRE_ROL[usuario.rol]} · ${usuario.sucursal}`} />
      <div className="page">
        <Tarjeta titulo="Tus ventanas" sub="Lo que tu rol puede hacer en el sistema" icono="home">
          <div className="accesos">
            {accesos.map((a) => {
              const p = pantalla(a.ruta);
              return (
                <Link key={a.ruta + a.titulo} to={a.ruta} className="acceso">
                  <span className="kpi-ico"><Icono nombre={p.icono} /></span>
                  <span><b>{a.titulo}</b><small>{p.sub}</small></span>
                  <Icono nombre="chevR" clase="chev" />
                </Link>
              );
            })}
          </div>
        </Tarjeta>
      </div>
    </>
  );
}
