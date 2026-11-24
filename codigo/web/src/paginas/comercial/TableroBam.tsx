import { useEffect, useState } from 'react';
import { analitica, type Indicador } from '../../api/servicios';
import { Encabezado } from '../../componentes/Encabezado';
import { Icono } from '../../componentes/Icono';
import { fecha, num, pct, SUCURSALES } from '../../componentes/formato';

const SIN_DATOS = 'No hay información suficiente para el periodo seleccionado';

/** Cómo se lee cada indicador: su ícono, la unidad del valor y qué dice la gráfica. */
const LECTURA = [
  { icono: 'utensils', unidad: (v: number) => `${num(v)} platos vendidos`, grafica: 'Los 5 platos más pedidos' },
  { icono: 'store', unidad: (v: number) => `${num(v)} platos vendidos`, grafica: 'Platos vendidos por sucursal' },
  { icono: 'scale', unidad: (v: number) => `${pct(v)} frente al precio estándar, en promedio`, grafica: 'Diferencia promedio contra el precio estándar' },
];

/**
 * Tablero BAM (HU-18, CU-14; mockup en mockups_faltantes/tablero_bam): los tres indicadores del director comercial con su valor y su gráfica
 * (GET /api/bam/kpis, ms-analitica). Sin fechas, la demanda es del último mes y el proveedor del último año; con
 * fechas, los tres usan el rango; la sucursal filtra solo la demanda (PD-03). Cada tarjeta dice el periodo aplicado.
 */
export function TableroBam() {
  const [desde, setDesde] = useState('');
  const [hasta, setHasta] = useState('');
  const [sucursal, setSucursal] = useState('');
  const [indicadores, setIndicadores] = useState<Indicador[] | null>(null);
  const [error, setError] = useState('');
  const [buscando, setBuscando] = useState(false);
  const [aplicado, setAplicado] = useState({ rango: false, sucursal: '' });
  const [hora, setHora] = useState('');

  const consultar = async (d = desde, h = hasta, s = sucursal) => {
    setBuscando(true);
    try {
      setIndicadores(await analitica.kpis({ desde: d || undefined, hasta: h || undefined, sucursalId: s ? Number(s) : undefined }));
      setAplicado({ rango: Boolean(d || h), sucursal: s });
      setHora(new Date().toLocaleTimeString('es-CO', { hour: 'numeric', minute: '2-digit' }));
      setError('');
    } catch (e) {
      setError(e instanceof Error ? e.message : 'No se pudo consultar el tablero.');
    } finally {
      setBuscando(false);
    }
  };
  useEffect(() => { void consultar(); }, []); // eslint-disable-line react-hooks/exhaustive-deps
  const periodosFijos = () => { setDesde(''); setHasta(''); void consultar('', '', sucursal); };
  const nombreSucursal = SUCURSALES.find((x) => String(x.id) === aplicado.sucursal)?.nombre;

  return (
    <>
      <Encabezado titulo="Tablero BAM" sub="Indicadores del negocio para decidir compras y negociaciones" icono="chart" />
      <div className="page">
        <section className="card filters compact">
          <label>Desde<input type="date" value={desde} onChange={(e) => setDesde(e.target.value)} aria-label="Desde" /></label>
          <label>Hasta<input type="date" value={hasta} onChange={(e) => setHasta(e.target.value)} aria-label="Hasta" /></label>
          <label>Sucursal<select value={sucursal} onChange={(e) => setSucursal(e.target.value)} aria-label="Sucursal">
            <option value="">Todas</option>{SUCURSALES.map((s) => <option key={s.id} value={s.id}>{s.nombre}</option>)}</select></label>
          <div className="filter-btns">
            <button type="button" className="btn outline sm" disabled={buscando} onClick={() => void consultar()}><Icono nombre="search" /> Consultar</button>
            <button type="button" className="btn ghost sm" disabled={buscando} onClick={periodosFijos}><Icono nombre="calendar" /> Restablecer periodo</button>
          </div>
          {error && <p className="form-error" role="alert">{error}</p>}
        </section>
        <p className="bam-nota">
          {aplicado.rango
            ? 'Los tres indicadores usan el rango elegido.'
            : 'Sin fechas: la demanda es de los últimos 30 días y el proveedor, de los últimos 365.'}
          {nombreSucursal ? ` La sucursal (${nombreSucursal}) filtra solo la demanda: las compras son centrales.` : ''}
          {hora && <span> Consultado a las {hora}</span>}
        </p>
        <div className="bam-grid">
          {(indicadores ?? []).map((k, i) => <TarjetaIndicador key={k.kpi} k={k} lectura={LECTURA[i] ?? LECTURA[0]} precio={i === 2} />)}
        </div>
      </div>
    </>
  );
}

