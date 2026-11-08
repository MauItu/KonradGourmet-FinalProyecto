package co.edu.konradlorenz.ordenes.adaptadores.salida;

import co.edu.konradlorenz.ordenes.dominio.CotizacionOpcionada;
import co.edu.konradlorenz.ordenes.puertos.salida.CotizacionConsultaPort;

/**
 * «REST cliente → GET /api/cotizaciones/{id}»
 */
public class CotizacionRestClient implements CotizacionConsultaPort {

    @Override
    public CotizacionOpcionada opcionada(String numero) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
