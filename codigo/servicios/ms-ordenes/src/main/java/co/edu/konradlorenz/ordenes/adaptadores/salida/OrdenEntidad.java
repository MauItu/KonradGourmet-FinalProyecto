package co.edu.konradlorenz.ordenes.adaptadores.salida;

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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Fila de orden_compra con sus solicitudes (orden_solicitud) y sus productos (item_orden). Solo la usa OrdenJpaAdapter. */
@Entity
@Table(name = "orden_compra")
class OrdenEntidad {

    @Id
    String numero;

    @Column(nullable = false)
    LocalDate fecha;

    @Column(name = "cotizacion_numero", nullable = false, unique = true)
    String cotizacionNumero;

    @Column(nullable = false)
    String proveedor;

    @Column(name = "correo_proveedor", nullable = false)
    String correoProveedor;

    @Column(name = "pdf_url")
    String pdfUrl;

    @Column(nullable = false)
    String estado;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "orden_solicitud", joinColumns = @JoinColumn(name = "orden_numero"))
    @OrderColumn(name = "posicion")
    @Column(name = "solicitud_numero")
    List<String> solicitudes = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "item_orden", joinColumns = @JoinColumn(name = "orden_numero"))
    @OrderColumn(name = "posicion")
    List<Item> items = new ArrayList<>();

    @Embeddable
    static class Item {

        @Column(name = "producto_id", nullable = false)
        Long productoId;

        @Column(nullable = false)
        BigDecimal cantidad;

        @Column(nullable = false)
        String unidad;

        @Column(nullable = false)
        BigDecimal precio;

        Item() {
        }

        Item(Long productoId, BigDecimal cantidad, String unidad, BigDecimal precio) {
            this.productoId = productoId;
            this.cantidad = cantidad;
            this.unidad = unidad;
            this.precio = precio;
        }
    }

}
