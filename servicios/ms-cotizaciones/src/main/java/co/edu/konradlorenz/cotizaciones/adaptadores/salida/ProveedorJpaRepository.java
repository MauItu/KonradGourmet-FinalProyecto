package co.edu.konradlorenz.cotizaciones.adaptadores.salida;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface ProveedorJpaRepository extends JpaRepository<ProveedorEntidad, Long> {

    boolean existsByNit(String nit);

    List<ProveedorEntidad> findByActivoTrueOrderByRazonSocial();

    List<ProveedorEntidad> findAllByOrderByRazonSocial();

}
