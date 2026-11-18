import { createContext, useContext, useEffect, useState, type ReactNode } from 'react';
import { administracion } from '../api/servicios';
import { useSesion } from './Sesion';

/** Lo que la SPA toma del tema (HU-20): el nombre, el color principal y el logo. */
export interface Tema { nombre: string; color: string; logo: string }

export const TEMA_INICIAL: Tema = { nombre: 'Konrad Gourmet', color: '#F28705', logo: '/assets/logo/logo.png' };

const Contexto = createContext<{ tema: Tema; aplicar: (t: Record<string, string>) => void }>({ tema: TEMA_INICIAL, aplicar: () => undefined });

const desdeParametros = (p: Record<string, string>): Tema => ({
  nombre: p['tema.nombre'] || TEMA_INICIAL.nombre,
  color: /^#[0-9A-Fa-f]{6}$/.test(p['tema.color-primario'] ?? '') ? p['tema.color-primario'] : TEMA_INICIAL.color,
  logo: p['tema.logo'] || TEMA_INICIAL.logo,
});

/** Las variables de app.css que salen del color principal: el naranja, su tono oscuro, el degradado y los fondos suaves. */
function pintar(color: string | null) {
  const raiz = document.documentElement.style;
  const variables: Record<string, string> = color ? {
    '--naranja': color,
    '--naranja-nombre': color,
    '--naranja-oscuro': `color-mix(in srgb, ${color} 86%, black)`,
    '--degradado': `linear-gradient(135deg, color-mix(in srgb, ${color} 82%, white) 0%, ${color} 55%, color-mix(in srgb, ${color} 90%, black) 100%)`,
    '--durazno': `color-mix(in srgb, ${color} 8%, white)`,
    '--durazno-2': `color-mix(in srgb, ${color} 16%, white)`,
  } : {};
  for (const v of ['--naranja', '--naranja-nombre', '--naranja-oscuro', '--degradado', '--durazno', '--durazno-2']) {
    if (variables[v]) raiz.setProperty(v, variables[v]); else raiz.removeProperty(v);
  }
}

/**
 * El tema de la empresa (HU-20, nota n_parametros): al iniciar sesión la SPA lee GET /api/admin/parametros/tema y lo
 * aplica, así que un cambio del administrador se ve sin redesplegar. Lo lee cualquier rol con sesión (DEC-86); la
 * pantalla de inicio de sesión conserva el logo y el color de siempre.
 */
export function ProveedorTema({ children }: { children: ReactNode }) {
  const { usuario } = useSesion();
  const [tema, setTema] = useState<Tema>(TEMA_INICIAL);
  const aplicar = (p: Record<string, string>) => {
    const t = desdeParametros(p);
    setTema(t);
    pintar(t.color.toUpperCase() === TEMA_INICIAL.color ? null : t.color);
    document.title = `${t.nombre} — Sistema de Abastecimiento`;
  };

  useEffect(() => {
    if (!usuario) {
      setTema(TEMA_INICIAL);
      pintar(null);
      return undefined;
    }
    let vivo = true;
    administracion.tema().then((p) => { if (vivo) aplicar(p); }).catch(() => { /* sin tema: el de siempre */ });
    return () => { vivo = false; };
  }, [usuario?.correo]); // eslint-disable-line react-hooks/exhaustive-deps

  return <Contexto.Provider value={{ tema, aplicar }}>{children}</Contexto.Provider>;
}

export const useTema = () => useContext(Contexto);
