package co.edu.konradlorenz.pedidos.adaptadores.salida;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface PlatoCatalogoJpaRepository extends JpaRepository<PlatoCatalogoEntidad, Long> {

    List<PlatoCatalogoEntidad> findBySucursalId(Long sucursalId);

}
