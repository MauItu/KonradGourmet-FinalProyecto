package co.edu.konradlorenz.menu.dominio;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Plato {

    private Long id;
    private Long sucursalId;
    private Long creadoPor;
    private String nombre;
    private String categoria;
    private String descripcion;
    private BigDecimal precioVenta;
    private boolean activo;
    private String imagenUrl;
    private List<Ingrediente> ingredientes = new ArrayList<>();

    /**
     * Un plato nuevo nace activo, sin id y sin imagen: el repositorio le asigna el id al guardarlo. La
     * categoría es la de la carta (Fuertes, Sopas, Bebidas…), con la que el mesero filtra V03.
     */
    public Plato(Long sucursalId, Long creadoPor, String nombre, String categoria, String descripcion, BigDecimal precioVenta) {
        this.sucursalId = sucursalId;
        this.creadoPor = creadoPor;
        this.nombre = nombre == null ? null : nombre.trim();
        this.categoria = categoria == null ? null : categoria.trim();
        this.descripcion = descripcion == null ? "" : descripcion.trim();
        this.precioVenta = precioVenta;
        this.activo = true;
    }

    public Long id() {
        return id;
    }

    /** Lo usa el repositorio al guardar un plato nuevo. */
    public void asignarId(Long id) {
        this.id = id;
    }

    public Long sucursalId() {
        return sucursalId;
    }

    public Long creadoPor() {
        return creadoPor;
    }

    public String nombre() {
        return nombre;
    }

    public String categoria() {
        return categoria;
    }

    public String descripcion() {
        return descripcion;
    }

    public BigDecimal precioVenta() {
        return precioVenta;
    }

    public boolean activo() {
        return activo;
    }

    /** Dónde está la imagen en el almacén de objetos, o null si el plato no tiene. */
    public String imagenUrl() {
        return imagenUrl;
    }

    /** La guarda PlatoService después de subir el archivo al almacén. */
    public void asignarImagen(String url) {
        this.imagenUrl = url;
    }

    public List<Ingrediente> ingredientes() {
        return List.copyOf(ingredientes);
    }

    /** Agrega el ingrediente; si el producto ya estaba en la receta, suma las cantidades. */
    public void agregarIngrediente(Ingrediente i) {
        if (i == null || i.producto() == null || i.producto().id() == null) {
            throw new IllegalStateException("El ingrediente necesita un producto");
        }
        if (i.cantidad() == null || i.cantidad().signum() <= 0) {
            throw new IllegalStateException("La cantidad de cada ingrediente debe ser mayor que cero");
        }
        for (int k = 0; k < ingredientes.size(); k++) {
            Ingrediente existente = ingredientes.get(k);
            if (existente.producto().id().equals(i.producto().id())) {
                ingredientes.set(k, new Ingrediente(existente.producto(), existente.enKg().add(i.enKg()), UnidadMedida.KG));
                return;
            }
        }
        ingredientes.add(i);
    }

    /** Exige sucursal, quién lo crea, nombre, categoría y un precio de venta mayor que cero. */
    public void validar() {
        if (sucursalId == null) {
            throw new IllegalStateException("El plato necesita una sucursal");
        }
        if (creadoPor == null) {
            throw new IllegalStateException("El plato necesita el usuario que lo crea");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalStateException("El nombre del plato es obligatorio");
        }
        if (categoria == null || categoria.isBlank()) {
            throw new IllegalStateException("La categoría del plato es obligatoria");
        }
        if (precioVenta == null || precioVenta.signum() <= 0) {
            throw new IllegalStateException("El precio de venta debe ser mayor que cero");
        }
    }

    /**
     * Copia los datos editables; el id, la sucursal, quién lo creó, el estado y la imagen no cambian (la
     * imagen se cambia con asignarImagen). Si algo no es válido no cambia nada.
     */
    public void editar(Plato datos) {
        datos.validar();
        this.nombre = datos.nombre;
        this.categoria = datos.categoria;
        this.descripcion = datos.descripcion;
        this.precioVenta = datos.precioVenta;
        this.ingredientes = new ArrayList<>(datos.ingredientes);
    }

    /** El plato sale del catálogo del mesero, pero no se borra: los pedidos que ya lo tienen siguen intactos. */
    public void desactivar() {
        this.activo = false;
    }

    public void reactivar() {
        this.activo = true;
    }

    /** La receta como la entiende ms-inventario: producto → kilogramos por plato. */
    public Map<Long, BigDecimal> receta() {
        Map<Long, BigDecimal> receta = new LinkedHashMap<>();
        for (Ingrediente i : ingredientes) {
            receta.merge(i.producto().id(), i.enKg(), BigDecimal::add);
        }
        return receta;
    }

}
