package co.edu.konradlorenz.analitica.adaptadores.salida;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/** Spring Data sobre la tabla sugerencia. Solo lo usa SugerenciaJpaAdapter. */
interface SugerenciaJpaRepository extends JpaRepository<SugerenciaEntidad, Long> {

    /** Las vigentes en algún día del rango. */
    List<SugerenciaEntidad> findByVigenciaHastaGreaterThanEqualAndVigenciaDesdeLessThanEqualOrderByVigenciaDesdeDescIdAsc(LocalDate desde, LocalDate hasta);

    List<SugerenciaEntidad> findAllByOrderByVigenciaDesdeDescIdAsc();

    boolean existsByVigenciaDesde(LocalDate fecha);

    /** Una sugerencia nueva; si otra réplica ya guardó la misma (producto y día de inicio), no hace nada. */
    @Modifying
    @Query(value = "INSERT INTO sugerencia (producto, motivo, vigencia_desde, vigencia_hasta, estado) "
            + "VALUES (:producto, :motivo, :desde, :hasta, :estado) ON CONFLICT DO NOTHING", nativeQuery = true)
    int insertar(@Param("producto") String producto, @Param("motivo") String motivo, @Param("desde") LocalDate desde,
                 @Param("hasta") LocalDate hasta, @Param("estado") String estado);

    /** Solo cambia una sugerencia que sigue SUGERIDA: si otro jefe ya la resolvió, no cambia nada (0 filas). */
    @Modifying
    @Query("UPDATE SugerenciaEntidad s SET s.estado = :estado WHERE s.id = :id AND s.estado = 'SUGERIDA'")
    int resolver(@Param("id") Long id, @Param("estado") String estado);

}
