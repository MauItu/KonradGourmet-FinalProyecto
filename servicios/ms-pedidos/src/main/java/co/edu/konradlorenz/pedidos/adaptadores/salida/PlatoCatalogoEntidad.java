package co.edu.konradlorenz.pedidos.adaptadores.salida;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/** Fila de plato_catalogo: la copia local del menú que llega por menu.actualizado. */
@Entity
@Table(name = "plato_catalogo")
class PlatoCatalogoEntidad {

    @Id
    @Column(name = "plato_id")
    Long platoId;

    @Column(name = "sucursal_id", nullable = false)
    Long sucursalId;

    @Column(nullable = false)
    String nombre;

    String categoria;

    @Column(nullable = false)
    BigDecimal precio;

    @Column(nullable = false)
    boolean activo;

}
