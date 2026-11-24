package co.edu.konradlorenz.pedidos.adaptadores.salida;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Fila de la tabla pedido con sus detalles (tabla detalle_pedido). Solo la usa PedidoJpaAdapter. */
@Entity
@Table(name = "pedido")
class PedidoEntidad {

    @Id
    String numero;

    @Column(name = "sucursal_id", nullable = false)
    Long sucursalId;

    @Column(name = "mesa_numero", nullable = false)
    int mesaNumero;

    @Column(name = "mesero_id", nullable = false)
    Long meseroId;

    @Column(nullable = false)
    LocalDateTime fecha;

    @Column(nullable = false)
    String estado;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "detalle_pedido", joinColumns = @JoinColumn(name = "pedido_numero"))
    @OrderColumn(name = "orden")
    List<Detalle> detalles = new ArrayList<>();

    @Embeddable
    static class Detalle {

        @Column(name = "plato_id", nullable = false)
        Long platoId;

        @Column(nullable = false)
        int cantidad;

        String nota;

        @Column(name = "precio_unitario", nullable = false)
        BigDecimal precioUnitario;

        Detalle() {
        }

        Detalle(Long platoId, int cantidad, String nota, BigDecimal precioUnitario) {
            this.platoId = platoId;
            this.cantidad = cantidad;
            this.nota = nota;
            this.precioUnitario = precioUnitario;
        }
    }

}
