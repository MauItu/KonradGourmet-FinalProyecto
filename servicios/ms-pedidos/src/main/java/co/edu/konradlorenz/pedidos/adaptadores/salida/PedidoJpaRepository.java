package co.edu.konradlorenz.pedidos.adaptadores.salida;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

interface PedidoJpaRepository extends JpaRepository<PedidoEntidad, String> {

    List<PedidoEntidad> findByMeseroIdAndFechaGreaterThanEqualOrderByFechaDesc(Long meseroId, LocalDateTime desde);

    @Query(value = "SELECT nextval('pedido_numero')", nativeQuery = true)
    long siguienteNumero();

}
