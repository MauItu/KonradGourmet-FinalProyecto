import type { ReactNode } from 'react';
import { Icono } from './Icono';

export function Tarjeta(p: { titulo?: string; sub?: string; icono?: string; extra?: ReactNode; clase?: string; children: ReactNode }) {
  return (
    <section className={`card ${p.clase ?? ''}`}>
      {p.titulo && (
        <div className="card-head">
          <div className="card-title">
            {p.icono && <span className="title-ico sm"><Icono nombre={p.icono} /></span>}
            <div><h2>{p.titulo}</h2>{p.sub && <p>{p.sub}</p>}</div>
          </div>
          {p.extra}
        </div>
      )}
      {p.children}
    </section>
  );
}
