package co.edu.konradlorenz.cotizaciones.adaptadores.salida;

import co.edu.konradlorenz.cotizaciones.dominio.Cotizacion;
import co.edu.konradlorenz.cotizaciones.dominio.EstadoCotizacion;
import co.edu.konradlorenz.cotizaciones.dominio.ItemCotizacion;
import co.edu.konradlorenz.cotizaciones.puertos.salida.CotizacionRepositoryPort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Guarda las cotizaciones en ms_cotizaciones (PostgreSQL). El número lo trae la cotización desde que se
 * arma (NumeracionCotizaciones, COT-018 en adelante), porque el PDF lo lleva antes de guardarse. Guardar
 * reemplaza la fila completa: el estado, el PDF y los dos precios de cada producto.
 */
@Repository
public class CotizacionJpaAdapter implements CotizacionRepositoryPort {

    private final CotizacionJpaRepository cotizaciones;
    private final ProveedorJpaRepository proveedores;

    public CotizacionJpaAdapter(CotizacionJpaRepository cotizaciones, ProveedorJpaRepository proveedores) {
        this.cotizaciones = cotizaciones;
        this.proveedores = proveedores;
    }

    @Override
    @Transactional
    public Cotizacion guardar(Cotizacion c) {
        CotizacionEntidad e = cotizaciones.findById(c.numero()).orElseGet(CotizacionEntidad::new);
        e.numero = c.numero();
        e.proveedor = proveedores.findById(c.proveedor().id())
                .orElseThrow(() -> new IllegalArgumentException("No existe el proveedor " + c.proveedor().id()));
        e.fechaEnvio = c.fechaEnvio();
        e.fechaLimite = c.fechaLimite();
        e.pdfUrl = c.pdfUrl();
        e.estado = c.estado().name();
        e.solicitudes.clear();
        e.solicitudes.addAll(c.solicitudNumeros());
        e.items.clear();
        for (ItemCotizacion i : c.items()) {
            e.items.add(new CotizacionEntidad.Item(i.productoId(), i.cantidad(), i.unidad(), i.precioEstandar(), i.precioProveedor()));
        }
        return cotizacion(cotizaciones.save(e));
    }

    @Override
    @Transactional(readOnly = true)
    public Cotizacion porNumero(String numero) {
        return numero == null ? null : cotizaciones.findById(numero).map(CotizacionJpaAdapter::cotizacion).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cotizacion> porEstado(EstadoCotizacion e) {
        return cotizaciones.findByEstadoOrderByNumero(e.name()).stream().map(CotizacionJpaAdapter::cotizacion).toList();
    }

    private static Cotizacion cotizacion(CotizacionEntidad e) {
        List<ItemCotizacion> items = e.items.stream()
                .map(i -> new ItemCotizacion(i.productoId, i.cantidad, i.unidad, i.precioEstandar, i.precioProveedor)).toList();
        return new Cotizacion(e.numero, new ArrayList<>(e.solicitudes), ProveedorJpaAdapter.proveedor(e.proveedor), e.fechaEnvio,
                e.fechaLimite, e.pdfUrl, EstadoCotizacion.valueOf(e.estado), items);
    }

}
