package co.edu.konradlorenz.pedidos.dominio;

import java.math.BigDecimal;

public class DetallePedido {

    private Long platoId;
    private int cantidad;
    private String nota;
    private BigDecimal precioUnitario;

    public BigDecimal subtotal() {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
