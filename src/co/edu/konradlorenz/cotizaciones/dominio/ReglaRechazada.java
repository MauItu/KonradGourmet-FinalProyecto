package co.edu.konradlorenz.cotizaciones.dominio;

import java.math.BigDecimal;

/**
 * «más de 25 % por encima»
 */
public class ReglaRechazada extends ReglaValidacion {

    @Override
    protected boolean aplica(BigDecimal diferencia) {
        return diferencia.compareTo(new BigDecimal("25")) > 0;
    }

    @Override
    protected EstadoCotizacion estado() {
        return EstadoCotizacion.RECHAZADA;
    }

}
