package co.edu.konradlorenz.cotizaciones.dominio;

import java.math.BigDecimal;

/**
 * «Chain of Responsibility · manejador»
 */
public abstract class ReglaValidacion {

    protected ReglaValidacion siguiente;

    public static ReglaValidacion cadenaPorDefecto() {
        // el orden importa: primero lo más grave; Opcionada recoge lo que queda dentro del rango
        ReglaValidacion cabeza = new ReglaRechazada();
        cabeza.enlazar(new ReglaSospechosa()).enlazar(new ReglaOpcionada());
        return cabeza;
    }

    public ReglaValidacion enlazar(ReglaValidacion siguiente) {
        this.siguiente = siguiente;
        return siguiente; // devuelve la siguiente para poder encadenar: a.enlazar(b).enlazar(c)
    }

    public EstadoCotizacion evaluar(Cotizacion c) {
        BigDecimal diferencia = c.diferenciaPorcentual();
        if (aplica(diferencia)) {
            return estado();
        }
        if (siguiente == null) {
            throw new IllegalStateException("Ninguna regla aplica a una diferencia de " + diferencia + " %");
        }
        return siguiente.evaluar(c);
    }

    protected abstract boolean aplica(BigDecimal diferencia);

    protected abstract EstadoCotizacion estado();

}
