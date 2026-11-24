package co.edu.konradlorenz.cotizaciones.adaptadores.salida;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface CotizacionJpaRepository extends JpaRepository<CotizacionEntidad, String> {

    List<CotizacionEntidad> findByEstadoOrderByNumero(String estado);

}
