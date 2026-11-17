package co.edu.konradlorenz.identidad.adaptadores.salida;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Fila de la tabla sucursal (Centro, Norte y Chapinero). */
@Entity
@Table(name = "sucursal")
class SucursalEntidad {

    @Id
    Long id;

    String nombre;

}
