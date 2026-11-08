package co.edu.konradlorenz.cotizaciones.dominio;

import java.math.BigDecimal;

/**
 * «Chain of Responsibility · manejador»
 */
public abstract class ReglaValidacion {

    protected ReglaValidacion siguiente;

    public static ReglaValidacion cadenaPorDefecto() {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    public ReglaValidacion enlazar(ReglaValidacion siguiente) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    public EstadoCotizacion evaluar(Cotizacion c) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    protected abstract boolean aplica(BigDecimal diferencia);

    protected abstract EstadoCotizacion estado();

}
