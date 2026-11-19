package co.edu.konradlorenz.menu.adaptadores.salida;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Fila de la tabla plato con su receta (tabla ingrediente). Solo la usa PlatoJpaAdapter. */
@Entity
@Table(name = "plato")
class PlatoEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "sucursal_id", nullable = false)
    Long sucursalId;

    @Column(name = "creado_por", nullable = false)
    Long creadoPor;

    @Column(nullable = false)
    String nombre;

    @Column(nullable = false)
    String categoria;

    @Column(nullable = false)
    String descripcion;

    @Column(name = "precio_venta", nullable = false)
    BigDecimal precioVenta;

    @Column(nullable = false)
    boolean activo;

    @Column(name = "imagen_url")
    String imagenUrl;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "ingrediente", joinColumns = @JoinColumn(name = "plato_id"))
    @OrderColumn(name = "orden")
    List<Fila> ingredientes = new ArrayList<>();

    @Embeddable
    static class Fila {

        @Column(name = "producto_id", nullable = false)
        Long productoId;

        @Column(nullable = false)
        BigDecimal cantidad;

        @Column(nullable = false)
        String unidad;

        Fila() {
        }

        Fila(Long productoId, BigDecimal cantidad, String unidad) {
            this.productoId = productoId;
            this.cantidad = cantidad;
            this.unidad = unidad;
        }
    }

}
