package co.edu.konradlorenz.cotizaciones.dominio;

import java.math.BigDecimal;

/**
 * «más de 50 % por debajo»
 */
public class ReglaSospechosa extends ReglaValidacion {

    @Override
    protected boolean aplica(BigDecimal diferencia) {
        return diferencia.compareTo(new BigDecimal("-50")) < 0;
    }

    @Override
    protected EstadoCotizacion estado() {
        return EstadoCotizacion.SOSPECHOSA;
    }

}
