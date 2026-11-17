package co.edu.konradlorenz.cotizaciones.adaptadores.salida;

import co.edu.konradlorenz.cotizaciones.dominio.Proveedor;
import co.edu.konradlorenz.cotizaciones.puertos.salida.ProveedorRepositoryPort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Guarda los proveedores en ms_cotizaciones (PostgreSQL). Uno nuevo llega sin id y la base le asigna el
 * siguiente (los cuatro de la demo son 1 a 4). Inactivar es guardar con activo = false: nunca se borra,
 * porque las cotizaciones viejas lo siguen nombrando.
 */
@Repository
public class ProveedorJpaAdapter implements ProveedorRepositoryPort {

    private final ProveedorJpaRepository proveedores;

    public ProveedorJpaAdapter(ProveedorJpaRepository proveedores) {
        this.proveedores = proveedores;
    }

    @Override
    @Transactional
    public Proveedor guardar(Proveedor p) {
        ProveedorEntidad e = p.id() == null ? new ProveedorEntidad() : proveedores.findById(p.id()).orElseGet(ProveedorEntidad::new);
        e.razonSocial = p.razonSocial();
        e.nit = p.nit();
        e.correo = p.correo();
        e.telefono = p.telefono();
        e.direccion = p.direccion();
        e.activo = p.activo();
        e.categorias.clear();
        if (p.categorias() != null) {
            e.categorias.addAll(p.categorias());
        }
        return proveedor(proveedores.save(e));
    }

    @Override
    @Transactional(readOnly = true)
    public Proveedor porId(Long id) {
        return id == null ? null : proveedores.findById(id).map(ProveedorJpaAdapter::proveedor).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeNit(String nit) {
        return proveedores.existsByNit(nit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Proveedor> activos() {
        return proveedores.findByActivoTrueOrderByRazonSocial().stream().map(ProveedorJpaAdapter::proveedor).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Proveedor> todos() {
        return proveedores.findAllByOrderByRazonSocial().stream().map(ProveedorJpaAdapter::proveedor).toList();
    }

    static Proveedor proveedor(ProveedorEntidad e) {
        return new Proveedor(e.id, e.razonSocial, e.nit, e.correo, e.telefono, e.direccion, new ArrayList<>(e.categorias), e.activo);
    }

}
