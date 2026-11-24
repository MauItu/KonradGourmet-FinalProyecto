import { Link } from 'react-router-dom';
import { Encabezado } from '../componentes/Encabezado';
import { Icono } from '../componentes/Icono';
import { Tarjeta } from '../componentes/Tarjeta';

/**
 * Una ruta del menú que no tiene página. No debería verse: Armazon manda cualquier dirección que no esté en el
 * menú del rol (escrita a mano, por ejemplo) a la primera opción del menú (C7-16).
 */
export function NoExiste() {
  return (
    <>
      <Encabezado titulo="La ventana no existe" sub="La dirección no corresponde a ninguna opción del menú" icono="info" />
      <div className="page">
        <Tarjeta>
          <div className="empty-detail">
            <Icono nombre="info" />
            <p>La ventana que busca no existe. <Link to="/inicio">Volver al inicio</Link></p>
          </div>
        </Tarjeta>
      </div>
    </>
  );
}
