package co.edu.konradlorenz.inventario.aplicacion;

import co.edu.konradlorenz.compartido.puertos.EventPublisherPort;
import co.edu.konradlorenz.inventario.dominio.InsumoInventario;
import co.edu.konradlorenz.inventario.dominio.Inventario;
import co.edu.konradlorenz.inventario.dominio.RecetaPlato;
import co.edu.konradlorenz.inventario.dominio.UmbralStock;
import co.edu.konradlorenz.inventario.eventos.StockBajoEvent;
import co.edu.konradlorenz.inventario.puertos.entrada.GestionarInventarioUseCase;
import co.edu.konradlorenz.inventario.puertos.salida.InventarioRepositoryPort;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class InventarioService implements GestionarInventarioUseCase {

    private final InventarioRepositoryPort repositorio;
    private final UmbralStock umbral;
    private final EventPublisherPort eventos;

    public InventarioService(InventarioRepositoryPort repositorio, UmbralStock umbral, EventPublisherPort eventos) {
        this.repositorio = repositorio;
        this.umbral = umbral;
        this.eventos = eventos;
    }

    @Override
    public void acumularConsumo(Long sucursalId, Map<Long, Integer> lineas) {
        Map<Long, BigDecimal> consumo = new LinkedHashMap<>();
        lineas.forEach((platoId, cantidad) -> {
            RecetaPlato receta = repositorio.recetaDe(platoId);
            if (receta != null) { // un plato sin receta no consume insumos
                receta.consumoPara(cantidad).forEach((productoId, c) -> consumo.merge(productoId, c, BigDecimal::add));
            }
        });
        repositorio.acumular(sucursalId, consumo);
    }

    @Override
    public void descontarConsumoDiario() {
        for (Long sucursalId : repositorio.sucursalesConConsumo()) {
            Map<Long, BigDecimal> pendiente = repositorio.consumoPendiente(sucursalId);
            Inventario inventario = repositorio.inventarioDe(sucursalId);
            List<InsumoInventario> cruzaron = new ArrayList<>();
            for (InsumoInventario insumo : inventario.insumos()) {
                BigDecimal cantidad = pendiente.get(insumo.productoId());
                if (cantidad == null) {
                    continue;
                }
                boolean estabaBajo = umbral.superado(insumo);
                boolean atendida = insumo.alertaAtendida();
                insumo.descontar(cantidad); // descontar() reactiva la alerta
                if (estabaBajo && atendida) {
                    insumo.atenderAlerta(); // sigue siendo la misma alerta, y ya tiene una solicitud en camino
                }
                if (!estabaBajo && umbral.superado(insumo)) {
                    cruzaron.add(insumo); // solo estos cruzaron el umbral en esta ejecución
                }
            }
            repositorio.guardar(inventario);
            // lo ya descontado se resta del consumo pendiente, para que otra ejecución no lo repita
            Map<Long, BigDecimal> descontado = new LinkedHashMap<>();
            pendiente.forEach((productoId, cantidad) -> descontado.put(productoId, cantidad.negate()));
            repositorio.acumular(sucursalId, descontado);
            for (InsumoInventario insumo : cruzaron) {
                eventos.publicar(new StockBajoEvent("sistema", sucursalId, insumo.productoId(), insumo.nombre(),
                        insumo.stockActual(), insumo.unidad(), insumo.porcentajeExistencia()));
            }
        }
    }

    @Override
    public List<InsumoInventario> alertas(Long sucursalId) {
        return repositorio.inventarioDe(sucursalId).insumosBajoUmbral(umbral).stream()
                .filter(i -> !i.alertaAtendida())
                .toList();
    }

    @Override
    public List<InsumoInventario> insumos(Long sucursalId) {
        return repositorio.inventarioDe(sucursalId).insumos();
    }

    @Override
    public void actualizarReceta(RecetaPlato r) {
        repositorio.guardarReceta(r);
    }

    @Override
    public void atenderAlertas(Long sucursalId, List<Long> productoIds) {
        Inventario inventario = repositorio.inventarioDe(sucursalId);
        for (InsumoInventario insumo : inventario.insumos()) {
            if (productoIds.contains(insumo.productoId())) {
                insumo.atenderAlerta();
            }
        }
        repositorio.guardar(inventario);
    }

}
