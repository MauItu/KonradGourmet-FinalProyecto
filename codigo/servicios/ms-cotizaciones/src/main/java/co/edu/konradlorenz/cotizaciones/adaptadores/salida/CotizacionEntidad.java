package co.edu.konradlorenz.cotizaciones.adaptadores.salida;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Fila de la tabla cotizacion con su proveedor, sus solicitudes (cotizacion_solicitud) y sus productos
 * (item_cotizacion). Solo la usa CotizacionJpaAdapter.
 */
@Entity
@Table(name = "cotizacion")
class CotizacionEntidad {

    @Id
    String numero;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "proveedor_id")
    ProveedorEntidad proveedor;

    @Column(name = "fecha_envio", nullable = false)
    LocalDate fechaEnvio;

    @Column(name = "fecha_limite", nullable = false)
    LocalDate fechaLimite;

    @Column(name = "pdf_url")
    String pdfUrl;

    @Column(nullable = false)
    String estado;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "cotizacion_solicitud", joinColumns = @JoinColumn(name = "cotizacion_numero"))
    @OrderColumn(name = "orden")
    @Column(name = "solicitud_numero")
    List<String> solicitudes = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "item_cotizacion", joinColumns = @JoinColumn(name = "cotizacion_numero"))
    @OrderColumn(name = "orden")
    List<Item> items = new ArrayList<>();

    @Embeddable
    static class Item {

        @Column(name = "producto_id", nullable = false)
        Long productoId;

        @Column(nullable = false)
        BigDecimal cantidad;

        @Column(nullable = false)
        String unidad;

        @Column(name = "precio_estandar")
        BigDecimal precioEstandar;

        @Column(name = "precio_proveedor")
        BigDecimal precioProveedor;

        Item() {
        }

        Item(Long productoId, BigDecimal cantidad, String unidad, BigDecimal precioEstandar, BigDecimal precioProveedor) {
            this.productoId = productoId;
            this.cantidad = cantidad;
            this.unidad = unidad;
            this.precioEstandar = precioEstandar;
            this.precioProveedor = precioProveedor;
        }
    }

}
