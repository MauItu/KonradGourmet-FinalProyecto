package co.edu.konradlorenz.administracion.adaptadores.salida;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/** Fila de la bitácora. Solo la usa AuditoriaJpaAdapter. */
@Entity
@Table(name = "auditoria")
class AuditoriaEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    String accion;

    @Column(nullable = false)
    String usuario;

    @Column(nullable = false)
    LocalDate fecha;

    @Column(nullable = false)
    LocalTime hora;

    @Column(name = "correlacion_id")
    UUID correlacionId;

}
