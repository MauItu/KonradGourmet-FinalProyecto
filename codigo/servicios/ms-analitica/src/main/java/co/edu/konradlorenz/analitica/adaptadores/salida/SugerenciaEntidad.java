package co.edu.konradlorenz.analitica.adaptadores.salida;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Fila de la tabla sugerencia. Solo la usa SugerenciaJpaAdapter. */
@Entity
@Table(name = "sugerencia")
class SugerenciaEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    String producto;

    @Column(nullable = false)
    String motivo;

    @Column(name = "vigencia_desde", nullable = false)
    LocalDate vigenciaDesde;

    @Column(name = "vigencia_hasta", nullable = false)
    LocalDate vigenciaHasta;

    @Column(nullable = false)
    String estado;

    @Column(name = "creada_en", nullable = false)
    LocalDateTime creadaEn;

}
