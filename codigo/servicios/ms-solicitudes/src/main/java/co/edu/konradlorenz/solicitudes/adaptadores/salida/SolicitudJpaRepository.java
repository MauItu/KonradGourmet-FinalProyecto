package co.edu.konradlorenz.solicitudes.adaptadores.salida;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

interface SolicitudJpaRepository extends JpaRepository<SolicitudEntidad, String>, JpaSpecificationExecutor<SolicitudEntidad> {

    @Query(value = "SELECT nextval('solicitud_numero')", nativeQuery = true)
    long siguienteNumero();

}
