package co.edu.konradlorenz.analitica.adaptadores.salida;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

/** Spring Data sobre la tabla hecho. Solo lo usa KpiJpaAdapter. */
interface HechoJpaRepository extends JpaRepository<HechoEntidad, Long> {

    List<HechoEntidad> findByFechaBetweenOrderByFechaAscIdAsc(LocalDate desde, LocalDate hasta);

    List<HechoEntidad> findByFechaBetweenAndSucursalIdOrderByFechaAscIdAsc(LocalDate desde, LocalDate hasta, Long sucursalId);

}
