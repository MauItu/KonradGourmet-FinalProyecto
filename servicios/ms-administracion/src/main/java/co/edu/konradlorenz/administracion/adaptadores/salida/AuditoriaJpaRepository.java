package co.edu.konradlorenz.administracion.adaptadores.salida;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

interface AuditoriaJpaRepository extends JpaRepository<AuditoriaEntidad, Long> {

    List<AuditoriaEntidad> findByFechaBetweenOrderByFechaDescHoraDescIdDesc(LocalDate desde, LocalDate hasta);

    boolean existsByFechaAndHoraAndUsuarioAndAccionAndCorrelacionId(LocalDate fecha, LocalTime hora, String usuario, String accion,
                                                                     UUID correlacionId);

}
