import { useState } from 'react';
import { ordenes, type Cotizacion, type OrdenCompra } from '../api/servicios';
import { abrirArchivo, OkIcono, Pares } from './Cotizaciones';
import { fecha, money, num } from './formato';
import { Icono } from './Icono';
import { Cerrar, Insignia, Modal, Tabla, VacioDetalle } from './Piezas';

// Piezas de las órdenes de compra (ms-ordenes), con el marcado de app.js para que valgan los estilos de Codex.

/** «Ver PDF» o «Descargar» de V23 (DEC-75): el PDF de la orden sale del almacén de objetos, pedido con el token. */
export function abrirPdfOrden(numero: string, descargar: boolean, avisar: (m: string, error?: boolean) => void) {
  return abrirArchivo(() => ordenes.pdf(numero, descargar), numero, `La orden ${numero} no tiene PDF.`, descargar, avisar);
}

/**
 * V21 · Generar orden de compra: la cotización OPCIONADA escogida en V20, con sus solicitudes, el proveedor,
 * los productos con su valor y el total. Al confirmar, ms-ordenes la genera (POST /api/ordenes-compra).
 */
export function VentanaOrden({ c, alCerrar, alGenerar }: { c: Cotizacion; alCerrar: () => void; alGenerar: (o: OrdenCompra) => void }) {
  const [error, setError] = useState('');
  const [enviando, setEnviando] = useState(false);
  const confirmar = async () => {
    setEnviando(true);
    try {
      alGenerar(await ordenes.generar(c.numero));
    } catch (e) {
      setError(e instanceof Error ? e.message : 'No se pudo generar la orden de compra.');
      setEnviando(false);
    }
  };
  return (
    <Modal clase="modal md" alCerrar={alCerrar}>
      <header className="m-head"><span className="m-ico"><Icono nombre="receipt" /></span><h2>Generar orden de compra</h2><Cerrar alCerrar={alCerrar} /></header>
      <div className="info-box three">
        <div><small>Cotización:</small><b>{c.numero}</b></div>
        <div><small>{c.solicitudes.length > 1 ? 'Solicitudes:' : 'Solicitud:'}</small><b>{c.solicitudes.join(', ')}</b></div>
        <div><small>Proveedor:</small><b>{c.proveedor}</b></div>
      </div>
      <h3 className="m-h3">Productos</h3>
      <Tabla cabeceras={['Producto', 'Cantidad', 'Precio']}>
        {c.items.map((i) => (
          <tr key={i.productoId}><td>{i.producto}</td><td>{num(i.cantidad)} {i.unidad}</td>
            <td>{money(i.precioProveedor != null ? i.precioProveedor * i.cantidad : null)}</td></tr>
        ))}
      </Tabla>
      <div className="total-row peach"><span>Total</span><b>{money(c.cotizado)}</b></div>
      {error && <p className="form-error" role="alert">{error}</p>}
      <footer className="m-foot">
        <button type="button" className="btn ghost" onClick={alCerrar}>Cancelar</button>
        <button type="button" className="btn primary" disabled={enviando} onClick={() => void confirmar()}>Confirmar orden de compra</button>
      </footer>
    </Modal>
  );
}

/** V22 · Orden generada: el número consecutivo, el proveedor, la cotización y el total. */
export function OrdenGenerada({ o, alVer, alInicio, alCerrar }: { o: OrdenCompra; alVer: () => void; alInicio: () => void; alCerrar: () => void }) {
  return (
    <Modal clase="ticket-modal" alCerrar={alCerrar}>
      <Cerrar alCerrar={alCerrar} />
      <div className="ticket">
        <OkIcono />
        <h2 className="caps">Orden generada</h2>
        <div className="code-big dark">{o.numero}</div>
        <Pares pares={[['Proveedor:', <b key="p">{o.proveedor}</b>], ['Cotización:', <b key="c">{o.cotizacion}</b>]]} />
        <div className="total-row peach"><span>Total</span><b>{money(o.total)}</b></div>
        <button type="button" className="btn primary w" onClick={alVer}>VER ORDEN</button>
        <button type="button" className="btn outline w" onClick={alInicio}>VOLVER AL INICIO</button>
      </div>
    </Modal>
  );
}

/** El detalle de V23: cabecera de la orden, datos, siguiente paso, productos, total y su PDF (DEC-75). */
export function DetalleOrden({ o, alVerPdf, alDescargar }: { o: OrdenCompra | null; alVerPdf: () => void; alDescargar: () => void }) {
  if (!o) return <VacioDetalle texto="Aún no hay órdenes de compra." />;
  return (
    <>
      <div className="oc-head">
        <div><small>ORDEN DE COMPRA</small><h3>{o.numero}</h3><span>Fecha: {fecha(o.fecha)}</span></div>
        <div><small>Estado</small><Insignia estado={o.estado} mayus /></div>
      </div>
      <Pares pares={[['Proveedor', <b key="p">{o.proveedor}</b>], [o.solicitudes.length > 1 ? 'Solicitudes' : 'Solicitud', o.solicitudes.join(', ')],
        ['Cotización', o.cotizacion]]} />
      {o.estado === 'EN_PROCESO' && (
        <div className="next-step"><Icono nombre="alert" /><div><b>Siguiente paso</b>
          <p>La orden está en proceso. Debe continuarse con la gestión de compra, seguimiento al proveedor y verificación de entrega.</p></div></div>
      )}
      <h4 className="sub-title"><Icono nombre="box" /> Productos</h4>
      <Tabla cabeceras={['Producto', 'Cantidad', 'Unidad', 'Precio']}>
        {o.items.map((i) => <tr key={i.productoId}><td>{i.producto}</td><td>{num(i.cantidad)}</td><td>{i.unidad}</td><td>{money(i.valor)}</td></tr>)}
      </Tabla>
      <div className="total-row big"><span>TOTAL:</span><b>{money(o.total)}</b></div>
      {o.pdf && (
        <div className="ticket-acciones dos">
          <button type="button" className="btn outline sm w" onClick={alVerPdf}><Icono nombre="file" /> Ver PDF</button>
          <button type="button" className="btn outline sm w" onClick={alDescargar}><Icono nombre="download" /> Descargar</button>
        </div>
      )}
    </>
  );
}
