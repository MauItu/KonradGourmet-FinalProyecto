import { useCallback, useState } from 'react';
import { useLocation, useNavigate, useSearchParams } from 'react-router-dom';
import { platos, type Plato } from '../../api/servicios';
import { Encabezado } from '../../componentes/Encabezado';
import { money, num, plural } from '../../componentes/formato';
import { Icono } from '../../componentes/Icono';
import { ErrorCarga, Miniatura, Tabla, useAviso, useDatos, VacioDetalle } from '../../componentes/Piezas';
import { Tarjeta } from '../../componentes/Tarjeta';
import { VentanaPlato } from './VentanaPlato';

const Estado = ({ activo }: { activo: boolean }) =>
  activo ? <span className="badge verde">Activo</span> : <span className="badge gris">Inactivo</span>;

/**
 * V06 · Platos (ms-menu): la lista de la sucursal, también los inactivos, y el detalle con su receta.
 * «Nuevo plato» (/platos/nuevo) y «Editar plato» abren la misma ventana; desactivar no borra, solo saca
 * el plato del catálogo del mesero (HU-04/05). Cada cambio publica menu.actualizado.
 */
export function Platos() {
  const navegar = useNavigate();
  const { pathname } = useLocation();
  const [params, setParams] = useSearchParams();
  const avisar = useAviso();
  const { datos, error, recargar } = useDatos(platos.consultar);
  const [editando, setEditando] = useState<Plato | null>(null);
  const [version, setVersion] = useState(0);
  const lista = datos ?? [];
  const sel = lista.find((p) => p.id === Number(params.get('sel'))) ?? lista[0];
  const nuevo = pathname === '/platos/nuevo';

  const cerrar = useCallback(() => { setEditando(null); if (nuevo) navegar('/platos'); }, [nuevo, navegar]);
  const guardado = useCallback((p: Plato, esNuevo: boolean) => {
    setEditando(null);
    setVersion((v) => v + 1);
    recargar();
    navegar(`/platos?sel=${p.id}`);
    avisar(`Plato «${p.nombre}» guardado: ms-menu publicó menu.actualizado y ${esNuevo ? 'ya aparece en el catálogo del mesero.' : 'el catálogo del mesero y la receta quedaron al día.'}`);
  }, [recargar, navegar, avisar]);

  const cambiarEstado = async (p: Plato) => {
    try {
      if (p.activo) await platos.desactivar(p.id); else await platos.reactivar(p.id);
      recargar();
      avisar(p.activo
        ? `Plato «${p.nombre}» desactivado: sale del catálogo del mesero, sus pedidos anteriores siguen intactos.`
        : `Plato «${p.nombre}» activado: vuelve al catálogo del mesero.`);
    } catch (e) {
      avisar(e instanceof Error ? e.message : 'No se pudo cambiar el estado.', true);
    }
  };

  return (
    <>
      <Encabezado icono="utensils" titulo="Platos" sub="Gestiona el menú y consulta los detalles de cada plato"
        accion={<button type="button" className="btn primary" onClick={() => navegar('/platos/nuevo')}><Icono nombre="plus" /> Nuevo plato</button>} />
      <div className="page">
        <ErrorCarga mensaje={error} />
        <div className="grid main-side">
          <Tarjeta titulo="Lista de platos" icono="clipboard" sub={`${plural(lista.length, 'plato registrado', 'platos registrados')} en el menú`}>
            <Tabla cabeceras={['Plato', 'Categoría', 'Precio', 'Estado']}>
              {lista.map((p) => (
                <tr key={p.id} className={`clickable ${sel && p.id === sel.id ? 'selected' : ''}`} onClick={() => setParams({ sel: String(p.id) })}>
                  <td><span className="cell-thumb"><Miniatura nombre={p.nombre} clase="sm" platoId={p.id} tiene={p.imagen} version={version} /><b>{p.nombre}</b></span></td>
                  <td>{p.categoria}</td><td>{money(p.precioVenta)}</td><td><Estado activo={p.activo} /></td>
                </tr>
              ))}
            </Tabla>
          </Tarjeta>
          <section className="card plate-detail">
            {sel ? (
              <>
                <Miniatura nombre={sel.nombre} clase="hero" platoId={sel.id} tiene={sel.imagen} version={version} />
                <div className="detail-head"><h3>{sel.nombre}</h3><Estado activo={sel.activo} /></div>
                <div className="detail-actions">
                  <button type="button" className="btn outline sm" onClick={() => setEditando(sel)}><Icono nombre="pencil" /> Editar plato</button>
                  <button type="button" className={`btn ${sel.activo ? 'ghost' : 'primary'} sm`} onClick={() => void cambiarEstado(sel)}>{sel.activo ? 'Desactivar' : 'Activar'}</button>
                </div>
                <div className="price-line"><span>Precio de venta</span><b>{money(sel.precioVenta)}</b></div>
                <dl className="dl"><dt>Categoría</dt><dd>{sel.categoria}</dd><dt>Descripción</dt><dd>{sel.descripcion || '—'}</dd></dl>
                <h4 className="sub-title"><Icono nombre="utensils" /> Ingredientes <small>({sel.ingredientes.length})</small></h4>
                {sel.ingredientes.length ? (
                  <ul className="ing-list">
                    {sel.ingredientes.map((g) => (
                      <li key={g.productoId}><Miniatura nombre={g.producto} clase="xs" /><span>{g.producto}</span><b>{num(g.cantidad)} {g.unidad}</b></li>
                    ))}
                  </ul>
                ) : <p className="muted">Este plato no descuenta insumos del inventario.</p>}
              </>
            ) : <VacioDetalle texto={datos ? 'Aún no hay platos. Usa «Nuevo plato».' : 'Cargando…'} />}
          </section>
        </div>
      </div>
      {(nuevo || editando) && <VentanaPlato plato={editando} alCerrar={cerrar} alGuardar={guardado} />}
    </>
  );
}
