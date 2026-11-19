package co.edu.konradlorenz.ordenes.adaptadores.salida;

import co.edu.konradlorenz.ordenes.dominio.EstadoOrden;
import co.edu.konradlorenz.ordenes.dominio.ItemOrden;
import co.edu.konradlorenz.ordenes.dominio.OrdenCompra;
import co.edu.konradlorenz.ordenes.puertos.salida.OrdenRepositoryPort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Guarda las órdenes en ms_ordenes (PostgreSQL). La orden nueva llega sin número y recibe el siguiente de
 * una secuencia (OC-0012 en adelante, como la demo); la segunda vez llega con el PDF y se reemplaza la fila.
 * cotizacion_numero es único en la base: si dos peticiones generan la orden de la misma cotización a la
 * vez, la segunda falla al guardar (saveAndFlush lo hace saber de inmediato).
 */
@Repository
public class OrdenJpaAdapter implements OrdenRepositoryPort {

    private final OrdenJpaRepository ordenes;

    public OrdenJpaAdapter(OrdenJpaRepository ordenes) {
        this.ordenes = ordenes;
    }

    @Override
    @Transactional
    public OrdenCompra guardar(OrdenCompra o) {
        OrdenCompra conNumero = o.numero() != null ? o : new OrdenCompra(String.format("OC-%04d", ordenes.siguienteNumero()), o);
        OrdenEntidad e = ordenes.findById(conNumero.numero()).orElseGet(OrdenEntidad::new);
        e.numero = conNumero.numero();
        e.fecha = conNumero.fecha();
        e.cotizacionNumero = conNumero.cotizacionNumero();
        e.proveedor = conNumero.proveedor();
        e.correoProveedor = conNumero.correoProveedor();
        e.pdfUrl = conNumero.pdfUrl();
        e.estado = conNumero.estado().name();
        e.solicitudes.clear();
        e.solicitudes.addAll(conNumero.solicitudNumeros());
        e.items.clear();
        for (ItemOrden i : conNumero.items()) {
            e.items.add(new OrdenEntidad.Item(i.productoId(), i.cantidad(), i.unidad(), i.precio()));
        }
        ordenes.saveAndFlush(e);
        return conNumero;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdenCompra> porEstado(EstadoOrden e) {
        return ordenes.findByEstadoOrderByNumero(e.name()).stream().map(OrdenJpaAdapter::orden).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorCotizacion(String numero) {
        return ordenes.existsByCotizacionNumero(numero);
    }

    @Override
    @Transactional(readOnly = true)
    public OrdenCompra porNumero(String numero) {
        return numero == null ? null : ordenes.findById(numero).map(OrdenJpaAdapter::orden).orElse(null);
    }

    private static OrdenCompra orden(OrdenEntidad e) {
        List<ItemOrden> items = e.items.stream().map(i -> new ItemOrden(i.productoId, i.cantidad, i.unidad, i.precio)).toList();
        return new OrdenCompra(e.numero, e.fecha, e.cotizacionNumero, new ArrayList<>(e.solicitudes), e.proveedor, e.correoProveedor,
                e.pdfUrl, EstadoOrden.valueOf(e.estado), items);
    }

}
