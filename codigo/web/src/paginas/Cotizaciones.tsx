import { useSesion } from '../sesion/Sesion';
import { CotizacionesAuxiliar } from './auxiliar/CotizacionesAuxiliar';
import { CotizacionesDirector } from './director/CotizacionesDirector';

/**
 * /cotizaciones es «Cotizaciones enviadas» del auxiliar de compras (V15; /cotizaciones/nueva abre V13) y,
 * para el director de compras, la cabeza de su grupo del menú: «Todas» (V17).
 */
export function Cotizaciones() {
  const { usuario } = useSesion();
  return usuario?.rol === 'DIRECTOR_COMPRAS' ? <CotizacionesDirector soloPendientes={false} /> : <CotizacionesAuxiliar />;
}
