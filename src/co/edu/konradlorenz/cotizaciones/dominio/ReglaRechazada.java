package co.edu.konradlorenz.cotizaciones.dominio;

import java.math.BigDecimal;

/**
 * «más de 25 % por encima»
 */
public class ReglaRechazada extends ReglaValidacion {

    @Override
    protected boolean aplica(BigDecimal diferencia) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    protected EstadoCotizacion estado() {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
