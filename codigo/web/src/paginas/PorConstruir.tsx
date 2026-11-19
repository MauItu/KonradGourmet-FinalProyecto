import { useLocation } from 'react-router-dom';
import { Encabezado } from '../componentes/Encabezado';
import { Icono } from '../componentes/Icono';
import { Tarjeta } from '../componentes/Tarjeta';
import { pantalla } from '../pantallas';

/** Ventana del menú que todavía no está construida: dice qué servicio la atiende y en qué corte llega. */
export function PorConstruir() {
  const { pathname } = useLocation();
  const p = pantalla(pathname);
  return (
    <>
      <Encabezado titulo={p.titulo} sub={p.sub} icono={p.icono} />
      <div className="page">
        <Tarjeta>
          <div className="empty-detail">
            <Icono nombre="info" />
            <p>Esta ventana{p.ventanas ? ` (${p.ventanas})` : ''} la atiende <b>{p.servicio}</b> y se construye en el corte {p.corte}.</p>
          </div>
        </Tarjeta>
      </div>
    </>
  );
}
