import { useState } from 'react';
import { NavLink, useLocation } from 'react-router-dom';
import { Icono } from './Icono';
import { pantalla } from '../pantallas';
import { NOMBRE_ROL, type ElementoMenu, type Usuario } from '../sesion/token';

const iniciales = (nombre: string) => nombre.split(/\s+/).filter(Boolean).slice(0, 2).map((p) => p[0]).join('').toUpperCase();

/**
 * Panel izquierdo (V02, V05, V11, V16): marca, perfil y el menú que armó ms-identidad con el
 * Composite (GrupoMenu / OpcionMenu). «Mi perfil» y «Cerrar sesión» van al pie, como en los mockups.
 */
export function Barra({ usuario, alSalir }: { usuario: Usuario; alSalir: () => void }) {
  const { pathname } = useLocation();
  const [abiertos, setAbiertos] = useState<Record<string, boolean>>({});
  const opciones = usuario.menu.hijos.filter((h) => h.ruta !== '/perfil' && h.ruta !== '/cerrar-sesion');

  const grupo = (g: ElementoMenu) => {
    const activo = g.hijos.some((h) => h.ruta === pathname);
    const abierto = activo || abiertos[g.titulo];
    return (
      <div key={g.titulo} className={`nav-group ${abierto ? 'open' : ''}`}>
        <button type="button" className={`nav-item ${activo ? 'parent-active' : ''}`}
          onClick={() => setAbiertos((a) => ({ ...a, [g.titulo]: !abierto }))} aria-expanded={abierto}>
          <Icono nombre={pantalla(g.ruta).icono} /><span>{g.titulo}</span>
          <Icono nombre={abierto ? 'chevD' : 'chevR'} clase="chev" />
        </button>
        <div className="nav-sub">
          {g.hijos.map((h) => (
            <NavLink key={h.titulo} to={h.ruta} end className={({ isActive }) => (isActive ? 'active' : '')}>
              <i></i>{h.titulo}
            </NavLink>
          ))}
        </div>
      </div>
    );
  };

  return (
    <aside className="sidebar">
      <div className="brand">
        <img src="/assets/logo/logo.png" alt="" />
        <div><b>Konrad <span>Gourmet</span></b><small>Sistema de Abastecimiento</small></div>
      </div>
      <div className="profile">
        <div className="avatar"><span>{iniciales(usuario.nombre)}</span><i className="online" title="En línea"></i></div>
        <b>{usuario.nombre}</b><span className="cargo">{NOMBRE_ROL[usuario.rol]}</span><small>{usuario.sucursal}</small>
      </div>
      <nav className="nav" aria-label="Menú principal">
        {opciones.map((o) => (o.hijos.length ? grupo(o) : (
          <NavLink key={o.titulo} to={o.ruta} end className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
            <Icono nombre={pantalla(o.ruta).icono} /><span>{o.titulo}</span>
          </NavLink>
        )))}
      </nav>
      <div className="nav-foot">
        <NavLink to="/perfil" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
          <Icono nombre="user" /><span>Mi perfil</span>
        </NavLink>
        <button type="button" className="nav-item" onClick={alSalir}><Icono nombre="logout" /><span>Cerrar sesión</span></button>
      </div>
    </aside>
  );
}
