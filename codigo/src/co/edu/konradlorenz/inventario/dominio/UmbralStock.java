package co.edu.konradlorenz.inventario.dominio;

import java.math.BigDecimal;

/**
 * Nota del diagrama: El umbral del 25 % es un parámetro propio de ms-inventario: no se lee de
 * ParametrosSistema (ms-administracion), así que no hace falta otro tópico.
 */
public class UmbralStock {

    private BigDecimal porcentaje;

    public UmbralStock(BigDecimal porcentaje) {
        this.porcentaje = porcentaje;
    }

    public BigDecimal porcentaje() {
        return porcentaje;
    }

    public boolean superado(InsumoInventario insumo) {
        // "superado" quiere decir que la existencia cayó por debajo del umbral
        return insumo.porcentajeExistencia().compareTo(porcentaje) < 0;
    }

}
