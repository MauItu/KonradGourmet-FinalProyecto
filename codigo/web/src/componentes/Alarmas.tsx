import { useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { leerToken } from '../sesion/almacen';
import type { Usuario } from '../sesion/token';
import { hora } from './formato';
import { Icono } from './Icono';

/** Lo que manda ms-notificaciones por el WebSocket (AlarmaPushAdapter → Redis → CanalAlarmas, PD-01). */
interface Alarma { destino: string; contenido: string; fechaHora: string }

/** Evento del navegador que avisa a V05 y V07 que llegó una alarma: vuelven a leer las alertas de ms-inventario. */
export const ALARMA_RECIBIDA = 'kg:alarma';

/** Ejecuta alLlegar cada vez que llega una alarma en vivo (V05 y V07 recargan sus alertas). */
export function useAlarmaRecibida(alLlegar: () => void) {
  const actual = useRef(alLlegar);
  actual.current = alLlegar;
  useEffect(() => {
    const oir = () => actual.current();
    window.addEventListener(ALARMA_RECIBIDA, oir);
    return () => window.removeEventListener(ALARMA_RECIBIDA, oir);
  }, []);
}

/**
 * La alarma de stock bajo en vivo (HU-16): mientras el jefe de cocina tiene la sesión abierta, un WebSocket
 * con ms-notificaciones (/api/notificaciones/alarmas; el JWT va en la URL porque el navegador no deja poner
 * la cabecera) recibe cada alarma y la muestra arriba hasta cerrarla, con «Solicitar», que abre V08. Si se
 * cae la conexión, vuelve a abrirla. Las alertas guardadas las sigue mostrando V05 al abrirse.
 */
export function AlarmasEnVivo({ usuario }: { usuario: Usuario }) {
  const navegar = useNavigate();
  const [alarmas, setAlarmas] = useState<(Alarma & { id: number })[]>([]);

  const { correo, vence } = usuario;
  useEffect(() => {
    let vivo = true;
    let socket: WebSocket | null = null;
    let reloj = 0;
    let siguiente = 1;
    const abrir = () => {
      const token = leerToken();
      if (!vivo || !token || vence <= Date.now()) return;
      const esquema = window.location.protocol === 'https:' ? 'wss' : 'ws';
      socket = new WebSocket(`${esquema}://${window.location.host}/api/notificaciones/alarmas?access_token=${encodeURIComponent(token)}`);
      socket.onmessage = (m) => {
        try {
          const a = JSON.parse(String(m.data)) as Alarma;
          setAlarmas((x) => [{ ...a, id: siguiente++ }, ...x].slice(0, 3));
          window.dispatchEvent(new Event(ALARMA_RECIBIDA));
        } catch { /* mensaje ilegible: se ignora */ }
      };
      socket.onclose = () => { if (vivo) reloj = window.setTimeout(abrir, 5000); };
    };
    abrir();
    return () => {
      vivo = false;
      window.clearTimeout(reloj);
      // cerrarlo mientras conecta deja un error en la consola: se cierra apenas abra
      if (socket?.readyState === WebSocket.CONNECTING) socket.onopen = () => socket?.close();
      else socket?.close();
    };
  }, [correo, vence]);

  if (!alarmas.length) return null;
  const cerrar = (id: number) => setAlarmas((x) => x.filter((a) => a.id !== id));
  return (
    <div className="alarmas-vivas" role="alert" aria-live="assertive">
      {alarmas.map((a) => (
        <div key={a.id} className="alarma-viva">
          <Icono nombre="bell" />
          <div>
            <b>Alarma de stock bajo</b>
            <p>{a.contenido}</p>
            <div className="acciones">
              <button type="button" className="btn primary sm" onClick={() => { cerrar(a.id); navegar('/solicitudes/nueva'); }}>Solicitar</button>
              <small>{hora(a.fechaHora)}</small>
            </div>
          </div>
          <button type="button" className="icon-btn" onClick={() => cerrar(a.id)} aria-label="Cerrar la alarma"><Icono nombre="x" /></button>
        </div>
      ))}
    </div>
  );
}
