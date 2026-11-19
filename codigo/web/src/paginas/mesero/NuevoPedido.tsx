import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { pedidos, type Pedido, type PlatoCarta } from '../../api/servicios';
import { Encabezado } from '../../componentes/Encabezado';
import { CATEGORIAS_CARTA, fecha, hora, mesaTxt, money, pad2 } from '../../componentes/formato';
import { Icono } from '../../componentes/Icono';
import { Cerrar, ErrorCarga, Miniatura, Modal, useAviso, useDatos } from '../../componentes/Piezas';
import { useSesion } from '../../sesion/Sesion';
import { NOMBRE_ROL } from '../../sesion/token';

interface Linea { cantidad: number; nota: string }

/**
 * V03 · Nuevo pedido: el catálogo vigente de la sucursal (GET /api/pedidos/catalogo), las mesas
 * activas (GET …/mesas) y el pedido actual; «Registrar pedido» hace POST /api/pedidos y abre V04.
 * El precio que se envía es el que el mesero vio: si el menú cambió entre tanto, ms-pedidos lo rechaza.
 */
export function NuevoPedido() {
  const { usuario } = useSesion();
  const navegar = useNavigate();
  const avisar = useAviso();
  const carta = useDatos(pedidos.catalogo);
  const mesas = useDatos(pedidos.mesas);
  const [categoria, setCategoria] = useState('Todos');
  const [buscar, setBuscar] = useState('');
  const [mesa, setMesa] = useState<number | null>(null);
  const [carrito, setCarrito] = useState<Record<number, Linea> | null>(null);
  const [porTarjeta, setPorTarjeta] = useState<Record<number, number>>({});
  const [notas, setNotas] = useState<Record<number, string>>({});
  const [enviando, setEnviando] = useState(false);
  const [registrado, setRegistrado] = useState<Pedido | null>(null);
  const platos: PlatoCarta[] = carta.datos ?? [];

  // La primera vez del turno trae precargado el pedido de la demo (2 bandejas paisas «Sin arepa» y 1 ajiaco
  // = $82.000), igual que la interfaz en memoria; la mesa 5, si está activa.
  useEffect(() => {
    if (carrito !== null || !carta.datos) return;
    void pedidos.delTurno().then((hechos) => {
      const inicial: Record<number, Linea> = {};
      if (!hechos.length) {
        const id = (n: string) => carta.datos!.find((p) => p.nombre === n)?.platoId;
        const bandeja = id('Bandeja paisa'), ajiaco = id('Ajiaco');
        if (bandeja) inicial[bandeja] = { cantidad: 2, nota: 'Sin arepa' };
        if (ajiaco) inicial[ajiaco] = { cantidad: 1, nota: '' };
      }
      setCarrito((c) => c ?? inicial);
    }).catch(() => setCarrito((c) => c ?? {}));
  }, [carta.datos, carrito]);
  useEffect(() => {
    if (mesa === null && mesas.datos?.length) setMesa(mesas.datos.some((m) => m.numero === 5) ? 5 : mesas.datos[0].numero);
  }, [mesas.datos, mesa]);

  const visibles = platos.filter((p) => (categoria === 'Todos' || p.categoria === categoria)
    && (!buscar.trim() || p.nombre.toLowerCase().includes(buscar.trim().toLowerCase())));
  const items = Object.entries(carrito ?? {}).map(([id, l]) => ({ p: platos.find((x) => x.platoId === Number(id)), ...l }))
    .filter((x): x is { p: PlatoCarta } & Linea => !!x.p);
  const totalPlatos = items.reduce((a, x) => a + x.cantidad, 0);
  const totalPagar = items.reduce((a, x) => a + x.cantidad * x.p.precio, 0);

  const agregar = (p: PlatoCarta) => {
    const nota = (notas[p.platoId] ?? '').trim();
    setCarrito((c) => {
      const actual = c?.[p.platoId] ?? { cantidad: 0, nota: '' };
      return { ...c, [p.platoId]: { cantidad: actual.cantidad + (porTarjeta[p.platoId] ?? 1), nota: nota || actual.nota } };
    });
    setPorTarjeta((t) => ({ ...t, [p.platoId]: 1 }));
    setNotas((n) => ({ ...n, [p.platoId]: '' }));
  };
  const cambiar = (id: number, d: number) => setCarrito((c) => {
    const l = c?.[id];
    if (!l) return c;
    const siguiente = { ...c };
    if (l.cantidad + d <= 0) delete siguiente[id]; else siguiente[id] = { ...l, cantidad: l.cantidad + d };
    return siguiente;
  });
  const quitar = (id: number) => setCarrito((c) => { const s = { ...c }; delete s[id]; return s; });

  const registrar = async () => {
    if (mesa === null || !items.length) return;
    setEnviando(true);
    try {
      const p = await pedidos.registrar(mesa, items.map((x) => ({ platoId: x.p.platoId, cantidad: x.cantidad, nota: x.nota, precioUnitario: x.p.precio })));
      setCarrito({});
      setRegistrado(p);
    } catch (e) {
      avisar(e instanceof Error ? e.message : 'No se pudo registrar el pedido.', true);
      carta.recargar();
    } finally {
      setEnviando(false);
    }
  };
  const cerrarV04 = useCallback(() => setRegistrado(null), []);

  if (!usuario) return null;
  return (
    <>
      <Encabezado saludoA={usuario.nombre} sub={`${NOMBRE_ROL[usuario.rol]} · ${usuario.sucursal}`} />
      <div className="page">
        <ErrorCarga mensaje={carta.error || mesas.error} />
        <div className="grid pos">
          <section className="card catalog">
            <div className="catalog-bar">
              <label className="search"><Icono nombre="search" />
                <input value={buscar} onChange={(e) => setBuscar(e.target.value)} placeholder="Buscar plato, bebida o entrada..." aria-label="Buscar plato" />
              </label>
              <div className="chips">
                {['Todos', ...CATEGORIAS_CARTA].map((c) => (
                  <button type="button" key={c} className={`fchip ${categoria === c ? 'on' : ''}`} onClick={() => setCategoria(c)}>{c}</button>
                ))}
              </div>
            </div>
            <div className="dish-grid">
              {visibles.map((p) => (
                <article key={p.platoId} className="dish">
                  <Miniatura nombre={p.nombre} clase="dish-img" platoId={p.platoId} />
                  <h4>{p.nombre}</h4><p>{p.categoria}</p><b className="price">{money(p.precio)}</b>
                  <div className="stepper">
                    <button type="button" aria-label="Menos" onClick={() => setPorTarjeta((t) => ({ ...t, [p.platoId]: Math.max(1, (t[p.platoId] ?? 1) - 1) }))}><Icono nombre="minus" /></button>
                    <span>{porTarjeta[p.platoId] ?? 1}</span>
                    <button type="button" aria-label="Más" onClick={() => setPorTarjeta((t) => ({ ...t, [p.platoId]: (t[p.platoId] ?? 1) + 1 }))}><Icono nombre="plus" /></button>
                  </div>
                  <label className="note"><Icono nombre="pencil" />
                    <input value={notas[p.platoId] ?? ''} onChange={(e) => setNotas((n) => ({ ...n, [p.platoId]: e.target.value }))}
                      placeholder="Ej. Sin cebolla..." aria-label={`Nota para ${p.nombre}`} maxLength={200} />
                  </label>
                  <button type="button" className="btn primary sm w" onClick={() => agregar(p)}><Icono nombre="plus" /> Agregar</button>
                </article>
              ))}
              {carta.datos && !visibles.length && <p className="muted pad">No hay platos en esta categoría.</p>}
            </div>
          </section>
          <aside className="card order">
            <label className="mesa-sel"><Icono nombre="table" />
              <span><small>Mesa</small>
                <select value={mesa ?? ''} onChange={(e) => setMesa(Number(e.target.value))} aria-label="Mesa">
                  {(mesas.datos ?? []).map((m) => <option key={m.numero} value={m.numero}>{mesaTxt(m.numero)}</option>)}
                </select>
              </span>
            </label>
            <h3 className="order-title"><Icono nombre="clipboard" /> Pedido actual <span>({items.length})</span></h3>
            <div className="order-items">
              {items.length ? items.map((x) => (
                <div key={x.p.platoId} className="oi">
                  <Miniatura nombre={x.p.nombre} clase="sm" platoId={x.p.platoId} />
                  <div className="oi-main"><b>{x.p.nombre}</b><span className="price">{money(x.p.precio)}</span>
                    <div className="stepper sm">
                      <button type="button" aria-label="Menos" onClick={() => cambiar(x.p.platoId, -1)}><Icono nombre="minus" /></button>
                      <span>{x.cantidad}</span>
                      <button type="button" aria-label="Más" onClick={() => cambiar(x.p.platoId, 1)}><Icono nombre="plus" /></button>
                    </div>
                  </div>
                  <b className="oi-sub">{money(x.cantidad * x.p.precio)}</b>
                  <div className="oi-note"><Icono nombre="pencil" /><span>{x.nota || 'Sin notas'}</span>
                    <button type="button" className="icon-btn danger" onClick={() => quitar(x.p.platoId)} aria-label={`Quitar ${x.p.nombre}`}><Icono nombre="trash" /></button>
                  </div>
                </div>
              )) : <p className="muted pad">Agrega platos del catálogo.</p>}
            </div>
            <div className="order-tot">
              <div><Icono nombre="utensils" /><span>Total de platos:</span><b>{totalPlatos}</b></div>
              <div><Icono nombre="receipt" /><span>Total a pagar:</span><b className="big">{money(totalPagar)}</b></div>
            </div>
            <button type="button" className="btn ghost w" onClick={() => setCarrito({})}>Cancelar</button>
            <button type="button" className="btn primary w" disabled={!items.length || mesa === null || enviando} onClick={() => void registrar()}>
              <Icono nombre="check" /> Registrar pedido
            </button>
          </aside>
        </div>
      </div>
      {registrado && (
        <Modal clase="modal sm center" alCerrar={cerrarV04}>
          <Cerrar alCerrar={cerrarV04} />
          <div className="ok-icon verde"><span><Icono nombre="check" /></span></div>
          <h2>Pedido registrado</h2>
          <div className="code-pill">{registrado.numero}</div>
          <div className="facts four boxed">
            <div><Icono nombre="table" /><small>Mesa:</small><b>{pad2(registrado.mesa)}</b></div>
            <div><Icono nombre="utensils" /><small>Platos:</small><b>{registrado.platos}</b></div>
            <div><Icono nombre="calendar" /><small>Fecha:</small><b>{fecha(registrado.fecha)}</b></div>
            <div><Icono nombre="clock" /><small>Hora:</small><b>{hora(registrado.fecha)}</b></div>
          </div>
          <div className="m-actions">
            <button type="button" className="btn ghost" onClick={() => navegar(`/pedidos?sel=${registrado.numero}`)}>Ver pedido</button>
            <button type="button" className="btn primary" onClick={cerrarV04}><Icono nombre="plus" /> Nuevo pedido</button>
          </div>
        </Modal>
      )}
    </>
  );
}