function TarjetaIndicador({ k, lectura, precio }: { k: Indicador; lectura: (typeof LECTURA)[number]; precio: boolean }) {
  const periodo = k.desde === k.hasta ? `El ${fecha(k.desde)}` : `Del ${fecha(k.desde)} al ${fecha(k.hasta)}`;
  return (
    <section className="card bam-kpi" aria-label={k.kpi}>
      <div className="card-head">
        <div><h2>{k.kpi}</h2><p>{periodo}</p></div>
        <span className="kpi-ico"><Icono nombre={lectura.icono} /></span>
      </div>
      {k.ganador && k.valor != null ? (
        <>
          <div className="bam-valor">
            <strong>{k.ganador}</strong>
            <span>{lectura.unidad(k.valor)}</span>
          </div>
          <p className="bam-grafica-titulo">{lectura.grafica}</p>
          {precio ? <BarrasDiferencia puestos={k.ranking} /> : <Barras puestos={k.ranking} />}
        </>
      ) : (
        <div className="empty-detail bam-vacio"><Icono nombre="chart" /><p>{SIN_DATOS}</p></div>
      )}
    </section>
  );
}

/** Barras horizontales desde cero, de mayor a menor; el ganador va en el color principal. */
function Barras({ puestos }: { puestos: { clave: string; valor: number }[] }) {
  const max = Math.max(...puestos.map((p) => p.valor), 1);
  return (
    <ul className="bam-barras">
      {puestos.map((p, i) => (
        <li key={p.clave} title={`${p.clave}: ${num(p.valor)}`}>
          <span className="bam-etiqueta">{p.clave}</span>
          <span className="bam-pista"><i className={i === 0 ? 'gana' : ''} style={{ width: `${Math.max(2, (p.valor / max) * 100)}%` }} /></span>
          <span className="bam-cifra">{num(p.valor)}</span>
        </li>
      ))}
    </ul>
  );
}

/**
 * La diferencia promedio de cada proveedor contra el precio estándar: el eje es el 0 % (el precio estándar); a la
 * izquierda, por debajo (mejor), y a la derecha, por encima. El primero, el de la diferencia más baja, es el ganador.
 */
function BarrasDiferencia({ puestos }: { puestos: { clave: string; valor: number }[] }) {
  const max = Math.max(...puestos.map((p) => Math.abs(p.valor)), 1);
  return (
    <>
      <ul className="bam-barras diferencia">
        {puestos.map((p, i) => {
          const ancho = `${Math.max(2, (Math.abs(p.valor) / max) * 50)}%`;
          return (
            <li key={p.clave} title={`${p.clave}: ${pct(p.valor)}`}>
              <span className="bam-etiqueta">{p.clave}</span>
              <span className="bam-pista eje">
                <i className={i === 0 ? 'gana' : ''} style={p.valor < 0 ? { right: '50%', width: ancho } : { left: '50%', width: ancho }} />
              </span>
              <span className="bam-cifra">{pct(p.valor)}</span>
            </li>
          );
        })}
      </ul>
      <p className="bam-eje"><span /><span className="bam-eje-pista"><span className="izq">← Más barato</span><span className="centro">Estándar</span><span className="der">Más caro →</span></span><span className="bam-cifra" /></p>
    </>
  );
}
