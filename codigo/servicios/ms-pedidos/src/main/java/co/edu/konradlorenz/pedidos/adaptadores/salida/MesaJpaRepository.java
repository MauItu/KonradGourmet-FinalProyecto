package co.edu.konradlorenz.pedidos.adaptadores.salida;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface MesaJpaRepository extends JpaRepository<MesaEntidad, MesaEntidad.Llave> {

    List<MesaEntidad> findBySucursalIdAndActivaTrueOrderByNumero(Long sucursalId);

    List<MesaEntidad> findBySucursalId(Long sucursalId);

}
