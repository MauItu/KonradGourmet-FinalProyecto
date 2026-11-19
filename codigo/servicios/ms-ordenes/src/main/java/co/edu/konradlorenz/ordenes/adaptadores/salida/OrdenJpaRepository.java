package co.edu.konradlorenz.ordenes.adaptadores.salida;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

interface OrdenJpaRepository extends JpaRepository<OrdenEntidad, String> {

    List<OrdenEntidad> findByEstadoOrderByNumero(String estado);

    boolean existsByCotizacionNumero(String cotizacionNumero);

    @Query(value = "SELECT nextval('orden_numero')", nativeQuery = true)
    Long siguienteNumero();

}
