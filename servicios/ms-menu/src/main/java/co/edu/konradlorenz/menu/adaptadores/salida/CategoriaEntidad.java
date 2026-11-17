package co.edu.konradlorenz.menu.adaptadores.salida;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Fila de la tabla categoria (categorías de productos: Proteína, Cereal, Tubérculo…). */
@Entity
@Table(name = "categoria")
class CategoriaEntidad {

    @Id
    Long id;

    @Column(nullable = false)
    String nombre;

}
