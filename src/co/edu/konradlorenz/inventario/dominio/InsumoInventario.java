package co.edu.konradlorenz.inventario.dominio;

import java.math.BigDecimal;

/**
 * Nota del diagrama: alertas() devuelve los insumos bajo el umbral con la alerta sin atender. La
 * alerta sigue activa hasta que llega solicitud.registrada con ese producto en la misma sucursal
 * (HU-16). descontar() la reactiva (alertaAtendida = false) cuando el insumo vuelve a cruzar el
 * umbral.
 */
public class InsumoInventario {

    private Long productoId;
    private String nombre;
    private BigDecimal stockActual;
    private BigDecimal stockReferencia;
    private String unidad;
    private boolean alertaAtendida;

    public void descontar(BigDecimal cantidad) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    public BigDecimal porcentajeExistencia() {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    public void atenderAlerta() {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
