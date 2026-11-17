package co.edu.konradlorenz.menu.adaptadores.salida;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Fila de la tabla producto (los insumos con que se arman las recetas) y su categoría. */
@Entity
@Table(name = "producto")
class ProductoEntidad {

    @Id
    Long id;

    @Column(nullable = false)
    String nombre;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "categoria_id")
    CategoriaEntidad categoria;

}
