package co.edu.konradlorenz.menu.adaptadores.salida;

import co.edu.konradlorenz.menu.dominio.Categoria;
import co.edu.konradlorenz.menu.dominio.Ingrediente;
import co.edu.konradlorenz.menu.dominio.Plato;
import co.edu.konradlorenz.menu.dominio.Producto;
import co.edu.konradlorenz.menu.dominio.UnidadMedida;
import co.edu.konradlorenz.menu.puertos.salida.PlatoRepositoryPort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Guarda los platos y sus recetas en ms_menu (PostgreSQL). Un plato nuevo recibe su id de la base; uno
 * que ya lo tiene reemplaza sus datos y su receta. Al leer, cada ingrediente trae su producto con la
 * categoría.
 */
@Repository
public class PlatoJpaAdapter implements PlatoRepositoryPort {

    private final PlatoJpaRepository platos;
    private final ProductoJpaRepository productos;

    public PlatoJpaAdapter(PlatoJpaRepository platos, ProductoJpaRepository productos) {
        this.platos = platos;
        this.productos = productos;
    }

    @Override
    @Transactional
    public Plato guardar(Plato p) {
        PlatoEntidad e = p.id() == null ? new PlatoEntidad() : platos.findById(p.id()).orElseGet(PlatoEntidad::new);
        e.sucursalId = p.sucursalId();
        e.creadoPor = p.creadoPor();
        e.nombre = p.nombre();
        e.categoria = p.categoria();
        e.descripcion = p.descripcion();
        e.precioVenta = p.precioVenta();
        e.activo = p.activo();
        e.imagenUrl = p.imagenUrl();
        e.ingredientes.clear();
        for (Ingrediente i : p.ingredientes()) {
            if (!productos.existsById(i.producto().id())) {
                throw new IllegalArgumentException("El producto " + i.producto().id() + " no existe");
            }
            e.ingredientes.add(new PlatoEntidad.Fila(i.producto().id(), i.cantidad(), i.unidad().name()));
        }
        PlatoEntidad guardado = platos.save(e);
        if (p.id() == null) {
            p.asignarId(guardado.id);
        }
        return p;
    }

    /** Sin categoría (null) devuelve todos los productos: así V06 arma el selector de categoría y producto. */
    @Override
    @Transactional(readOnly = true)
    public List<Producto> productosPorCategoria(Long categoriaId) {
        List<ProductoEntidad> filas = categoriaId == null ? productos.findAllByOrderByNombre() : productos.findByCategoriaIdOrderByNombre(categoriaId);
        return filas.stream().map(PlatoJpaAdapter::producto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Plato porId(Long id) {
        return platos.findById(id).map(e -> aDominio(e, catalogoProductos())).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Plato> porSucursal(Long sucursalId) {
        Map<Long, Producto> catalogo = catalogoProductos();
        return platos.findBySucursalIdOrderByNombre(sucursalId).stream().map(e -> aDominio(e, catalogo)).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeNombre(Long sucursalId, String nombre) {
        return platos.existsBySucursalIdAndNombreIgnoreCase(sucursalId, nombre == null ? "" : nombre.trim());
    }

    private Map<Long, Producto> catalogoProductos() {
        return productos.findAll().stream().map(PlatoJpaAdapter::producto).collect(Collectors.toMap(Producto::id, Function.identity()));
    }

    private static Producto producto(ProductoEntidad e) {
        return new Producto(e.id, e.nombre, new Categoria(e.categoria.id, e.categoria.nombre));
    }

    private static Plato aDominio(PlatoEntidad e, Map<Long, Producto> catalogo) {
        Plato p = new Plato(e.sucursalId, e.creadoPor, e.nombre, e.categoria, e.descripcion, e.precioVenta);
        p.asignarId(e.id);
        p.asignarImagen(e.imagenUrl);
        if (!e.activo) {
            p.desactivar();
        }
        for (PlatoEntidad.Fila f : e.ingredientes) {
            p.agregarIngrediente(new Ingrediente(catalogo.get(f.productoId), f.cantidad, UnidadMedida.valueOf(f.unidad)));
        }
        return p;
    }

}
