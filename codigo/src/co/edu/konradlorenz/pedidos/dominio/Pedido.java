package co.edu.konradlorenz.pedidos.dominio;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Pedido {

    private String numero;
    private Mesa mesa;
    private LocalDateTime fecha;
    private EstadoPedido estado;
    private List<DetallePedido> detalles = new ArrayList<>();

    /** Un pedido nuevo nace EN_PREPARACION, con la fecha de ahora. */
    public Pedido(String numero, Mesa mesa) {
        this.numero = numero;
        this.mesa = mesa;
        this.fecha = LocalDateTime.now();
        this.estado = EstadoPedido.EN_PREPARACION;
    }

    public String numero() {
        return numero;
    }

    public Mesa mesa() {
        return mesa;
    }

    public LocalDateTime fecha() {
        return fecha;
    }

    public EstadoPedido estado() {
        return estado;
    }

    public List<DetallePedido> detalles() {
        return detalles;
    }

    public void agregarPlato(Long platoId, int cantidad, String nota, BigDecimal precio) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }
        detalles.add(new DetallePedido(platoId, cantidad, nota, precio));
    }

    public BigDecimal total() {
        return detalles.stream()
                .map(DetallePedido::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Exige una mesa activa y al menos un plato. */
    public void validar() {
        if (mesa == null || !mesa.activa()) {
            throw new IllegalStateException("Debe escoger una mesa activa");
        }
        if (detalles.isEmpty()) {
            throw new IllegalStateException("Debe agregar al menos un plato");
        }
    }

}
