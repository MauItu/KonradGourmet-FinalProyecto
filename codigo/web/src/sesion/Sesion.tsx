import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import { api, SESION_VENCIDA } from '../api/cliente';
import { cerrarSesionLocal, guardarToken, leerToken } from './almacen';
import { leerUsuario, type ElementoMenu, type Usuario } from './token';

interface ContextoSesion {
  usuario: Usuario | null;
  vencida: boolean;
  iniciar: (correo: string, clave: string, recordar: boolean) => Promise<void>;
  salir: () => void;
}

const Contexto = createContext<ContextoSesion | null>(null);

export function ProveedorSesion({ children }: { children: ReactNode }) {
  const [usuario, setUsuario] = useState<Usuario | null>(() => {
    const t = leerToken();
    const u = t ? leerUsuario(t) : null;
    if (t && !u) cerrarSesionLocal();
    return u;
  });
  const [vencida, setVencida] = useState(false);

  useEffect(() => {
    const alVencer = () => { setUsuario(null); setVencida(true); };
    window.addEventListener(SESION_VENCIDA, alVencer);
    return () => window.removeEventListener(SESION_VENCIDA, alVencer);
  }, []);

  // V01: POST /api/auth/token → ms-identidad (IniciarSesionService)
  const iniciar = useCallback(async (correo: string, clave: string, recordar: boolean) => {
    const r = await api<{ jwt: string; menu: ElementoMenu }>('/api/auth/token', { cuerpo: { correo, clave }, publica: true });
    const u = leerUsuario(r.jwt);
    if (!u) throw new Error('El servidor respondió un token que no se pudo leer.');
    guardarToken(r.jwt, recordar);
    setVencida(false);
    setUsuario({ ...u, menu: r.menu });
  }, []);

  const salir = useCallback(() => { cerrarSesionLocal(); setVencida(false); setUsuario(null); }, []);

  const valor = useMemo(() => ({ usuario, vencida, iniciar, salir }), [usuario, vencida, iniciar, salir]);
  return <Contexto.Provider value={valor}>{children}</Contexto.Provider>;
}

export function useSesion(): ContextoSesion {
  const c = useContext(Contexto);
  if (!c) throw new Error('useSesion fuera de ProveedorSesion');
  return c;
}
