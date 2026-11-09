package co.edu.konradlorenz.pedidos.dominio;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Pedido {

    private String numero;
    private Mesa mesa;
    private Long meseroId;
    private LocalDateTime fecha;
    private EstadoPedido estado;
    private List<DetallePedido> detalles = new ArrayList<>();

    /**
     * Un pedido nuevo nace EN_PREPARACION, con la fecha de ahora y el mesero que lo tomó
     * (HU-01, DEC-51). En el proyecto real, el controlador saca el meseroId del token JWT.
     */
    public Pedido(String numero, Mesa mesa, Long meseroId) {
        this.numero = numero;
        this.mesa = mesa;
        this.meseroId = meseroId;
        this.fecha = LocalDateTime.now();
        this.estado = EstadoPedido.EN_PREPARACION;
    }

    public String numero() {
        return numero;
    }

    public Mesa mesa() {
        return mesa;
    }

    public Long meseroId() {
        return meseroId;
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

    /** Exige el mesero que lo tomó, una mesa activa y al menos un plato. */
    public void validar() {
        if (meseroId == null) {
            throw new IllegalStateException("El pedido debe quedar asociado al mesero que lo tomó");
        }
        if (mesa == null || !mesa.activa()) {
            throw new IllegalStateException("Debe escoger una mesa activa");
        }
        if (detalles.isEmpty()) {
            throw new IllegalStateException("Debe agregar al menos un plato");
        }
    }

}
