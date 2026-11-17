import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { inventario, platos } from '../../api/servicios';
import { Encabezado } from '../../componentes/Encabezado';
import { num, plural } from '../../componentes/formato';
import { Icono } from '../../componentes/Icono';
import { ErrorCarga, Kpi, Miniatura, Tabla, useAviso, useDatos } from '../../componentes/Piezas';
import { Tarjeta } from '../../componentes/Tarjeta';
import { useSesion } from '../../sesion/Sesion';
import { NOMBRE_ROL } from '../../sesion/token';

/**
 * V07 · Inventario: GET /api/inventario/insumos (ms-inventario). La categoría de cada insumo es la del
 * producto en ms-menu. «Cierre del día» adelanta el ConsumoDiarioJob de las 23:59 (solo en la demo, DEC-68).
 */
export function Inventario() {
  const { usuario } = useSesion();
  const navegar = useNavigate();
  const avisar = useAviso();
  const insumos = useDatos(inventario.insumos);
  const productos = useDatos(platos.productos);
  const [buscar, setBuscar] = useState('');
  const [cat, setCat] = useState('');
  const [cerrando, setCerrando] = useState(false);
  const categoria = (id: number) => productos.datos?.find((p) => p.id === id)?.categoria ?? '—';
  const lista = insumos.datos ?? [];
  const bajos = lista.filter((i) => i.bajoUmbral);
  const cats = [...new Set(lista.map((i) => categoria(i.productoId)))];
  const filtro = buscar.trim().toLowerCase();
  const visibles = lista.filter((i) => (!cat || categoria(i.productoId) === cat)
    && (!filtro || `${i.nombre} ${categoria(i.productoId)}`.toLowerCase().includes(filtro)));

  const cierre = async () => {
    setCerrando(true);
    try {
      await inventario.cierre();
      const alertas = await inventario.alertas();
      insumos.recargar();
      const n = alertas.length;
      avisar(`Cierre ejecutado: ${n ? plural(n, 'insumo quedó', 'insumos quedaron') + ' bajo el 25 %' : 'ningún insumo quedó bajo el 25 %'}.`);
    } catch (e) {
      avisar(e instanceof Error ? e.message : 'No se pudo ejecutar el cierre.', true);
    } finally {
      setCerrando(false);
    }
  };

  if (!usuario) return null;
  return (
    <>
      <Encabezado saludoA={usuario.nombre} sub={`${NOMBRE_ROL[usuario.rol]} · ${usuario.sucursal}`} />
      <div className="page">
        <ErrorCarga mensaje={insumos.error || productos.error} />
        <div className="kpis three">
          <Kpi icono="box" valor={lista.length} etiqueta="Productos en inventario" />
          <Kpi icono="alert" valor={bajos.length} etiqueta="Stock bajo" tono="danger" />
          <Kpi icono="circleCheck" valor={lista.length - bajos.length} etiqueta="Stock normal" tono="ok" />
        </div>
        <Tarjeta titulo="Inventario de insumos" clase="underline" extra={
          <div className="toolbar">
            <label className="search sm"><Icono nombre="search" />
              <input value={buscar} onChange={(e) => setBuscar(e.target.value)} placeholder="Buscar ingrediente, categoría..." aria-label="Buscar insumo" />
            </label>
            <select value={cat} onChange={(e) => setCat(e.target.value)} aria-label="Categoría">
              <option value="">Todas las categorías</option>
              {cats.map((c) => <option key={c}>{c}</option>)}
            </select>
            <button type="button" className="btn ghost sm" disabled={cerrando} onClick={() => void cierre()}
              title="Ejecuta ConsumoDiarioJob: descuenta el consumo del día y genera las alertas"><Icono nombre="refresh" /> Cierre del día (23:59)</button>
          </div>}>
          <Tabla cabeceras={['Imagen', 'Nombre del insumo', 'Categoría', 'Stock actual', 'Unidad', 'Stock mínimo', 'Estado', 'Acción']}>
            {visibles.map((i) => (
              <tr key={i.productoId}>
                <td><Miniatura nombre={i.nombre} clase="sm" /></td><td><b>{i.nombre}</b></td><td>{categoria(i.productoId)}</td>
                <td>{num(i.stockActual)}</td><td>{i.unidad}</td><td>{num(i.minimo)}</td>
                <td>{i.bajoUmbral ? <span className="badge rojo">Stock bajo</span> : <span className="badge verde">Normal</span>}</td>
                <td><button type="button" className="btn outline sm" onClick={() => navegar(`/solicitudes/nueva?producto=${i.productoId}`)}><Icono nombre="cart" /> Solicitar</button></td>
              </tr>
            ))}
          </Tabla>
          <p className="note-foot"><Icono nombre="info" /> Stock mínimo: 25 % del stock de referencia. El consumo de los pedidos se descuenta en el cierre diario de las 23:59.</p>
        </Tarjeta>
      </div>
    </>
  );
}
