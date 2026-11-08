package co.edu.konradlorenz.integracion.puertos.salida;

import co.edu.konradlorenz.integracion.dominio.PrecioEstandar;

/**
 * «Proxy · sujeto / Adapter · objetivo»
 */
public interface PrecioReferenciaPort {

    PrecioEstandar precioEstandar(Long productoId);

}
