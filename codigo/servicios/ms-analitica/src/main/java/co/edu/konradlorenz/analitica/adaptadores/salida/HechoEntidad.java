package co.edu.konradlorenz.analitica.adaptadores.salida;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Fila de la tabla hecho. Solo la usa KpiJpaAdapter. */
@Entity
@Table(name = "hecho")
class HechoEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    String tipo;

    @Column(name = "sucursal_id")
    Long sucursalId;

    @Column(nullable = false)
    String clave;

    @Column(nullable = false)
    BigDecimal valor;

    @Column(nullable = false)
    LocalDate fecha;

    @Column(name = "correlacion_id")
    UUID correlacionId;

}
