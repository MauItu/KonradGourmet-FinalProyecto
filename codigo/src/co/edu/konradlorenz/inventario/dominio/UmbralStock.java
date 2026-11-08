package co.edu.konradlorenz.inventario.dominio;

import java.math.BigDecimal;

/**
 * Nota del diagrama: El umbral del 25 % es un parámetro propio de ms-inventario: no se lee de
 * ParametrosSistema (ms-administracion), así que no hace falta otro tópico.
 */
public class UmbralStock {

    private BigDecimal porcentaje;

    public boolean superado(InsumoInventario insumo) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
