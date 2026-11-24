package co.edu.konradlorenz.inventario.adaptadores.salida;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

/**
 * Los insumos con JPA; la receta y el consumo pendiente con SQL directo sobre la misma base, porque
 * son sumas y reemplazos de filas que no necesitan entidades.
 */
interface InsumoJpaRepository extends JpaRepository<InsumoEntidad, InsumoEntidad.Llave> {

    List<InsumoEntidad> findBySucursalIdOrderByNombre(Long sucursalId);

    /** Suma (o resta, si es negativa) al consumo pendiente sin leerlo antes: dos pedidos a la vez no se pisan. */
    @Modifying
    @Query(value = """
            INSERT INTO consumo_pendiente (sucursal_id, producto_id, cantidad) VALUES (:sucursal, :producto, :cantidad)
            ON CONFLICT (sucursal_id, producto_id) DO UPDATE SET cantidad = consumo_pendiente.cantidad + EXCLUDED.cantidad
            """, nativeQuery = true)
    void sumarConsumo(Long sucursal, Long producto, BigDecimal cantidad);

    @Modifying
    @Query(value = "DELETE FROM consumo_pendiente WHERE sucursal_id = :sucursal AND cantidad <= 0", nativeQuery = true)
    void quitarConsumoEnCero(Long sucursal);

    @Query(value = "SELECT DISTINCT sucursal_id FROM consumo_pendiente WHERE cantidad > 0 ORDER BY sucursal_id", nativeQuery = true)
    List<Long> sucursalesConConsumo();

    @Query(value = "SELECT producto_id, cantidad FROM consumo_pendiente WHERE sucursal_id = :sucursal AND cantidad > 0 ORDER BY producto_id", nativeQuery = true)
    List<Object[]> consumoPendiente(Long sucursal);

    @Modifying
    @Query(value = "DELETE FROM receta WHERE plato_id = :plato", nativeQuery = true)
    void borrarReceta(Long plato);

    @Modifying
    @Query(value = "INSERT INTO receta (plato_id, producto_id, cantidad) VALUES (:plato, :producto, :cantidad)", nativeQuery = true)
    void agregarAReceta(Long plato, Long producto, BigDecimal cantidad);

    @Query(value = "SELECT producto_id, cantidad FROM receta WHERE plato_id = :plato ORDER BY producto_id", nativeQuery = true)
    List<Object[]> receta(Long plato);

}
