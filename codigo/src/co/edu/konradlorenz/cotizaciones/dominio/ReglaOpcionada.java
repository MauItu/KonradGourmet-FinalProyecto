package co.edu.konradlorenz.cotizaciones.dominio;

import java.math.BigDecimal;

/**
 * «dentro del rango»
 */
public class ReglaOpcionada extends ReglaValidacion {

    @Override
    protected boolean aplica(BigDecimal diferencia) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    protected EstadoCotizacion estado() {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
