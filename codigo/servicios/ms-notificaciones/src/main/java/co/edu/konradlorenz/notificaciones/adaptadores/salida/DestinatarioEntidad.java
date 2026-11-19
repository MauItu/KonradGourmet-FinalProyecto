package co.edu.konradlorenz.notificaciones.adaptadores.salida;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Fila de la libreta de jefes por sucursal. Solo la usa DestinatarioJpaAdapter. */
@Entity
@Table(name = "destinatario")
class DestinatarioEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "sucursal_id", nullable = false)
    Long sucursalId;

    @Column(nullable = false)
    String correo;

}
