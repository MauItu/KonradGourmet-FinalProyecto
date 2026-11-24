import { useEffect, useState } from 'react';
import { analitica, type Sugerencia } from '../../api/servicios';
import { Encabezado } from '../../componentes/Encabezado';
import { Icono } from '../../componentes/Icono';
import { Insignia, Kpi, Tabla, useAviso, usePagina } from '../../componentes/Piezas';
import { Tarjeta } from '../../componentes/Tarjeta';
import { fecha, hoyISO, plural } from '../../componentes/formato';

/**
 * Promociones sugeridas (HU-19, CU-06; mockup en mockups_faltantes/promociones_sugeridas): el histórico de lo que el análisis semanal sugiere al jefe de
 * cocina —tendencias en redes y los 3 platos más pedidos de la semana (PD-04)—, con producto, motivo y vigencia, y
 * «Aceptar» o «Descartar» mientras esté sugerida (GET y PATCH /api/bam/promociones, ms-analitica). Sin fechas, todo
 * el histórico; con fechas, las sugerencias vigentes en algún día del rango.
 */
export function Promociones() {
  const avisar = useAviso();
  const [desde, setDesde] = useState('');
  const [hasta, setHasta] = useState('');
  const [sugerencias, setSugerencias] = useState<Sugerencia[] | null>(null);
  const [periodo, setPeriodo] = useState('');
  const [error, setError] = useState('');
  const [ocupado, setOcupado] = useState<number | 'buscar' | null>(null);
  const { filas, pie, irA } = usePagina(sugerencias ?? [], 10);

  const buscar = async (d = desde, h = hasta, pagina: number | null = 1) => {
    setOcupado('buscar');
    try {
      setSugerencias(await analitica.promociones(d || undefined, h || undefined));
      setPeriodo(d && h ? (d === h ? `vigentes el ${fecha(d)}` : `vigentes entre el ${fecha(d)} y el ${fecha(h)}`) : 'todo el histórico');
      setError('');
      if (pagina) irA(pagina);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'No se pudieron consultar las promociones sugeridas.');
    } finally {
      setOcupado(null);
    }
  };
  useEffect(() => { void buscar(); }, []); // eslint-disable-line react-hooks/exhaustive-deps
  const todo = () => { setDesde(''); setHasta(''); void buscar('', ''); };

  const resolver = async (s: Sugerencia, aceptar: boolean) => {
    setOcupado(s.id);
    try {
      await (aceptar ? analitica.aceptar(s.id) : analitica.descartar(s.id));
      avisar(aceptar ? `Aceptaste la promoción de ${s.producto}.` : `Descartaste la promoción de ${s.producto}.`);
    } catch (e) {
      avisar(e instanceof Error ? e.message : 'No se pudo guardar tu decisión.', true);
    }
    await buscar(desde, hasta, null);
  };

  const hoy = hoyISO();
  const vigentes = (sugerencias ?? []).filter((s) => s.estado === 'SUGERIDA' && s.vigenciaHasta >= hoy).length;
  const aceptadas = (sugerencias ?? []).filter((s) => s.estado === 'ACEPTADA').length;
  const descartadas = (sugerencias ?? []).filter((s) => s.estado === 'DESCARTADA').length;

  return (
    <>
      <Encabezado titulo="Promociones sugeridas" sub="Productos destacados del análisis semanal de redes sociales y pedidos" icono="tag" />
      <div className="page">
        <div className="kpis three soft">
          <Kpi icono="tag" valor={vigentes} etiqueta="Por revisar (vigentes)" />
          <Kpi icono="circleCheck" valor={aceptadas} etiqueta="Aceptadas" tono="ok" />
          <Kpi icono="circleX" valor={descartadas} etiqueta="Descartadas" />
        </div>
        <section className="card filters compact">
          <label>Desde<input type="date" value={desde} onChange={(e) => setDesde(e.target.value)} aria-label="Desde" /></label>
          <label>Hasta<input type="date" value={hasta} onChange={(e) => setHasta(e.target.value)} aria-label="Hasta" /></label>
          <div className="filter-btns">
            <button type="button" className="btn outline sm" disabled={ocupado !== null} onClick={() => void buscar()}><Icono nombre="search" /> Consultar</button>
            <button type="button" className="btn ghost sm" disabled={ocupado !== null} onClick={todo}><Icono nombre="calendar" /> Todo el histórico</button>
          </div>
          {error && <p className="form-error" role="alert">{error}</p>}
        </section>
        <Tarjeta titulo="Histórico de promociones sugeridas" icono="tag"
          sub={sugerencias ? `${plural(sugerencias.length, 'sugerencia', 'sugerencias')} · ${periodo} · las más recientes primero` : 'Cargando…'}>
          <p className="promo-nota"><Icono nombre="info" /> Cada semana el sistema revisa las tendencias en redes sociales y los 3 platos
            más pedidos de los 7 días anteriores. Cada sugerencia vale 7 días y, cuando hay nuevas, te llega un correo (máximo uno al día).</p>
          <Tabla cabeceras={['Producto', 'Motivo', 'Vigencia', 'Estado', 'Acción']} vacio={sugerencias ? 'No hay promociones sugeridas para el periodo seleccionado.' : 'Cargando…'}>
            {filas.map((s) => (
              <tr key={s.id}>
                <td><b>{s.producto}</b></td>
                <td>{s.motivo}</td>
                <td className="nowrap">{fecha(s.vigenciaDesde)} – {fecha(s.vigenciaHasta)}</td>
                <td><Insignia estado={s.estado} punto /></td>
                <td>
                  {s.estado === 'SUGERIDA' ? (
                    <div className="promo-acciones">
                      <button type="button" className="btn primary sm" disabled={ocupado !== null} onClick={() => void resolver(s, true)}
                        aria-label={`Aceptar ${s.producto}`}><Icono nombre="check" /> Aceptar</button>
                      <button type="button" className="btn ghost sm" disabled={ocupado !== null} onClick={() => void resolver(s, false)}
                        aria-label={`Descartar ${s.producto}`}><Icono nombre="x" /> Descartar</button>
                    </div>
                  ) : <span className="muted">—</span>}
                </td>
              </tr>
            ))}
          </Tabla>
          {pie}
        </Tarjeta>
      </div>
    </>
  );
}
