package co.edu.konradlorenz.ordenes.puertos.salida;

import co.edu.konradlorenz.ordenes.dominio.CotizacionOpcionada;

public interface CotizacionConsultaPort {

    CotizacionOpcionada opcionada(String numero);

}
