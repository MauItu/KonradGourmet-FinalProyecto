package co.edu.konradlorenz.solicitudes.adaptadores.salida;

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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Fila de la tabla solicitud con sus productos (item_solicitud) y las cotizaciones ya aplicadas
 * (cotizacion_aplicada). Solo la usa SolicitudJpaAdapter.
 */
@Entity
@Table(name = "solicitud")
class SolicitudEntidad {

    @Id
    String numero;

    @Column(name = "sucursal_id", nullable = false)
    Long sucursalId;

    @Column(name = "solicitante_id", nullable = false)
    Long solicitanteId;

    @Column(name = "correo_solicitante", nullable = false)
    String correoSolicitante;

    @Column(nullable = false)
    LocalDateTime fecha;

    @Column(nullable = false)
    String estado;

    @Column(name = "valor_sugerido_total", nullable = false)
    BigDecimal valorSugeridoTotal;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "item_solicitud", joinColumns = @JoinColumn(name = "solicitud_numero"))
    @OrderColumn(name = "orden")
    List<Item> items = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "cotizacion_aplicada", joinColumns = @JoinColumn(name = "solicitud_numero"))
    @Column(name = "cotizacion_numero")
    Set<String> cotizacionesAplicadas = new LinkedHashSet<>();

    @Embeddable
    static class Item {

        @Column(name = "producto_id", nullable = false)
        Long productoId;

        @Column(nullable = false)
        BigDecimal cantidad;

        String marca;

        @Column(nullable = false)
        String unidad;

        @Column(nullable = false)
        boolean cotizado;

        @Column(name = "precio_sugerido")
        BigDecimal precioSugerido;

        Item() {
        }

        Item(Long productoId, BigDecimal cantidad, String marca, String unidad, boolean cotizado, BigDecimal precioSugerido) {
            this.productoId = productoId;
            this.cantidad = cantidad;
            this.marca = marca;
            this.unidad = unidad;
            this.cotizado = cotizado;
            this.precioSugerido = precioSugerido;
        }
    }

}
