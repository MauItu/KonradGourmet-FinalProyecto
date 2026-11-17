import { useSesion } from '../sesion/Sesion';
import { SolicitudesAuxiliar } from './auxiliar/SolicitudesAuxiliar';
import { MisSolicitudes } from './jefe/MisSolicitudes';

/** /solicitudes es «Mis solicitudes» del jefe de cocina (V10) y «Todas las solicitudes» del auxiliar (V12). */
export function Solicitudes() {
  const { usuario } = useSesion();
  return usuario?.rol === 'AUXILIAR_COMPRAS' ? <SolicitudesAuxiliar /> : <MisSolicitudes />;
}
