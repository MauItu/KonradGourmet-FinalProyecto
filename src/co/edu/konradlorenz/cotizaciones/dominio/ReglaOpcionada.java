package co.edu.konradlorenz.cotizaciones.dominio;

import java.math.BigDecimal;

/**
 * «dentro del rango»
 */
public class ReglaOpcionada extends ReglaValidacion {

    @Override
    protected boolean aplica(BigDecimal diferencia) {
        return diferencia.compareTo(new BigDecimal("-50")) >= 0 && diferencia.compareTo(new BigDecimal("25")) <= 0;
    }

    @Override
    protected EstadoCotizacion estado() {
        return EstadoCotizacion.OPCIONADA;
    }

}
