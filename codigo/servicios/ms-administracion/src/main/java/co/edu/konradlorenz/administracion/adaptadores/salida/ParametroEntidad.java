package co.edu.konradlorenz.administracion.adaptadores.salida;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/** Fila de la tabla de parámetros. Solo la usa ParametrosJpaAdapter. */
@Entity
@Table(name = "parametro")
class ParametroEntidad {

    @Id
    String clave;

    @Column(nullable = false)
    String valor;

    @Column(nullable = false)
    LocalDateTime actualizado;

}
