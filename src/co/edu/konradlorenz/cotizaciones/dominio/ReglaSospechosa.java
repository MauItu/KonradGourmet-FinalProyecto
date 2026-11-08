package co.edu.konradlorenz.cotizaciones.dominio;

import java.math.BigDecimal;

/**
 * «más de 50 % por debajo»
 */
public class ReglaSospechosa extends ReglaValidacion {

    @Override
    protected boolean aplica(BigDecimal diferencia) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    protected EstadoCotizacion estado() {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
