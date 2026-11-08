package co.edu.konradlorenz.inventario.dominio;

import java.math.BigDecimal;
import java.math.RoundingMode;

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

    public InsumoInventario(Long productoId, String nombre, BigDecimal stockActual, BigDecimal stockReferencia, String unidad) {
        this.productoId = productoId;
        this.nombre = nombre;
        this.stockActual = stockActual;
        this.stockReferencia = stockReferencia;
        this.unidad = unidad;
    }

    public Long productoId() {
        return productoId;
    }

    public String nombre() {
        return nombre;
    }

    public BigDecimal stockActual() {
        return stockActual;
    }

    public BigDecimal stockReferencia() {
        return stockReferencia;
    }

    public String unidad() {
        return unidad;
    }

    public boolean alertaAtendida() {
        return alertaAtendida;
    }

    public void descontar(BigDecimal cantidad) {
        if (cantidad.signum() < 0) {
            throw new IllegalArgumentException("La cantidad a descontar no puede ser negativa");
        }
        // el stock no baja de cero; descontar reactiva la alerta (alertaAtendida = false)
        stockActual = stockActual.subtract(cantidad).max(BigDecimal.ZERO);
        alertaAtendida = false;
    }

    public BigDecimal porcentajeExistencia() {
        return stockActual.multiply(BigDecimal.valueOf(100)).divide(stockReferencia, 2, RoundingMode.HALF_UP);
    }

    public void atenderAlerta() {
        alertaAtendida = true;
    }

}
