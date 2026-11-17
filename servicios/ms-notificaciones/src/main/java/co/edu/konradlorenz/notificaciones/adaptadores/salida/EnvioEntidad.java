package co.edu.konradlorenz.notificaciones.adaptadores.salida;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/** Fila de la traza de envíos. Solo la usa EnvioJpaAdapter. */
@Entity
@Table(name = "envio")
class EnvioEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    String destino;

    @Column(nullable = false)
    String tipo;

    @Column(name = "fecha_hora", nullable = false)
    LocalDateTime fechaHora;

    @Column(nullable = false)
    boolean exito;

    String error;

    @Column(name = "correlacion_id")
    UUID correlacionId;

}
