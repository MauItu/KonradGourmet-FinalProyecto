package co.edu.konradlorenz.menu.dominio;

import java.math.BigDecimal;

public class Ingrediente {

    private static final BigDecimal KG_POR_LIBRA = new BigDecimal("0.45359237");
    private static final BigDecimal GRAMOS_POR_KG = new BigDecimal("1000");

    private Producto producto;
    private BigDecimal cantidad;
    private UnidadMedida unidad;

    public Ingrediente(Producto producto, BigDecimal cantidad, UnidadMedida unidad) {
        this.producto = producto;
        this.cantidad = cantidad;
        this.unidad = unidad;
    }

    public Producto producto() {
        return producto;
    }

    public BigDecimal cantidad() {
        return cantidad;
    }

    public UnidadMedida unidad() {
        return unidad;
    }

    /** La cantidad en kilogramos, que es en lo que ms-inventario lleva las recetas. Litros y unidades no se descuentan. */
    public BigDecimal enKg() {
        return switch (unidad) {
            case KG -> cantidad;
            case GRAMO -> cantidad.divide(GRAMOS_POR_KG);
            case LIBRA -> cantidad.multiply(KG_POR_LIBRA);
            default -> throw new IllegalStateException("La unidad " + unidad + " no se puede descontar del inventario en kg");
        };
    }

}
