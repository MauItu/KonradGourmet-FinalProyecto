package co.edu.konradlorenz.menu.adaptadores.salida;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface PlatoJpaRepository extends JpaRepository<PlatoEntidad, Long> {

    List<PlatoEntidad> findBySucursalIdOrderByNombre(Long sucursalId);

    boolean existsBySucursalIdAndNombreIgnoreCase(Long sucursalId, String nombre);

}
