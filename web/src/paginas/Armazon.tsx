import { useEffect } from 'react';
import { Navigate, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { Barra } from '../componentes/Barra';
import { useSesion } from '../sesion/Sesion';
import { CLASE_ROL, type ElementoMenu } from '../sesion/token';
import '../estilos/app.css';

/** Todas las rutas del menú del usuario, incluidas las de los grupos. */
export function rutasDelMenu(m: ElementoMenu): string[] {
  return [m.ruta, ...m.hijos.flatMap(rutasDelMenu)];
}

/** La primera opción del menú: Inicio para casi todos, el tablero BAM para el director comercial. */
export function primeraRuta(m: ElementoMenu): string {
  const h = m.hijos[0];
  return h ? (h.hijos.length ? h.hijos[0].ruta : h.ruta) : '/perfil';
}

/**
 * Panel izquierdo + contenido. Solo deja ver las rutas que están en el menú del rol (el JWT lo trae
 * firmado); el servicio vuelve a comprobar el rol en cada endpoint.
 */
export function Armazon() {
  const { usuario, salir } = useSesion();
  const { pathname } = useLocation();
  const navegar = useNavigate();

  useEffect(() => {
    if (!usuario) return undefined;
    document.body.className = CLASE_ROL[usuario.rol];
    return () => { document.body.className = ''; };
  }, [usuario]);

  if (!usuario) return <Navigate to="/login" replace />;
  if (pathname === '/') return <Navigate to={primeraRuta(usuario.menu)} replace />;
  if (pathname !== '/perfil' && !rutasDelMenu(usuario.menu).includes(pathname)) {
    return <Navigate to={primeraRuta(usuario.menu)} replace />;
  }

  return (
    <div className="armazon">
      <Barra usuario={usuario} alSalir={() => { salir(); navegar('/login'); }} />
      <main className="shell" tabIndex={-1}>
        <Outlet />
      </main>
    </div>
  );
}
