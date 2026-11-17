package co.edu.konradlorenz.inventario.adaptadores.salida;

import co.edu.konradlorenz.inventario.dominio.InsumoInventario;
import co.edu.konradlorenz.inventario.dominio.Inventario;
import co.edu.konradlorenz.inventario.dominio.RecetaPlato;
import co.edu.konradlorenz.inventario.puertos.salida.InventarioRepositoryPort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Guarda en ms_inventario (PostgreSQL) los insumos de cada sucursal, la copia local de las recetas y el
 * consumo pendiente del día. acumular() suma en la base, así que dos pedidos que llegan a la vez no
 * pierden consumo; una cantidad negativa resta (así descuenta el cierre lo que ya aplicó).
 */
@Repository
public class InventarioJpaAdapter implements InventarioRepositoryPort {

    private final InsumoJpaRepository insumos;

    public InventarioJpaAdapter(InsumoJpaRepository insumos) {
        this.insumos = insumos;
    }

    @Override
    @Transactional(readOnly = true)
    public Inventario inventarioDe(Long sucursalId) {
        List<InsumoInventario> lista = new ArrayList<>();
        for (InsumoEntidad e : insumos.findBySucursalIdOrderByNombre(sucursalId)) {
            InsumoInventario i = new InsumoInventario(e.productoId, e.nombre, e.stockActual, e.stockReferencia, e.unidad);
            if (e.alertaAtendida) {
                i.atenderAlerta();
            }
            lista.add(i);
        }
        return new Inventario(sucursalId, lista);
    }

    /** Actualiza el stock y el estado de la alerta de cada insumo; no crea insumos nuevos. */
    @Override
    @Transactional
    public void guardar(Inventario i) {
        for (InsumoInventario insumo : i.insumos()) {
            InsumoEntidad.Llave llave = new InsumoEntidad.Llave();
            llave.sucursalId = i.sucursalId();
            llave.productoId = insumo.productoId();
            insumos.findById(llave).ifPresent(e -> {
                e.stockActual = insumo.stockActual();
                e.alertaAtendida = insumo.alertaAtendida();
            });
        }
    }

    @Override
    @Transactional
    public void acumular(Long sucursalId, Map<Long, BigDecimal> consumo) {
        consumo.forEach((productoId, cantidad) -> insumos.sumarConsumo(sucursalId, productoId, cantidad));
        insumos.quitarConsumoEnCero(sucursalId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> sucursalesConConsumo() {
        return insumos.sucursalesConConsumo();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, BigDecimal> consumoPendiente(Long sucursalId) {
        return mapa(insumos.consumoPendiente(sucursalId));
    }

    /** Reemplaza la receta del plato (llega completa en cada menu.actualizado). */
    @Override
    @Transactional
    public void guardarReceta(RecetaPlato r) {
        insumos.borrarReceta(r.platoId());
        r.insumos().forEach((productoId, cantidad) -> insumos.agregarAReceta(r.platoId(), productoId, cantidad));
    }

    /** null si el plato no tiene receta: no consume insumos. */
    @Override
    @Transactional(readOnly = true)
    public RecetaPlato recetaDe(Long platoId) {
        Map<Long, BigDecimal> receta = mapa(insumos.receta(platoId));
        return receta.isEmpty() ? null : new RecetaPlato(platoId, receta);
    }

    private static Map<Long, BigDecimal> mapa(List<Object[]> filas) {
        Map<Long, BigDecimal> m = new LinkedHashMap<>();
        for (Object[] f : filas) {
            m.put(((Number) f[0]).longValue(), (BigDecimal) f[1]);
        }
        return m;
    }

}
