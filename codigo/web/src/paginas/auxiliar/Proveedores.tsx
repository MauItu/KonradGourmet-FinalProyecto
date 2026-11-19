import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { proveedores, type DatosProveedor, type Proveedor } from '../../api/servicios';
import { Encabezado } from '../../componentes/Encabezado';
import { Icono } from '../../componentes/Icono';
import { ErrorCarga, Insignia, Tabla, useAviso, usePagina, useDatos } from '../../componentes/Piezas';
import { Tarjeta } from '../../componentes/Tarjeta';

const VACIO = { razonSocial: '', nit: '', correo: '', telefono: '', direccion: '', categorias: '' };
type Formulario = typeof VACIO;

const formulario = (p: Proveedor | null): Formulario => (p
  ? { razonSocial: p.razonSocial, nit: p.nit, correo: p.correo, telefono: p.telefono ?? '', direccion: p.direccion ?? '', categorias: p.categorias.join(', ') }
  : VACIO);

const datos = (f: Formulario): DatosProveedor => ({
  razonSocial: f.razonSocial, nit: f.nit, correo: f.correo, telefono: f.telefono || null, direccion: f.direccion || null,
  categorias: f.categorias.split(',').map((c) => c.trim()).filter(Boolean),
});

/**
 * Proveedores (HU-15, sin mockup; con las piezas de V12): la lista con su estado y, al lado, el formulario
 * para registrar uno nuevo o editar el escogido (POST y PUT /api/proveedores, ms-cotizaciones). Inactivar
 * no borra: el proveedor deja de salir en el desplegable de V13, y sus cotizaciones viejas lo siguen
 * nombrando. El NIT no se puede repetir y el correo debe tener forma de correo (las reglas son del núcleo).
 */
export function Proveedores() {
  const avisar = useAviso();
  const [params, setParams] = useSearchParams();
  const lista = useDatos(() => proveedores.consultar());
  const [buscar, setBuscar] = useState('');
  const todos = lista.datos ?? [];
  const editando = todos.find((p) => String(p.id) === params.get('sel')) ?? null;
  const [f, setF] = useState<Formulario>(VACIO);
  const [error, setError] = useState('');
  const [ocupado, setOcupado] = useState(false);
  const texto = buscar.trim().toLowerCase();
  const visibles = todos.filter((p) => !texto || `${p.razonSocial} ${p.nit} ${p.correo} ${p.categorias.join(' ')}`.toLowerCase().includes(texto));
  const { filas, pie } = usePagina(visibles, 6);

  // al escoger otro proveedor (o «Nuevo») el formulario toma sus datos
  useEffect(() => { setF(formulario(editando)); setError(''); }, [editando?.id, lista.datos]); // eslint-disable-line react-hooks/exhaustive-deps

  const campo = (k: keyof Formulario) => (e: { target: { value: string } }) => setF((x) => ({ ...x, [k]: e.target.value }));
  const guardar = async () => {
    setOcupado(true);
    try {
      const p = editando ? await proveedores.editar(editando.id, datos(f)) : await proveedores.registrar(datos(f));
      lista.recargar();
      setParams({ sel: String(p.id) });
      avisar(editando ? `Proveedor «${p.razonSocial}» actualizado.` : `Proveedor «${p.razonSocial}» registrado: ya sale en «Generar cotización».`);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'No se pudo guardar el proveedor.');
    } finally {
      setOcupado(false);
    }
  };
  const inactivar = async (p: Proveedor) => {
    if (!window.confirm(`¿Inactivar a ${p.razonSocial}? Dejará de salir en «Generar cotización»; sus cotizaciones se conservan.`)) return;
    try {
      await proveedores.inactivar(p.id);
      lista.recargar();
      avisar(`Proveedor «${p.razonSocial}» inactivado.`);
    } catch (e) {
      avisar(e instanceof Error ? e.message : 'No se pudo inactivar.', true);
    }
  };

  return (
    <>
      <Encabezado titulo="Proveedores" sub="Registro y mantenimiento de los proveedores de la empresa" icono="store"
        accion={<button type="button" className="btn primary sm" onClick={() => setParams({})}><Icono nombre="plus" /> Nuevo proveedor</button>} />
      <div className="page">
        <ErrorCarga mensaje={lista.error} />
        <div className="grid halves">
          <Tarjeta titulo="Proveedores registrados" icono="store" sub={`${todos.filter((p) => p.activo).length} activos de ${todos.length}`}
            extra={<label className="search sm"><Icono nombre="search" /><input value={buscar} onChange={(e) => setBuscar(e.target.value)} placeholder="Buscar proveedor..." aria-label="Buscar proveedor" /></label>}>
            <Tabla cabeceras={['Razón social', 'NIT', 'Categorías', 'Estado', 'Acción']} vacio={todos.length ? 'Ningún proveedor coincide con la búsqueda.' : 'Aún no hay proveedores.'}>
              {filas.map((p) => (
                <tr key={p.id} className={`clickable ${editando?.id === p.id ? 'selected' : ''}`} onClick={() => setParams({ sel: String(p.id) })}>
                  <td><b>{p.razonSocial}</b><small className="unit">{p.correo}</small></td><td>{p.nit}</td><td>{p.categorias.join(', ') || '—'}</td>
                  <td><Insignia estado={p.activo ? 'ACTIVO' : 'INACTIVO'} punto /></td>
                  <td><span className="link-btn">Editar <Icono nombre="chevR" /></span></td>
                </tr>
              ))}
            </Tabla>
            {pie}
          </Tarjeta>
          <Tarjeta titulo={editando ? 'Editar proveedor' : 'Nuevo proveedor'} icono={editando ? 'pencil' : 'plusCircle'}
            sub={editando ? `${editando.razonSocial} · ${editando.activo ? 'activo' : 'inactivo'}` : 'Los campos con * son obligatorios'}>
            <div className="form-grid">
              <label className="fl">Razón social *<input value={f.razonSocial} onChange={campo('razonSocial')} maxLength={120} aria-label="Razón social" /></label>
              <label className="fl">NIT *<input value={f.nit} onChange={campo('nit')} maxLength={30} placeholder="900123456-1" aria-label="NIT" /></label>
              <label className="fl">Correo *<input type="email" value={f.correo} onChange={campo('correo')} maxLength={160} placeholder="ventas@proveedor.com" aria-label="Correo" /></label>
              <label className="fl">Teléfono<input value={f.telefono} onChange={campo('telefono')} maxLength={30} aria-label="Teléfono" /></label>
              <label className="fl span2">Dirección<input value={f.direccion} onChange={campo('direccion')} maxLength={160} aria-label="Dirección" /></label>
              <label className="fl span2">Categorías (separadas por comas)<input value={f.categorias} onChange={campo('categorias')} placeholder="Carnes, Granos" aria-label="Categorías" /></label>
            </div>
            {error && <p className="form-error" role="alert">{error}</p>}
            <div className="m-actions">
              {editando?.activo && <button type="button" className="btn ghost" onClick={() => void inactivar(editando)}><Icono nombre="circleX" /> Inactivar</button>}
              <button type="button" className="btn primary" disabled={ocupado} onClick={() => void guardar()}>
                <Icono nombre="check" /> {editando ? 'Guardar cambios' : 'Registrar proveedor'}</button>
            </div>
          </Tarjeta>
        </div>
      </div>
    </>
  );
}
