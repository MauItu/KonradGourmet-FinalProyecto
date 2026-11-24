package co.edu.konradlorenz.notificaciones.adaptadores.salida;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface DestinatarioJpaRepository extends JpaRepository<DestinatarioEntidad, Long> {

    List<DestinatarioEntidad> findBySucursalIdOrderByCorreo(Long sucursalId);

    boolean existsBySucursalIdAndCorreo(Long sucursalId, String correo);

}
