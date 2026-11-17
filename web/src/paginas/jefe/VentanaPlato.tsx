import { useEffect, useState } from 'react';
import { platos, type DatosPlato, type Plato } from '../../api/servicios';
import { CATEGORIAS_CARTA, num } from '../../componentes/formato';
import { Icono } from '../../componentes/Icono';
import { Miniatura, Modal, olvidarImagen, Tabla, useDatos } from '../../componentes/Piezas';

interface Fila { productoId: number; cantidad: number; unidad: 'kg' | 'g' }
const MAX_IMAGEN = 1024 * 1024;

/**
 * V06 · Nuevo plato / Editar plato: POST /api/platos o PUT /api/platos/{id}, y si se eligió una foto,
 * PUT /api/platos/{id}/imagen (va al almacén de objetos, DEC-67). Los productos de los ingredientes son
 * los de ms-menu (GET /api/platos/productos), agrupados por su categoría.
 */
export function VentanaPlato({ plato, alCerrar, alGuardar }: { plato: Plato | null; alCerrar: () => void; alGuardar: (p: Plato, esNuevo: boolean) => void }) {
  const productos = useDatos(platos.productos);
  const [nombre, setNombre] = useState(plato?.nombre ?? '');
  const [descripcion, setDescripcion] = useState(plato?.descripcion ?? '');
  const [categoria, setCategoria] = useState(plato?.categoria ?? '');
  const [precio, setPrecio] = useState(plato ? String(plato.precioVenta) : '');
  const [filas, setFilas] = useState<Fila[]>(plato?.ingredientes.map((g) => ({ productoId: g.productoId, cantidad: g.cantidad, unidad: g.unidad === 'g' ? 'g' : 'kg' })) ?? []);
  const [ingCat, setIngCat] = useState('');
  const [ingProd, setIngProd] = useState('');
  const [ingCant, setIngCant] = useState('');
  const [ingUnidad, setIngUnidad] = useState<'kg' | 'g'>('kg');
  const [foto, setFoto] = useState<File | null>(null);
  const [vista, setVista] = useState<string | null>(null);
  const [error, setError] = useState('');
  const [enviando, setEnviando] = useState(false);

  const lista = productos.datos ?? [];
  const cats = [...new Set(lista.map((p) => p.categoria))];
  const deCategoria = lista.filter((p) => p.categoria === ingCat);
  const producto = (id: number) => lista.find((p) => p.id === id);

  useEffect(() => () => { if (vista) URL.revokeObjectURL(vista); }, [vista]);
  useEffect(() => { setIngProd(deCategoria[0] ? String(deCategoria[0].id) : ''); }, [ingCat, productos.datos]); // eslint-disable-line react-hooks/exhaustive-deps

  const elegirFoto = (f: File | undefined) => {
    if (!f) return;
    if (!['image/png', 'image/jpeg', 'image/webp'].includes(f.type)) { setError('La imagen debe ser JPG, PNG o WebP.'); return; }
    if (f.size > MAX_IMAGEN) { setError('La imagen supera 1 MB.'); return; }
    setError('');
    setFoto(f);
    setVista(URL.createObjectURL(f));
  };

  const agregarIngrediente = () => {
    const id = Number(ingProd), cantidad = Number(ingCant.replace(',', '.'));
    if (!id || !(cantidad > 0)) { setError('Elige un producto y una cantidad mayor que cero.'); return; }
    setError('');
    setFilas((f) => [...f, { productoId: id, cantidad, unidad: ingUnidad }]);
    setIngCant('');
  };

  const guardar = async () => {
    if (!nombre.trim() || !categoria || !(Number(precio) > 0)) { setError('Completa el nombre, la categoría y el precio de venta.'); return; }
    const datos: DatosPlato = { nombre: nombre.trim(), categoria, descripcion: descripcion.trim(), precioVenta: Number(precio), ingredientes: filas };
    setEnviando(true);
    setError('');
    try {
      const guardado = plato ? await platos.editar(plato.id, datos) : await platos.crear(datos);
      if (foto) {
        try {
          await platos.subirImagen(guardado.id, foto);
          olvidarImagen(guardado.id);
        } catch (e) {
          setError(`El plato se guardó, pero la imagen no: ${e instanceof Error ? e.message : ''}`);
          setEnviando(false);
          return;
        }
      }
      alGuardar(guardado, !plato);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'No se pudo guardar el plato.');
      setEnviando(false);
    }
  };

  return (
    <Modal clase="modal lg" alCerrar={alCerrar}>
      <header className="m-head"><span className="m-ico"><Icono nombre="utensils" /></span><h2>{plato ? 'Editar plato' : 'Nuevo plato'}</h2>
        <button type="button" className="icon-btn m-close" onClick={alCerrar} aria-label="Cerrar"><Icono nombre="x" /></button>
      </header>
      <p className="m-sub">{plato ? 'Modifica los datos del plato; el cambio llega al catálogo del mesero y a la receta del inventario.' : 'Completa la información para crear un nuevo plato en el menú.'}</p>
      <div className="plato-top">
        <div className="stack-sm">
          <label className="fl"><span>Nombre del plato <i>*</i></span>
            <input value={nombre} onChange={(e) => setNombre(e.target.value)} placeholder="Ej. Bandeja paisa" maxLength={120} /></label>
          <label className="fl">Descripción
            <textarea value={descripcion} onChange={(e) => setDescripcion(e.target.value)} maxLength={200} rows={3}
              placeholder="Describe brevemente el plato, sus características, preparación, etc." />
            <small className="counter">{descripcion.length}/200</small></label>
          <div className="frow two">
            <label className="fl"><span>Categoría <i>*</i></span>
              <select value={categoria} onChange={(e) => setCategoria(e.target.value)}>
                <option value="">Selecciona una categoría</option>
                {CATEGORIAS_CARTA.map((c) => <option key={c}>{c}</option>)}
              </select></label>
            <label className="fl"><span>Precio de venta <i>*</i></span>
              <input type="number" min={0} step={100} value={precio} onChange={(e) => setPrecio(e.target.value)} placeholder="$ 0" /></label>
          </div>
        </div>
        <label className="upload">
          {vista ? <img src={vista} alt="Vista previa" />
            : plato?.imagen ? <Miniatura nombre={plato.nombre} clase="hero" platoId={plato.id} tiene />
              : <><Icono nombre="image" /><b>Subir imagen del plato</b><small>JPG, PNG o WebP (máx. 1 MB)</small>
                <span className="btn outline sm"><Icono nombre="upload" /> Elegir archivo</span></>}
          <input type="file" accept="image/png,image/jpeg,image/webp" hidden onChange={(e) => elegirFoto(e.target.files?.[0])} aria-label="Imagen del plato" />
        </label>
      </div>
      <section className="m-sec ing">
        <div className="sub-head"><h3><Icono nombre="utensils" /> Ingredientes</h3><small>Agrega los ingredientes que componen el plato.</small></div>
        <div className="frow four">
          <label>Categoría<select value={ingCat} onChange={(e) => setIngCat(e.target.value)}>
            <option value="">Selecciona</option>{cats.map((c) => <option key={c}>{c}</option>)}</select></label>
          <label>Producto<select value={ingProd} onChange={(e) => setIngProd(e.target.value)}>
            {deCategoria.length ? deCategoria.map((p) => <option key={p.id} value={p.id}>{p.nombre}</option>) : <option value="">Selecciona un producto</option>}</select></label>
          <label>Cantidad<input type="number" min={0} step="any" value={ingCant} onChange={(e) => setIngCant(e.target.value)} placeholder="0.00" /></label>
          <label>Unidad<select value={ingUnidad} onChange={(e) => setIngUnidad(e.target.value as 'kg' | 'g')}><option value="kg">kg</option><option value="g">g</option></select></label>
        </div>
        <button type="button" className="btn primary sm push-r" onClick={agregarIngrediente}><Icono nombre="plus" /> Agregar ingrediente</button>
        {error && <p className="form-error" role="alert">{error}</p>}
        <h3 className="mt">Ingredientes agregados ({filas.length})</h3>
        {filas.length ? (
          <Tabla cabeceras={['Categoría', 'Producto', 'Cantidad', 'Unidad', 'Acción']}>
            {filas.map((g, k) => (
              <tr key={k}><td>{producto(g.productoId)?.categoria ?? '—'}</td><td>{producto(g.productoId)?.nombre ?? `Producto ${g.productoId}`}</td>
                <td>{num(g.cantidad)}</td><td>{g.unidad}</td>
                <td><button type="button" className="icon-btn danger" aria-label="Quitar" onClick={() => setFilas((f) => f.filter((_, j) => j !== k))}><Icono nombre="trash" /></button></td></tr>
            ))}
          </Tabla>
        ) : (
          <div className="empty-box"><Icono nombre="bowl" /><b>Aún no has agregado ingredientes</b><small>Usa el formulario de arriba para añadir ingredientes al plato.</small></div>
        )}
      </section>
      <footer className="m-foot">
        <button type="button" className="btn ghost" onClick={alCerrar}>Cancelar</button>
        <button type="button" className="btn primary" disabled={enviando} onClick={() => void guardar()}><Icono nombre="file" /> {plato ? 'Guardar cambios' : 'Guardar plato'}</button>
      </footer>
    </Modal>
  );
}
