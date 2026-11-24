import { useEffect, useState, type ReactNode } from 'react';
import { Icono } from './Icono';

const mayus = (s: string) => s.charAt(0).toUpperCase() + s.slice(1);
const saludo = (h: number) => (h < 12 ? 'Buenos días' : h < 19 ? 'Buenas tardes' : 'Buenas noches');

/** Encabezado de cada ventana: saludo (en el inicio) o título, los chips de fecha y hora y, si hay, la acción principal. */
export function Encabezado(p: { saludoA?: string; titulo?: string; sub: string; icono?: string; accion?: ReactNode }) {
  const [ahora, setAhora] = useState(() => new Date());
  useEffect(() => {
    const t = setInterval(() => setAhora(new Date()), 30_000);
    return () => clearInterval(t);
  }, []);
  const fecha = mayus(ahora.toLocaleDateString('es-CO', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' }));
  const hora = ahora.toLocaleTimeString('es-CO', { hour: 'numeric', minute: '2-digit' });
  return (
    <header className={`topbar ${p.saludoA ? 'is-greeting' : ''}`}>
      <div className="topbar-text">
        {p.saludoA ? (
          <><h1>{saludo(ahora.getHours())}, <em>{p.saludoA}</em></h1><p>{p.sub}</p></>
        ) : (
          <div className="page-title">
            {p.icono && <span className="title-ico"><Icono nombre={p.icono} /></span>}
            <div><h1>{p.titulo}</h1><p>{p.sub}</p></div>
          </div>
        )}
      </div>
      <div className="topbar-chips">
        <span className="chip"><Icono nombre="calendar" /><span>{fecha}</span></span>
        <span className="chip"><Icono nombre="clock" /><span>{hora}</span></span>
        {p.accion}
      </div>
    </header>
  );
}
