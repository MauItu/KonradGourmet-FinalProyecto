package co.edu.konradlorenz.pedidos.dominio;

import java.math.BigDecimal;

public class DetallePedido {

    private Long platoId;
    private int cantidad;
    private String nota;
    private BigDecimal precioUnitario;

    public DetallePedido(Long platoId, int cantidad, String nota, BigDecimal precioUnitario) {
        this.platoId = platoId;
        this.cantidad = cantidad;
        this.nota = nota;
        this.precioUnitario = precioUnitario;
    }

    public Long platoId() {
        return platoId;
    }

    public int cantidad() {
        return cantidad;
    }

    public String nota() {
        return nota;
    }

    public BigDecimal precioUnitario() {
        return precioUnitario;
    }

    public BigDecimal subtotal() {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

}
