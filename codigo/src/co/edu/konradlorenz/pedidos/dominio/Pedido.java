package co.edu.konradlorenz.pedidos.dominio;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class Pedido {

    private String numero;
    private Mesa mesa;
    private LocalDateTime fecha;
    private EstadoPedido estado;
    private List<DetallePedido> detalles;

    public void agregarPlato(Long platoId, int cantidad, String nota, BigDecimal precio) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    public BigDecimal total() {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    public void validar() {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
